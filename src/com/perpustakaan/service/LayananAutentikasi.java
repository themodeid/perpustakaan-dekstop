package com.perpustakaan.service;

import com.perpustakaan.dao.PenggunaDAO;
import com.perpustakaan.model.Pengguna;

import java.sql.SQLException;

/**
 * NAMA FILE : LayananAutentikasi.java
 * FUNGSI    : Logika bisnis autentikasi (login, logout, dan manajemen sesi pengguna aktif).
 */
public class LayananAutentikasi {
    private static LayananAutentikasi instance;
    private final PenggunaDAO penggunaDAO;
    private Pengguna currentUser;

    private LayananAutentikasi() {
        this.penggunaDAO = new PenggunaDAO();
    }

    public static synchronized LayananAutentikasi getInstance() {
        if (instance == null) {
            instance = new LayananAutentikasi();
        }
        return instance;
    }

    public boolean login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        Pengguna pengguna = penggunaDAO.authenticate(username.trim(), password);
        if (pengguna != null) {
            this.currentUser = pengguna;
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Pengguna getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
