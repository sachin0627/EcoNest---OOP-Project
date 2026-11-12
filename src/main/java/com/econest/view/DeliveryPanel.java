package com.econest.view;

import com.econest.controller.DeliveryController;
import com.econest.model.Delivery;
import com.econest.util.CSVUtil;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * View panel for Module 06: Delivery Agent Assignment & Tracking.
 */
public class DeliveryPanel extends JPanel {
    private final DeliveryController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbStatusFilter;

    public DeliveryPanel() {
        this.controller = new DeliveryController();
        initUI();
        loadDeliveries();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Control Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout(14, 0));

        // Left Filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblStatus = new JLabel("Filter Status:");
        lblStatus.setFont(UITheme.FONT_BODY_BOLD);
        String[] statuses = {"All Deliveries", "Assigned", "Picked Up", "In Transit", "Delivered", "Failed"};
        cmbStatusFilter = new JComboBox<>(statuses);
        cmbStatusFilter.setFont(UITheme.FONT_BODY);
        cmbStatusFilter.addActionListener(e -> filterDeliveries());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadDeliveries());

        filterPanel.add(lblStatus);
        filterPanel.add(cmbStatusFilter);
        filterPanel.add(btnRefresh);
        topBar.add(filterPanel, BorderLayout.WEST);

        // Right Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAssign = UITheme.createPrimaryButton("+ Assign Agent to Order");
        btnAssign.addActionListener(e -> openAssignDialog());

        JButton btnUpdateStatus = UITheme.createAccentButton("Update Delivery Progress");
        btnUpdateStatus.addActionListener(e -> handleUpdateProgress());

        JButton btnExport = UITheme.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCSV());

        actionPanel.add(btnAssign);
        actionPanel.add(btnUpdateStatus);
        actionPanel.add(btnExport);
        topBar.add(actionPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"Delivery ID", "Order ID", "Assigned Agent", "Client Name", "Phone", "Scheduled Date", "Delivery Status", "Destination Address", "Tracking Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadDeliveries() {
        tableModel.setRowCount(0);
        List<Delivery> list = controller.getAllDeliveries();
        populateTable(list);
    }

    private void filterDeliveries() {
        String filter = (String) cmbStatusFilter.getSelectedItem();
        tableModel.setRowCount(0);
        List<Delivery> list = controller.getAllDeliveries();
        for (Delivery d : list) {
            if ("All Deliveries".equalsIgnoreCase(filter) || d.getDeliveryStatus().equalsIgnoreCase(filter)) {
                addRow(d);
            }
        }
    }

    private void populateTable(List<Delivery> list) {
        for (Delivery d : list) {
            addRow(d);
        }
    }

    private void addRow(Delivery d) {
        tableModel.addRow(new Object[]{
            d.getDeliveryId(),
            d.getOrderId(),
            d.getAgentName() != null ? d.getAgentName() : d.getAgentId(),
            d.getClientName() != null ? d.getClientName() : "N/A",
            d.getClientPhone() != null ? d.getClientPhone() : "N/A",
            UITheme.formatDate(d.getDeliveryDate()),
            d.getDeliveryStatus(),
            d.getDeliveryAddress(),
            d.getTrackingNotes() != null ? d.getTrackingNotes() : ""
        });
    }

    private void openAssignDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        AssignDeliveryDialog dlg = new AssignDeliveryDialog((Frame) parent, null);
        dlg.setVisible(true);
        if (dlg.isCompleted()) {
            loadDeliveries();
        }
    }

    private void handleUpdateProgress() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a delivery from the table to update progress.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String deliveryId = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 6);
        String currentNotes = (String) tableModel.getValueAt(selectedRow, 8);

        String[] statuses = {"Picked Up", "In Transit", "Delivered", "Failed"};
        JComboBox<String> cmbNewStatus = new JComboBox<>(statuses);
        cmbNewStatus.setSelectedItem(currentStatus);

        JTextField txtNotes = new JTextField(currentNotes != null ? currentNotes : "", 20);

        JPanel pnl = new JPanel(new GridLayout(3, 2, 8, 8));
        pnl.add(new JLabel("New Delivery Status:"));
        pnl.add(cmbNewStatus);
        pnl.add(new JLabel("Tracking Updates / Notes:"));
        pnl.add(txtNotes);
        pnl.add(new JLabel("Client Email Trigger:"));
        JLabel lblNotice = new JLabel("Auto-sends if 'In Transit'");
        lblNotice.setFont(UITheme.FONT_SMALL);
        lblNotice.setForeground(UITheme.COLOR_PRIMARY);
        pnl.add(lblNotice);

        int result = JOptionPane.showConfirmDialog(
            this,
            pnl,
            "Update Progress: " + deliveryId,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newStatus = (String) cmbNewStatus.getSelectedItem();
            String notes = txtNotes.getText().trim();

            boolean ok = controller.updateStatus(deliveryId, newStatus, notes);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                    "Delivery progress updated to '" + newStatus + "'!\n" +
                    ("In Transit".equalsIgnoreCase(newStatus) ? "Client has been notified via email." : ""),
                    "Updated",
                    JOptionPane.INFORMATION_MESSAGE
                );
                loadDeliveries();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update delivery progress.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_Deliveries.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportTableToCSV(table, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Deliveries exported to CSV successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
