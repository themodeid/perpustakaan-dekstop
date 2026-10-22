package com.perpustakaan.ui;

import com.perpustakaan.dao.AnggotaDAO;
import com.perpustakaan.dao.BukuDAO;
import com.perpustakaan.model.Anggota;
import com.perpustakaan.model.Buku;
import com.perpustakaan.model.Peminjaman;
import com.perpustakaan.service.LayananAutentikasi;
import com.perpustakaan.service.LayananSirkulasi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * NAMA FILE : FormTransaksiPeminjaman.java
 * FUNGSI    : Tampilan pemrosesan transaksi peminjaman buku dan cetak bukti transaksi (struk) peminjaman.
 */
public class FormTransaksiPeminjaman extends JPanel {
    private final LayananSirkulasi layananSirkulasi = new LayananSirkulasi();
    private final AnggotaDAO anggotaDAO = new AnggotaDAO();
    private final BukuDAO bukuDAO = new BukuDAO();

    private JComboBox<Anggota> cbAnggota;
    private JComboBox<Buku> cbBuku;
    private JSpinner spDurasi;
    private JLabel lblTglPinjam;
    private JLabel lblTglTempo;
    private JTextArea txtCatatan;

    private JTable tblKeranjang;
    private DefaultTableModel tableModel;
    private final List<Buku> keranjangBuku = new ArrayList<>();

    public FormTransaksiPeminjaman() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadDropdownData();
    }

    private void initUI() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Sirkulasi: Transaksi Peminjaman Buku");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnReload = UIConstants.createStyledButton("Segarkan Data", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnReload.addActionListener(e -> loadDropdownData());
        topPanel.add(btnReload, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center Content Split
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);

        // Left: Form Peminjaman
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(UIConstants.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblFormTitle = new JLabel("Form Data Peminjaman");
        lblFormTitle.setFont(UIConstants.FONT_HEADER);
        formCard.add(lblFormTitle, gbc);

        // Anggota
        gbc.gridy = 1; gbc.gridwidth = 1;
        formCard.add(new JLabel("Pilih Anggota (Aktif):"), gbc);
        gbc.gridx = 1;
        cbAnggota = new JComboBox<>();
        formCard.add(cbAnggota, gbc);

        // Pilih Buku
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Pilih Buku Tersedia:"), gbc);
        gbc.gridx = 1;
        cbBuku = new JComboBox<>();
        formCard.add(cbBuku, gbc);

        // Tombol Tambah ke Keranjang
        gbc.gridx = 1; gbc.gridy = 3;
        JButton btnTambahBuku = UIConstants.createStyledButton("+ Tambah ke Daftar", UIConstants.PRIMARY_DARK, Color.WHITE);
        formCard.add(btnTambahBuku, gbc);

        // Durasi Pinjam
        gbc.gridx = 0; gbc.gridy = 4;
        formCard.add(new JLabel("Durasi Peminjaman (Hari):"), gbc);
        gbc.gridx = 1;
        spDurasi = new JSpinner(new SpinnerNumberModel(7, 1, 30, 1));
        spDurasi.addChangeListener(e -> updateTanggalTempo());
        formCard.add(spDurasi, gbc);

        // Tanggal Pinjam
        gbc.gridx = 0; gbc.gridy = 5;
        formCard.add(new JLabel("Tanggal Pinjam:"), gbc);
        gbc.gridx = 1;
        lblTglPinjam = new JLabel(LocalDate.now().toString());
        lblTglPinjam.setFont(UIConstants.FONT_BOLD);
        formCard.add(lblTglPinjam, gbc);

        // Tanggal Jatuh Tempo
        gbc.gridx = 0; gbc.gridy = 6;
        formCard.add(new JLabel("Tanggal Jatuh Tempo:"), gbc);
        gbc.gridx = 1;
        lblTglTempo = new JLabel(LocalDate.now().plusDays(7).toString());
        lblTglTempo.setFont(UIConstants.FONT_BOLD);
        lblTglTempo.setForeground(UIConstants.ACCENT_ORANGE);
        formCard.add(lblTglTempo, gbc);

        // Catatan
        gbc.gridx = 0; gbc.gridy = 7;
        formCard.add(new JLabel("Catatan (Opsional):"), gbc);
        gbc.gridx = 1;
        txtCatatan = new JTextArea(3, 15);
        txtCatatan.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formCard.add(new JScrollPane(txtCatatan), gbc);

        contentPanel.add(formCard);

        // Right: Daftar Buku yang akan dipinjam (Keranjang)
        JPanel cartCard = new JPanel(new BorderLayout(0, 10));
        cartCard.setBackground(Color.WHITE);
        cartCard.setBorder(UIConstants.createCardBorder());

        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setOpaque(false);
        JLabel lblCartTitle = new JLabel("Daftar Buku yang Dipinjam (Maks 3 Buku)");
        lblCartTitle.setFont(UIConstants.FONT_HEADER);
        JButton btnHapusBuku = UIConstants.createStyledButton("Hapus Pilihan", UIConstants.ACCENT_RED, Color.WHITE);
        cartHeader.add(lblCartTitle, BorderLayout.WEST);
        cartHeader.add(btnHapusBuku, BorderLayout.EAST);
        cartCard.add(cartHeader, BorderLayout.NORTH);

        String[] columns = {"ID Buku", "Judul Buku", "Pengarang"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKeranjang = new JTable(tableModel);
        tblKeranjang.setRowHeight(28);
        tblKeranjang.setFont(UIConstants.FONT_REGULAR);
        cartCard.add(new JScrollPane(tblKeranjang), BorderLayout.CENTER);

        // Bottom Action Button
        JPanel cartBottom = new JPanel(new BorderLayout());
        cartBottom.setOpaque(false);
        JButton btnSimpanPeminjaman = UIConstants.createStyledButton("Simpan Transaksi Peminjaman", UIConstants.ACCENT_GREEN, Color.WHITE);
        btnSimpanPeminjaman.setFont(UIConstants.FONT_HEADER);
        btnSimpanPeminjaman.setPreferredSize(new Dimension(200, 42));
        cartBottom.add(btnSimpanPeminjaman, BorderLayout.CENTER);
        cartCard.add(cartBottom, BorderLayout.SOUTH);

        contentPanel.add(cartCard);
        add(contentPanel, BorderLayout.CENTER);

        // Action Listeners
        btnTambahBuku.addActionListener(e -> {
            Buku selectedBuku = (Buku) cbBuku.getSelectedItem();
            if (selectedBuku == null) {
                JOptionPane.showMessageDialog(this, "Pilih buku terlebih dahulu!");
                return;
            }
            if (keranjangBuku.size() >= LayananSirkulasi.MAX_BUKU_PINJAM) {
                JOptionPane.showMessageDialog(this, "Maksimal peminjaman sekaligus adalah " + LayananSirkulasi.MAX_BUKU_PINJAM + " buku!");
                return;
            }
            for (Buku b : keranjangBuku) {
                if (b.getId() == selectedBuku.getId()) {
                    JOptionPane.showMessageDialog(this, "Buku ini sudah ada di dalam daftar!");
                    return;
                }
            }
            keranjangBuku.add(selectedBuku);
            refreshKeranjangTable();
        });

        btnHapusBuku.addActionListener(e -> {
            int row = tblKeranjang.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih baris buku di daftar yang ingin dihapus!");
                return;
            }
            keranjangBuku.remove(row);
            refreshKeranjangTable();
        });

        btnSimpanPeminjaman.addActionListener(e -> {
            Anggota anggota = (Anggota) cbAnggota.getSelectedItem();
            if (anggota == null) {
                JOptionPane.showMessageDialog(this, "Pilih anggota peminjam terlebih dahulu!");
                return;
            }
            if (keranjangBuku.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Daftar buku yang dipinjam masih kosong!");
                return;
            }

            int durasi = (int) spDurasi.getValue();
            String catatan = txtCatatan.getText().trim();
            List<Integer> bukuIds = new ArrayList<>();
            List<Buku> bukuDipinjamSalinan = new ArrayList<>(keranjangBuku);
            for (Buku b : keranjangBuku) {
                bukuIds.add(b.getId());
            }

            try {
                Peminjaman p = layananSirkulasi.buatPeminjaman(anggota.getId(), bukuIds, durasi, catatan);
                
                int tanyaCetak = JOptionPane.showConfirmDialog(this,
                        "Transaksi Peminjaman Berhasil Disimpan!\n\n" +
                        "Kode Transaksi : " + p.getKodeTransaksi() + "\n" +
                        "Peminjam       : " + anggota.getNama() + " (" + anggota.getNim() + ")\n" +
                        "Jumlah Buku    : " + bukuIds.size() + " buku\n" +
                        "Jatuh Tempo    : " + p.getTanggalJatuhTempo() + "\n\n" +
                        "Apakah Anda ingin langsung mencetak Bukti Peminjaman (Struk) sekarang?",
                        "Transaksi Sukses - Cetak Bukti?", JOptionPane.YES_NO_OPTION);

                if (tanyaCetak == JOptionPane.YES_OPTION) {
                    cetakBuktiPeminjaman(p, anggota, bukuDipinjamSalinan);
                }

                // Reset Form
                keranjangBuku.clear();
                refreshKeranjangTable();
                txtCatatan.setText("");
                loadDropdownData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal memproses peminjaman:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void cetakBuktiPeminjaman(Peminjaman p, Anggota anggota, List<Buku> bukuList) {
        String namaPetugas = LayananAutentikasi.getInstance().getCurrentUser() != null ?
                LayananAutentikasi.getInstance().getCurrentUser().getNamaLengkap() : "Petugas";

        String[] headers = {"No", "Judul Buku", "Pengarang"};
        int[] weights = {1, 9, 5};
        List<String[]> rows = new ArrayList<>();
        int no = 1;
        for (Buku b : bukuList) {
            rows.add(new String[]{String.valueOf(no++), b.getJudul(), b.getPengarang()});
        }

        String subJudul = "No. Transaksi: " + p.getKodeTransaksi() + "  |  Peminjam: " + anggota.getNama() + " (" + anggota.getNim() + ")";
        String summary = "Tgl Pinjam: " + p.getTanggalPinjam() + "  |  Batas Jatuh Tempo: " + p.getTanggalJatuhTempo() + "  |  Petugas: " + namaPetugas +
                         "\n* Harap simpan bukti ini dan kembalikan buku tepat waktu. Denda keterlambatan Rp 2.000 / hari / buku.";

        DialogCetakLaporan dialog = new DialogCetakLaporan(
                SwingUtilities.getWindowAncestor(this),
                "BUKTI RESMI TRANSAKSI PEMINJAMAN BUKU",
                subJudul,
                headers, weights, rows, summary, true
        );
        dialog.setVisible(true);
    }

    private void updateTanggalTempo() {
        int durasi = (int) spDurasi.getValue();
        lblTglTempo.setText(LocalDate.now().plusDays(durasi).toString());
    }

    private void refreshKeranjangTable() {
        tableModel.setRowCount(0);
        for (Buku b : keranjangBuku) {
            tableModel.addRow(new Object[]{b.getId(), b.getJudul(), b.getPengarang()});
        }
    }

    public void loadDropdownData() {
        try {
            // Load Anggota Aktif
            List<Anggota> anggotaList = anggotaDAO.getAktifOnly();
            cbAnggota.removeAllItems();
            for (Anggota a : anggotaList) {
                cbAnggota.addItem(a);
            }

            // Load Buku yang stoknya > 0
            List<Buku> bukuList = bukuDAO.getAll();
            cbBuku.removeAllItems();
            for (Buku b : bukuList) {
                if (b.getStokTersedia() > 0) {
                    cbBuku.addItem(b);
                }
            }
        } catch (Exception ex) {
            System.err.println("Gagal memuat dropdown: " + ex.getMessage());
        }
    }
}
