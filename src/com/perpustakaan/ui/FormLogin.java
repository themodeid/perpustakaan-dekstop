package com.perpustakaan.ui;

import com.perpustakaan.service.LayananAutentikasi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * NAMA FILE : FormLogin.java
 * FUNGSI    : Dialog antarmuka login bagi Administrator dan Petugas perpustakaan.
 */
public class FormLogin extends JDialog {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private boolean loginSuccessful = false;

    public FormLogin(Frame parent) {
        super(parent, "SIPerpus - Login Petugas & Administrator", true);
        setSize(420, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.BG_LIGHT);

        // Header Panel (Logo Resmi Kemhan RI & Title)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        ImageIcon logoLogin = UIConstants.loadLogoKemhanIcon(56, 56);
        if (logoLogin != null) {
            JLabel lblLogo = new JLabel(logoLogin);
            lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerPanel.add(lblLogo);
            headerPanel.add(Box.createVerticalStrut(8));
        }

        JLabel lblTitle = new JLabel("SIPerpus Desktop", SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Perpustakaan Kementerian Pertahanan RI", SwingConstants.CENTER);
        lblSub.setFont(UIConstants.FONT_REGULAR);
        lblSub.setForeground(new Color(225, 238, 255));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblSub);

        // Form Card Panel
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 30, 25, 30),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UIConstants.FONT_BOLD);
        formCard.add(lblUser, gbc);

        gbc.gridy = 1;
        txtUsername = new JTextField(18);
        txtUsername.setFont(UIConstants.FONT_REGULAR);
        txtUsername.setPreferredSize(new Dimension(200, 36));
        formCard.add(txtUsername, gbc);

        // Password
        gbc.gridy = 2;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UIConstants.FONT_BOLD);
        formCard.add(lblPass, gbc);

        gbc.gridy = 3;
        txtPassword = new JPasswordField(18);
        txtPassword.setFont(UIConstants.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(200, 36));
        formCard.add(txtPassword, gbc);

        // Tombol Login
        gbc.gridy = 4;
        gbc.insets = new Insets(18, 4, 10, 4);
        btnLogin = UIConstants.createStyledButton("Masuk ke Sistem", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(200, 40));
        formCard.add(btnLogin, gbc);

        // Status / Info
        gbc.gridy = 5;
        lblStatus = new JLabel("Default: admin / admin123  |  petugas / petugas123", SwingConstants.CENTER);
        lblStatus.setFont(UIConstants.FONT_SMALL);
        lblStatus.setForeground(Color.GRAY);
        formCard.add(lblStatus, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formCard, BorderLayout.CENTER);
        add(mainPanel);

        // Event Listeners
        btnLogin.addActionListener(e -> performLogin());

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Silakan isi username dan password!", 
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Memeriksa...");

        SwingUtilities.invokeLater(() -> {
            try {
                boolean success = LayananAutentikasi.getInstance().login(username, password);
                if (success) {
                    loginSuccessful = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                            "Username atau password salah!", 
                            "Login Gagal", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                        "Koneksi ke Database MySQL gagal!\n\n" +
                        "Pastikan Docker MySQL sudah berjalan (docker compose up -d).\nDetail: " + ex.getMessage(), 
                        "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
            } finally {
                btnLogin.setEnabled(true);
                btnLogin.setText("Masuk ke Sistem");
            }
        });
    }
}
