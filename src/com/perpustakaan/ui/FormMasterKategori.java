package com.perpustakaan.ui;

import com.perpustakaan.dao.KategoriDAO;
import com.perpustakaan.model.Kategori;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * NAMA FILE : FormMasterKategori.java
 * FUNGSI    : Tampilan manajemen data klasifikasi/kategori buku (CRUD kategori).
 */
public class FormMasterKategori extends JPanel {
    private final KategoriDAO kategoriDAO = new KategoriDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtNama;
    private int selectedId = -1;

    public FormMasterKategori() {
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
        JLabel lblTitle = new JLabel("Manajemen Kategori Buku");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Content: Left form, Right table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(6);

        // Form Panel
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(UIConstants.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblFormTitle = new JLabel("Form Kategori");
        lblFormTitle.setFont(UIConstants.FONT_HEADER);
        formCard.add(lblFormTitle, gbc);

        gbc.gridy = 1;
        formCard.add(new JLabel("Nama Kategori:"), gbc);

        gbc.gridy = 2;
        txtNama = new JTextField(15);
        txtNama.setFont(UIConstants.FONT_REGULAR);
        txtNama.setPreferredSize(new Dimension(150, 32));
        formCard.add(txtNama, gbc);

        // Buttons Panel
        gbc.gridy = 3;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        btnPanel.setOpaque(false);

        JButton btnSimpan = UIConstants.createStyledButton("Simpan", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnReset = UIConstants.createStyledButton("Reset", Color.LIGHT_GRAY, Color.BLACK);
        JButton btnHapus = UIConstants.createStyledButton("Hapus", UIConstants.ACCENT_RED, Color.WHITE);

        btnPanel.add(btnSimpan);
        btnPanel.add(btnReset);
        btnPanel.add(btnHapus);
        formCard.add(btnPanel, gbc);

        // Table Panel
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(UIConstants.createCardBorder());

        String[] columns = {"ID", "Nama Kategori"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(UIConstants.FONT_REGULAR);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtNama.setText((String) tableModel.getValueAt(row, 1));
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        splitPane.setLeftComponent(formCard);
        splitPane.setRightComponent(tableCard);
        add(splitPane, BorderLayout.CENTER);

        // Button Actions
        btnSimpan.addActionListener(e -> {
            String nama = txtNama.getText().trim();
            if (nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama kategori tidak boleh kosong!");
                return;
            }
            try {
                if (selectedId == -1) {
                    kategoriDAO.insert(new Kategori(0, nama));
                    JOptionPane.showMessageDialog(this, "Kategori berhasil ditambahkan!");
                } else {
                    kategoriDAO.update(new Kategori(selectedId, nama));
                    JOptionPane.showMessageDialog(this, "Kategori berhasil diperbarui!");
                }
                resetForm();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnReset.addActionListener(e -> resetForm());

        btnHapus.addActionListener(e -> {
            if (selectedId == -1) {
                JOptionPane.showMessageDialog(this, "Pilih kategori yang akan dihapus!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus kategori terpilih?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    kategoriDAO.delete(selectedId);
                    JOptionPane.showMessageDialog(this, "Kategori berhasil dihapus!");
                    resetForm();
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus: Kategori mungkin masih digunakan oleh data buku!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void resetForm() {
        selectedId = -1;
        txtNama.setText("");
        table.clearSelection();
    }

    public void loadData() {
        try {
            List<Kategori> list = kategoriDAO.getAll();
            tableModel.setRowCount(0);
            for (Kategori k : list) {
                tableModel.addRow(new Object[]{k.getId(), k.getNamaKategori()});
            }
        } catch (Exception ex) {
            System.err.println("Gagal memuat kategori: " + ex.getMessage());
        }
    }
}
