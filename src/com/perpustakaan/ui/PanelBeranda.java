package com.perpustakaan.ui;

import com.perpustakaan.dao.PeminjamanDAO;
import com.perpustakaan.model.Peminjaman;
import com.perpustakaan.service.LayananAutentikasi;
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

/**
 * NAMA FILE : PanelBeranda.java
 * FUNGSI    : Tampilan beranda/dashboard sistem perpustakaan (statistik koleksi, ringkasan sirkulasi, dan sambutan pengunjung).
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

    // Welcome Banner Components
    private JPanel welcomeCard;
    private JLabel lblWelcomeTitle;
    private JLabel lblWelcomeDesc;
    private JButton btnLoginHero;
    private Runnable onLoginRequested;

    public PanelBeranda() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        refreshData();
    }

    public void setOnLoginRequested(Runnable listener) {
        this.onLoginRequested = listener;
    }

    private void initUI() {
        // Top Section: Welcome Banner + Refresh Button
        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        // Welcome Hero Banner
        welcomeCard = new JPanel(new BorderLayout(15, 10));
        welcomeCard.setBackground(Color.WHITE);
        welcomeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 225, 248), 2),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        JPanel welcomeTextPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        welcomeTextPanel.setOpaque(false);

        lblWelcomeTitle = new JLabel("Selamat Datang di SIPerpus Desktop");
        lblWelcomeTitle.setFont(UIConstants.FONT_TITLE);
        lblWelcomeTitle.setForeground(UIConstants.PRIMARY_DARK);

        lblWelcomeDesc = new JLabel("Layanan Mandiri Perpustakaan. Silakan cari buku pada menu katalog. Staf perpustakaan silakan login.");
        lblWelcomeDesc.setFont(UIConstants.FONT_REGULAR);
        lblWelcomeDesc.setForeground(new Color(100, 116, 139));

        welcomeTextPanel.add(lblWelcomeTitle);
        welcomeTextPanel.add(lblWelcomeDesc);

        btnLoginHero = UIConstants.createStyledButton("Login Petugas / Admin", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnLoginHero.setPreferredSize(new Dimension(190, 42));
        btnLoginHero.addActionListener(e -> {
            if (onLoginRequested != null) onLoginRequested.run();
        });

        welcomeCard.add(welcomeTextPanel, BorderLayout.CENTER);
        welcomeCard.add(btnLoginHero, BorderLayout.EAST);
        topContainer.add(welcomeCard, BorderLayout.NORTH);

        // Header Title Bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel lblSectionTitle = new JLabel("Ringkasan Statistik Perpustakaan");
        lblSectionTitle.setFont(UIConstants.FONT_HEADER);
        lblSectionTitle.setForeground(new Color(30, 41, 59));

        JButton btnRefresh = UIConstants.createStyledButton("Segarkan Data", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnRefresh.addActionListener(e -> refreshData());

        titleBar.add(lblSectionTitle, BorderLayout.WEST);
        titleBar.add(btnRefresh, BorderLayout.EAST);
        topContainer.add(titleBar, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // Center Section: Cards + Recent Transactions
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        // Cards Panel (4 Metric Cards)
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

        centerPanel.add(cardsGrid, BorderLayout.NORTH);

        // Recent Transactions Section
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(UIConstants.createCardBorder());

        JLabel lblTableTitle = new JLabel("Aktivitas Sirkulasi Terkini");
        lblTableTitle.setFont(UIConstants.FONT_HEADER);
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] columns = {"Kode Transaksi", "NIM", "Peminjam", "Petugas", "Tgl Pinjam", "Jatuh Tempo", "Status", "Denda"};
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
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
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
        boolean loggedIn = LayananAutentikasi.getInstance().isLoggedIn();
        if (loggedIn) {
            String role = LayananAutentikasi.getInstance().getCurrentUser().getRole().toUpperCase();
            String name = LayananAutentikasi.getInstance().getCurrentUser().getNamaLengkap();
            lblWelcomeTitle.setText("Selamat Datang, " + name + " (" + role + ")");
            lblWelcomeDesc.setText("Anda login sebagai pengelola perpustakaan. Seluruh modul Master, Transaksi, dan Laporan aktif.");
            btnLoginHero.setVisible(false);
        } else {
            lblWelcomeTitle.setText("Selamat Datang di SIPerpus Desktop");
            lblWelcomeDesc.setText("Layanan Mandiri Perpustakaan. Silakan cari buku pada menu katalog. Staf perpustakaan silakan login.");
            btnLoginHero.setVisible(true);
        }
    }

    public void refreshData() {
        updateModeUI();
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
                    if (recentList != null) {
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
                    }
                } catch (Exception ex) {
                    System.err.println("Gagal memuat dashboard: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
