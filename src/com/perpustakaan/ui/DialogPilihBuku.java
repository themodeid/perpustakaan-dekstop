package com.perpustakaan.ui;

import com.perpustakaan.dao.BukuDAO;
import com.perpustakaan.model.Buku;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * NAMA FILE : DialogPilihBuku.java
 * FUNGSI    : Dialog modal katalog pencarian dan pemilihan buku yang tersedia untuk transaksi peminjaman.
 */
public class DialogPilihBuku extends JDialog {

    private final BukuDAO bukuDAO = new BukuDAO();
    private final List<Buku> bukuList = new ArrayList<>();

    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private Buku selectedBuku = null;

    public DialogPilihBuku(Window parent) {
        super(parent, "Katalog Pencarian Buku Tersedia", ModalityType.APPLICATION_MODAL);
        setSize(880, 540);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIConstants.BG_LIGHT);

        initUI();
        loadData("");
    }

    private void initUI() {
        // 1. Header Card
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel lblTitle = new JLabel("Katalog Pencarian Koleksi Buku Tersedia");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Pilih buku yang memiliki ketersediaan stok fisik di rak untuk dimasukkan ke daftar pinjam");
        lblSubtitle.setFont(UIConstants.FONT_SMALL);
        lblSubtitle.setForeground(Color.GRAY);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(lblSubtitle);
        headerPanel.add(titleBox, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content (Search + Table)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Filter / Search Toolbar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(UIConstants.createCardBorder());

        JLabel lblSearch = new JLabel("Cari (Judul / Pengarang / Penerbit / Kategori):");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        searchBar.add(lblSearch);

        txtSearch = new JTextField(26);
        txtSearch.setFont(UIConstants.FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(260, 32));
        searchBar.add(txtSearch);

        JButton btnReset = UIConstants.createStyledButton("Reset", Color.LIGHT_GRAY, Color.BLACK);
        searchBar.add(btnReset);

        centerPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Judul Buku", "Pengarang", "Penerbit", "Tahun", "Kategori", "Lokasi Rak", "Stok Tersedia"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(UIConstants.FONT_REGULAR);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(55);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setCellRenderer(center);
        table.getColumnModel().getColumn(7).setPreferredWidth(85);
        table.getColumnModel().getColumn(7).setCellRenderer(center);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(UIConstants.createCardBorder());
        centerPanel.add(scrollTable, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnPilih = UIConstants.createStyledButton("+ Pilih / Tambah ke Daftar", UIConstants.ACCENT_GREEN, Color.WHITE);
        btnPilih.setFont(UIConstants.FONT_BOLD);
        btnPilih.setPreferredSize(new Dimension(200, 36));

        JButton btnBatal = UIConstants.createStyledButton("Tutup", Color.LIGHT_GRAY, Color.BLACK);
        btnBatal.setPreferredSize(new Dimension(90, 36));

        bottomPanel.add(btnBatal);
        bottomPanel.add(btnPilih);
        add(bottomPanel, BorderLayout.SOUTH);

        // Events
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { cari(); }
            public void removeUpdate(DocumentEvent e) { cari(); }
            public void changedUpdate(DocumentEvent e) { cari(); }
            private void cari() { loadData(txtSearch.getText().trim()); }
        });

        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadData("");
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    pilihDanTutup();
                }
            }
        });

        btnPilih.addActionListener(e -> pilihDanTutup());
        btnBatal.addActionListener(e -> {
            selectedBuku = null;
            dispose();
        });
    }

    private void pilihDanTutup() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu buku terlebih dahulu dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (row < bukuList.size()) {
            selectedBuku = bukuList.get(row);
            dispose();
        }
    }

    private void loadData(String keyword) {
        try {
            tableModel.setRowCount(0);
            bukuList.clear();

            List<Buku> data = (keyword == null || keyword.isEmpty()) ?
                    bukuDAO.searchTersedia(null) :
                    bukuDAO.searchTersedia(keyword);

            for (Buku b : data) {
                bukuList.add(b);
                tableModel.addRow(new Object[]{
                        b.getId(),
                        b.getJudul(),
                        b.getPengarang(),
                        b.getPenerbit() != null ? b.getPenerbit() : "-",
                        b.getTahunTerbit(),
                        b.getNamaKategori() != null ? b.getNamaKategori() : "-",
                        b.getKodeRak() != null ? b.getKodeRak() : "-",
                        b.getStokTersedia()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data buku:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Buku getSelectedBuku() {
        return selectedBuku;
    }
}
