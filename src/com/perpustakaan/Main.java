package com.perpustakaan;

import com.formdev.flatlaf.FlatDarkLaf;
import com.perpustakaan.ui.HalamanUtama;

import javax.swing.*;
import java.awt.*;


public class Main {
    public static void main(String[] args) {
        // Atur antarmuka modern FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Penyesuaian font default sistem
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
            // Aksen Tema Gelap Pertahanan (Midnight Navy + Emas)
            UIManager.put("Component.focusColor", new Color(212, 175, 55));
            UIManager.put("Table.selectionBackground", new Color(38, 64, 115));
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("TextComponent.selectionBackground", new Color(212, 175, 55));
            UIManager.put("TextComponent.selectionForeground", new Color(13, 20, 36));
            UIManager.put("Panel.background", new Color(13, 20, 36));
            UIManager.put("Table.background", new Color(21, 31, 52));
            UIManager.put("TextField.background", new Color(16, 24, 42));
            UIManager.put("ComboBox.background", new Color(16, 24, 42));
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
