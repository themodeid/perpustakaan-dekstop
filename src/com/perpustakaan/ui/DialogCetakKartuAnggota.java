package com.perpustakaan.ui;

import com.perpustakaan.model.Anggota;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * NAMA FILE : DialogCetakKartuAnggota.java
 * FUNGSI    : Jendela pratinjau dan cetak Kartu Tanda Anggota (KTA) resmi Personel Perpustakaan Kementerian Pertahanan RI.
 *             Dapat dicetak langsung ke printer fisik atau disimpan sebagai berkas PDF (Print to PDF) untuk dikirim via WhatsApp/Email.
 */
public class DialogCetakKartuAnggota extends JDialog implements Printable {

    private final Anggota anggota;
    private JPanel kartuPanel;

    public DialogCetakKartuAnggota(Window parent, Anggota anggota) {
        super(parent, "Kartu Tanda Anggota (KTA) Perpustakaan Kemhan RI", ModalityType.APPLICATION_MODAL);
        this.anggota = anggota;

        setSize(680, 560);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(230, 235, 243));

        // 1. Toolbar Atas
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

        JButton btnPrint = UIConstants.createStyledButton("🖨️ Cetak / Simpan ke PDF", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnPrint.setFont(UIConstants.FONT_BOLD);
        btnPrint.setPreferredSize(new Dimension(210, 36));

        JButton btnTutup = UIConstants.createStyledButton("Tutup", Color.LIGHT_GRAY, Color.BLACK);
        btnTutup.setPreferredSize(new Dimension(100, 36));

        JLabel lblPetunjuk = new JLabel("💡 Simpan ke PDF via opsi printer untuk dikirim melalui WhatsApp / Email");
        lblPetunjuk.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblPetunjuk.setForeground(new Color(100, 116, 139));

        toolbar.add(btnPrint);
        toolbar.add(btnTutup);
        toolbar.add(lblPetunjuk);
        root.add(toolbar, BorderLayout.NORTH);

        // 2. Area Pratinjau Kartu (Center)
        JPanel backgroundArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 25));
        backgroundArea.setBackground(new Color(226, 232, 240));

        kartuPanel = new JPanel();
        kartuPanel.setLayout(new BorderLayout(0, 8));
        kartuPanel.setBackground(Color.WHITE);
        kartuPanel.setPreferredSize(new Dimension(560, 350));
        kartuPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(30, 41, 59), 2, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        bangunTampilanKartu();
        backgroundArea.add(kartuPanel);

        JScrollPane scrollPane = new JScrollPane(backgroundArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        root.add(scrollPane, BorderLayout.CENTER);

        add(root);

        // Action Handlers
        btnPrint.addActionListener(e -> cetakKartu());
        btnTutup.addActionListener(e -> dispose());
    }

    private void bangunTampilanKartu() {
        kartuPanel.removeAll();

        // A. HEADER KARTU (Kop Resmi Kemhan RI)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblInstansi1 = new JLabel("KEMENTERIAN PERTAHANAN REPUBLIK INDONESIA", SwingConstants.CENTER);
        lblInstansi1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInstansi1.setForeground(new Color(15, 23, 42));
        lblInstansi1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInstansi2 = new JLabel("PERPUSTAKAAN KEMENTERIAN PERTAHANAN RI", SwingConstants.CENTER);
        lblInstansi2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInstansi2.setForeground(UIConstants.PRIMARY_COLOR);
        lblInstansi2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAlamat = new JLabel("Jl. Medan Merdeka Barat No. 13-14, Jakarta Pusat 10110 | kemhan.go.id", SwingConstants.CENTER);
        lblAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblAlamat.setForeground(new Color(100, 116, 139));
        lblAlamat.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblInstansi1);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblInstansi2);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblAlamat);
        headerPanel.add(Box.createVerticalStrut(6));

        // Garis Pembatas Emas / Navy
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(30, 41, 59));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        headerPanel.add(sep);
        headerPanel.add(Box.createVerticalStrut(4));

        JLabel lblJudulKartu = new JLabel("KARTU TANDA ANGGOTA PERPUSTAKAAN (KTA)", SwingConstants.CENTER);
        lblJudulKartu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblJudulKartu.setForeground(new Color(30, 41, 59));
        lblJudulKartu.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblJudulKartu);

        kartuPanel.add(headerPanel, BorderLayout.NORTH);

        // B. BODY KARTU (Foto & Biodata Personel)
        JPanel bodyPanel = new JPanel(new BorderLayout(14, 0));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(6, 4, 6, 4));

        // Sisi Kiri: Foto Profil Placeholder / Badge
        JPanel fotoPanel = new JPanel();
        fotoPanel.setLayout(new BoxLayout(fotoPanel, BoxLayout.Y_AXIS));
        fotoPanel.setOpaque(false);
        fotoPanel.setPreferredSize(new Dimension(105, 135));

        JPanel fotoBox = new JPanel(new BorderLayout());
        fotoBox.setBackground(new Color(241, 245, 249));
        fotoBox.setBorder(new LineBorder(new Color(148, 163, 184), 1));
        fotoBox.setPreferredSize(new Dimension(95, 115));
        fotoBox.setMaximumSize(new Dimension(95, 115));

        JLabel lblFotoIcon = new JLabel("PAS FOTO", SwingConstants.CENTER);
        lblFotoIcon.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblFotoIcon.setForeground(new Color(100, 116, 139));
        fotoBox.add(lblFotoIcon, BorderLayout.CENTER);

        JLabel lblBadgeStatus = new JLabel("● AKTIF", SwingConstants.CENTER);
        lblBadgeStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadgeStatus.setOpaque(true);
        lblBadgeStatus.setBackground(new Color(220, 252, 231));
        lblBadgeStatus.setForeground(new Color(22, 101, 52));
        lblBadgeStatus.setBorder(new LineBorder(new Color(134, 239, 172), 1));
        lblBadgeStatus.setPreferredSize(new Dimension(95, 20));
        lblBadgeStatus.setMaximumSize(new Dimension(95, 20));
        lblBadgeStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        fotoPanel.add(fotoBox);
        fotoPanel.add(Box.createVerticalStrut(4));
        fotoPanel.add(lblBadgeStatus);
        bodyPanel.add(fotoPanel, BorderLayout.WEST);

        // Sisi Kanan: Biodata Personel Kemhan RI
        JPanel bioPanel = new JPanel(new GridBagLayout());
        bioPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);

        int row = 0;
        addBioRow(bioPanel, gbc, row++, "NIP / NRP", anggota.getNim(), true);
        addBioRow(bioPanel, gbc, row++, "Nama Personel", anggota.getNama(), true);
        addBioRow(bioPanel, gbc, row++, "Satker / Divisi", anggota.getJurusan(), false);
        addBioRow(bioPanel, gbc, row++, "No. Kontak", anggota.getNoTelp() != null ? anggota.getNoTelp() : "-", false);
        addBioRow(bioPanel, gbc, row++, "Email Dinas", anggota.getEmail() != null ? anggota.getEmail() : "-", false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        addBioRow(bioPanel, gbc, row++, "Tgl Terbit", sdf.format(new Date()), false);

        bodyPanel.add(bioPanel, BorderLayout.CENTER);
        kartuPanel.add(bodyPanel, BorderLayout.CENTER);

        // C. FOOTER KARTU (Barcode Visual & Tanda Tangan Pejabat)
        JPanel footerPanel = new JPanel(new BorderLayout(8, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(4, 4, 0, 4)
        ));

        // Sisi Kiri Bawah: Barcode Visual & Tata Tertib
        JPanel barcodePanel = new JPanel();
        barcodePanel.setLayout(new BoxLayout(barcodePanel, BoxLayout.Y_AXIS));
        barcodePanel.setOpaque(false);

        JLabel lblBarcodeVisual = new JLabel("||||||| | ||||| |||| |||||| || |||||||||||||", SwingConstants.LEFT);
        lblBarcodeVisual.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblBarcodeVisual.setForeground(Color.DARK_GRAY);

        JLabel lblBarcodeKode = new JLabel("* KTA-KEMHAN-" + anggota.getNim() + " *", SwingConstants.LEFT);
        lblBarcodeKode.setFont(new Font("Monospaced", Font.BOLD, 9));
        lblBarcodeKode.setForeground(new Color(71, 85, 105));

        JLabel lblNotice = new JLabel("<html><i>* Tanda pengenal sah operasional sirkulasi Perpustakaan Kemhan RI.</i></html>");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblNotice.setForeground(new Color(100, 116, 139));

        barcodePanel.add(lblBarcodeVisual);
        barcodePanel.add(lblBarcodeKode);
        barcodePanel.add(Box.createVerticalStrut(2));
        barcodePanel.add(lblNotice);
        footerPanel.add(barcodePanel, BorderLayout.WEST);

        // Sisi Kanan Bawah: Tanda Tangan Pejabat Pengesah
        JPanel ttdPanel = new JPanel();
        ttdPanel.setLayout(new BoxLayout(ttdPanel, BoxLayout.Y_AXIS));
        ttdPanel.setOpaque(false);

        JLabel lblTtdKota = new JLabel("Jakarta, " + sdf.format(new Date()), SwingConstants.RIGHT);
        lblTtdKota.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblTtdKota.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblTtdJabatan = new JLabel("Kepala Perpustakaan Kemhan RI,", SwingConstants.RIGHT);
        lblTtdJabatan.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTtdJabatan.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblTtdNama = new JLabel("Kolonel Inf. Dr. Suryanto, M.Si.", SwingConstants.RIGHT);
        lblTtdNama.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTtdNama.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblTtdNip = new JLabel("NIP. 19750812 200003 1 001", SwingConstants.RIGHT);
        lblTtdNip.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblTtdNip.setAlignmentX(Component.RIGHT_ALIGNMENT);

        ttdPanel.add(lblTtdKota);
        ttdPanel.add(lblTtdJabatan);
        ttdPanel.add(Box.createVerticalStrut(18)); // Space tanda tangan
        ttdPanel.add(lblTtdNama);
        ttdPanel.add(lblTtdNip);
        footerPanel.add(ttdPanel, BorderLayout.EAST);

        kartuPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void addBioRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value, boolean isBold) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.32;
        JLabel lbl = new JLabel(label + " :");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(71, 85, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.68;
        JLabel val = new JLabel(value != null && !value.isEmpty() ? value : "-");
        val.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 11));
        val.setForeground(isBold ? new Color(15, 23, 42) : new Color(30, 41, 59));
        panel.add(val, gbc);
    }

    private void cetakKartu() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);

        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, 
                        "Kartu Tanda Anggota (KTA) berhasil dikirim ke printer / tersimpan sebagai PDF!\n" +
                        "Dokumen digital siap dikirimkan kepada anggota via WhatsApp atau Email.", 
                        "Cetak Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage(), "Kesalahan Pencetakan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        // Skala agar presisi di tengah kertas cetak
        double panelWidth = kartuPanel.getWidth();
        double panelHeight = kartuPanel.getHeight();
        double pageWidth = pf.getImageableWidth();
        double pageHeight = pf.getImageableHeight();

        double scaleX = pageWidth / panelWidth;
        double scaleY = pageHeight / panelHeight;
        double scale = Math.min(scaleX, scaleY);

        g2d.scale(scale, scale);
        kartuPanel.printAll(g2d);

        return PAGE_EXISTS;
    }
}
