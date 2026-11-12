package com.econest.view;

import com.econest.dao.NotificationDAO;
import com.econest.model.EmailNotification;
import com.econest.service.EmailService;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * View panel for Module 08 & 09: Automated Email Notifications Audit Log.
 */
public class NotificationPanel extends JPanel {
    private final NotificationDAO dao;
    private final EmailService emailService;
    private JTable table;
    private DefaultTableModel tableModel;

    public NotificationPanel() {
        this.dao = new NotificationDAO();
        this.emailService = new EmailService();
        initUI();
        loadNotifications();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Control Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout(14, 0));

        JLabel lblTitle = new JLabel("Automated Email Notifications Audit Log (Client & Agent Dispatch)");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(UITheme.COLOR_PRIMARY);
        topBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnViewEmail = UITheme.createSecondaryButton("View Email Message");
        btnViewEmail.addActionListener(e -> viewSelectedEmail());

        JButton btnTestEmail = UITheme.createAccentButton("Send Test Notification");
        btnTestEmail.addActionListener(e -> sendTestEmail());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadNotifications());

        actionPanel.add(btnViewEmail);
        actionPanel.add(btnTestEmail);
        actionPanel.add(btnRefresh);
        topBar.add(actionPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"Log ID", "Recipient Name", "Recipient Email", "Category", "Subject", "Reference ID", "Status", "Sent Date / Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadNotifications() {
        tableModel.setRowCount(0);
        List<EmailNotification> list = dao.getRecentNotifications(50);
        for (EmailNotification n : list) {
            tableModel.addRow(new Object[]{
                n.getNotificationId(),
                n.getRecipientName(),
                n.getRecipientEmail(),
                n.getRecipientType(),
                n.getSubject(),
                n.getReferenceId() != null ? n.getReferenceId() : "-",
                n.getStatus(),
                UITheme.formatDateTime(n.getSentAt())
            });
        }
    }

    private void viewSelectedEmail() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an email log to view its content.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int logId = (Integer) tableModel.getValueAt(row, 0);
        List<EmailNotification> list = dao.getRecentNotifications(50);
        EmailNotification target = null;
        for (EmailNotification n : list) {
            if (n.getNotificationId() == logId) {
                target = n;
                break;
            }
        }

        if (target != null) {
            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Email Content: " + target.getSubject(), true);
            dlg.setSize(600, 500);
            dlg.setLocationRelativeTo(this);

            JEditorPane editor = new JEditorPane("text/html", target.getContent());
            editor.setEditable(false);

            JPanel pnl = new JPanel(new BorderLayout(10, 10));
            pnl.setBorder(new EmptyBorder(16, 16, 16, 16));
            pnl.add(new JScrollPane(editor), BorderLayout.CENTER);

            JButton btnClose = UITheme.createPrimaryButton("Close");
            btnClose.addActionListener(e -> dlg.dispose());
            JPanel bPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bPnl.add(btnClose);
            pnl.add(bPnl, BorderLayout.SOUTH);

            dlg.add(pnl);
            dlg.setVisible(true);
        }
    }

    private void sendTestEmail() {
        String testEmail = JOptionPane.showInputDialog(
            this,
            "Enter recipient email to send an EcoNest test notification:",
            "Test Email Dispatch",
            JOptionPane.QUESTION_MESSAGE
        );

        if (testEmail != null && !testEmail.trim().isEmpty()) {
            new Thread(() -> {
                com.econest.model.Order testOrder = new com.econest.model.Order();
                testOrder.setOrderId("ORD-TEST");
                testOrder.setTotalAmount(5200.0);
                testOrder.setDeliveryAddress("88, Green Park Way, Colombo 03");

                boolean ok = emailService.sendOrderDispatchedEmail(testOrder, testEmail.trim(), "EcoNest Valued Client", "Test delivery dispatch verification");
                SwingUtilities.invokeLater(() -> {
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Test email dispatched successfully to " + testEmail + "!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Email delivery failed or credentials not configured in .env.\n" +
                            "Audit entry was recorded in database with status PENDING/FAILED.",
                            "Email Status", JOptionPane.WARNING_MESSAGE);
                    }
                    loadNotifications();
                });
            }).start();
        }
    }
}
