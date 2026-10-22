package com.perpustakaan.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

public class UIConstants {
    // Skema Warna Modern
    public static final Color PRIMARY_COLOR = new Color(30, 110, 235);     // Royal Blue
    public static final Color PRIMARY_DARK = new Color(20, 80, 180);
    public static final Color ACCENT_GREEN = new Color(40, 167, 69);       // Emerald Green
    public static final Color ACCENT_RED = new Color(220, 53, 69);         // Danger Red
    public static final Color ACCENT_ORANGE = new Color(245, 134, 52);     // Warning Orange
    public static final Color SIDEBAR_BG = new Color(28, 36, 52);          // Deep Dark Navy
    public static final Color SIDEBAR_TEXT = new Color(210, 220, 235);
    public static final Color SIDEBAR_ACTIVE = new Color(45, 58, 85);
    public static final Color BG_LIGHT = new Color(244, 246, 249);
    public static final Color CARD_BG = Color.WHITE;

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
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
    }
}
