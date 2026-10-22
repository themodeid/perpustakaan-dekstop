package com.perpustakaan;

import com.formdev.flatlaf.FlatLightLaf;
import com.perpustakaan.ui.HalamanUtama;

import javax.swing.*;
import java.awt.*;


public class Main {
    public static void main(String[] args) {
        // Atur antarmuka modern FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Penyesuaian font default sistem
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception ex) {
            System.err.println("Gagal memuat tema FlatLaf, menggunakan tema bawaan sistem: " + ex.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Jalankan Migrasi Database Otomatis jika koneksi tersedia
        new Thread(() -> {
            try {
                com.perpustakaan.config.DatabaseMigration.runMigrations();
            } catch (Exception ex) {
                System.err.println("Gagal migrasi database otomatis: " + ex.getMessage());
            }
        }).start();

        // Jalankan Halaman Utama langsung (Default: Mode Pengunjung Publik OPAC)
        SwingUtilities.invokeLater(() -> {
            HalamanUtama halamanUtama = new HalamanUtama();
            halamanUtama.setVisible(true);
        });
    }
}
