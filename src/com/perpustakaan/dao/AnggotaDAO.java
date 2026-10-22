package com.perpustakaan.dao;

import com.perpustakaan.config.DatabaseConfig;
import com.perpustakaan.model.Anggota;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnggotaDAO {

    public List<Anggota> getAll() throws SQLException {
        List<Anggota> list = new ArrayList<>();
        String sql = "SELECT * FROM anggota WHERE is_deleted = 0 ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToAnggota(rs));
            }
        }
        return list;
    }

    public List<Anggota> getAktifOnly() throws SQLException {
        List<Anggota> list = new ArrayList<>();
        String sql = "SELECT * FROM anggota WHERE status = 'aktif' AND is_deleted = 0 ORDER BY nama ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToAnggota(rs));
            }
        }
        return list;
    }

    public List<Anggota> search(String keyword) throws SQLException {
        List<Anggota> list = new ArrayList<>();
        String sql = "SELECT * FROM anggota WHERE is_deleted = 0 AND (nim LIKE ? OR nama LIKE ? OR jurusan LIKE ?) ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String p = "%" + keyword.trim() + "%";
            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAnggota(rs));
                }
            }
        }
        return list;
    }

    public Anggota getById(int id) throws SQLException {
        String sql = "SELECT * FROM anggota WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnggota(rs);
                }
            }
        }
        return null;
    }

    public boolean insert(Anggota a) throws SQLException {
        String sql = "INSERT INTO anggota (nim, nama, jurusan, no_telp, email, status, is_deleted) VALUES (?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNim());
            ps.setString(2, a.getNama());
            ps.setString(3, a.getJurusan());
            ps.setString(4, a.getNoTelp());
            ps.setString(5, a.getEmail());
            ps.setString(6, a.getStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        a.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Anggota a) throws SQLException {
        String sql = "UPDATE anggota SET nim = ?, nama = ?, jurusan = ?, no_telp = ?, email = ?, status = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNim());
            ps.setString(2, a.getNama());
            ps.setString(3, a.getJurusan());
            ps.setString(4, a.getNoTelp());
            ps.setString(5, a.getEmail());
            ps.setString(6, a.getStatus());
            ps.setInt(7, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Soft Delete: Menandai is_deleted = 1
    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE anggota SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isNimExists(String nim, int excludeId) throws SQLException {
        String sql = "SELECT id FROM anggota WHERE nim = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nim);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countTotalAnggota() throws SQLException {
        String sql = "SELECT COUNT(*) FROM anggota WHERE status = 'aktif'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Anggota mapResultSetToAnggota(ResultSet rs) throws SQLException {
        Anggota a = new Anggota();
        a.setId(rs.getInt("id"));
        a.setNim(rs.getString("nim"));
        a.setNama(rs.getString("nama"));
        a.setJurusan(rs.getString("jurusan"));
        a.setNoTelp(rs.getString("no_telp"));
        a.setEmail(rs.getString("email"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
