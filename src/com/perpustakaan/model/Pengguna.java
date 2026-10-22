package com.perpustakaan.model;

import java.sql.Timestamp;

/**
 * NAMA FILE : Pengguna.java
 * FUNGSI    : Entitas data akun pengguna (Admin dan Petugas perpustakaan).
 * TABEL DB  : users
 */
public class Pengguna {
    private int id;
    private String username;
    private String password;
    private String namaLengkap;
    private String role; // "admin" atau "petugas"
    private String noTelp;
    private Timestamp createdAt;

    public Pengguna() {}

    public Pengguna(int id, String username, String password, String namaLengkap, String role, String noTelp) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.namaLengkap = namaLengkap;
        this.role = role;
        this.noTelp = noTelp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return namaLengkap + " (" + role + ")";
    }
}
