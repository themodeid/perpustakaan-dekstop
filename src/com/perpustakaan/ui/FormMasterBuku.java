package com.perpustakaan.ui;

import com.perpustakaan.dao.BukuDAO;
import com.perpustakaan.dao.KategoriDAO;
import com.perpustakaan.dao.RakDAO;
import com.perpustakaan.model.Buku;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.Rak;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * NAMA FILE : FormMasterBuku.java
 * FUNGSI    : Tampilan manajemen data koleksi buku (pencarian, filter kategori, tambah, edit, dan hapus buku).
 */
public class FormMasterBuku extends JPanel {
    private final BukuDAO bukuDAO = new BukuDAO();
    private final KategoriDAO kategoriDAO = new KategoriDAO();
    private final RakDAO rakDAO = new RakDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterKategori;
    private List<Kategori> kategoriList;
    private JPanel actionButtonsPanel;

    public void setReadOnly(boolean readOnly) {
        if (actionButtonsPanel != null) {
            actionButtonsPanel.setVisible(!readOnly);
        }
    }

    public FormMasterBuku() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadKategoriFilter();
        loadData();
    }

    private void initUI() {
        // Top Panel (Title & Action Buttons)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Master Data: Koleksi Buku");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton btnTambah = UIConstants.createStyledButton("+ Tambah Buku", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnEdit = UIConstants.createStyledButton("Edit", UIConstants.ACCENT_ORANGE, Color.WHITE);
        JButton btnHapus = UIConstants.createStyledButton("Hapus", UIConstants.ACCENT_RED, Color.WHITE);

        actionBtns.add(btnTambah);
        actionBtns.add(btnEdit);
        actionBtns.add(btnHapus);
        this.actionButtonsPanel = actionBtns;
        topPanel.add(actionBtns, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Filter / Search Toolbar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(UIConstants.createCardBorder());

        filterPanel.add(new JLabel("Pencarian:"));
        txtSearch = new JTextField(20);
        txtSearch.setFont(UIConstants.FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(200, 32));
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Kategori:"));
        cbFilterKategori = new JComboBox<>();
        cbFilterKategori.setPreferredSize(new Dimension(200, 32));
        filterPanel.add(cbFilterKategori);

        JButton btnCari = UIConstants.createStyledButton("Cari", UIConstants.PRIMARY_DARK, Color.WHITE);
        JButton btnReset = UIConstants.createStyledButton("Reset", Color.LIGHT_GRAY, Color.BLACK);
        filterPanel.add(btnCari);
        filterPanel.add(btnReset);

        // Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(UIConstants.createCardBorder());

        String[] columns = {"ID", "Judul Buku", "Pengarang", "Penerbit", "Tahun", "Kategori", "Rak", "Total Stok", "Tersedia"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_REGULAR);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        // Center alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 10));
        centerWrapper.setOpaque(false);
        centerWrapper.add(filterPanel, BorderLayout.NORTH);
        centerWrapper.add(tableContainer, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // Listeners
        btnCari.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbFilterKategori.setSelectedIndex(0);
            loadData();
        });

        btnTambah.addActionListener(e -> openBukuDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih buku yang ingin diedit!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                Buku b = bukuDAO.getById(id);
                if (b != null) openBukuDialog(b);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnHapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih buku yang ingin dihapus!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            String judul = (String) tableModel.getValueAt(row, 1);

            int conf = JOptionPane.showConfirmDialog(this, 
                    "Hapus buku: '" + judul + "'?", 
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    bukuDAO.delete(id);
                    JOptionPane.showMessageDialog(this, "Buku berhasil dihapus!");
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus: Buku mungkin ada dalam riwayat peminjaman!");
                }
            }
        });
    }

    public void loadKategoriFilter() {
        try {
            kategoriList = kategoriDAO.getAll();
            cbFilterKategori.removeAllItems();
            cbFilterKategori.addItem("Semua Kategori");
            for (Kategori k : kategoriList) {
                cbFilterKategori.addItem(k.getNamaKategori());
            }
        } catch (Exception ex) {
            System.err.println("Gagal memuat filter kategori: " + ex.getMessage());
        }
    }

    public void loadData() {
        try {
            List<Buku> list = bukuDAO.getAll();
            renderTable(list);
        } catch (Exception ex) {
            System.err.println("Gagal memuat buku: " + ex.getMessage());
        }
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        Integer katId = null;
        int idx = cbFilterKategori.getSelectedIndex();
        if (idx > 0 && kategoriList != null && idx - 1 < kategoriList.size()) {
            katId = kategoriList.get(idx - 1).getId();
        }

        try {
            List<Buku> list = bukuDAO.search(keyword, katId, null);
            renderTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data: " + ex.getMessage());
        }
    }

    private void renderTable(List<Buku> list) {
        tableModel.setRowCount(0);
        for (Buku b : list) {
            String displayRak = b.getKodeRak() != null ? b.getKodeRak() : "-";
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getJudul(),
                    b.getPengarang(),
                    b.getPenerbit(),
                    b.getTahunTerbit(),
                    b.getNamaKategori(),
                    displayRak,
                    b.getStokTotal(),
                    b.getStokTersedia()
            });
        }
    }

    private void openBukuDialog(Buku bukuToEdit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                bukuToEdit == null ? "Tambah Buku Baru" : "Edit Buku", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField txtJudul = new JTextField();
        JTextField txtPengarang = new JTextField();
        JTextField txtPenerbit = new JTextField();
        JTextField txtTahun = new JTextField("2024");
        JSpinner spStok = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JComboBox<Kategori> cbKategori = new JComboBox<>();
        JComboBox<Rak> cbRak = new JComboBox<>();

        try {
            List<Kategori> kats = kategoriDAO.getAll();
            for (Kategori k : kats) {
                cbKategori.addItem(k);
            }
            List<Rak> raks = rakDAO.getAll();
            for (Rak r : raks) {
                cbRak.addItem(r);
            }
        } catch (Exception ignored) {}

        if (bukuToEdit != null) {
            txtJudul.setText(bukuToEdit.getJudul());
            txtPengarang.setText(bukuToEdit.getPengarang());
            txtPenerbit.setText(bukuToEdit.getPenerbit());
            txtTahun.setText(String.valueOf(bukuToEdit.getTahunTerbit()));
            spStok.setValue(bukuToEdit.getStokTotal());
            for (int i = 0; i < cbKategori.getItemCount(); i++) {
                if (cbKategori.getItemAt(i).getId() == bukuToEdit.getKategoriId()) {
                    cbKategori.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cbRak.getItemCount(); i++) {
                if (cbRak.getItemAt(i).getId() == bukuToEdit.getRakId()) {
                    cbRak.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Judul Buku:", txtJudul);
        addFormField(form, gbc, row++, "Pengarang:", txtPengarang);
        addFormField(form, gbc, row++, "Penerbit:", txtPenerbit);
        addFormField(form, gbc, row++, "Tahun Terbit:", txtTahun);
        addFormField(form, gbc, row++, "Kategori:", cbKategori);
        addFormField(form, gbc, row++, "Lokasi Rak Buku:", cbRak);
        addFormField(form, gbc, row++, "Jumlah Total Stok:", spStok);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = UIConstants.createStyledButton("Simpan", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnBatal = UIConstants.createStyledButton("Batal", Color.GRAY, Color.WHITE);

        btnBar.add(btnBatal);
        btnBar.add(btnSimpan);

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            String judul = txtJudul.getText().trim();
            String pengarang = txtPengarang.getText().trim();
            String penerbit = txtPenerbit.getText().trim();
            String tahunStr = txtTahun.getText().trim();
            Kategori selectedKat = (Kategori) cbKategori.getSelectedItem();
            Rak selectedRak = (Rak) cbRak.getSelectedItem();
            int totalStok = (int) spStok.getValue();

            if (judul.isEmpty() || pengarang.isEmpty() || penerbit.isEmpty() || selectedKat == null || selectedRak == null) {
                JOptionPane.showMessageDialog(dialog, "Semua kolom wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int tahun;
            try {
                tahun = Integer.parseInt(tahunStr);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Tahun harus berupa angka!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (bukuToEdit == null) {
                    Buku b = new Buku(0, judul, pengarang, penerbit, tahun, selectedKat.getId(), selectedRak.getId(), totalStok, totalStok);
                    bukuDAO.insert(b);
                    JOptionPane.showMessageDialog(this, "Buku berhasil ditambahkan!");
                } else {
                    int selisihStok = totalStok - bukuToEdit.getStokTotal();
                    int stokTersediaBaru = Math.max(0, bukuToEdit.getStokTersedia() + selisihStok);

                    bukuToEdit.setJudul(judul);
                    bukuToEdit.setPengarang(pengarang);
                    bukuToEdit.setPenerbit(penerbit);
                    bukuToEdit.setTahunTerbit(tahun);
                    bukuToEdit.setKategoriId(selectedKat.getId());
                    bukuToEdit.setRakId(selectedRak.getId());
                    bukuToEdit.setStokTotal(totalStok);
                    bukuToEdit.setStokTersedia(stokTersediaBaru);

                    bukuDAO.update(bukuToEdit);
                    JOptionPane.showMessageDialog(this, "Data buku berhasil diperbarui!");
                }

                dialog.dispose();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan buku: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(200, 32));
        panel.add(field, gbc);
    }
}
