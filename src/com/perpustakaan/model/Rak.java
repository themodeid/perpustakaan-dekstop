package com.perpustakaan.model;

public class Rak {
    private int id;
    private String kodeRak;
    private String namaRak;
    private String lokasi;

    public Rak() {}

    public Rak(int id, String kodeRak, String namaRak, String lokasi) {
        this.id = id;
        this.kodeRak = kodeRak;
        this.namaRak = namaRak;
        this.lokasi = lokasi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodeRak() { return kodeRak; }
    public void setKodeRak(String kodeRak) { this.kodeRak = kodeRak; }

    public String getNamaRak() { return namaRak; }
    public void setNamaRak(String namaRak) { this.namaRak = namaRak; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    @Override
    public String toString() {
        return kodeRak + " - " + namaRak + " (" + lokasi + ")";
    }
}
