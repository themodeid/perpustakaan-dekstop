package com.perpustakaan.dao;

import com.perpustakaan.config.DatabaseConfig;
import com.perpustakaan.model.Buku;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BukuDAO {

    public List<Buku> getAll() throws SQLException {
        List<Buku> list = new ArrayList<>();
        String sql = "SELECT b.*, k.nama_kategori, r.kode_rak, r.nama_rak, r.lokasi AS lokasi_rak " +
                     "FROM buku b " +
                     "JOIN kategori k ON b.kategori_id = k.id " +
                     "LEFT JOIN rak r ON b.rak_id = r.id " +
                     "WHERE b.is_deleted = 0 " +
                     "ORDER BY b.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToBuku(rs));
            }
        }
        return list;
    }

    public List<Buku> search(String keyword, Integer kategoriId, Integer rakId) throws SQLException {
        List<Buku> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT b.*, k.nama_kategori, r.kode_rak, r.nama_rak, r.lokasi AS lokasi_rak " +
                "FROM buku b " +
                "JOIN kategori k ON b.kategori_id = k.id " +
                "LEFT JOIN rak r ON b.rak_id = r.id WHERE b.is_deleted = 0 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (b.judul LIKE ? OR b.isbn LIKE ? OR b.pengarang LIKE ? OR b.penerbit LIKE ?) ");
        }
        if (kategoriId != null && kategoriId > 0) {
            sql.append("AND b.kategori_id = ? ");
        }
        if (rakId != null && rakId > 0) {
            sql.append("AND b.rak_id = ? ");
        }
        sql.append("ORDER BY b.id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String p = "%" + keyword.trim() + "%";
                ps.setString(idx++, p);
                ps.setString(idx++, p);
                ps.setString(idx++, p);
                ps.setString(idx++, p);
            }
            if (kategoriId != null && kategoriId > 0) {
                ps.setInt(idx++, kategoriId);
            }
            if (rakId != null && rakId > 0) {
                ps.setInt(idx, rakId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBuku(rs));
                }
            }
        }
        return list;
    }

    public Buku getById(int id) throws SQLException {
        String sql = "SELECT b.*, k.nama_kategori, r.kode_rak, r.nama_rak, r.lokasi AS lokasi_rak " +
                     "FROM buku b " +
                     "JOIN kategori k ON b.kategori_id = k.id " +
                     "LEFT JOIN rak r ON b.rak_id = r.id WHERE b.id = ? AND b.is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBuku(rs);
                }
            }
        }
        return null;
    }

    public boolean insert(Buku b) throws SQLException {
        String sql = "INSERT INTO buku (isbn, judul, pengarang, penerbit, tahun_terbit, kategori_id, rak_id, stok_total, stok_tersedia, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getJudul());
            ps.setString(3, b.getPengarang());
            ps.setString(4, b.getPenerbit());
            ps.setInt(5, b.getTahunTerbit());
            ps.setInt(6, b.getKategoriId());
            ps.setInt(7, b.getRakId() > 0 ? b.getRakId() : 1);
            ps.setInt(8, b.getStokTotal());
            ps.setInt(9, b.getStokTersedia());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        b.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Buku b) throws SQLException {
        String sql = "UPDATE buku SET isbn = ?, judul = ?, pengarang = ?, penerbit = ?, " +
                     "tahun_terbit = ?, kategori_id = ?, rak_id = ?, stok_total = ?, stok_tersedia = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getJudul());
            ps.setString(3, b.getPengarang());
            ps.setString(4, b.getPenerbit());
            ps.setInt(5, b.getTahunTerbit());
            ps.setInt(6, b.getKategoriId());
            ps.setInt(7, b.getRakId() > 0 ? b.getRakId() : 1);
            ps.setInt(8, b.getStokTotal());
            ps.setInt(9, b.getStokTersedia());
            ps.setInt(10, b.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Soft Delete: Menandai is_deleted = 1
    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE buku SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isIsbnExists(String isbn, int excludeId) throws SQLException {
        String sql = "SELECT id FROM buku WHERE isbn = ? AND id != ? AND is_deleted = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void kurangiStok(Connection conn, int bukuId, int qty) throws SQLException {
        String sql = "UPDATE buku SET stok_tersedia = stok_tersedia - ? WHERE id = ? AND stok_tersedia >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, bukuId);
            ps.setInt(3, qty);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Stok buku tidak mencukupi untuk dipinjam!");
            }
        }
    }

    public void tambahStok(Connection conn, int bukuId, int qty) throws SQLException {
        String sql = "UPDATE buku SET stok_tersedia = LEAST(stok_total, stok_tersedia + ?) WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, bukuId);
            ps.executeUpdate();
        }
    }

    public int countTotalBuku() throws SQLException {
        String sql = "SELECT COUNT(*) FROM buku";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countBukuDipinjam() throws SQLException {
        String sql = "SELECT COALESCE(SUM(stok_total - stok_tersedia), 0) FROM buku";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Buku mapResultSetToBuku(ResultSet rs) throws SQLException {
        Buku b = new Buku();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setJudul(rs.getString("judul"));
        b.setPengarang(rs.getString("pengarang"));
        b.setPenerbit(rs.getString("penerbit"));
        b.setTahunTerbit(rs.getInt("tahun_terbit"));
        b.setKategoriId(rs.getInt("kategori_id"));
        b.setNamaKategori(rs.getString("nama_kategori"));
        b.setRakId(rs.getInt("rak_id"));
        b.setKodeRak(rs.getString("kode_rak"));
        b.setNamaRak(rs.getString("nama_rak"));
        b.setLokasiRak(rs.getString("lokasi_rak"));
        b.setStokTotal(rs.getInt("stok_total"));
        b.setStokTersedia(rs.getInt("stok_tersedia"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}
