package com.perpustakaan.ui;

import com.perpustakaan.dao.PeminjamanDAO;
import com.perpustakaan.model.DetailPeminjaman;
import com.perpustakaan.model.Peminjaman;
import com.perpustakaan.service.LayananAutentikasi;
import com.perpustakaan.service.LayananSirkulasi;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * NAMA FILE : FormTransaksiPengembalian.java
 * FUNGSI    : Tampilan pemrosesan pengembalian buku pinjaman dan kalkulasi denda keterlambatan secara otomatis.
 */
public class FormTransaksiPengembalian extends JPanel {
    private final PeminjamanDAO peminjamanDAO = new PeminjamanDAO();
    private final LayananSirkulasi layananSirkulasi = new LayananSirkulasi();

    private JTable tableAktif;
    private DefaultTableModel tableModel;
    private JTextField txtSearchKode;

    // Info Section
    private JLabel lblKode;
    private JLabel lblPeminjam;
    private JLabel lblTglTempo;
    private JLabel lblTglKembali;
    private JLabel lblStatusTelat;
    private JLabel lblTotalDenda;
    private JTextArea txtCatatan;
    private JButton btnProsesKembali;

    private Peminjaman selectedPeminjaman;
    private BigDecimal currentDenda = BigDecimal.ZERO;
    private List<Peminjaman> daftarAktif = new ArrayList<>();

    public FormTransaksiPengembalian() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadDataAktif();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Sirkulasi: Pengembalian Buku & Hitung Denda");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = UIConstants.createStyledButton("Segarkan Data", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnRefresh.addActionListener(e -> loadDataAktif());
        topPanel.add(btnRefresh, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Content Split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);

        // Left Panel: Table Transaksi Dipinjam Aktif
        JPanel leftCard = new JPanel(new BorderLayout(0, 10));
        leftCard.setBackground(UIConstants.CARD_BG);
        leftCard.setBorder(UIConstants.createCardBorder());

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Cari Kode / NIP / Nama:"));
        txtSearchKode = new JTextField(18);
        txtSearchKode.setPreferredSize(new Dimension(180, 30));
        JButton btnCari = UIConstants.createStyledButton("Cari", UIConstants.PRIMARY_DARK, Color.WHITE);
        searchBar.add(txtSearchKode);
        searchBar.add(btnCari);
        leftCard.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Kode Transaksi", "NIP / NRP", "Nama Personel", "Tgl Pinjam", "Jatuh Tempo", "Buku"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAktif = new JTable(tableModel);
        tableAktif.setRowHeight(30);
        tableAktif.setFont(UIConstants.FONT_REGULAR);
        tableAktif.getTableHeader().setFont(UIConstants.FONT_BOLD);
        tableAktif.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);
        tableAktif.getTableHeader().setForeground(UIConstants.TEXT_PRIMARY);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        tableAktif.getColumnModel().getColumn(0).setPreferredWidth(30);
        tableAktif.getColumnModel().getColumn(0).setCellRenderer(cr);
        tableAktif.getColumnModel().getColumn(4).setCellRenderer(cr);
        tableAktif.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String text = val != null ? val.toString() : "";
                if (text.contains("TELAT")) {
                    lbl.setForeground(sel ? Color.WHITE : UIConstants.TEXT_RED);
                    lbl.setFont(UIConstants.FONT_BOLD);
                } else {
                    lbl.setForeground(sel ? Color.WHITE : UIConstants.TEXT_PRIMARY);
                    lbl.setFont(UIConstants.FONT_REGULAR);
                }
                return lbl;
            }
        });
        tableAktif.getColumnModel().getColumn(6).setCellRenderer(cr);

        leftCard.add(new JScrollPane(tableAktif), BorderLayout.CENTER);
        splitPane.setLeftComponent(leftCard);

        // Right Panel: Detail Pengembalian & Kalkulasi Denda
        JPanel rightCard = new JPanel(new BorderLayout(0, 15));
        rightCard.setBackground(UIConstants.CARD_BG);
        rightCard.setBorder(UIConstants.createCardBorder());

        JLabel lblDetailTitle = new JLabel("Verifikasi Pengembalian & Denda");
        lblDetailTitle.setFont(UIConstants.FONT_HEADER);
        rightCard.add(lblDetailTitle, BorderLayout.NORTH);

        JPanel detailGrid = new JPanel(new GridBagLayout());
        detailGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        lblKode = new JLabel("-");
        lblPeminjam = new JLabel("-");
        lblTglTempo = new JLabel("-");
        lblTglKembali = new JLabel(LocalDate.now().toString());
        lblStatusTelat = new JLabel("-");
        lblTotalDenda = new JLabel("Rp 0");
        lblTotalDenda.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalDenda.setForeground(UIConstants.TEXT_RED);
        txtCatatan = new JTextArea(3, 15);
        txtCatatan.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));

        int r = 0;
        addDetailRow(detailGrid, gbc, r++, "Kode Transaksi:", lblKode);
        addDetailRow(detailGrid, gbc, r++, "Personel Peminjam:", lblPeminjam);
        addDetailRow(detailGrid, gbc, r++, "Jatuh Tempo:", lblTglTempo);
        addDetailRow(detailGrid, gbc, r++, "Tanggal Pengembalian:", lblTglKembali);
        addDetailRow(detailGrid, gbc, r++, "Status Keterlambatan:", lblStatusTelat);
        addDetailRow(detailGrid, gbc, r++, "Total Denda:", lblTotalDenda);
        addDetailRow(detailGrid, gbc, r++, "Catatan Petugas:", new JScrollPane(txtCatatan));

        rightCard.add(detailGrid, BorderLayout.CENTER);

        JPanel actionBtnPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        actionBtnPanel.setOpaque(false);

        btnProsesKembali = UIConstants.createStyledButton("Konfirmasi Pengembalian Buku", UIConstants.ACCENT_GREEN, Color.WHITE);
        btnProsesKembali.setFont(UIConstants.FONT_HEADER);
        btnProsesKembali.setPreferredSize(new Dimension(200, 42));
        btnProsesKembali.setEnabled(false);

        JButton btnCetakBuktiManual = UIConstants.createStyledButton("🧾 Cetak Struk Bukti Pengembalian", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnCetakBuktiManual.setFont(UIConstants.FONT_BOLD);
        btnCetakBuktiManual.setPreferredSize(new Dimension(200, 36));

        actionBtnPanel.add(btnProsesKembali);
        actionBtnPanel.add(btnCetakBuktiManual);
        rightCard.add(actionBtnPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightCard);
        add(splitPane, BorderLayout.CENTER);

        // Listeners
        tableAktif.getSelectionModel().addListSelectionListener(e -> {
            int row = tableAktif.getSelectedRow();
            if (row != -1) {
                String kode = (String) tableModel.getValueAt(row, 1);
                loadPeminjamanDetail(kode);
            }
        });

        btnCari.addActionListener(e -> {
            String query = txtSearchKode.getText().trim();
            filterTabelAktif(query);
            if (tableModel.getRowCount() == 1) {
                tableAktif.setRowSelectionInterval(0, 0);
            } else if (tableModel.getRowCount() == 0 && !query.isEmpty()) {
                loadPeminjamanDetail(query);
            }
        });

        txtSearchKode.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTabelAktif(txtSearchKode.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { filterTabelAktif(txtSearchKode.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { filterTabelAktif(txtSearchKode.getText()); }
        });

        btnCetakBuktiManual.addActionListener(e -> {
            if (selectedPeminjaman == null) {
                JOptionPane.showMessageDialog(this, "Pilih transaksi terlebih dahulu untuk mencetak bukti pengembalian!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date today = Date.valueOf(LocalDate.now());
            cetakBuktiPengembalian(selectedPeminjaman, today, currentDenda);
        });

        btnProsesKembali.addActionListener(e -> {
            if (selectedPeminjaman == null) return;

            int conf = JOptionPane.showConfirmDialog(this, 
                    "Konfirmasi pengembalian untuk transaksi " + selectedPeminjaman.getKodeTransaksi() + "?\n" +
                    "Peminjam   : " + selectedPeminjaman.getNamaAnggota() + " (" + selectedPeminjaman.getNimAnggota() + ")\n" +
                    "Total Denda: " + lblTotalDenda.getText(), 
                    "Konfirmasi Pengembalian", JOptionPane.YES_NO_OPTION);

            if (conf == JOptionPane.YES_OPTION) {
                try {
                    Date today = Date.valueOf(LocalDate.now());
                    String catatan = txtCatatan.getText().trim();
                    Peminjaman peminjamanSalinan = selectedPeminjaman;
                    BigDecimal dendaSalinan = currentDenda;

                    layananSirkulasi.prosesPengembalian(selectedPeminjaman.getId(), today, currentDenda, catatan);

                    int tanyaCetak = JOptionPane.showConfirmDialog(this,
                            "Buku berhasil dikembalikan dan stok telah dipulihkan ke rak!\n\n" +
                            "Kode Transaksi : " + peminjamanSalinan.getKodeTransaksi() + "\n" +
                            "Peminjam       : " + peminjamanSalinan.getNamaAnggota() + " (" + peminjamanSalinan.getNimAnggota() + ")\n" +
                            "Total Denda    : " + NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(dendaSalinan) + "\n\n" +
                            "Apakah Anda ingin langsung mencetak Struk Bukti Pengembalian sekarang?\n" +
                            "(Dapat dicetak fisik atau disimpan ke PDF untuk diberikan ke personel)",
                            "Pengembalian Sukses - Cetak Bukti?", JOptionPane.YES_NO_OPTION);

                    if (tanyaCetak == JOptionPane.YES_OPTION) {
                        cetakBuktiPengembalian(peminjamanSalinan, today, dendaSalinan);
                    }

                    resetDetail();
                    loadDataAktif();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal memproses pengembalian: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void addDetailRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.FONT_BOLD);
        p.add(l, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        p.add(comp, gbc);
    }

    public void loadDataAktif() {
        try {
            daftarAktif = peminjamanDAO.getAktifPinjam();
            filterTabelAktif(txtSearchKode != null ? txtSearchKode.getText() : "");
        } catch (Exception ex) {
            System.err.println("Gagal memuat transaksi aktif: " + ex.getMessage());
        }
    }

    private void filterTabelAktif(String keyword) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase();
        LocalDate today = LocalDate.now();
        tableModel.setRowCount(0);
        for (Peminjaman p : daftarAktif) {
            String kode = p.getKodeTransaksi() != null ? p.getKodeTransaksi() : "";
            String nip = p.getNimAnggota() != null ? p.getNimAnggota() : "";
            String nama = p.getNamaAnggota() != null ? p.getNamaAnggota() : "";
            if (!q.isEmpty() && !kode.toLowerCase().contains(q) && !nip.toLowerCase().contains(q) && !nama.toLowerCase().contains(q)) {
                continue;
            }
            int jumlahBuku = p.getDetailList() != null ? p.getDetailList().size() : 0;
            String tempoStr = p.getTanggalJatuhTempo() != null ? p.getTanggalJatuhTempo().toString() : "-";
            if (p.getTanggalJatuhTempo() != null && today.isAfter(p.getTanggalJatuhTempo().toLocalDate())) {
                tempoStr += " (TELAT)";
            }
            tableModel.addRow(new Object[]{
                    p.getId(),
                    kode,
                    nip,
                    nama,
                    p.getTanggalPinjam(),
                    tempoStr,
                    jumlahBuku + " buku"
            });
        }
    }

    private void loadPeminjamanDetail(String kodeTransaksi) {
        try {
            selectedPeminjaman = peminjamanDAO.getByKode(kodeTransaksi);
            if (selectedPeminjaman == null) {
                JOptionPane.showMessageDialog(this, "Transaksi '" + kodeTransaksi + "' tidak ditemukan atau sudah selesai!");
                resetDetail();
                return;
            }

            Date today = Date.valueOf(LocalDate.now());
            int jumlahBuku = selectedPeminjaman.getDetailList() != null ? selectedPeminjaman.getDetailList().size() : 1;
            long telat = layananSirkulasi.hitungHariKeterlambatan(selectedPeminjaman.getTanggalJatuhTempo(), today);
            currentDenda = layananSirkulasi.kalkulasiDenda(selectedPeminjaman.getTanggalJatuhTempo(), today, jumlahBuku);

            lblKode.setText(selectedPeminjaman.getKodeTransaksi());
            lblPeminjam.setText(selectedPeminjaman.getNamaAnggota() + " (" + selectedPeminjaman.getNimAnggota() + ")");
            lblTglTempo.setText(selectedPeminjaman.getTanggalJatuhTempo().toString());
            lblTglKembali.setText(today.toString());

            if (telat > 0) {
                lblStatusTelat.setText("TERLAMBAT " + telat + " HARI (" + jumlahBuku + " buku)");
                lblStatusTelat.setForeground(UIConstants.TEXT_RED);
                lblStatusTelat.setFont(UIConstants.FONT_BOLD);
            } else {
                lblStatusTelat.setText("Tepat Waktu (Tidak Ada Denda)");
                lblStatusTelat.setForeground(UIConstants.TEXT_GREEN);
                lblStatusTelat.setFont(UIConstants.FONT_BOLD);
            }

            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            lblTotalDenda.setText(nf.format(currentDenda));
            txtCatatan.setText(selectedPeminjaman.getCatatan() != null ? selectedPeminjaman.getCatatan() : "");
            btnProsesKembali.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void resetDetail() {
        selectedPeminjaman = null;
        currentDenda = BigDecimal.ZERO;
        lblKode.setText("-");
        lblPeminjam.setText("-");
        lblTglTempo.setText("-");
        lblStatusTelat.setText("-");
        lblTotalDenda.setText("Rp 0");
        txtCatatan.setText("");
        btnProsesKembali.setEnabled(false);
        tableAktif.clearSelection();
    }

    private void cetakBuktiPengembalian(Peminjaman p, Date tglKembali, BigDecimal denda) {
        if (p == null) return;
        String namaPetugas = LayananAutentikasi.getInstance().getCurrentUser() != null ?
                LayananAutentikasi.getInstance().getCurrentUser().getNamaLengkap() : "Petugas Layanan Kemhan";

        String[] headers = {"No", "Judul Koleksi Buku", "Pengarang", "Status Pengembalian"};
        int[] weights = {1, 8, 4, 3};
        List<String[]> rows = new ArrayList<>();
        int no = 1;
        if (p.getDetailList() != null && !p.getDetailList().isEmpty()) {
            for (DetailPeminjaman dp : p.getDetailList()) {
                rows.add(new String[]{String.valueOf(no++), dp.getJudulBuku(), dp.getPengarangBuku() != null ? dp.getPengarangBuku() : "-", "Diterima Baik (Rak)"});
            }
        } else {
            rows.add(new String[]{"1", "Koleksi Buku Perpustakaan Kemhan", "Kemhan RI", "Diterima Baik (Rak)"});
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String dendaStr = nf.format(denda);
        String subJudul = "No. Transaksi: " + p.getKodeTransaksi() + "  |  Personel: " + p.getNamaAnggota() + " (NIP/NRP: " + p.getNimAnggota() + ")";
        String summary = "Tgl Pinjam: " + p.getTanggalPinjam() + "  |  Batas Tempo: " + p.getTanggalJatuhTempo() + "  |  Tgl Pengembalian Riil: " + tglKembali +
                         "\nStatus Pelunasan: " + (denda.compareTo(BigDecimal.ZERO) > 0 ? "TERLAMBAT - DENDA LUNAS (" + dendaStr + ")" : "TEPAT WAKTU (BEBAS DENDA)") +
                         "\nPetugas Penerima: " + namaPetugas +
                         "\n* Tanda terima sah pengembalian buku kedinasan dan penyelesaian sirkulasi Perpustakaan Kemhan RI.";

        DialogCetakLaporan dialog = new DialogCetakLaporan(
                SwingUtilities.getWindowAncestor(this),
                "BUKTI RESMI PENGEMBALIAN BUKU & PELUNASAN KEMHAN RI",
                subJudul,
                headers, weights, rows, summary, true
        );
        dialog.setVisible(true);
    }
}
