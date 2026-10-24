package com.perpustakaan.ui;

import com.perpustakaan.dao.PeminjamanDAO;
import com.perpustakaan.model.Peminjaman;
import com.perpustakaan.service.LayananLaporan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * NAMA FILE : FormLaporanPengembalian.java
 * FUNGSI    : Tampilan rekapitulasi pengembalian buku pinjaman beserta rekap denda, cetak lembar A4, dan ekspor CSV.
 */
public class FormLaporanPengembalian extends JPanel {
    private final PeminjamanDAO peminjamanDAO = new PeminjamanDAO();
    private final LayananLaporan layananLaporan = new LayananLaporan();

    private JTextField txtTglMulai;
    private JTextField txtTglSelesai;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotalTrx;
    private JLabel lblTotalDenda;
    private List<Peminjaman> currentList;

    public FormLaporanPengembalian() {
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

        JLabel lblTitle = new JLabel("Laporan: Pengembalian Buku & Rekap Denda");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton btnCetakKuitansi = UIConstants.createStyledButton("🧾 Cetak Kuitansi / Bukti Kembali (Luaran)", new Color(13, 148, 136), Color.WHITE);
        JButton btnCetak = UIConstants.createStyledButton("Cetak Dokumen Formal", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnExport = UIConstants.createStyledButton("Ekspor ke CSV", UIConstants.ACCENT_GREEN, Color.WHITE);
        btnBar.add(btnCetakKuitansi);
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

        String[] cols = {"No", "Kode Transaksi", "NIP / NRP", "Personel", "Petugas", "Tgl Pinjam", "Jatuh Tempo", "Tgl Kembali", "Denda", "Catatan"};
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
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
        summaryPanel.setOpaque(false);

        lblTotalTrx = new JLabel("Total Pengembalian: 0");
        lblTotalTrx.setFont(UIConstants.FONT_BOLD);

        lblTotalDenda = new JLabel("Total Denda: Rp 0");
        lblTotalDenda.setFont(UIConstants.FONT_BOLD);
        lblTotalDenda.setForeground(UIConstants.ACCENT_RED);

        summaryPanel.add(lblTotalTrx);
        summaryPanel.add(lblTotalDenda);
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
                JOptionPane.showMessageDialog(this, "Tidak ada data pengembalian untuk dicetak!");
                return;
            }
            String[] headers = {"No", "Kode Transaksi", "NIP / NRP", "Nama Personel", "Tgl Pinjam", "Tgl Kembali", "Denda (Rp)"};
            int[] weights = {1, 3, 2, 4, 2, 2, 3};
            List<String[]> rows = new ArrayList<>();
            int no = 1;
            BigDecimal totalDendaSum = BigDecimal.ZERO;
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            for (Peminjaman p : currentList) {
                BigDecimal d = p.getTotalDenda() != null ? p.getTotalDenda() : BigDecimal.ZERO;
                totalDendaSum = totalDendaSum.add(d);
                rows.add(new String[]{
                        String.valueOf(no++),
                        p.getKodeTransaksi(),
                        p.getNimAnggota(),
                        p.getNamaAnggota(),
                        p.getTanggalPinjam().toString(),
                        p.getTanggalKembali() != null ? p.getTanggalKembali().toString() : "-",
                        nf.format(d)
                });
            }
            String periode = (!txtTglMulai.getText().trim().isEmpty() ? txtTglMulai.getText().trim() : "Awal") + 
                             " s/d " + (!txtTglSelesai.getText().trim().isEmpty() ? txtTglSelesai.getText().trim() : "Sekarang");
            String summary = "Total Pengembalian: " + currentList.size() + " Transaksi  |  Total Penerimaan Kas Denda: " + nf.format(totalDendaSum);
            DialogCetakLaporan dialog = new DialogCetakLaporan(
                    SwingUtilities.getWindowAncestor(this),
                    "LAPORAN PENGEMBALIAN BUKU PERPUSTAKAAN KEMHAN RI",
                    "Periode: " + periode,
                    headers, weights, rows, summary, true
            );
            dialog.setVisible(true);
        });

        btnCetakKuitansi.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, 
                        "Pilih salah satu baris transaksi pengembalian untuk mencetak Kuitansi / Bukti Pengembalian!", 
                        "Pilih Transaksi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (currentList != null && row < currentList.size()) {
                Peminjaman p = currentList.get(row);
                cetakKuitansiKembali(p);
            }
        });

        btnExport.addActionListener(e -> {
            if (currentList == null || currentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data pengembalian untuk diekspor!");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("laporan_pengembalian_" + LocalDate.now() + ".csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    layananLaporan.exportLaporanPengembalian(chooser.getSelectedFile(), currentList);
                    JOptionPane.showMessageDialog(this, "Laporan pengembalian & denda berhasil diekspor!", "Ekspor Sukses", JOptionPane.INFORMATION_MESSAGE);
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
            // Filter khusus status 'kembali'
            currentList = peminjamanDAO.filterLaporan("kembali", tglMulai, tglSelesai);
            tableModel.setRowCount(0);
            int no = 1;
            BigDecimal grandTotalDenda = BigDecimal.ZERO;
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            for (Peminjaman p : currentList) {
                BigDecimal denda = p.getTotalDenda() != null ? p.getTotalDenda() : BigDecimal.ZERO;
                grandTotalDenda = grandTotalDenda.add(denda);

                tableModel.addRow(new Object[]{
                        no++,
                        p.getKodeTransaksi(),
                        p.getNimAnggota(),
                        p.getNamaAnggota(),
                        p.getNamaPetugas(),
                        p.getTanggalPinjam(),
                        p.getTanggalJatuhTempo(),
                        p.getTanggalKembali() != null ? p.getTanggalKembali() : "-",
                        nf.format(denda),
                        p.getCatatan() != null ? p.getCatatan() : "-"
                });
            }
            lblTotalTrx.setText("Total Pengembalian: " + currentList.size() + " Transaksi");
            lblTotalDenda.setText("Total Denda: " + nf.format(grandTotalDenda));
        } catch (Exception ex) {
            System.err.println("Gagal memuat laporan pengembalian: " + ex.getMessage());
        }
    }

    private void cetakKuitansiKembali(Peminjaman p) {
        if (p == null) return;
        String namaPetugas = p.getNamaPetugas() != null ? p.getNamaPetugas() : "Petugas Layanan Kemhan";
        BigDecimal denda = p.getTotalDenda() != null ? p.getTotalDenda() : BigDecimal.ZERO;
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String dendaStr = nf.format(denda);

        boolean adaDenda = denda.compareTo(BigDecimal.ZERO) > 0;
        String judulDokumen = adaDenda ? 
                "KUITANSI RESMI PEMBAYARAN DENDA & BUKTI PENGEMBALIAN BUKU" : 
                "BUKTI RESMI PENGEMBALIAN BUKU PERPUSTAKAAN KEMHAN RI";

        String[] headers = {"No", "Uraian / Judul Koleksi Buku", "Status Pengembalian", "Nominal Denda"};
        int[] weights = {1, 8, 4, 3};
        List<String[]> rows = new ArrayList<>();
        int no = 1;
        if (p.getDetailList() != null && !p.getDetailList().isEmpty()) {
            for (int i = 0; i < p.getDetailList().size(); i++) {
                rows.add(new String[]{
                        String.valueOf(no++),
                        p.getDetailList().get(i).getJudulBuku(),
                        "Diterima Baik (Rak)",
                        adaDenda ? dendaStr : "Rp 0"
                });
            }
        } else {
            rows.add(new String[]{"1", "Koleksi Buku Perpustakaan Kemhan", "Diterima Baik", adaDenda ? dendaStr : "Rp 0"});
        }

        String subJudul = "No. Transaksi: " + p.getKodeTransaksi() + "  |  Telah Diterima Dari: " + p.getNamaAnggota() + " (NIP/NRP: " + p.getNimAnggota() + ")";
        String summary = "Tgl Pinjam: " + p.getTanggalPinjam() + "  |  Batas Jatuh Tempo: " + p.getTanggalJatuhTempo() + "  |  Tgl Pengembalian Riil: " + (p.getTanggalKembali() != null ? p.getTanggalKembali() : "-") +
                         "\nStatus Pelunasan: " + (adaDenda ? "TERLAMBAT — TOTAL DENDA DITERIMA: " + dendaStr + " (LUNAS)" : "TEPAT WAKTU (BEBAS DENDA)") +
                         "\nPetugas Penerima: " + namaPetugas +
                         "\n* Dokumen ini merupakan tanda bukti pengembalian dan kuitansi sah pembayaran denda sirkulasi Perpustakaan Kemhan RI.";

        DialogCetakLaporan dialog = new DialogCetakLaporan(
                SwingUtilities.getWindowAncestor(this),
                judulDokumen,
                subJudul,
                headers, weights, rows, summary, true
        );
        dialog.setVisible(true);
    }
}
