package com.perpustakaan.dao;

import com.perpustakaan.config.DatabaseConfig;
import com.perpustakaan.model.Rak;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RakDAO {

    public List<Rak> getAll() throws SQLException {
        List<Rak> list = new ArrayList<>();
        String sql = "SELECT * FROM rak WHERE is_deleted = 0 ORDER BY kode_rak ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Rak(
                        rs.getInt("id"),
                        rs.getString("kode_rak"),
                        rs.getString("nama_rak"),
                        rs.getString("lokasi")
                ));
            }
        }
        return list;
    }

    public Rak getById(int id) throws SQLException {
        String sql = "SELECT * FROM rak WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Rak(
                            rs.getInt("id"),
                            rs.getString("kode_rak"),
                            rs.getString("nama_rak"),
                            rs.getString("lokasi")
                    );
                }
            }
        }
        return null;
    }

    public boolean insert(Rak rak) throws SQLException {
        String sql = "INSERT INTO rak (kode_rak, nama_rak, lokasi, is_deleted) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rak.getKodeRak());
            ps.setString(2, rak.getNamaRak());
            ps.setString(3, rak.getLokasi());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        rak.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Rak rak) throws SQLException {
        String sql = "UPDATE rak SET kode_rak = ?, nama_rak = ?, lokasi = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rak.getKodeRak());
            ps.setString(2, rak.getNamaRak());
            ps.setString(3, rak.getLokasi());
            ps.setInt(4, rak.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Soft Delete: Menandai is_deleted = 1
    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE rak SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isKodeRakExists(String kodeRak, int excludeId) throws SQLException {
        String sql = "SELECT id FROM rak WHERE kode_rak = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeRak);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
