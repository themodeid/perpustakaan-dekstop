package com.perpustakaan.ui;

import com.perpustakaan.dao.PeminjamanDAO;
import com.perpustakaan.model.Peminjaman;
import com.perpustakaan.service.LayananLaporan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * NAMA FILE : PanelBeranda.java
 * FUNGSI    : Tampilan dashboard operasional utama perpustakaan (Executive Hub, Statistik, Sirkulasi Terkini, dan Quick Actions).
 */
public class PanelBeranda extends JPanel {
    private final LayananLaporan layananLaporan = new LayananLaporan();
    private final PeminjamanDAO peminjamanDAO = new PeminjamanDAO();

    private JLabel lblTotalBuku;
    private JLabel lblBukuDipinjam;
    private JLabel lblTotalAnggota;
    private JLabel lblTotalDenda;
    private JTable tblTransaksi;
    private DefaultTableModel tableModel;

    // CardLayout untuk Table vs Empty State
    private CardLayout tableSwitchLayout;
    private JPanel tableSwitchContainer;

    private Consumer<String> onNavigateRequested;

    public PanelBeranda() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        refreshData();
    }

    public void setOnNavigateRequested(Consumer<String> listener) {
        this.onNavigateRequested = listener;
    }

    public void setOnLoginRequested(Runnable listener) {
        // Dipertahankan untuk kompatibilitas
    }

    private void initUI() {
        // ================= TOP SECTION: Title Bar & Refresh =================
        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titleTextPanel.setOpaque(false);

        JLabel lblSectionTitle = new JLabel("Dashboard Utama: Pusat Kendali Perpustakaan");
        lblSectionTitle.setFont(UIConstants.FONT_TITLE);
        lblSectionTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSectionDesc = new JLabel("Sistem Informasi Manajemen Sirkulasi & Inventaris Buku — Kementerian Pertahanan RI");
        lblSectionDesc.setFont(UIConstants.FONT_REGULAR);
        lblSectionDesc.setForeground(new Color(100, 116, 139));

        titleTextPanel.add(lblSectionTitle);
        titleTextPanel.add(lblSectionDesc);

        JButton btnRefresh = UIConstants.createStyledButton("Segarkan Data", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(135, 36));
        btnRefresh.addActionListener(e -> refreshData());

        titleBar.add(titleTextPanel, BorderLayout.WEST);
        titleBar.add(btnRefresh, BorderLayout.EAST);
        topContainer.add(titleBar, BorderLayout.NORTH);

        // ================= METRIC CARDS (4 Cards) =================
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsGrid.setOpaque(false);

        lblTotalBuku = new JLabel("0", SwingConstants.LEFT);
        lblBukuDipinjam = new JLabel("0", SwingConstants.LEFT);
        lblTotalAnggota = new JLabel("0", SwingConstants.LEFT);
        lblTotalDenda = new JLabel("Rp 0", SwingConstants.LEFT);

        cardsGrid.add(createMetricCard("Total Koleksi Buku", lblTotalBuku, "BUKU", new Color(37, 99, 235)));
        cardsGrid.add(createMetricCard("Buku Sedang Dipinjam", lblBukuDipinjam, "PINJAM", new Color(217, 119, 6)));
        cardsGrid.add(createMetricCard("Anggota Terdaftar", lblTotalAnggota, "MEMBER", new Color(5, 150, 105)));
        cardsGrid.add(createMetricCard("Total Denda Terkumpul", lblTotalDenda, "DENDA", new Color(220, 38, 38)));

        topContainer.add(cardsGrid, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // ================= MAIN SPLIT (62% Left Sirkulasi : 38% Right Quick Actions) =================
        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ---------- LEFT PANEL (Tabel Sirkulasi / Empty State) ----------
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.63;
        gbc.insets = new Insets(0, 0, 0, 15);

        JPanel leftCard = new JPanel(new BorderLayout(0, 10));
        leftCard.setBackground(Color.WHITE);
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                UIConstants.createCardBorder(),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Aktivitas Sirkulasi Terkini");
        lblTableTitle.setFont(UIConstants.FONT_HEADER);
        lblTableTitle.setForeground(new Color(30, 41, 59));

        JLabel lblTableSub = new JLabel("Riwayat transaksi peminjaman aktif & jatuh tempo");
        lblTableSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTableSub.setForeground(new Color(100, 116, 139));

        tableHeaderPanel.add(lblTableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(lblTableSub, BorderLayout.EAST);
        leftCard.add(tableHeaderPanel, BorderLayout.NORTH);

        // Container Switch Antara Table & Empty State
        tableSwitchLayout = new CardLayout();
        tableSwitchContainer = new JPanel(tableSwitchLayout);
        tableSwitchContainer.setOpaque(false);

        // Table Panel
        String[] columns = {"Kode Transaksi", "NIP / NRP", "Personel", "Petugas", "Tgl Pinjam", "Jatuh Tempo", "Status", "Denda"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTransaksi = new JTable(tableModel);
        tblTransaksi.setFont(UIConstants.FONT_REGULAR);
        tblTransaksi.setRowHeight(30);
        tblTransaksi.getTableHeader().setFont(UIConstants.FONT_BOLD);
        tblTransaksi.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblTransaksi.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblTransaksi.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblTransaksi.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblTransaksi.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblTransaksi.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tblTransaksi);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableSwitchContainer.add(scrollPane, "table");

        // Empty State Panel (Anti-Halaman Kosong)
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(new Color(248, 250, 252));
        emptyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(30, 20, 30, 20)
        ));

        JLabel lblEmptyIcon = new JLabel("📚", SwingConstants.CENTER);
        lblEmptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        lblEmptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("Belum Ada Transaksi Sirkulasi Berjalan", SwingConstants.CENTER);
        lblEmptyTitle.setFont(UIConstants.FONT_HEADER);
        lblEmptyTitle.setForeground(new Color(30, 41, 59));
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyDesc = new JLabel("Seluruh buku tersedia di rak. Klik tombol di bawah untuk membuat peminjaman baru.", SwingConstants.CENTER);
        lblEmptyDesc.setFont(UIConstants.FONT_REGULAR);
        lblEmptyDesc.setForeground(new Color(100, 116, 139));
        lblEmptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnStartPinjam = UIConstants.createStyledButton("+ Buat Transaksi Peminjaman Baru", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnStartPinjam.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStartPinjam.setMaximumSize(new Dimension(260, 38));
        btnStartPinjam.setPreferredSize(new Dimension(260, 38));
        btnStartPinjam.addActionListener(e -> {
            if (onNavigateRequested != null) onNavigateRequested.accept("peminjaman");
        });

        emptyPanel.add(Box.createVerticalGlue());
        emptyPanel.add(lblEmptyIcon);
        emptyPanel.add(Box.createVerticalStrut(10));
        emptyPanel.add(lblEmptyTitle);
        emptyPanel.add(Box.createVerticalStrut(6));
        emptyPanel.add(lblEmptyDesc);
        emptyPanel.add(Box.createVerticalStrut(16));
        emptyPanel.add(btnStartPinjam);
        emptyPanel.add(Box.createVerticalGlue());

        tableSwitchContainer.add(emptyPanel, "empty");
        leftCard.add(tableSwitchContainer, BorderLayout.CENTER);
        mainGrid.add(leftCard, gbc);

        // ---------- RIGHT PANEL (Quick Actions & Ketentuan Layanan) ----------
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.37;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setOpaque(false);

        // Card A: Aksi Cepat Pustakawan
        JPanel actionCard = new JPanel(new BorderLayout(0, 12));
        actionCard.setBackground(Color.WHITE);
        actionCard.setBorder(BorderFactory.createCompoundBorder(
                UIConstants.createCardBorder(),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel actionTitleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        actionTitleBox.setOpaque(false);
        JLabel lblActionTitle = new JLabel("⚡ Aksi Cepat Pustakawan");
        lblActionTitle.setFont(UIConstants.FONT_HEADER);
        lblActionTitle.setForeground(new Color(15, 23, 42));

        JLabel lblActionDesc = new JLabel("Pintas eksekusi transaksi & data dalam 1-klik");
        lblActionDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblActionDesc.setForeground(new Color(100, 116, 139));

        actionTitleBox.add(lblActionTitle);
        actionTitleBox.add(lblActionDesc);
        actionCard.add(actionTitleBox, BorderLayout.NORTH);

        JPanel actionButtonsGrid = new JPanel(new GridLayout(4, 1, 0, 8));
        actionButtonsGrid.setOpaque(false);

        JButton btnActPinjam = UIConstants.createStyledButton("🔄  Transaksi Peminjaman Buku", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnActKembali = UIConstants.createStyledButton("↩  Pengembalian Buku & Denda", UIConstants.ACCENT_GREEN, Color.WHITE);
        JButton btnActBuku = UIConstants.createStyledButton("📖  Input Koleksi Buku Baru", UIConstants.ACCENT_ORANGE, Color.WHITE);
        JButton btnActAnggota = UIConstants.createStyledButton("👤  Daftarkan Anggota Baru", UIConstants.PRIMARY_DARK, Color.WHITE);

        btnActPinjam.setPreferredSize(new Dimension(200, 36));
        btnActKembali.setPreferredSize(new Dimension(200, 36));
        btnActBuku.setPreferredSize(new Dimension(200, 36));
        btnActAnggota.setPreferredSize(new Dimension(200, 36));

        btnActPinjam.addActionListener(e -> { if (onNavigateRequested != null) onNavigateRequested.accept("peminjaman"); });
        btnActKembali.addActionListener(e -> { if (onNavigateRequested != null) onNavigateRequested.accept("pengembalian"); });
        btnActBuku.addActionListener(e -> { if (onNavigateRequested != null) onNavigateRequested.accept("buku"); });
        btnActAnggota.addActionListener(e -> { if (onNavigateRequested != null) onNavigateRequested.accept("anggota"); });

        actionButtonsGrid.add(btnActPinjam);
        actionButtonsGrid.add(btnActKembali);
        actionButtonsGrid.add(btnActBuku);
        actionButtonsGrid.add(btnActAnggota);
        actionCard.add(actionButtonsGrid, BorderLayout.CENTER);

        rightContainer.add(actionCard);
        rightContainer.add(Box.createVerticalStrut(14));

        // Card B: Ketentuan & Parameter Perpustakaan
        JPanel policyCard = new JPanel(new BorderLayout(0, 10));
        policyCard.setBackground(Color.WHITE);
        policyCard.setBorder(BorderFactory.createCompoundBorder(
                UIConstants.createCardBorder(),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblPolicyTitle = new JLabel("ℹ️ Parameter Layanan Perpustakaan");
        lblPolicyTitle.setFont(UIConstants.FONT_HEADER);
        lblPolicyTitle.setForeground(new Color(15, 23, 42));
        policyCard.add(lblPolicyTitle, BorderLayout.NORTH);

        JPanel policyRows = new JPanel(new GridLayout(5, 1, 0, 6));
        policyRows.setOpaque(false);

        policyRows.add(createPolicyRow("Durasi Peminjaman", "7 Hari Kalender"));
        policyRows.add(createPolicyRow("Batas Maksimal Pinjam", "3 Buku / Anggota"));
        policyRows.add(createPolicyRow("Tarif Denda Keterlambatan", "Rp 2.000 / Hari"));
        policyRows.add(createPolicyRow("Lokasi Instansi", "Kemenhan RI (Jakarta Pusat)"));
        policyRows.add(createPolicyRow("Status Database", "MySQL 8.0 (Terhubung)"));

        policyCard.add(policyRows, BorderLayout.CENTER);
        rightContainer.add(policyCard);

        mainGrid.add(rightContainer, gbc);
        add(mainGrid, BorderLayout.CENTER);
    }

    private JPanel createPolicyRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKey.setForeground(new Color(100, 116, 139));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVal.setForeground(new Color(30, 41, 59));

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.EAST);
        return row;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String badgeText, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(UIConstants.createCardBorder());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblBadge = new JLabel(" " + badgeText + " ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25));
        lblBadge.setForeground(accentColor);
        lblBadge.setBorder(BorderFactory.createLineBorder(accentColor, 1));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblBadge, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(header, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void updateModeUI() {
        // Mode selalu Staf/Petugas internal perpustakaan
    }

    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Map<String, Object> stats;
            private List<Peminjaman> recentList;

            @Override
            protected Void doInBackground() throws Exception {
                stats = layananLaporan.getStatistikDashboard();
                recentList = peminjamanDAO.getAll();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (stats != null) {
                        lblTotalBuku.setText(String.valueOf(stats.getOrDefault("totalBuku", 0)));
                        lblBukuDipinjam.setText(String.valueOf(stats.getOrDefault("bukuDipinjam", 0)));
                        lblTotalAnggota.setText(String.valueOf(stats.getOrDefault("totalAnggota", 0)));

                        BigDecimal denda = (BigDecimal) stats.getOrDefault("totalDenda", BigDecimal.ZERO);
                        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        lblTotalDenda.setText(nf.format(denda));
                    }

                    tableModel.setRowCount(0);
                    if (recentList != null && !recentList.isEmpty()) {
                        tableSwitchLayout.show(tableSwitchContainer, "table");
                        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        int limit = Math.min(recentList.size(), 15);
                        for (int i = 0; i < limit; i++) {
                            Peminjaman p = recentList.get(i);
                            tableModel.addRow(new Object[]{
                                    p.getKodeTransaksi(),
                                    p.getNimAnggota(),
                                    p.getNamaAnggota(),
                                    p.getNamaPetugas(),
                                    p.getTanggalPinjam(),
                                    p.getTanggalJatuhTempo(),
                                    p.getStatus().toUpperCase(),
                                    nf.format(p.getTotalDenda() != null ? p.getTotalDenda() : BigDecimal.ZERO)
                            });
                        }
                    } else {
                        // Jika belum ada data transaksi, tampilkan Empty State yang elegan
                        tableSwitchLayout.show(tableSwitchContainer, "empty");
                    }
                } catch (Exception ex) {
                    System.err.println("Gagal memuat dashboard: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
