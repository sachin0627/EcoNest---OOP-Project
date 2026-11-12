package com.econest.view;

import com.econest.dao.UserDAO;
import com.econest.model.Admin;
import com.econest.model.Staff;
import com.econest.model.User;
import com.econest.util.DatabaseConnection;
import com.econest.util.EnvConfig;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * View panel for System Settings & User Management (Role-Based Access Control).
 */
public class SettingsPanel extends JPanel {
    private final User currentUser;
    private final UserDAO userDAO;

    private JTable userTable;
    private DefaultTableModel userTableModel;

    public SettingsPanel(User currentUser) {
        this.currentUser = currentUser;
        this.userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        // Tab 1: System & Environment Information
        JPanel sysPanel = UITheme.createCardPanel();
        sysPanel.setLayout(new GridLayout(6, 2, 12, 12));

        sysPanel.add(new JLabel("Database Status:"));
        boolean dbOnline = DatabaseConnection.getInstance().testConnection();
        JLabel lblDbStatus = new JLabel(dbOnline ? "✔ Connected to MySQL Server" : "✖ Offline / Unreachable");
        lblDbStatus.setForeground(dbOnline ? UITheme.COLOR_SUCCESS : UITheme.COLOR_DANGER);
        lblDbStatus.setFont(UITheme.FONT_BODY_BOLD);
        sysPanel.add(lblDbStatus);

        sysPanel.add(new JLabel("Database Host & Port:"));
        sysPanel.add(new JLabel(EnvConfig.get("DB_HOST", "localhost") + ":" + EnvConfig.get("DB_PORT", "3306")));

        sysPanel.add(new JLabel("Database Name:"));
        sysPanel.add(new JLabel(EnvConfig.get("DB_NAME", "econest_db")));

        sysPanel.add(new JLabel("Active User:"));
        sysPanel.add(new JLabel(currentUser != null ? currentUser.getFullName() + " (" + currentUser.getRole() + ")" : "Guest"));

        sysPanel.add(new JLabel("Role Privileges:"));
        sysPanel.add(new JLabel(currentUser != null ? currentUser.getRolePermissions() : "N/A"));

        sysPanel.add(new JLabel("SMTP Email Service:"));
        String smtpUser = EnvConfig.get("SMTP_USERNAME", "");
        sysPanel.add(new JLabel(smtpUser.isEmpty() ? "Disabled (Configured via .env)" : "Active (" + smtpUser + ")"));

        tabbedPane.addTab("System Configuration", sysPanel);

        // Tab 2: User Accounts (Admin Only)
        if (currentUser instanceof Admin) {
            JPanel userPanel = UITheme.createCardPanel();
            userPanel.setLayout(new BorderLayout(0, 10));

            JPanel topUserBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            topUserBar.setOpaque(false);

            JButton btnAddUser = UITheme.createPrimaryButton("+ Add New User");
            btnAddUser.addActionListener(e -> openAddUserDialog());

            JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
            btnRefresh.addActionListener(e -> loadUsers());

            topUserBar.add(btnAddUser);
            topUserBar.add(btnRefresh);
            userPanel.add(topUserBar, BorderLayout.NORTH);

            String[] userCols = {"User ID", "Username", "Full Name", "Email", "Role", "Phone", "Status"};
            userTableModel = new DefaultTableModel(userCols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            userTable = new JTable(userTableModel);
            UITheme.styleTable(userTable);

            JScrollPane scrollUser = new JScrollPane(userTable);
            scrollUser.setBorder(BorderFactory.createEmptyBorder());
            userPanel.add(scrollUser, BorderLayout.CENTER);

            tabbedPane.addTab("User Accounts (Admin)", userPanel);
            loadUsers();
        }

        // Tab 3: About EcoNest
        JPanel aboutPanel = UITheme.createCardPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));

        JLabel lblAboutTitle = new JLabel("EcoNest – Smart Home & Garden Retail Management System");
        lblAboutTitle.setFont(UITheme.FONT_SUBTITLE);
        lblAboutTitle.setForeground(UITheme.COLOR_PRIMARY);

        JTextArea txtAbout = new JTextArea(
            "EcoNest Retail Management System is a comprehensive, production-grade desktop enterprise application\n" +
            "developed using Java Swing, MySQL JDBC, and clean layered Object-Oriented Architecture.\n\n" +
            "Key Architectural Highlights:\n" +
            "• Demonstrates Core Java OOP: Encapsulation, Inheritance, Polymorphism, and Abstraction.\n" +
            "• Business Interfaces: Manageable, Reportable, Notifiable.\n" +
            "• Robust Relational Persistence: Normalized MySQL database (econest_db) with PreparedStatements and ACID Transactions.\n" +
            "• Automated Notifications: Jakarta Mail integration with branded HTML templates and database logging.\n" +
            "• Analytics & Reporting: JFreeChart graphical charts and exportable PDF / CSV reports.\n\n" +
            "Developed for Higher Diploma in Information Technology (HDIT) OOP Group Project 2026."
        );
        txtAbout.setEditable(false);
        txtAbout.setOpaque(false);
        txtAbout.setFont(UITheme.FONT_BODY);

        aboutPanel.add(lblAboutTitle);
        aboutPanel.add(Box.createVerticalStrut(12));
        aboutPanel.add(txtAbout);
        tabbedPane.addTab("About & Architecture", aboutPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadUsers() {
        if (userTableModel == null) return;
        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getRole(),
                u.getPhone(),
                u.getStatus()
            });
        }
    }

    private void openAddUserDialog() {
        JTextField txtUsername = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtFullName = new JTextField();
        JTextField txtEmail = new JTextField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        JTextField txtPhone = new JTextField("+94 ");

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Username:"));
        form.add(txtUsername);
        form.add(new JLabel("Password:"));
        form.add(txtPass);
        form.add(new JLabel("Full Name:"));
        form.add(txtFullName);
        form.add(new JLabel("Email Address:"));
        form.add(txtEmail);
        form.add(new JLabel("System Role:"));
        form.add(cmbRole);
        form.add(new JLabel("Phone Number:"));
        form.add(txtPhone);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New User Account", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String u = txtUsername.getText().trim();
            String p = new String(txtPass.getPassword());
            String f = txtFullName.getText().trim();
            String e = txtEmail.getText().trim();
            String r = (String) cmbRole.getSelectedItem();
            String ph = txtPhone.getText().trim();

            if (u.isEmpty() || p.isEmpty() || f.isEmpty() || e.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newUser = "ADMIN".equalsIgnoreCase(r) ? new Admin() : new Staff();
            newUser.setUsername(u);
            newUser.setFullName(f);
            newUser.setEmail(e);
            newUser.setRole(r);
            newUser.setPhone(ph);
            newUser.setStatus("ACTIVE");

            boolean ok = userDAO.addUser(newUser, p);
            if (ok) {
                JOptionPane.showMessageDialog(this, "User " + u + " created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
