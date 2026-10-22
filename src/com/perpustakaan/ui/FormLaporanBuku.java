package com.perpustakaan.ui;

import com.perpustakaan.dao.BukuDAO;
import com.perpustakaan.model.Buku;
import com.perpustakaan.service.LayananLaporan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * NAMA FILE : FormLaporanBuku.java
 * FUNGSI    : Tampilan laporan katalog inventaris koleksi buku, cetak dokumen resmi A4, dan ekspor ke file CSV/Excel.
 */
public class FormLaporanBuku extends JPanel {
    private final BukuDAO bukuDAO = new BukuDAO();
    private final LayananLaporan layananLaporan = new LayananLaporan();

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private List<Buku> currentList;

    public FormLaporanBuku() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Laporan: Katalog Koleksi Buku");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton btnRefresh = UIConstants.createStyledButton("Segarkan", UIConstants.PRIMARY_DARK, Color.WHITE);
        JButton btnCetak = UIConstants.createStyledButton("Cetak Dokumen Formal", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnExport = UIConstants.createStyledButton("Ekspor ke CSV", UIConstants.ACCENT_GREEN, Color.WHITE);

        btnBar.add(btnRefresh);
        btnBar.add(btnCetak);
        btnBar.add(btnExport);

        topPanel.add(btnBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(UIConstants.createCardBorder());

        String[] cols = {"No", "Kode Buku", "Judul Buku", "Pengarang", "Penerbit", "Tahun", "Kategori", "Rak", "Stok Total", "Tersedia"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(5).setCellRenderer(cr);
        table.getColumnModel().getColumn(7).setCellRenderer(cr);
        table.getColumnModel().getColumn(8).setCellRenderer(cr);
        table.getColumnModel().getColumn(9).setCellRenderer(cr);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Info
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        lblTotal = new JLabel("Total Koleksi: 0 Judul");
        lblTotal.setFont(UIConstants.FONT_BOLD);
        bottomBar.add(lblTotal);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Actions
        btnRefresh.addActionListener(e -> loadData());

        btnCetak.addActionListener(e -> {
            if (currentList == null || currentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data buku untuk dicetak!");
                return;
            }
            String[] headers = {"No", "Kode Buku", "Judul Buku", "Pengarang", "Kategori", "Rak", "Stok"};
            int[] weights = {1, 3, 6, 4, 3, 1, 2};
            List<String[]> rows = new ArrayList<>();
            int no = 1;
            int totalStok = 0;
            for (Buku b : currentList) {
                totalStok += b.getStokTotal();
                rows.add(new String[]{
                        String.valueOf(no++),
                        b.getKodeBuku(),
                        b.getJudul(),
                        b.getPengarang(),
                        b.getNamaKategori() != null ? b.getNamaKategori() : "-",
                        b.getKodeRak() != null ? b.getKodeRak() : "-",
                        b.getStokTersedia() + " / " + b.getStokTotal()
                });
            }
            String summary = "Total Koleksi: " + currentList.size() + " Judul Buku  |  Total Keseluruhan Stok Fisik: " + totalStok + " Eksemplar";
            DialogCetakLaporan dialog = new DialogCetakLaporan(
                    SwingUtilities.getWindowAncestor(this),
                    "LAPORAN DATA KOLEKSI BUKU PERPUSTAKAAN",
                    "Katalog Lengkap Koleksi Buku Aktif",
                    headers, weights, rows, summary, true
            );
            dialog.setVisible(true);
        });

        btnExport.addActionListener(e -> {
            if (currentList == null || currentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data buku untuk diekspor!");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("laporan_buku_" + LocalDate.now() + ".csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    layananLaporan.exportLaporanBuku(chooser.getSelectedFile(), currentList);
                    JOptionPane.showMessageDialog(this, "Laporan data buku berhasil diekspor!", "Ekspor Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal mengekspor: " + ex.getMessage());
                }
            }
        });
    }

    public void loadData() {
        try {
            currentList = bukuDAO.getAll();
            tableModel.setRowCount(0);
            int no = 1;
            for (Buku b : currentList) {
                tableModel.addRow(new Object[]{
                        no++,
                        b.getIsbn(),
                        b.getJudul(),
                        b.getPengarang(),
                        b.getPenerbit(),
                        b.getTahunTerbit(),
                        b.getNamaKategori(),
                        b.getKodeRak() != null ? b.getKodeRak() : "-",
                        b.getStokTotal(),
                        b.getStokTersedia()
                });
            }
            lblTotal.setText("Total Koleksi: " + currentList.size() + " Judul Buku");
        } catch (Exception ex) {
            System.err.println("Gagal memuat laporan buku: " + ex.getMessage());
        }
    }
}
