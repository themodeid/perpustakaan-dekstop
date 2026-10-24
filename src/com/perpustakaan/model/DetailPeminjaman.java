package com.perpustakaan.model;

import java.math.BigDecimal;

public class DetailPeminjaman {
    private int id;
    private int peminjamanId;
    private int bukuId;
    private BigDecimal dendaPerBuku = BigDecimal.ZERO;
    
    // Informasi tambahan untuk UI/Laporan
    private String judulBuku;
    private String pengarangBuku;

    public DetailPeminjaman() {}

    public DetailPeminjaman(int id, int peminjamanId, int bukuId, BigDecimal dendaPerBuku) {
        this.id = id;
        this.peminjamanId = peminjamanId;
        this.bukuId = bukuId;
        this.dendaPerBuku = dendaPerBuku;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPeminjamanId() { return peminjamanId; }
    public void setPeminjamanId(int peminjamanId) { this.peminjamanId = peminjamanId; }

    public int getBukuId() { return bukuId; }
    public void setBukuId(int bukuId) { this.bukuId = bukuId; }

    public BigDecimal getDendaPerBuku() { return dendaPerBuku; }
    public void setDendaPerBuku(BigDecimal dendaPerBuku) { this.dendaPerBuku = dendaPerBuku; }

    public String getJudulBuku() { return judulBuku; }
    public void setJudulBuku(String judulBuku) { this.judulBuku = judulBuku; }

    public String getPengarangBuku() { return pengarangBuku; }
    public void setPengarangBuku(String pengarangBuku) { this.pengarangBuku = pengarangBuku; }
}
