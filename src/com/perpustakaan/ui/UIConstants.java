package com.perpustakaan.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.Border;

public class UIConstants {
    // Skema Warna Tema Gelap Pertahanan (Midnight Navy + Emas, selaras Logo Kemhan RI)
    public static final Color PRIMARY_COLOR = new Color(38, 84, 160);      // Navy Terang (tombol utama)
    public static final Color PRIMARY_DARK = new Color(26, 52, 100);       // Navy Pertahanan
    public static final Color ACCENT_GREEN = new Color(40, 140, 98);       // Hijau Dinas
    public static final Color ACCENT_RED = new Color(190, 55, 55);         // Merah Peringatan
    public static final Color ACCENT_ORANGE = new Color(184, 134, 28);     // Emas Tua (aksi sekunder)
    public static final Color GOLD = new Color(212, 175, 55);              // Emas Kehormatan
    public static final Color MAROON = new Color(122, 24, 32);             // Marun Kemhan
    public static final Color INFO_BLUE = new Color(110, 160, 235);        // Biru teks/angka di latar gelap
    public static final Color TEXT_GREEN = new Color(72, 187, 138);
    public static final Color TEXT_RED = new Color(235, 100, 100);
    public static final Color TEXT_AMBER = new Color(230, 175, 60);
    public static final Color SIDEBAR_BG = new Color(8, 14, 28);           // Midnight
    public static final Color SIDEBAR_TEXT = new Color(210, 220, 235);
    public static final Color SIDEBAR_ACTIVE = new Color(24, 44, 84);
    public static final Color SIDEBAR_DIVIDER = new Color(30, 46, 78);
    public static final Color TABLE_HEADER_BG = new Color(30, 44, 74);
    public static final Color BG_LIGHT = new Color(13, 20, 36);            // Kanvas halaman gelap
    public static final Color CARD_BG = new Color(21, 31, 52);             // Permukaan kartu
    public static final Color BG_INSET = new Color(16, 24, 42);            // Area cekung / empty state
    public static final Color BORDER_COLOR = new Color(40, 56, 90);        // Garis tipis
    public static final Color TEXT_PRIMARY = new Color(230, 236, 246);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);
    public static final Color BTN_NEUTRAL = new Color(51, 65, 95);

    // Font Standar Modern
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
    }

    public static Image loadLogoKemhanImage() {
        String[] candidatePaths = {
                "assets/logo-kemhan.png",
                "kkp-perpustakaan-desktop/assets/logo-kemhan.png",
                "perpustakaan-desktop/assets/logo-kemhan.png"
        };
        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    return ImageIO.read(file);
                } catch (Exception ignored) {
                }
            }
        }
        try {
            var resourceUrl = UIConstants.class.getResource("/assets/logo-kemhan.png");
            if (resourceUrl != null) {
                return ImageIO.read(resourceUrl);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ImageIcon loadLogoKemhanIcon(int width, int height) {
        Image img = loadLogoKemhanImage();
        if (img == null) {
            return null;
        }
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
