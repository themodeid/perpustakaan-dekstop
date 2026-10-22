package com.perpustakaan.dao;

import com.perpustakaan.config.DatabaseConfig;
import com.perpustakaan.model.Kategori;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KategoriDAO {

    public List<Kategori> getAll() throws SQLException {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT * FROM kategori ORDER BY nama_kategori ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kategori(rs.getInt("id"), rs.getString("nama_kategori")));
            }
        }
        return list;
    }

    public Kategori getById(int id) throws SQLException {
        String sql = "SELECT * FROM kategori WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Kategori(rs.getInt("id"), rs.getString("nama_kategori"));
                }
            }
        }
        return null;
    }

    public boolean insert(Kategori kategori) throws SQLException {
        String sql = "INSERT INTO kategori (nama_kategori) VALUES (?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kategori.getNamaKategori());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        kategori.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Kategori kategori) throws SQLException {
        String sql = "UPDATE kategori SET nama_kategori = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kategori.getNamaKategori());
            ps.setInt(2, kategori.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM kategori WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
