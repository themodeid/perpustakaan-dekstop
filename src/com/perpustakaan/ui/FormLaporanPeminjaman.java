package com.perpustakaan.ui;

import com.perpustakaan.dao.PeminjamanDAO;
import com.perpustakaan.model.DetailPeminjaman;
import com.perpustakaan.model.Peminjaman;
import com.perpustakaan.service.LayananLaporan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * NAMA FILE : FormLaporanPeminjaman.java
 * FUNGSI    : Tampilan laporan transaksi peminjaman buku beserta filter rentang tanggal, cetak lembar A4, dan ekspor CSV.
 */
public class FormLaporanPeminjaman extends JPanel {
    private final PeminjamanDAO peminjamanDAO = new PeminjamanDAO();
    private final LayananLaporan layananLaporan = new LayananLaporan();

    private JTextField txtTglMulai;
    private JTextField txtTglSelesai;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private List<Peminjaman> currentList;

    public FormLaporanPeminjaman() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Laporan: Transaksi Peminjaman Buku");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton btnCetak = UIConstants.createStyledButton("Cetak Dokumen Formal", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnExport = UIConstants.createStyledButton("Ekspor ke CSV", UIConstants.ACCENT_GREEN, Color.WHITE);
        btnBar.add(btnCetak);
        btnBar.add(btnExport);

        topPanel.add(btnBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(UIConstants.createCardBorder());

        filterPanel.add(new JLabel("Tgl Mulai (YYYY-MM-DD):"));
        txtTglMulai = new JTextField(10);
        filterPanel.add(txtTglMulai);

        filterPanel.add(new JLabel("Tgl Selesai (YYYY-MM-DD):"));
        txtTglSelesai = new JTextField(10);
        txtTglSelesai.setText(LocalDate.now().toString());
        filterPanel.add(txtTglSelesai);

        JButton btnFilter = UIConstants.createStyledButton("Terapkan Filter", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnReset = UIConstants.createStyledButton("Reset", Color.LIGHT_GRAY, Color.BLACK);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        // Table Panel
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(UIConstants.createCardBorder());

        String[] cols = {"No", "Kode Transaksi", "NIM", "Nama Peminjam", "Petugas", "Tgl Pinjam", "Jatuh Tempo", "Status", "Buku Dipinjam"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_REGULAR);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(0).setCellRenderer(cr);
        table.getColumnModel().getColumn(1).setCellRenderer(cr);
        table.getColumnModel().getColumn(2).setCellRenderer(cr);
        table.getColumnModel().getColumn(5).setCellRenderer(cr);
        table.getColumnModel().getColumn(6).setCellRenderer(cr);
        table.getColumnModel().getColumn(7).setCellRenderer(cr);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Summary Bar
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setOpaque(false);
        lblTotal = new JLabel("Total Transaksi: 0");
        lblTotal.setFont(UIConstants.FONT_BOLD);
        summaryPanel.add(lblTotal);
        tableCard.add(summaryPanel, BorderLayout.SOUTH);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 10));
        centerWrapper.setOpaque(false);
        centerWrapper.add(filterPanel, BorderLayout.NORTH);
        centerWrapper.add(tableCard, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // Listeners
        btnFilter.addActionListener(e -> loadData());
        btnReset.addActionListener(e -> {
            txtTglMulai.setText("");
            txtTglSelesai.setText(LocalDate.now().toString());
            loadData();
        });

        btnCetak.addActionListener(e -> {
            if (currentList == null || currentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data peminjaman untuk dicetak!");
                return;
            }
            String[] headers = {"No", "Kode Transaksi", "NIM", "Nama Peminjam", "Tgl Pinjam", "Tempo", "Status", "Buku"};
            int[] weights = {1, 3, 2, 4, 2, 2, 2, 5};
            List<String[]> rows = new ArrayList<>();
            int no = 1;
            for (Peminjaman p : currentList) {
                StringBuilder bukuStr = new StringBuilder();
                if (p.getDetailList() != null) {
                    for (int i = 0; i < p.getDetailList().size(); i++) {
                        bukuStr.append(p.getDetailList().get(i).getJudulBuku());
                        if (i < p.getDetailList().size() - 1) bukuStr.append(", ");
                    }
                }
                rows.add(new String[]{
                        String.valueOf(no++),
                        p.getKodeTransaksi(),
                        p.getNimAnggota(),
                        p.getNamaAnggota(),
                        p.getTanggalPinjam().toString(),
                        p.getTanggalJatuhTempo().toString(),
                        p.getStatus().toUpperCase(),
                        bukuStr.toString()
                });
            }
            String periode = (!txtTglMulai.getText().trim().isEmpty() ? txtTglMulai.getText().trim() : "Awal") + 
                             " s/d " + (!txtTglSelesai.getText().trim().isEmpty() ? txtTglSelesai.getText().trim() : "Sekarang");
            String summary = "Total Aktivitas Transaksi: " + currentList.size() + " Transaksi";
            DialogCetakLaporan dialog = new DialogCetakLaporan(
                    SwingUtilities.getWindowAncestor(this),
                    "LAPORAN DATA TRANSAKSI PEMINJAMAN BUKU",
                    "Periode: " + periode,
                    headers, weights, rows, summary, true
            );
            dialog.setVisible(true);
        });

        btnExport.addActionListener(e -> {
            if (currentList == null || currentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data peminjaman untuk diekspor!");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("laporan_peminjaman_" + LocalDate.now() + ".csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    layananLaporan.exportLaporanPeminjaman(chooser.getSelectedFile(), currentList);
                    JOptionPane.showMessageDialog(this, "Laporan peminjaman berhasil diekspor!", "Ekspor Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal mengekspor: " + ex.getMessage());
                }
            }
        });
    }

    public void loadData() {
        String mulaiStr = txtTglMulai.getText().trim();
        String selesaiStr = txtTglSelesai.getText().trim();

        Date tglMulai = null;
        Date tglSelesai = null;

        try {
            if (!mulaiStr.isEmpty()) tglMulai = Date.valueOf(mulaiStr);
            if (!selesaiStr.isEmpty()) tglSelesai = Date.valueOf(selesaiStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal harus YYYY-MM-DD!", "Format Salah", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            currentList = peminjamanDAO.filterLaporan("Semua", tglMulai, tglSelesai);
            tableModel.setRowCount(0);
            int no = 1;

            for (Peminjaman p : currentList) {
                StringBuilder bukuStr = new StringBuilder();
                if (p.getDetailList() != null) {
                    for (int i = 0; i < p.getDetailList().size(); i++) {
                        bukuStr.append(p.getDetailList().get(i).getJudulBuku());
                        if (i < p.getDetailList().size() - 1) bukuStr.append(", ");
                    }
                }

                tableModel.addRow(new Object[]{
                        no++,
                        p.getKodeTransaksi(),
                        p.getNimAnggota(),
                        p.getNamaAnggota(),
                        p.getNamaPetugas(),
                        p.getTanggalPinjam(),
                        p.getTanggalJatuhTempo(),
                        p.getStatus().toUpperCase(),
                        bukuStr.toString()
                });
            }
            lblTotal.setText("Total Transaksi Peminjaman: " + currentList.size());
        } catch (Exception ex) {
            System.err.println("Gagal memuat laporan peminjaman: " + ex.getMessage());
        }
    }
}
