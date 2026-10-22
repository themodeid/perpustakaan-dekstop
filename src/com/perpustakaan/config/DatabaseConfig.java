package com.perpustakaan.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "perpustakaan_db";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    
    // User root dan perpus_user
    private static final String USER = "root";
    private static final String PASSWORD = "rootpassword";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL JDBC tidak ditemukan di classpath!");
        }
    }

    // Selalu kembalikan instance Connection independen agar thread-safe dan tidak saling menutup ResultSet
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Fallback coba user perpus_user jika root beda password
            try {
                return DriverManager.getConnection(URL, "perpus_user", "perpus_password");
            } catch (SQLException ignored) {
                throw e; // Lempar error awal
            }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Gagal koneksi ke database: " + e.getMessage());
            return false;
        }
    }
}
