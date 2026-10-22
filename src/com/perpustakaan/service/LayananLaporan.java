package com.perpustakaan.service;

import com.perpustakaan.dao.AnggotaDAO;
import com.perpustakaan.dao.BukuDAO;
import com.perpustakaan.dao.PeminjamanDAO;
import com.perpustakaan.model.Anggota;
import com.perpustakaan.model.Buku;
import com.perpustakaan.model.Peminjaman;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NAMA FILE : LayananLaporan.java
 * FUNGSI    : Agregasi statistik ringkasan dan pembuatan file laporan CSV untuk Buku, Anggota, Peminjaman, dan Pengembalian.
 */
public class LayananLaporan {
    private final BukuDAO bukuDAO = new BukuDAO();
    private final AnggotaDAO anggotaDAO = new AnggotaDAO();
    private final PeminjamanDAO peminjamanDAO = new PeminjamanDAO();

    public Map<String, Object> getStatistikDashboard() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBuku", bukuDAO.countTotalBuku());
        stats.put("bukuDipinjam", bukuDAO.countBukuDipinjam());
        stats.put("totalAnggota", anggotaDAO.countTotalAnggota());
        stats.put("totalDenda", peminjamanDAO.getTotalDendaTerkumpul());
        stats.put("transaksiBulanIni", peminjamanDAO.countTransaksiBulanIni());
        return stats;
    }

    // 1. Ekspor Laporan Data Buku
    public void exportLaporanBuku(File file, List<Buku> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write("No,Judul Buku,Pengarang,Penerbit,Tahun,Kategori,Rak,Total Stok,Stok Tersedia\n");
            int no = 1;
            for (Buku b : list) {
                String line = String.format("%d,\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",%d,%d\n",
                        no++,
                        escapeCsv(b.getJudul()),
                        escapeCsv(b.getPengarang()),
                        escapeCsv(b.getPenerbit()),
                        b.getTahunTerbit(),
                        escapeCsv(b.getNamaKategori()),
                        escapeCsv(b.getKodeRak() != null ? b.getKodeRak() : "-"),
                        b.getStokTotal(),
                        b.getStokTersedia()
                );
                writer.write(line);
            }
        }
    }

    // 2. Ekspor Laporan Data Anggota
    public void exportLaporanAnggota(File file, List<Anggota> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write("No,NIM / ID Anggota,Nama Lengkap,Program Studi / Jurusan,No. Telepon,Email,Status\n");
            int no = 1;
            for (Anggota a : list) {
                String line = String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        no++,
                        a.getNim(),
                        escapeCsv(a.getNama()),
                        escapeCsv(a.getJurusan()),
                        escapeCsv(a.getNoTelp() != null ? a.getNoTelp() : "-"),
                        escapeCsv(a.getEmail() != null ? a.getEmail() : "-"),
                        a.getStatus().toUpperCase()
                );
                writer.write(line);
            }
        }
    }

    // 3. Ekspor Laporan Peminjaman
    public void exportLaporanPeminjaman(File file, List<Peminjaman> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write("No,Kode Transaksi,NIM,Nama Peminjam,Petugas,Tanggal Pinjam,Jatuh Tempo,Status,Daftar Buku\n");
            int no = 1;
            for (Peminjaman p : list) {
                StringBuilder bukuStr = new StringBuilder();
                if (p.getDetailList() != null) {
                    for (int i = 0; i < p.getDetailList().size(); i++) {
                        bukuStr.append(p.getDetailList().get(i).getJudulBuku());
                        if (i < p.getDetailList().size() - 1) bukuStr.append(" | ");
                    }
                }
                String line = String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        no++,
                        p.getKodeTransaksi(),
                        p.getNimAnggota(),
                        escapeCsv(p.getNamaAnggota()),
                        escapeCsv(p.getNamaPetugas()),
                        p.getTanggalPinjam(),
                        p.getTanggalJatuhTempo(),
                        p.getStatus().toUpperCase(),
                        escapeCsv(bukuStr.toString())
                );
                writer.write(line);
            }
        }
    }

    // 4. Ekspor Laporan Pengembalian & Denda
    public void exportLaporanPengembalian(File file, List<Peminjaman> list) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write("No,Kode Transaksi,NIM,Nama Peminjam,Petugas,Tanggal Pinjam,Jatuh Tempo,Tanggal Kembali,Total Denda (Rp),Catatan\n");
            int no = 1;
            for (Peminjaman p : list) {
                String line = String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        no++,
                        p.getKodeTransaksi(),
                        p.getNimAnggota(),
                        escapeCsv(p.getNamaAnggota()),
                        escapeCsv(p.getNamaPetugas()),
                        p.getTanggalPinjam(),
                        p.getTanggalJatuhTempo(),
                        p.getTanggalKembali() != null ? p.getTanggalKembali().toString() : "-",
                        p.getTotalDenda() != null ? p.getTotalDenda().toString() : "0",
                        escapeCsv(p.getCatatan() != null ? p.getCatatan() : "-")
                );
                writer.write(line);
            }
        }
    }

    public void exportToCSV(File file, List<Peminjaman> list) throws IOException {
        exportLaporanPengembalian(file, list);
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
