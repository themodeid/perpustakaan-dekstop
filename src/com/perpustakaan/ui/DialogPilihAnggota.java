package com.perpustakaan.ui;

import com.perpustakaan.dao.AnggotaDAO;
import com.perpustakaan.model.Anggota;

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
 * NAMA FILE : DialogPilihAnggota.java
 * FUNGSI    : Dialog modal pencarian dan pemilihan data anggota perpustakaan aktif untuk transaksi peminjaman.
 */
public class DialogPilihAnggota extends JDialog {

    private final AnggotaDAO anggotaDAO = new AnggotaDAO();
    private final List<Anggota> anggotaList = new ArrayList<>();

    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private Anggota selectedAnggota = null;

    public DialogPilihAnggota(Window parent) {
        super(parent, "Cari & Pilih Data Personel (Peminjam Kemhan RI)", ModalityType.APPLICATION_MODAL);
        setSize(780, 520);
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

        JLabel lblTitle = new JLabel("Cari Data Personel Kemhan RI Aktif");
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Pilih personel yang terdaftar aktif untuk melakukan transaksi peminjaman buku kedinasan");
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

        JLabel lblSearch = new JLabel("Cari (NIP/NRP / Nama / Satker):");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        searchBar.add(lblSearch);

        txtSearch = new JTextField(24);
        txtSearch.setFont(UIConstants.FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(240, 32));
        searchBar.add(txtSearch);

        JButton btnReset = UIConstants.createStyledButton("Reset", Color.LIGHT_GRAY, Color.BLACK);
        searchBar.add(btnReset);

        centerPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "NIP / NRP", "Nama Personel", "Satuan Kerja (Satker)", "No. Telepon", "Status"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(170);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setCellRenderer(center);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(UIConstants.createCardBorder());
        centerPanel.add(scrollTable, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnPilih = UIConstants.createStyledButton("✓ Pilih Anggota", UIConstants.ACCENT_GREEN, Color.WHITE);
        btnPilih.setFont(UIConstants.FONT_BOLD);
        btnPilih.setPreferredSize(new Dimension(140, 36));

        JButton btnBatal = UIConstants.createStyledButton("Batal", Color.LIGHT_GRAY, Color.BLACK);
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
            selectedAnggota = null;
            dispose();
        });
    }

    private void pilihDanTutup() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu anggota terlebih dahulu dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (row < anggotaList.size()) {
            selectedAnggota = anggotaList.get(row);
            dispose();
        }
    }

    private void loadData(String keyword) {
        try {
            tableModel.setRowCount(0);
            anggotaList.clear();

            List<Anggota> data = (keyword == null || keyword.isEmpty()) ?
                    anggotaDAO.getAktifOnly() :
                    anggotaDAO.searchAktifOnly(keyword);

            for (Anggota a : data) {
                anggotaList.add(a);
                tableModel.addRow(new Object[]{
                        a.getId(),
                        a.getNim(),
                        a.getNama(),
                        a.getJurusan(),
                        a.getNoTelp() != null ? a.getNoTelp() : "-",
                        a.getStatus()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data anggota:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Anggota getSelectedAnggota() {
        return selectedAnggota;
    }
}
