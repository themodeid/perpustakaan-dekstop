package com.perpustakaan.service;

import com.perpustakaan.dao.AnggotaDAO;
import com.perpustakaan.dao.BukuDAO;
import com.perpustakaan.dao.PeminjamanDAO;
import com.perpustakaan.model.Anggota;
import com.perpustakaan.model.Buku;
import com.perpustakaan.model.DetailPeminjaman;
import com.perpustakaan.model.Peminjaman;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * NAMA FILE : LayananSirkulasi.java
 * FUNGSI    : Logika bisnis transaksi sirkulasi buku (peminjaman, pengembalian, dan kalkulasi denda).
 */
public class LayananSirkulasi {
    public static final BigDecimal TARIF_DENDA_PER_HARI = new BigDecimal("2000.00");
    public static final int DEFAULT_DURASI_PINJAM_HARI = 7;
    public static final int MAX_BUKU_PINJAM = 3;

    private final PeminjamanDAO peminjamanDAO;
    private final BukuDAO bukuDAO;
    private final AnggotaDAO anggotaDAO;

    public LayananSirkulasi() {
        this.peminjamanDAO = new PeminjamanDAO();
        this.bukuDAO = new BukuDAO();
        this.anggotaDAO = new AnggotaDAO();
    }

    public String generateKodeTransaksi() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new java.util.Date());
        int randomPart = 1000 + new Random().nextInt(9000);
        return "TRX-" + datePart + "-" + randomPart;
    }

    public Peminjaman buatPeminjaman(int anggotaId, List<Integer> bukuIds, int durasiHari, String catatan) throws Exception {
        if (bukuIds == null || bukuIds.isEmpty()) {
            throw new IllegalArgumentException("Daftar buku yang dipinjam tidak boleh kosong!");
        }
        if (bukuIds.size() > MAX_BUKU_PINJAM) {
            throw new IllegalArgumentException("Maksimal buku yang dapat dipinjam sekaligus adalah " + MAX_BUKU_PINJAM + " buku!");
        }

        Anggota anggota = anggotaDAO.getById(anggotaId);
        if (anggota == null) {
            throw new IllegalArgumentException("Anggota tidak ditemukan!");
        }
        if (!anggota.isAktif()) {
            throw new IllegalArgumentException("Anggota (" + anggota.getNama() + ") sedang berstatus NON-AKTIF!");
        }

        // Cek stok buku
        for (Integer bId : bukuIds) {
            Buku b = bukuDAO.getById(bId);
            if (b == null) {
                throw new IllegalArgumentException("Buku dengan ID " + bId + " tidak ditemukan!");
            }
            if (b.getStokTersedia() <= 0) {
                throw new IllegalArgumentException("Buku '" + b.getJudul() + "' stoknya sedang habis (0)!");
            }
        }

        LocalDate now = LocalDate.now();
        LocalDate jatuhTempo = now.plusDays(durasiHari > 0 ? durasiHari : DEFAULT_DURASI_PINJAM_HARI);

        Peminjaman p = new Peminjaman();
        p.setKodeTransaksi(generateKodeTransaksi());
        p.setAnggotaId(anggotaId);
        p.setUserId(LayananAutentikasi.getInstance().getCurrentUser() != null ? 
                    LayananAutentikasi.getInstance().getCurrentUser().getId() : 1);
        p.setTanggalPinjam(Date.valueOf(now));
        p.setTanggalJatuhTempo(Date.valueOf(jatuhTempo));
        p.setStatus("dipinjam");
        p.setTotalDenda(BigDecimal.ZERO);
        p.setCatatan(catatan);

        boolean ok = peminjamanDAO.insertTransaksi(p, bukuIds);
        if (!ok) {
            throw new SQLException("Gagal menyimpan transaksi peminjaman ke database.");
        }
        return p;
    }

    public long hitungHariKeterlambatan(Date tglJatuhTempo, Date tglKembali) {
        if (tglJatuhTempo == null || tglKembali == null) return 0;
        LocalDate tempo = tglJatuhTempo.toLocalDate();
        LocalDate kembali = tglKembali.toLocalDate();
        if (kembali.isAfter(tempo)) {
            return ChronoUnit.DAYS.between(tempo, kembali);
        }
        return 0;
    }

    public BigDecimal kalkulasiDenda(Date tglJatuhTempo, Date tglKembali, int jumlahBuku) {
        long hariTelat = hitungHariKeterlambatan(tglJatuhTempo, tglKembali);
        if (hariTelat <= 0) {
            return BigDecimal.ZERO;
        }
        return TARIF_DENDA_PER_HARI.multiply(BigDecimal.valueOf(hariTelat)).multiply(BigDecimal.valueOf(jumlahBuku));
    }

    public boolean prosesPengembalian(int peminjamanId, Date tglKembali, BigDecimal denda, String catatan) throws Exception {
        List<DetailPeminjaman> details = peminjamanDAO.getDetailByPeminjamanId(peminjamanId);
        if (details.isEmpty()) {
            throw new IllegalArgumentException("Tidak ada detail buku untuk transaksi ID: " + peminjamanId);
        }

        List<Integer> bukuIds = new ArrayList<>();
        for (DetailPeminjaman dp : details) {
            bukuIds.add(dp.getBukuId());
        }

        return peminjamanDAO.prosesPengembalian(peminjamanId, tglKembali, denda, catatan, bukuIds);
    }
}
