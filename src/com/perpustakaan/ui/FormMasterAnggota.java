package com.perpustakaan.ui;

import com.perpustakaan.dao.AnggotaDAO;
import com.perpustakaan.model.Anggota;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * NAMA FILE : FormMasterAnggota.java
 * FUNGSI    : Tampilan manajemen data anggota perpustakaan (pencarian, tambah, edit, dan hapus anggota).
 */
public class FormMasterAnggota extends JPanel {
    private final AnggotaDAO anggotaDAO = new AnggotaDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public FormMasterAnggota() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Data Keanggotaan Perpustakaan (Personel Kemhan RI)");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton btnTambah = UIConstants.createStyledButton("+ Tambah Personel", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnCetakKta = UIConstants.createStyledButton("💳 Cetak KTA", new Color(13, 148, 136), Color.WHITE);
        JButton btnEdit = UIConstants.createStyledButton("Edit", UIConstants.ACCENT_ORANGE, Color.WHITE);
        JButton btnHapus = UIConstants.createStyledButton("Hapus", UIConstants.ACCENT_RED, Color.WHITE);

        actionBtns.add(btnTambah);
        actionBtns.add(btnCetakKta);
        actionBtns.add(btnEdit);
        actionBtns.add(btnHapus);
        topPanel.add(actionBtns, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Filter / Search Toolbar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(UIConstants.createCardBorder());

        filterPanel.add(new JLabel("Pencarian (NIP/NRP / Nama / Satker):"));
        txtSearch = new JTextField(22);
        txtSearch.setFont(UIConstants.FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(220, 32));
        filterPanel.add(txtSearch);

        JButton btnCari = UIConstants.createStyledButton("Cari", UIConstants.PRIMARY_DARK, Color.WHITE);
        JButton btnReset = UIConstants.createStyledButton("Reset", Color.LIGHT_GRAY, Color.BLACK);
        filterPanel.add(btnCari);
        filterPanel.add(btnReset);

        // Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(UIConstants.createCardBorder());

        String[] columns = {"ID", "NIP / NRP", "Nama Personel", "Satuan Kerja (Satker)", "No. Telepon", "Email Dinas", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UIConstants.FONT_REGULAR);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 10));
        centerWrapper.setOpaque(false);
        centerWrapper.add(filterPanel, BorderLayout.NORTH);
        centerWrapper.add(tableContainer, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // Actions
        btnCari.addActionListener(e -> performSearch());
        txtSearch.addActionListener(e -> performSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        btnTambah.addActionListener(e -> openAnggotaDialog(null));
        btnCetakKta.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, 
                        "Pilih salah satu baris personel pada tabel untuk mencetak Kartu Tanda Anggota (KTA)!", 
                        "Pilih Personel", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                Anggota a = anggotaDAO.getById(id);
                if (a != null) {
                    DialogCetakKartuAnggota kta = new DialogCetakKartuAnggota(SwingUtilities.getWindowAncestor(this), a);
                    kta.setVisible(true);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat kartu anggota: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih anggota yang ingin diedit!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                Anggota a = anggotaDAO.getById(id);
                if (a != null) openAnggotaDialog(a);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnHapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih anggota yang ingin dihapus!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            String nama = (String) tableModel.getValueAt(row, 2);

            int conf = JOptionPane.showConfirmDialog(this, 
                    "Hapus anggota: '" + nama + "'?", 
                    "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    anggotaDAO.delete(id);
                    JOptionPane.showMessageDialog(this, "Anggota berhasil dihapus!");
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus: Anggota memiliki riwayat peminjaman!");
                }
            }
        });
    }

    public void loadData() {
        try {
            List<Anggota> list = anggotaDAO.getAll();
            renderTable(list);
        } catch (Exception ex) {
            System.err.println("Gagal memuat anggota: " + ex.getMessage());
        }
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        try {
            List<Anggota> list = keyword.isEmpty() ? anggotaDAO.getAll() : anggotaDAO.search(keyword);
            renderTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data: " + ex.getMessage());
        }
    }

    private void renderTable(List<Anggota> list) {
        tableModel.setRowCount(0);
        for (Anggota a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getNim(),
                    a.getNama(),
                    a.getJurusan(),
                    a.getNoTelp(),
                    a.getEmail(),
                    a.getStatus().toUpperCase()
            });
        }
    }

    private void openAnggotaDialog(Anggota toEdit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                toEdit == null ? "Tambah Anggota Baru" : "Edit Anggota", true);
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField txtNim = new JTextField();
        JTextField txtNama = new JTextField();
        JTextField txtJurusan = new JTextField();
        JTextField txtTelp = new JTextField();
        JTextField txtEmail = new JTextField();
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"aktif", "nonaktif"});

        if (toEdit != null) {
            txtNim.setText(toEdit.getNim());
            txtNama.setText(toEdit.getNama());
            txtJurusan.setText(toEdit.getJurusan());
            txtTelp.setText(toEdit.getNoTelp());
            txtEmail.setText(toEdit.getEmail());
            cbStatus.setSelectedItem(toEdit.getStatus());
        }

        int row = 0;
        addFormField(form, gbc, row++, "NIP / NRP Personel:", txtNim);
        addFormField(form, gbc, row++, "Nama Lengkap & Gelar:", txtNama);
        addFormField(form, gbc, row++, "Satuan Kerja (Satker):", txtJurusan);
        addFormField(form, gbc, row++, "Nomor Telepon / WA:", txtTelp);
        addFormField(form, gbc, row++, "Email Dinas (@kemhan.go.id):", txtEmail);
        addFormField(form, gbc, row++, "Status Keanggotaan:", cbStatus);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = UIConstants.createStyledButton("Simpan", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnBatal = UIConstants.createStyledButton("Batal", Color.GRAY, Color.WHITE);

        btnBar.add(btnBatal);
        btnBar.add(btnSimpan);

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            String nim = txtNim.getText().trim();
            String nama = txtNama.getText().trim();
            String jurusan = txtJurusan.getText().trim();
            String telp = txtTelp.getText().trim();
            String email = txtEmail.getText().trim();
            String status = (String) cbStatus.getSelectedItem();

            if (nim.isEmpty() || nama.isEmpty() || jurusan.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "NIP/NRP, Nama, dan Satuan Kerja (Satker) wajib diisi!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int editId = toEdit != null ? toEdit.getId() : 0;
                if (anggotaDAO.isNimExists(nim, editId)) {
                    JOptionPane.showMessageDialog(dialog, "NIP / NRP sudah terdaftar untuk personel lain!", "Validasi Duplikasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (toEdit == null) {
                    Anggota a = new Anggota(0, nim, nama, jurusan, telp, email, status);
                    anggotaDAO.insert(a);
                    
                    dialog.dispose();
                    loadData();

                    // Ambil record yang baru dimasukkan untuk dicetak
                    List<Anggota> newlyCreated = anggotaDAO.search(nim);
                    Anggota fresh = !newlyCreated.isEmpty() ? newlyCreated.get(0) : a;

                    int tanyaKta = JOptionPane.showConfirmDialog(this,
                            "Personel Kemhan RI berhasil didaftarkan sebagai anggota perpustakaan!\n\n" +
                            "Nama Personel : " + fresh.getNama() + "\n" +
                            "NIP / NRP     : " + fresh.getNim() + "\n" +
                            "Satker        : " + fresh.getJurusan() + "\n\n" +
                            "Apakah Anda ingin mencetak Kartu Tanda Anggota (KTA) resmi sekarang?\n" +
                            "(Bisa dicetak ke printer fisik atau disimpan ke PDF untuk dikirim via WA / Email)",
                            "Pendaftaran Berhasil - Cetak KTA?", JOptionPane.YES_NO_OPTION);

                    if (tanyaKta == JOptionPane.YES_OPTION) {
                        DialogCetakKartuAnggota kta = new DialogCetakKartuAnggota(SwingUtilities.getWindowAncestor(this), fresh);
                        kta.setVisible(true);
                    }
                } else {
                    toEdit.setNim(nim);
                    toEdit.setNama(nama);
                    toEdit.setJurusan(jurusan);
                    toEdit.setNoTelp(telp);
                    toEdit.setEmail(email);
                    toEdit.setStatus(status);
                    anggotaDAO.update(toEdit);
                    JOptionPane.showMessageDialog(this, "Data personel berhasil diperbarui!");
                    dialog.dispose();
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        comp.setPreferredSize(new Dimension(200, 30));
        panel.add(comp, gbc);
    }
}
