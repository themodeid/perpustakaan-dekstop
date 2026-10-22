package com.perpustakaan.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Peminjaman {
    private int id;
    private String kodeTransaksi;
    private int anggotaId;
    private int userId;
    private Date tanggalPinjam;
    private Date tanggalJatuhTempo;
    private Date tanggalKembali;
    private String status; // "dipinjam", "kembali", "terlambat"
    private BigDecimal totalDenda = BigDecimal.ZERO;
    private String catatan;
    private Timestamp createdAt;

    // Relasi untuk kemudahan presentasi data di GUI
    private String namaAnggota;
    private String nimAnggota;
    private String namaPetugas;
    private List<DetailPeminjaman> detailList = new ArrayList<>();

    public Peminjaman() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodeTransaksi() { return kodeTransaksi; }
    public void setKodeTransaksi(String kodeTransaksi) { this.kodeTransaksi = kodeTransaksi; }

    public int getAnggotaId() { return anggotaId; }
    public void setAnggotaId(int anggotaId) { this.anggotaId = anggotaId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getTanggalPinjam() { return tanggalPinjam; }
    public void setTanggalPinjam(Date tanggalPinjam) { this.tanggalPinjam = tanggalPinjam; }

    public Date getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public void setTanggalJatuhTempo(Date tanggalJatuhTempo) { this.tanggalJatuhTempo = tanggalJatuhTempo; }

    public Date getTanggalKembali() { return tanggalKembali; }
    public void setTanggalKembali(Date tanggalKembali) { this.tanggalKembali = tanggalKembali; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalDenda() { return totalDenda; }
    public void setTotalDenda(BigDecimal totalDenda) { this.totalDenda = totalDenda; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getNamaAnggota() { return namaAnggota; }
    public void setNamaAnggota(String namaAnggota) { this.namaAnggota = namaAnggota; }

    public String getNimAnggota() { return nimAnggota; }
    public void setNimAnggota(String nimAnggota) { this.nimAnggota = nimAnggota; }

    public String getNamaPetugas() { return namaPetugas; }
    public void setNamaPetugas(String namaPetugas) { this.namaPetugas = namaPetugas; }

    public List<DetailPeminjaman> getDetailList() { return detailList; }
    public void setDetailList(List<DetailPeminjaman> detailList) { this.detailList = detailList; }
}
