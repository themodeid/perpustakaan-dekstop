package com.perpustakaan.dao;

import com.perpustakaan.config.DatabaseConfig;
import com.perpustakaan.model.DetailPeminjaman;
import com.perpustakaan.model.Peminjaman;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeminjamanDAO {

    private final BukuDAO bukuDAO = new BukuDAO();

    public List<Peminjaman> getAll() throws SQLException {
        String sql = "SELECT p.*, a.nama AS nama_anggota, a.nim AS nim_anggota, u.nama_lengkap AS nama_petugas " +
                     "FROM peminjaman p " +
                     "JOIN anggota a ON p.anggota_id = a.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "ORDER BY p.id DESC";
        List<Peminjaman> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Peminjaman p = mapResultSetToPeminjaman(rs);
                p.setDetailList(getDetailByPeminjamanId(p.getId()));
                list.add(p);
            }
        }
        return list;
    }

    public List<Peminjaman> getAktifPinjam() throws SQLException {
        String sql = "SELECT p.*, a.nama AS nama_anggota, a.nim AS nim_anggota, u.nama_lengkap AS nama_petugas " +
                     "FROM peminjaman p " +
                     "JOIN anggota a ON p.anggota_id = a.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.status IN ('dipinjam', 'terlambat') " +
                     "ORDER BY p.tanggal_jatuh_tempo ASC";
        List<Peminjaman> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Peminjaman p = mapResultSetToPeminjaman(rs);
                p.setDetailList(getDetailByPeminjamanId(p.getId()));
                list.add(p);
            }
        }
        return list;
    }

    public Peminjaman getByKode(String kodeTransaksi) throws SQLException {
        String sql = "SELECT p.*, a.nama AS nama_anggota, a.nim AS nim_anggota, u.nama_lengkap AS nama_petugas " +
                     "FROM peminjaman p " +
                     "JOIN anggota a ON p.anggota_id = a.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.kode_transaksi = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeTransaksi.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Peminjaman p = mapResultSetToPeminjaman(rs);
                    p.setDetailList(getDetailByPeminjamanId(p.getId()));
                    return p;
                }
            }
        }
        return null;
    }

    public List<DetailPeminjaman> getDetailByPeminjamanId(int peminjamanId) throws SQLException {
        List<DetailPeminjaman> details = new ArrayList<>();
        String sql = "SELECT dp.*, b.judul, b.pengarang FROM detail_peminjaman dp " +
                     "JOIN buku b ON dp.buku_id = b.id WHERE dp.peminjaman_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, peminjamanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetailPeminjaman dp = new DetailPeminjaman();
                    dp.setId(rs.getInt("id"));
                    dp.setPeminjamanId(rs.getInt("peminjaman_id"));
                    dp.setBukuId(rs.getInt("buku_id"));
                    dp.setDendaPerBuku(rs.getBigDecimal("denda_per_buku"));
                    dp.setJudulBuku(rs.getString("judul"));
                    dp.setPengarangBuku(rs.getString("pengarang"));
                    details.add(dp);
                }
            }
        }
        return details;
    }

    public boolean insertTransaksi(Peminjaman p, List<Integer> bukuIds) throws SQLException {
        String sqlPeminjaman = "INSERT INTO peminjaman (kode_transaksi, anggota_id, user_id, tanggal_pinjam, " +
                              "tanggal_jatuh_tempo, status, total_denda, catatan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO detail_peminjaman (peminjaman_id, buku_id, denda_per_buku) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Transaksi ACID

            int generatedPeminjamanId;
            try (PreparedStatement ps = conn.prepareStatement(sqlPeminjaman, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getKodeTransaksi());
                ps.setInt(2, p.getAnggotaId());
                ps.setInt(3, p.getUserId());
                ps.setDate(4, p.getTanggalPinjam());
                ps.setDate(5, p.getTanggalJatuhTempo());
                ps.setString(6, p.getStatus());
                ps.setBigDecimal(7, p.getTotalDenda() != null ? p.getTotalDenda() : BigDecimal.ZERO);
                ps.setString(8, p.getCatatan());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedPeminjamanId = rs.getInt(1);
                        p.setId(generatedPeminjamanId);
                    } else {
                        throw new SQLException("Gagal mendapatkan ID transaksi peminjaman");
                    }
                }
            }

            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (Integer bukuId : bukuIds) {
                    psDetail.setInt(1, generatedPeminjamanId);
                    psDetail.setInt(2, bukuId);
                    psDetail.setBigDecimal(3, BigDecimal.ZERO);
                    psDetail.addBatch();

                    // Kurangi stok buku
                    bukuDAO.kurangiStok(conn, bukuId, 1);
                }
                psDetail.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean prosesPengembalian(int peminjamanId, Date tglKembali, BigDecimal denda, 
                                     String catatan, List<Integer> bukuIds) throws SQLException {
        String sqlUpdate = "UPDATE peminjaman SET tanggal_kembali = ?, total_denda = ?, status = 'kembali', catatan = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDate(1, tglKembali);
                ps.setBigDecimal(2, denda);
                ps.setString(3, catatan);
                ps.setInt(4, peminjamanId);
                ps.executeUpdate();
            }

            // Kembalikan stok untuk setiap buku
            for (Integer bukuId : bukuIds) {
                bukuDAO.tambahStok(conn, bukuId, 1);
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<Peminjaman> filterLaporan(String status, Date tglMulai, Date tglSelesai) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, a.nama AS nama_anggota, a.nim AS nim_anggota, u.nama_lengkap AS nama_petugas " +
                "FROM peminjaman p " +
                "JOIN anggota a ON p.anggota_id = a.id " +
                "JOIN users u ON p.user_id = u.id WHERE 1=1 ");

        if (status != null && !status.equalsIgnoreCase("Semua")) {
            sql.append("AND p.status = ? ");
        }
        if (tglMulai != null) {
            sql.append("AND p.tanggal_pinjam >= ? ");
        }
        if (tglSelesai != null) {
            sql.append("AND p.tanggal_pinjam <= ? ");
        }
        sql.append("ORDER BY p.id DESC");

        List<Peminjaman> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.equalsIgnoreCase("Semua")) {
                ps.setString(idx++, status.toLowerCase());
            }
            if (tglMulai != null) {
                ps.setDate(idx++, tglMulai);
            }
            if (tglSelesai != null) {
                ps.setDate(idx++, tglSelesai);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Peminjaman p = mapResultSetToPeminjaman(rs);
                    p.setDetailList(getDetailByPeminjamanId(p.getId()));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public BigDecimal getTotalDendaTerkumpul() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_denda), 0) FROM peminjaman WHERE status = 'kembali'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    public int countTransaksiBulanIni() throws SQLException {
        String sql = "SELECT COUNT(*) FROM peminjaman WHERE MONTH(tanggal_pinjam) = MONTH(CURRENT_DATE()) AND YEAR(tanggal_pinjam) = YEAR(CURRENT_DATE())";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Peminjaman mapResultSetToPeminjaman(ResultSet rs) throws SQLException {
        Peminjaman p = new Peminjaman();
        p.setId(rs.getInt("id"));
        p.setKodeTransaksi(rs.getString("kode_transaksi"));
        p.setAnggotaId(rs.getInt("anggota_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setTanggalPinjam(rs.getDate("tanggal_pinjam"));
        p.setTanggalJatuhTempo(rs.getDate("tanggal_jatuh_tempo"));
        p.setTanggalKembali(rs.getDate("tanggal_kembali"));
        p.setStatus(rs.getString("status"));
        p.setTotalDenda(rs.getBigDecimal("total_denda"));
        p.setCatatan(rs.getString("catatan"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setNamaAnggota(rs.getString("nama_anggota"));
        p.setNimAnggota(rs.getString("nim_anggota"));
        p.setNamaPetugas(rs.getString("nama_petugas"));
        return p;
    }
}
