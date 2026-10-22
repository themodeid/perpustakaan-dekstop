package com.perpustakaan.dao;

import com.perpustakaan.config.DatabaseConfig;
import com.perpustakaan.model.Pengguna;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NAMA FILE : PenggunaDAO.java
 * FUNGSI    : Akses data database (CRUD) untuk tabel 'users' (Admin & Petugas).
 * TABEL DB  : users
 */
public class PenggunaDAO {

    public Pengguna authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPengguna(rs);
                }
            }
        }
        return null;
    }

    public List<Pengguna> getAll() throws SQLException {
        List<Pengguna> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_deleted = 0 ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPengguna(rs));
            }
        }
        return list;
    }

    public Pengguna getById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPengguna(rs);
                }
            }
        }
        return null;
    }

    public boolean insert(Pengguna pengguna) throws SQLException {
        String sql = "INSERT INTO users (username, password, nama_lengkap, role, no_telp, is_deleted) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pengguna.getUsername());
            ps.setString(2, pengguna.getPassword());
            ps.setString(3, pengguna.getNamaLengkap());
            ps.setString(4, pengguna.getRole());
            ps.setString(5, pengguna.getNoTelp());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        pengguna.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Pengguna pengguna) throws SQLException {
        boolean updatePass = pengguna.getPassword() != null && !pengguna.getPassword().trim().isEmpty();
        String sql = updatePass ?
                "UPDATE users SET username = ?, password = ?, nama_lengkap = ?, role = ?, no_telp = ? WHERE id = ? AND is_deleted = 0" :
                "UPDATE users SET username = ?, nama_lengkap = ?, role = ?, no_telp = ? WHERE id = ? AND is_deleted = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, pengguna.getUsername());
            if (updatePass) {
                ps.setString(idx++, pengguna.getPassword());
            }
            ps.setString(idx++, pengguna.getNamaLengkap());
            ps.setString(idx++, pengguna.getRole());
            ps.setString(idx++, pengguna.getNoTelp());
            ps.setInt(idx, pengguna.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Soft Delete: Hanya menandai is_deleted = 1
    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE users SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isUsernameExists(String username, int excludeId) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Pengguna mapResultSetToPengguna(ResultSet rs) throws SQLException {
        Pengguna pengguna = new Pengguna();
        pengguna.setId(rs.getInt("id"));
        pengguna.setUsername(rs.getString("username"));
        pengguna.setPassword(rs.getString("password"));
        pengguna.setNamaLengkap(rs.getString("nama_lengkap"));
        pengguna.setRole(rs.getString("role"));
        pengguna.setNoTelp(rs.getString("no_telp"));
        pengguna.setCreatedAt(rs.getTimestamp("created_at"));
        return pengguna;
    }
}
