package com.perpustakaan.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * NAMA FILE : DialogCetakLaporan.java
 * FUNGSI    : Jendela pratinjau lembar cetak formal ukuran A4 (Kop Surat Resmi, Tabel, Tanda Tangan, dan Cetak ke Printer/PDF).
 */
public class DialogCetakLaporan extends JDialog implements Printable {

    private final String judulDokumen;
    private final String subJudul;
    private final String[] kolomHeader;
    private final int[] lebarKolom;
    private final List<String[]> barisData;
    private final String ringkasanTotal;
    private final boolean adaTandaTangan;

    private JPanel lembarKertasA4;

    public DialogCetakLaporan(Window parent, String judulDokumen, String subJudul,
                              String[] kolomHeader, int[] lebarKolom, List<String[]> barisData,
                              String ringkasanTotal, boolean adaTandaTangan) {
        super(parent, "Pratinjau Cetak Laporan Formal (A4)", ModalityType.APPLICATION_MODAL);
        this.judulDokumen = judulDokumen;
        this.subJudul = subJudul;
        this.kolomHeader = kolomHeader;
        this.lebarKolom = lebarKolom;
        this.barisData = barisData;
        this.ringkasanTotal = ringkasanTotal;
        this.adaTandaTangan = adaTandaTangan;

        setSize(880, 820);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(230, 235, 243));

        // Toolbar Atas
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

        JButton btnPrint = UIConstants.createStyledButton("Cetak Dokumen / Print (Ctrl+P)", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnPrint.setFont(UIConstants.FONT_BOLD);
        btnPrint.setPreferredSize(new Dimension(230, 36));

        JButton btnTutup = UIConstants.createStyledButton("Tutup Pratinjau", Color.LIGHT_GRAY, Color.BLACK);
        btnTutup.setPreferredSize(new Dimension(130, 36));

        toolbar.add(btnPrint);
        toolbar.add(btnTutup);
        root.add(toolbar, BorderLayout.NORTH);

        // Kertas A4 Dokumen di Area Scroll
        JPanel backgroundArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        backgroundArea.setBackground(new Color(226, 232, 240));

        lembarKertasA4 = new JPanel();
        lembarKertasA4.setLayout(new BoxLayout(lembarKertasA4, BoxLayout.Y_AXIS));
        lembarKertasA4.setBackground(Color.WHITE);
        lembarKertasA4.setPreferredSize(new Dimension(720, Math.max(960, 250 + (barisData.size() * 26) + 180)));
        lembarKertasA4.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 190, 205), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        bangunDokumenKertas();
        backgroundArea.add(lembarKertasA4);

        JScrollPane scrollPane = new JScrollPane(backgroundArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        root.add(scrollPane, BorderLayout.CENTER);

        add(root);

        // Action Handlers
        btnPrint.addActionListener(e -> cetakDokumenKePrinter());
        btnTutup.addActionListener(e -> dispose());
    }

    private void bangunDokumenKertas() {
        lembarKertasA4.removeAll();

        // 1. KOP SURAT RESMI (Rapat & Proporsional, Anti-Stretching)
        JPanel kopPanel = new JPanel();
        kopPanel.setLayout(new BoxLayout(kopPanel, BoxLayout.Y_AXIS));
        kopPanel.setOpaque(false);

        JLabel lblKop0 = new JLabel("KEMENTERIAN PERTAHANAN REPUBLIK INDONESIA", SwingConstants.CENTER);
        lblKop0.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKop0.setForeground(new Color(15, 23, 42));
        lblKop0.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblKop1 = new JLabel("PERPUSTAKAAN KEMENTERIAN PERTAHANAN RI", SwingConstants.CENTER);
        lblKop1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKop1.setForeground(UIConstants.PRIMARY_COLOR);
        lblKop1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblKop2 = new JLabel("Jl. Medan Merdeka Barat No. 13-14, Jakarta Pusat 10110 | Telp: (021) 3829460", SwingConstants.CENTER);
        lblKop2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblKop2.setForeground(new Color(71, 85, 105));
        lblKop2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblKop3 = new JLabel("Laman Resmi: www.kemhan.go.id | Email: perpustakaan@kemhan.go.id", SwingConstants.CENTER);
        lblKop3.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblKop3.setForeground(new Color(71, 85, 105));
        lblKop3.setAlignmentX(Component.CENTER_ALIGNMENT);

        kopPanel.add(lblKop0);
        kopPanel.add(Box.createVerticalStrut(2));
        kopPanel.add(lblKop1);
        kopPanel.add(Box.createVerticalStrut(2));
        kopPanel.add(lblKop2);
        kopPanel.add(Box.createVerticalStrut(1));
        kopPanel.add(lblKop3);
        kopPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        lembarKertasA4.add(kopPanel);

        // Garis Ganda Pemisah Kop
        lembarKertasA4.add(Box.createVerticalStrut(8));
        JSeparator line1 = new JSeparator();
        line1.setForeground(Color.BLACK);
        line1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        lembarKertasA4.add(line1);
        lembarKertasA4.add(Box.createVerticalStrut(2));

        JSeparator line2 = new JSeparator();
        line2.setForeground(Color.DARK_GRAY);
        line2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        lembarKertasA4.add(line2);
        lembarKertasA4.add(Box.createVerticalStrut(14));

        // 2. JUDUL DOKUMEN & SUB-JUDUL
        JLabel lblJudul = new JLabel(judulDokumen, SwingConstants.CENTER);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblJudul.setAlignmentX(Component.CENTER_ALIGNMENT);
        lembarKertasA4.add(lblJudul);

        if (subJudul != null && !subJudul.isEmpty()) {
            lembarKertasA4.add(Box.createVerticalStrut(4));
            JLabel lblSub = new JLabel(subJudul, SwingConstants.CENTER);
            lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblSub.setForeground(new Color(100, 116, 139));
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
            lembarKertasA4.add(lblSub);
        }
        lembarKertasA4.add(Box.createVerticalStrut(14));

        // 3. TABEL DATA RESMI
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setOpaque(false);

        // Header Tabel
        JPanel headerRow = new JPanel(new GridBagLayout());
        headerRow.setBackground(new Color(226, 232, 240));
        headerRow.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 6, 4, 6);

        for (int i = 0; i < kolomHeader.length; i++) {
            gbc.gridx = i;
            gbc.weightx = (lebarKolom != null && i < lebarKolom.length) ? lebarKolom[i] : 1.0;
            JLabel th = new JLabel(kolomHeader[i]);
            th.setFont(new Font("Segoe UI", Font.BOLD, 10));
            if (i == 0 || kolomHeader[i].equalsIgnoreCase("Rak") || kolomHeader[i].equalsIgnoreCase("Stok") || kolomHeader[i].equalsIgnoreCase("Status")) {
                th.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (kolomHeader[i].toLowerCase().contains("denda")) {
                th.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            headerRow.add(th, gbc);
        }
        tablePanel.add(headerRow);

        // Baris Data Tabel
        for (int r = 0; r < barisData.size(); r++) {
            String[] rowVals = barisData.get(r);
            JPanel rowPanel = new JPanel(new GridBagLayout());
            rowPanel.setOpaque(false);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            rowPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(203, 213, 225)));

            for (int c = 0; c < rowVals.length; c++) {
                gbc.gridx = c;
                gbc.weightx = (lebarKolom != null && c < lebarKolom.length) ? lebarKolom[c] : 1.0;
                JLabel td = new JLabel(rowVals[c]);
                td.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                if (c == 0 || (kolomHeader.length > c && (kolomHeader[c].equalsIgnoreCase("Rak") || kolomHeader[c].equalsIgnoreCase("Stok") || kolomHeader[c].equalsIgnoreCase("Status")))) {
                    td.setHorizontalAlignment(SwingConstants.CENTER);
                } else if (kolomHeader.length > c && kolomHeader[c].toLowerCase().contains("denda")) {
                    td.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                rowPanel.add(td, gbc);
            }
            tablePanel.add(rowPanel);
        }

        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28 + (barisData.size() * 24)));
        lembarKertasA4.add(tablePanel);

        // 4. RINGKASAN TOTAL
        if (ringkasanTotal != null && !ringkasanTotal.isEmpty()) {
            lembarKertasA4.add(Box.createVerticalStrut(10));
            JPanel summaryPanel = new JPanel(new BorderLayout());
            summaryPanel.setBackground(new Color(241, 245, 249));
            summaryPanel.setBorder(new CompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

            JLabel lblSum = new JLabel(ringkasanTotal);
            lblSum.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblSum.setForeground(new Color(30, 41, 59));
            summaryPanel.add(lblSum, BorderLayout.WEST);
            lembarKertasA4.add(summaryPanel);
        }

        // Spacer fleksibel: Menyerap seluruh sisa ruang vertikal kertas
        lembarKertasA4.add(Box.createVerticalGlue());
        lembarKertasA4.add(Box.createVerticalStrut(20));

        // 5. TANDA TANGAN FORMAL
        if (adaTandaTangan) {
            JPanel ttdContainer = new JPanel(new BorderLayout());
            ttdContainer.setOpaque(false);
            ttdContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            String tglCetak = sdf.format(new Date());

            // Tanda Tangan Kiri: Petugas Perpustakaan
            JPanel ttdKiri = new JPanel();
            ttdKiri.setLayout(new BoxLayout(ttdKiri, BoxLayout.Y_AXIS));
            ttdKiri.setOpaque(false);
            ttdKiri.setPreferredSize(new Dimension(220, 110));

            JLabel lblPetugas = new JLabel("Petugas Perpustakaan,", SwingConstants.CENTER);
            lblPetugas.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblPetugas.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            JLabel lblGarisKiri = new JLabel("______________________", SwingConstants.CENTER);
            lblGarisKiri.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblNamaKiri = new JLabel("( Staf Pelayanan )", SwingConstants.CENTER);
            lblNamaKiri.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblNamaKiri.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            ttdKiri.add(lblPetugas);
            ttdKiri.add(Box.createVerticalStrut(40));
            ttdKiri.add(lblGarisKiri);
            ttdKiri.add(Box.createVerticalStrut(4));
            ttdKiri.add(lblNamaKiri);

            // Tanda Tangan Kanan: Kepala Perpustakaan
            JPanel ttdKanan = new JPanel();
            ttdKanan.setLayout(new BoxLayout(ttdKanan, BoxLayout.Y_AXIS));
            ttdKanan.setOpaque(false);
            ttdKanan.setPreferredSize(new Dimension(240, 110));

            JLabel lblTgl = new JLabel("Jakarta, " + tglCetak, SwingConstants.CENTER);
            lblTgl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblTgl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            JLabel lblKepala = new JLabel("Mengetahui, Kepala Perpustakaan Kemhan RI", SwingConstants.CENTER);
            lblKepala.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblKepala.setFont(new Font("Segoe UI", Font.BOLD, 11));

            JLabel lblGarisKanan = new JLabel("Kolonel Inf. Dr. Suryanto, M.Si.", SwingConstants.CENTER);
            lblGarisKanan.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblGarisKanan.setFont(new Font("Segoe UI", Font.BOLD, 11));

            JLabel lblNip = new JLabel("NIP. 19750812 200003 1 001", SwingConstants.CENTER);
            lblNip.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblNip.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            ttdKanan.add(lblTgl);
            ttdKanan.add(Box.createVerticalStrut(2));
            ttdKanan.add(lblKepala);
            ttdKanan.add(Box.createVerticalStrut(38));
            ttdKanan.add(lblGarisKanan);
            ttdKanan.add(Box.createVerticalStrut(4));
            ttdKanan.add(lblNip);

            ttdContainer.add(ttdKiri, BorderLayout.WEST);
            ttdContainer.add(ttdKanan, BorderLayout.EAST);
            lembarKertasA4.add(ttdContainer);
        }
    }

    private void cetakDokumenKePrinter() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);

        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Perintah cetak berhasil dikirim ke printer/PDF!", "Cetak Sukses", JOptionPane.INFORMATION_MESSAGE);
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

        // Skala agar pas di batas cetak halaman A4
        double panelWidth = lembarKertasA4.getWidth();
        double panelHeight = lembarKertasA4.getHeight();
        double pageWidth = pf.getImageableWidth();
        double pageHeight = pf.getImageableHeight();

        double scaleX = pageWidth / panelWidth;
        double scaleY = pageHeight / panelHeight;
        double scale = Math.min(scaleX, scaleY);

        g2d.scale(scale, scale);
        lembarKertasA4.printAll(g2d);

        return PAGE_EXISTS;
    }
}
