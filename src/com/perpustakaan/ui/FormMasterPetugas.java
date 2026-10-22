package com.perpustakaan.ui;

import com.perpustakaan.dao.PenggunaDAO;
import com.perpustakaan.model.Pengguna;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * NAMA FILE : FormMasterPetugas.java
 * FUNGSI    : Tampilan manajemen akun petugas dan administrator perpustakaan (CRUD data staf).
 */
public class FormMasterPetugas extends JPanel {
    private final PenggunaDAO penggunaDAO = new PenggunaDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public FormMasterPetugas() {
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

        JLabel lblTitle = new JLabel("Manajemen Petugas & Admin");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton btnTambah = UIConstants.createStyledButton("+ Tambah Petugas", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnEdit = UIConstants.createStyledButton("Edit", UIConstants.ACCENT_ORANGE, Color.WHITE);
        JButton btnHapus = UIConstants.createStyledButton("Hapus", UIConstants.ACCENT_RED, Color.WHITE);

        actionBtns.add(btnTambah);
        actionBtns.add(btnEdit);
        actionBtns.add(btnHapus);
        topPanel.add(actionBtns, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(UIConstants.createCardBorder());

        String[] columns = {"ID", "Username", "Nama Lengkap", "Role Hak Akses", "No. Telepon", "Tanggal Dibuat"};
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
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // Actions
        btnTambah.addActionListener(e -> openPenggunaDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih data petugas yang ingin diedit!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                Pengguna u = penggunaDAO.getById(id);
                if (u != null) openPenggunaDialog(u);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnHapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih data petugas yang ingin dihapus!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            String user = (String) tableModel.getValueAt(row, 1);

            int conf = JOptionPane.showConfirmDialog(this, 
                    "Hapus user: '" + user + "'?", 
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    penggunaDAO.delete(id);
                    JOptionPane.showMessageDialog(this, "User berhasil dihapus!");
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus: User memiliki riwayat aktivitas transaksi!");
                }
            }
        });
    }

    public void loadData() {
        try {
            List<Pengguna> list = penggunaDAO.getAll();
            tableModel.setRowCount(0);
            for (Pengguna u : list) {
                tableModel.addRow(new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getNamaLengkap(),
                        u.getRole().toUpperCase(),
                        u.getNoTelp() != null ? u.getNoTelp() : "-",
                        u.getCreatedAt()
                });
            }
        } catch (Exception ex) {
            System.err.println("Gagal memuat petugas: " + ex.getMessage());
        }
    }

    private void openPenggunaDialog(Pengguna toEdit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                toEdit == null ? "Tambah Petugas/Admin Baru" : "Edit Akun Petugas", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JTextField txtNama = new JTextField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"petugas", "admin"});
        JTextField txtTelp = new JTextField();

        if (toEdit != null) {
            txtUsername.setText(toEdit.getUsername());
            txtNama.setText(toEdit.getNamaLengkap());
            cbRole.setSelectedItem(toEdit.getRole());
            txtTelp.setText(toEdit.getNoTelp() != null ? toEdit.getNoTelp() : "");
        }

        int row = 0;
        addFormField(form, gbc, row++, "Username:", txtUsername);
        addFormField(form, gbc, row++, toEdit == null ? "Password:" : "Password Baru (Opsional):", txtPassword);
        addFormField(form, gbc, row++, "Nama Lengkap:", txtNama);
        addFormField(form, gbc, row++, "Role Hak Akses:", cbRole);
        addFormField(form, gbc, row++, "No. Telepon:", txtTelp);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = UIConstants.createStyledButton("Simpan", UIConstants.PRIMARY_COLOR, Color.WHITE);
        JButton btnBatal = UIConstants.createStyledButton("Batal", Color.GRAY, Color.WHITE);

        btnBar.add(btnBatal);
        btnBar.add(btnSimpan);

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword()).trim();
            String nama = txtNama.getText().trim();
            String role = (String) cbRole.getSelectedItem();
            String telp = txtTelp.getText().trim();

            if (username.isEmpty() || nama.isEmpty() || (toEdit == null && pass.isEmpty())) {
                JOptionPane.showMessageDialog(dialog, "Username, Password, dan Nama Lengkap wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int editId = toEdit != null ? toEdit.getId() : 0;
                if (penggunaDAO.isUsernameExists(username, editId)) {
                    JOptionPane.showMessageDialog(dialog, "Username sudah digunakan oleh akun lain!", "Validasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (toEdit == null) {
                    Pengguna u = new Pengguna(0, username, pass, nama, role, telp);
                    penggunaDAO.insert(u);
                    JOptionPane.showMessageDialog(this, "Petugas berhasil ditambahkan!");
                } else {
                    toEdit.setUsername(username);
                    if (!pass.isEmpty()) {
                        toEdit.setPassword(pass);
                    }
                    toEdit.setNamaLengkap(nama);
                    toEdit.setRole(role);
                    toEdit.setNoTelp(telp);
                    penggunaDAO.update(toEdit);
                    JOptionPane.showMessageDialog(this, "Data petugas berhasil diperbarui!");
                }

                dialog.dispose();
                loadData();
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
