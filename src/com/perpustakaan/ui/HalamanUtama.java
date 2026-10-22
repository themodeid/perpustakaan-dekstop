package com.perpustakaan.ui;

import com.perpustakaan.model.Pengguna;
import com.perpustakaan.service.LayananAutentikasi;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NAMA FILE : HalamanUtama.java
 * FUNGSI    : Jendela utama (Main Frame) sistem informasi perpustakaan berbasis desktop untuk pengelola & pustakawan (Kemenhan RI).
 */
public class HalamanUtama extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final List<JButton> menuButtons = new ArrayList<>();
    private JPanel sidebarContent;
    private JLabel lblUser;
    private JButton btnAuthAction;

    // Sub-panels (Antarmuka Modul Sistem)
    private PanelBeranda panelBeranda;
    private FormMasterBuku formMasterBuku;
    private FormMasterAnggota formMasterAnggota;
    private FormMasterPetugas formMasterPetugas;
    private FormMasterRak formMasterRak;
    private FormMasterKategori formMasterKategori;
    private FormTransaksiPeminjaman formTransaksiPeminjaman;
    private FormTransaksiPengembalian formTransaksiPengembalian;
    private FormLaporanBuku formLaporanBuku;
    private FormLaporanAnggota formLaporanAnggota;
    private FormLaporanPeminjaman formLaporanPeminjaman;
    private FormLaporanPengembalian formLaporanPengembalian;

    public HalamanUtama() {
        setTitle("SIPerpus - Sistem Informasi Perpustakaan Desktop (Kementerian Pertahanan RI)");
        setSize(1240, 780);
        setMinimumSize(new Dimension(1050, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Auto-Login Default Petugas / Operator untuk pengalaman kerja zero-friction
        ensureOperatorLoggedIn();

        initUI();
        refreshAuthUI();
    }

    private void ensureOperatorLoggedIn() {
        if (!LayananAutentikasi.getInstance().isLoggedIn()) {
            try {
                boolean ok = LayananAutentikasi.getInstance().login("admin", "admin123");
                if (!ok) {
                    Pengguna fallback = new Pengguna(1, "admin", "admin123", "Administrator Utama", "admin", "081234567890");
                    LayananAutentikasi.getInstance().setCurrentUser(fallback);
                }
            } catch (Exception ex) {
                Pengguna fallback = new Pengguna(1, "admin", "admin123", "Administrator Utama", "admin", "081234567890");
                LayananAutentikasi.getInstance().setCurrentUser(fallback);
            }
        }
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        // Header Panel Atas (Branding Instansi Resmi)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 230, 238)),
                BorderFactory.createEmptyBorder(10, 22, 10, 22)
        ));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel lblBrand = new JLabel("SIPerpus Desktop — Perpustakaan Digital");
        lblBrand.setFont(UIConstants.FONT_HEADER);
        lblBrand.setForeground(UIConstants.PRIMARY_COLOR);

        JLabel lblSubBrand = new JLabel("Perpustakaan Kementerian Pertahanan Republik Indonesia");
        lblSubBrand.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSubBrand.setForeground(new Color(100, 116, 139));

        brandPanel.add(lblBrand);
        brandPanel.add(lblSubBrand);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userInfoPanel.setOpaque(false);

        lblUser = new JLabel("● Operator: Administrator Perpustakaan (Online)");
        lblUser.setFont(UIConstants.FONT_BOLD);
        lblUser.setForeground(new Color(16, 149, 103)); // Online Green

        btnAuthAction = UIConstants.createStyledButton("Ganti Akun", UIConstants.PRIMARY_DARK, Color.WHITE);
        btnAuthAction.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAuthAction.setPreferredSize(new Dimension(110, 32));
        btnAuthAction.addActionListener(e -> showLoginDialog());

        userInfoPanel.add(lblUser);
        userInfoPanel.add(btnAuthAction);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Sidebar Navigation Kiri
        sidebarContent = new JPanel();
        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
        sidebarContent.setBackground(UIConstants.SIDEBAR_BG);
        sidebarContent.setBorder(BorderFactory.createEmptyBorder(14, 12, 15, 12));

        // Center Content with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Inisialisasi Seluruh Panel
        panelBeranda = new PanelBeranda();
        panelBeranda.setOnNavigateRequested(this::switchCardByName);

        formMasterBuku = new FormMasterBuku();
        formMasterAnggota = new FormMasterAnggota();
        formMasterPetugas = new FormMasterPetugas();
        formMasterRak = new FormMasterRak();
        formMasterKategori = new FormMasterKategori();
        formTransaksiPeminjaman = new FormTransaksiPeminjaman();
        formTransaksiPengembalian = new FormTransaksiPengembalian();
        formLaporanBuku = new FormLaporanBuku();
        formLaporanAnggota = new FormLaporanAnggota();
        formLaporanPeminjaman = new FormLaporanPeminjaman();
        formLaporanPengembalian = new FormLaporanPengembalian();

        contentPanel.add(panelBeranda, "beranda");
        contentPanel.add(formMasterBuku, "buku");
        contentPanel.add(formMasterAnggota, "anggota");
        contentPanel.add(formMasterPetugas, "petugas");
        contentPanel.add(formMasterRak, "rak");
        contentPanel.add(formMasterKategori, "kategori");
        contentPanel.add(formTransaksiPeminjaman, "peminjaman");
        contentPanel.add(formTransaksiPengembalian, "pengembalian");
        contentPanel.add(formLaporanBuku, "lap_buku");
        contentPanel.add(formLaporanAnggota, "lap_anggota");
        contentPanel.add(formLaporanPeminjaman, "lap_peminjaman");
        contentPanel.add(formLaporanPengembalian, "lap_pengembalian");

        JScrollPane sidebarScroll = new JScrollPane(sidebarContent);
        sidebarScroll.setBorder(BorderFactory.createEmptyBorder());
        sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setPreferredSize(new Dimension(250, 0));

        rootPanel.add(sidebarScroll, BorderLayout.WEST);
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        add(rootPanel);
    }

    public void refreshAuthUI() {
        Pengguna user = LayananAutentikasi.getInstance().getCurrentUser();

        // 1. Update Header Status
        if (user != null) {
            lblUser.setText("● Operator: " + user.getNamaLengkap() + " (" + user.getRole().toUpperCase() + ")");
            lblUser.setForeground(new Color(16, 149, 103));
        }

        // 2. Set Seluruh Modul Full Akses (Operator Mode)
        formMasterBuku.setReadOnly(false);
        formMasterRak.setReadOnly(false);

        // 3. Bangun Sidebar Navigasi Penuh (Zero Empty Space)
        sidebarContent.removeAll();
        menuButtons.clear();

        // SEKSI 1: UTAMA
        addSectionHeader(sidebarContent, "DASHBOARD");
        addMenuItem(sidebarContent, "  Dashboard Utama", "beranda");
        sidebarContent.add(Box.createVerticalStrut(10));

        // SEKSI 2: DATA MASTER
        addSectionHeader(sidebarContent, "DATA MASTER");
        addMenuItem(sidebarContent, "  1. Master Koleksi Buku", "buku");
        addMenuItem(sidebarContent, "  2. Master Data Anggota", "anggota");
        addMenuItem(sidebarContent, "  3. Lokasi Rak Buku", "rak");
        addMenuItem(sidebarContent, "  4. Kategori Buku", "kategori");
        if (user == null || user.isAdmin()) {
            addMenuItem(sidebarContent, "  5. Pengguna & Petugas", "petugas");
        }
        sidebarContent.add(Box.createVerticalStrut(10));

        // SEKSI 3: FORM TRANSAKSI
        addSectionHeader(sidebarContent, "FORM TRANSAKSI");
        addMenuItem(sidebarContent, "  1. Peminjaman Buku", "peminjaman");
        addMenuItem(sidebarContent, "  2. Pengembalian & Denda", "pengembalian");
        sidebarContent.add(Box.createVerticalStrut(10));

        // SEKSI 4: MENU LAPORAN
        addSectionHeader(sidebarContent, "MENU LAPORAN");
        addMenuItem(sidebarContent, "  1. Laporan Koleksi Buku", "lap_buku");
        addMenuItem(sidebarContent, "  2. Laporan Data Anggota", "lap_anggota");
        addMenuItem(sidebarContent, "  3. Laporan Peminjaman", "lap_peminjaman");
        addMenuItem(sidebarContent, "  4. Laporan Rekapitulasi Denda", "lap_pengembalian");

        sidebarContent.add(Box.createVerticalGlue());
        sidebarContent.revalidate();
        sidebarContent.repaint();

        // Buka menu Beranda sebagai default
        if (!menuButtons.isEmpty()) {
            switchCard("beranda", menuButtons.get(0));
        }
    }

    private void showLoginDialog() {
        FormLogin loginDialog = new FormLogin(this);
        loginDialog.setVisible(true);
        if (loginDialog.isLoginSuccessful()) {
            refreshAuthUI();
        }
    }

    private void addSectionHeader(JPanel panel, String title) {
        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(148, 163, 184));

        headerWrap.add(lbl, BorderLayout.CENTER);
        headerWrap.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)));

        panel.add(Box.createVerticalStrut(6));
        panel.add(headerWrap);
        panel.add(Box.createVerticalStrut(4));
    }

    private JButton createMenuButton(String title) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(new Color(226, 232, 240));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private void addMenuItem(JPanel sidebar, String title, String cardName) {
        JButton btn = createMenuButton(title);
        btn.putClientProperty("targetCard", cardName);
        btn.addActionListener(e -> switchCard(cardName, btn));

        menuButtons.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(2));
    }

    public void switchCardByName(String cardName) {
        for (JButton b : menuButtons) {
            Object target = b.getClientProperty("targetCard");
            if (cardName != null && cardName.equals(target)) {
                switchCard(cardName, b);
                return;
            }
        }
        switchCard(cardName, null);
    }

    private void switchCard(String cardName, JButton activeBtn) {
        for (JButton b : menuButtons) {
            b.setOpaque(false);
            b.setBackground(UIConstants.SIDEBAR_BG);
            b.setForeground(new Color(203, 213, 225));
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        }
        if (activeBtn != null) {
            activeBtn.setOpaque(true);
            activeBtn.setBackground(new Color(37, 99, 235));
            activeBtn.setForeground(Color.WHITE);
            activeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            activeBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(147, 197, 253)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 14)
            ));
        }

        cardLayout.show(contentPanel, cardName);

        // Auto Refresh data panel terpilih
        switch (cardName) {
            case "beranda":
                panelBeranda.refreshData();
                break;
            case "buku":
                formMasterBuku.loadKategoriFilter();
                formMasterBuku.loadData();
                break;
            case "anggota":
                formMasterAnggota.loadData();
                break;
            case "petugas":
                if (formMasterPetugas != null) formMasterPetugas.loadData();
                break;
            case "rak":
                formMasterRak.loadData();
                break;
            case "kategori":
                formMasterKategori.loadData();
                break;
            case "peminjaman":
                formTransaksiPeminjaman.loadDropdownData();
                break;
            case "pengembalian":
                formTransaksiPengembalian.loadDataAktif();
                break;
            case "lap_buku":
                formLaporanBuku.loadData();
                break;
            case "lap_anggota":
                formLaporanAnggota.loadData();
                break;
            case "lap_peminjaman":
                formLaporanPeminjaman.loadData();
                break;
            case "lap_pengembalian":
                formLaporanPengembalian.loadData();
                break;
        }
    }
}
