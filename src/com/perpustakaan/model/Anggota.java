package com.perpustakaan.model;

import java.sql.Timestamp;

public class Anggota {
    private int id;
    private String nim;
    private String nama;
    private String jurusan;
    private String noTelp;
    private String email;
    private String status; // "aktif" atau "nonaktif"
    private Timestamp createdAt;

    public Anggota() {}

    public Anggota(int id, String nim, String nama, String jurusan, String noTelp, String email, String status) {
        this.id = id;
        this.nim = nim;
        this.nama = nama;
        this.jurusan = jurusan;
        this.noTelp = noTelp;
        this.email = email;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAktif() {
        return "aktif".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return nim + " - " + nama + " (" + jurusan + ")";
    }
}
