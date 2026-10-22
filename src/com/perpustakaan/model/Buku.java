package com.perpustakaan.model;

import java.sql.Timestamp;

public class Buku {
    private int id;
    private String isbn;
    private String judul;
    private String pengarang;
    private String penerbit;
    private int tahunTerbit;
    private int kategoriId;
    private String namaKategori;
    private int rakId = 1;
    private String kodeRak;
    private String namaRak;
    private String lokasiRak;
    private int stokTotal;
    private int stokTersedia;
    private Timestamp createdAt;

    public Buku() {}

    public Buku(int id, String isbn, String judul, String pengarang, String penerbit, 
                int tahunTerbit, int kategoriId, int rakId, int stokTotal, int stokTersedia) {
        this.id = id;
        this.isbn = isbn;
        this.judul = judul;
        this.pengarang = pengarang;
        this.penerbit = penerbit;
        this.tahunTerbit = tahunTerbit;
        this.kategoriId = kategoriId;
        this.rakId = rakId;
        this.stokTotal = stokTotal;
        this.stokTersedia = stokTersedia;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getPengarang() { return pengarang; }
    public void setPengarang(String pengarang) { this.pengarang = pengarang; }

    public String getPenerbit() { return penerbit; }
    public void setPenerbit(String penerbit) { this.penerbit = penerbit; }

    public int getTahunTerbit() { return tahunTerbit; }
    public void setTahunTerbit(int tahunTerbit) { this.tahunTerbit = tahunTerbit; }

    public int getKategoriId() { return kategoriId; }
    public void setKategoriId(int kategoriId) { this.kategoriId = kategoriId; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public int getRakId() { return rakId; }
    public void setRakId(int rakId) { this.rakId = rakId; }

    public String getKodeRak() { return kodeRak; }
    public void setKodeRak(String kodeRak) { this.kodeRak = kodeRak; }

    public String getNamaRak() { return namaRak; }
    public void setNamaRak(String namaRak) { this.namaRak = namaRak; }

    public String getLokasiRak() { return lokasiRak; }
    public void setLokasiRak(String lokasiRak) { this.lokasiRak = lokasiRak; }

    public int getStokTotal() { return stokTotal; }
    public void setStokTotal(int stokTotal) { this.stokTotal = stokTotal; }

    public int getStokTersedia() { return stokTersedia; }
    public void setStokTersedia(int stokTersedia) { this.stokTersedia = stokTersedia; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getInfoRak() {
        if (kodeRak != null && namaRak != null) {
            return kodeRak + " (" + namaRak + ")";
        }
        return "Rak ID: " + rakId;
    }

    @Override
    public String toString() {
        return judul + " [Rak: " + (kodeRak != null ? kodeRak : "-") + "] (Tersedia: " + stokTersedia + ")";
    }
}
