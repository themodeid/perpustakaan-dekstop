package com.perpustakaan.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class DatabaseMigration {

    public static void main(String[] args) {
        runMigrations();
    }

    public static synchronized void runMigrations() {
        System.out.println("==========================================");
        System.out.println("  Memeriksa Migrasi Database Otomatis...  ");
        System.out.println("==========================================");

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null || conn.isClosed()) {
                System.err.println("[MIGRATION WARNING] Koneksi ke MySQL tidak tersedia. Migrasi dilewati.");
                return;
            }

            // 1. Inisialisasi tabel tracking migrasi
            createMigrationTableIfNotExists(conn);

            // 2. Ambil daftar migrasi yang sudah pernah diaplikasikan
            Set<String> appliedMigrations = getAppliedMigrations(conn);

            // 3. Cari file migrasi di folder database/migrations
            File migrationDir = findMigrationDirectory();
            if (migrationDir == null || !migrationDir.exists() || !migrationDir.isDirectory()) {
                System.out.println("[MIGRATION] Folder database/migrations tidak ditemukan atau kosong. Migrasi dilewati.");
                return;
            }

            File[] files = migrationDir.listFiles((dir, name) -> name.endsWith(".sql"));
            if (files == null || files.length == 0) {
                System.out.println("[MIGRATION] Tidak ada file .sql di folder migrasi.");
                return;
            }

            // Urutkan berdasarkan nama file (V1, V2, V3...)
            Arrays.sort(files, Comparator.comparing(File::getName));

            int newlyAppliedCount = 0;

            for (File file : files) {
                String fileName = file.getName();
                if (!appliedMigrations.contains(fileName)) {
                    System.out.println("[MIGRATION] Menjalankan migrasi: " + fileName + " ...");
                    executeMigrationFile(conn, file);
                    recordMigration(conn, fileName);
                    System.out.println("[MIGRATION] [SUKSES] " + fileName + " berhasil diaplikasikan.");
                    newlyAppliedCount++;
                }
            }

            if (newlyAppliedCount == 0) {
                System.out.println("[MIGRATION] Database sudah versi terbaru (Up-to-date).");
            } else {
                System.out.println("[MIGRATION] Selesai! Sebanyak " + newlyAppliedCount + " migrasi baru berhasil dijalankan.");
            }

        } catch (SQLException ex) {
            System.err.println("[MIGRATION ERROR] Terjadi kesalahan saat memeriksa migrasi: " + ex.getMessage());
        }
        System.out.println("==========================================\n");
    }

    private static void createMigrationTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schema_migrations (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "version VARCHAR(100) NOT NULL UNIQUE, " +
                     "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static Set<String> getAppliedMigrations(Connection conn) throws SQLException {
        Set<String> set = new HashSet<>();
        String sql = "SELECT version FROM schema_migrations";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                set.add(rs.getString("version"));
            }
        }
        return set;
    }

    private static void recordMigration(Connection conn, String version) throws SQLException {
        String sql = "INSERT INTO schema_migrations (version) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, version);
            ps.executeUpdate();
        }
    }

    private static void executeMigrationFile(Connection conn, File sqlFile) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Abaikan baris komentar SQL
                if (trimmed.startsWith("--") || trimmed.startsWith("//") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new SQLException("Gagal membaca file migrasi: " + sqlFile.getName(), e);
        }

        // Pisahkan perintah berdasarkan tanda titik koma ';'
        String[] statements = sb.toString().split(";");

        boolean autoCommitOrig = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                for (String rawSql : statements) {
                    String sql = rawSql.trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(autoCommitOrig);
        }
    }

    private static File findMigrationDirectory() {
        // Cek path lokal relatif terhadap running directory
        File dir = new File("database/migrations");
        if (dir.exists() && dir.isDirectory()) return dir;

        dir = new File("perpustakaan-desktop/database/migrations");
        if (dir.exists() && dir.isDirectory()) return dir;

        // Cek dari direktori root repo
        dir = new File("../perpustakaan-desktop/database/migrations");
        if (dir.exists() && dir.isDirectory()) return dir;

        return null;
    }
}
