package com.econest.view;

import com.econest.controller.LoginController;
import com.econest.model.User;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern, nature-inspired Login Frame for EcoNest.
 */
public class LoginFrame extends JFrame {
    private final LoginController loginController;
    private JTextField txtIdentifier;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JLabel lblStatus;
    private JButton btnLogin;

    public LoginFrame() {
        this.loginController = new LoginController();
        initUI();
    }

    private void initUI() {
        setTitle("EcoNest – Smart Home & Garden Retail Management System | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.COLOR_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(25, 35, 25, 35));

        // Header Panel (Branding)
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UITheme.COLOR_BACKGROUND);
        brandPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLogo = new JLabel("\uD83C\uDF3F"); // Foliage emoji / Icon
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("EcoNest");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(UITheme.COLOR_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTagline = new JLabel("Smart Home & Garden Retail Management");
        lblTagline.setFont(UITheme.FONT_BODY);
        lblTagline.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblTagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandPanel.add(lblLogo);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(lblTitle);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(lblTagline);

        // Form Card Panel
        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new GridLayout(6, 1, 6, 8));
        cardPanel.setMaximumSize(new Dimension(380, 260));

        JLabel lblUser = new JLabel("Username or Email:");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(UITheme.COLOR_TEXT_MAIN);
        txtIdentifier = new JTextField("admin");
        txtIdentifier.setFont(UITheme.FONT_BODY);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        lblPass.setForeground(UITheme.COLOR_TEXT_MAIN);
        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(UITheme.FONT_BODY);

        // Show/Hide password toggle
        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(UITheme.FONT_SMALL);
        chkShowPassword.setBackground(UITheme.COLOR_CARD_BG);
        chkShowPassword.setForeground(UITheme.COLOR_TEXT_MUTED);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('\u2022');
            }
        });

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.COLOR_DANGER);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        cardPanel.add(lblUser);
        cardPanel.add(txtIdentifier);
        cardPanel.add(lblPass);
        cardPanel.add(txtPassword);
        cardPanel.add(chkShowPassword);
        cardPanel.add(lblStatus);

        // Login Button
        btnLogin = UITheme.createPrimaryButton("Sign In to EcoNest");
        btnLogin.setMaximumSize(new Dimension(370, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> handleLogin());

        // Quick Demo Credentials Buttons (Admin / Staff)
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        demoPanel.setBackground(UITheme.COLOR_BACKGROUND);
        
        JButton btnQuickAdmin = new JButton("Use Admin");
        btnQuickAdmin.setFont(UITheme.FONT_SMALL);
        btnQuickAdmin.setFocusPainted(false);
        btnQuickAdmin.addActionListener(e -> {
            txtIdentifier.setText("admin");
            txtPassword.setText("admin123");
            lblStatus.setText(" ");
        });

        JButton btnQuickStaff = new JButton("Use Staff");
        btnQuickStaff.setFont(UITheme.FONT_SMALL);
        btnQuickStaff.setFocusPainted(false);
        btnQuickStaff.addActionListener(e -> {
            txtIdentifier.setText("staff");
            txtPassword.setText("staff123");
            lblStatus.setText(" ");
        });

        demoPanel.add(new JLabel("Demo Logins:"));
        demoPanel.add(btnQuickAdmin);
        demoPanel.add(btnQuickStaff);

        // Enter key listeners
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        txtIdentifier.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        mainPanel.add(brandPanel);
        mainPanel.add(Box.createVerticalStrut(18));
        mainPanel.add(cardPanel);
        mainPanel.add(Box.createVerticalStrut(16));
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(demoPanel);

        add(mainPanel);
    }

    private void handleLogin() {
        String identifier = txtIdentifier.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter both username and password.");
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblStatus.setText("Authenticating with MySQL...");

        SwingUtilities.invokeLater(() -> {
            try {
                User user = loginController.login(identifier, password);
                if (user != null) {
                    lblStatus.setForeground(UITheme.COLOR_SUCCESS);
                    lblStatus.setText("Login successful! Welcome, " + user.getFullName());
                    
                    // Open Main Dashboard
                    DashboardFrame dashboard = new DashboardFrame(user);
                    dashboard.setVisible(true);
                    dispose();
                } else {
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                    lblStatus.setText("Invalid credentials or inactive account.");
                    btnLogin.setEnabled(true);
                }
            } catch (Exception ex) {
                lblStatus.setForeground(UITheme.COLOR_DANGER);
                lblStatus.setText("Database error: " + ex.getMessage());
                btnLogin.setEnabled(true);
            }
        });
    }
}
