package com.perpustakaan.ui;

import com.perpustakaan.dao.RakDAO;
import com.perpustakaan.model.Rak;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * NAMA FILE : FormMasterRak.java
 * FUNGSI    : Tampilan manajemen data lokasi rak penyimpanan buku di perpustakaan (CRUD rak buku).
 */
public class FormMasterRak extends JPanel {
    private final RakDAO rakDAO = new RakDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtKode;
    private JTextField txtNama;
    private JTextField txtLokasi;
    private int selectedId = -1;
    private JPanel formCardPanel;
    private JSplitPane splitPane;

    public void setReadOnly(boolean readOnly) {
        if (splitPane != null) {
            if (readOnly) {
                splitPane.setLeftComponent(null);
            } else {
                splitPane.setLeftComponent(formCardPanel);
                splitPane.setDividerLocation(300);
            }
        }
    }

    public FormMasterRak() {
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
        JLabel lblTitle = new JLabel("Master Data: Rak Buku Perpustakaan");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Content: Left form, Right table
        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(6);

        // Form Panel
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(UIConstants.createCardBorder());
        this.formCardPanel = formCard;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblFormTitle = new JLabel("Form Input Rak Buku");
        lblFormTitle.setFont(UIConstants.FONT_HEADER);
        formCard.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;
        // Kode Rak
        gbc.gridx = 0; gbc.gridy = 1;
        formCard.add(new JLabel("Kode Rak:"), gbc);
        gbc.gridx = 1;
        txtKode = new JTextField(15);
        formCard.add(txtKode, gbc);

        // Nama Rak
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Nama Rak:"), gbc);
        gbc.gridx = 1;
        txtNama = new JTextField(15);
        formCard.add(txtNama, gbc);

        // Lokasi
        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Lokasi / Lantai:"), gbc);
        gbc.gridx = 1;
        txtLokasi = new JTextField(15);
        formCard.add(txtLokasi, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
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

        String[] columns = {"ID", "Kode Rak", "Nama Rak", "Lokasi Rak"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(UIConstants.FONT_REGULAR);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setCellRenderer(cr);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setCellRenderer(cr);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtKode.setText((String) tableModel.getValueAt(row, 1));
                txtNama.setText((String) tableModel.getValueAt(row, 2));
                txtLokasi.setText((String) tableModel.getValueAt(row, 3));
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        splitPane.setLeftComponent(formCard);
        splitPane.setRightComponent(tableCard);
        add(splitPane, BorderLayout.CENTER);

        // Action Handlers
        btnSimpan.addActionListener(e -> {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            String lokasi = txtLokasi.getText().trim();

            if (kode.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kode Rak dan Nama Rak wajib diisi!");
                return;
            }

            try {
                if (rakDAO.isKodeRakExists(kode, selectedId)) {
                    JOptionPane.showMessageDialog(this, "Kode Rak sudah digunakan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (selectedId == -1) {
                    rakDAO.insert(new Rak(0, kode, nama, lokasi));
                    JOptionPane.showMessageDialog(this, "Data Rak berhasil ditambahkan!");
                } else {
                    rakDAO.update(new Rak(selectedId, kode, nama, lokasi));
                    JOptionPane.showMessageDialog(this, "Data Rak berhasil diperbarui!");
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
                JOptionPane.showMessageDialog(this, "Pilih data rak yang akan dihapus!");
                return;
            }
            int conf = JOptionPane.showConfirmDialog(this, "Hapus data rak terpilih?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    rakDAO.delete(selectedId);
                    JOptionPane.showMessageDialog(this, "Data Rak berhasil dihapus!");
                    resetForm();
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus: Rak masih digunakan oleh koleksi buku!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void resetForm() {
        selectedId = -1;
        txtKode.setText("");
        txtNama.setText("");
        txtLokasi.setText("");
        table.clearSelection();
    }

    public void loadData() {
        try {
            List<Rak> list = rakDAO.getAll();
            tableModel.setRowCount(0);
            for (Rak r : list) {
                tableModel.addRow(new Object[]{r.getId(), r.getKodeRak(), r.getNamaRak(), r.getLokasi()});
            }
        } catch (Exception ex) {
            System.err.println("Gagal memuat rak: " + ex.getMessage());
        }
    }
}
