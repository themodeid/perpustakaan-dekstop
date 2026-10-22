package com.perpustakaan.ui;

import com.perpustakaan.dao.AnggotaDAO;
import com.perpustakaan.model.Anggota;
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
 * NAMA FILE : FormLaporanAnggota.java
 * FUNGSI    : Tampilan laporan data master anggota perpustakaan terdaftar, cetak lembar A4, dan ekspor CSV.
 */
public class FormLaporanAnggota extends JPanel {
    private final AnggotaDAO anggotaDAO = new AnggotaDAO();
    private final LayananLaporan layananLaporan = new LayananLaporan();

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private List<Anggota> currentList;

    public FormLaporanAnggota() {
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

        JLabel lblTitle = new JLabel("Laporan: Data Anggota Terdaftar");
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

        String[] cols = {"No", "NIM", "Nama Anggota", "Jurusan / Prodi", "No. Telepon", "Email", "Status"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setCellRenderer(cr);
        table.getColumnModel().getColumn(6).setCellRenderer(cr);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Info
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        lblTotal = new JLabel("Total Anggota: 0 Orang");
        lblTotal.setFont(UIConstants.FONT_BOLD);
        bottomBar.add(lblTotal);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Actions
        btnRefresh.addActionListener(e -> loadData());

        btnCetak.addActionListener(e -> {
            if (currentList == null || currentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data anggota untuk dicetak!");
                return;
            }
            String[] headers = {"No", "NIM / ID", "Nama Lengkap", "Jurusan / Program Studi", "No. Telepon", "Status"};
            int[] weights = {1, 3, 5, 4, 3, 2};
            List<String[]> rows = new ArrayList<>();
            int no = 1;
            int aktifCount = 0;
            for (Anggota a : currentList) {
                if (a.isAktif()) aktifCount++;
                rows.add(new String[]{
                        String.valueOf(no++),
                        a.getNim(),
                        a.getNama(),
                        a.getJurusan(),
                        a.getNoTelp() != null ? a.getNoTelp() : "-",
                        a.getStatus().toUpperCase()
                });
            }
            String summary = "Total Anggota Terdaftar: " + currentList.size() + " Orang  |  Status Aktif: " + aktifCount + " Orang";
            DialogCetakLaporan dialog = new DialogCetakLaporan(
                    SwingUtilities.getWindowAncestor(this),
                    "LAPORAN DATA ANGGOTA PERPUSTAKAAN",
                    "Rekapitulasi Anggota Aktif & Sivitas Akademika",
                    headers, weights, rows, summary, true
            );
            dialog.setVisible(true);
        });

        btnExport.addActionListener(e -> {
            if (currentList == null || currentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data anggota untuk diekspor!");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("laporan_anggota_" + LocalDate.now() + ".csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    layananLaporan.exportLaporanAnggota(chooser.getSelectedFile(), currentList);
                    JOptionPane.showMessageDialog(this, "Laporan data anggota berhasil diekspor!", "Ekspor Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal mengekspor: " + ex.getMessage());
                }
            }
        });
    }

    public void loadData() {
        try {
            currentList = anggotaDAO.getAll();
            tableModel.setRowCount(0);
            int no = 1;
            for (Anggota a : currentList) {
                tableModel.addRow(new Object[]{
                        no++,
                        a.getNim(),
                        a.getNama(),
                        a.getJurusan(),
                        a.getNoTelp() != null ? a.getNoTelp() : "-",
                        a.getEmail() != null ? a.getEmail() : "-",
                        a.getStatus().toUpperCase()
                });
            }
            lblTotal.setText("Total Anggota: " + currentList.size() + " Orang");
        } catch (Exception ex) {
            System.err.println("Gagal memuat laporan anggota: " + ex.getMessage());
        }
    }
}
