package com.econest.view;

import com.econest.controller.DeliveryAgentController;
import com.econest.model.DeliveryAgent;
import com.econest.util.CSVUtil;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * View panel for Module 04: Delivery Agent Management.
 */
public class DeliveryAgentPanel extends JPanel {
    private final DeliveryAgentController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatus;

    public DeliveryAgentPanel() {
        this.controller = new DeliveryAgentController();
        initUI();
        loadAgents();
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

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        txtSearch = new JTextField(15);
        txtSearch.setFont(UITheme.FONT_BODY);

        JLabel lblStatus = new JLabel("Availability:");
        lblStatus.setFont(UITheme.FONT_BODY_BOLD);
        String[] statuses = {"All Statuses", "Available", "Assigned", "Unavailable"};
        cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setFont(UITheme.FONT_BODY);

        JButton btnSearch = UITheme.createSecondaryButton("Filter");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            loadAgents();
        });

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblStatus);
        filterPanel.add(cmbStatus);
        filterPanel.add(btnSearch);
        filterPanel.add(btnReset);
        topBar.add(filterPanel, BorderLayout.WEST);

        // Right Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("+ Add Agent");
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnEdit = UITheme.createSecondaryButton("Edit");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnToggle = UITheme.createAccentButton("Toggle Availability");
        btnToggle.addActionListener(e -> handleToggleAvailability());

        JButton btnDelete = UITheme.createDangerButton("Remove");
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnExport = UITheme.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCSV());

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnToggle);
        actionPanel.add(btnDelete);
        actionPanel.add(btnExport);
        topBar.add(actionPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"Agent ID", "Full Name", "Phone Number", "Email Address", "Vehicle Type", "Vehicle Reg No", "Availability Status"};
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

    public void loadAgents() {
        tableModel.setRowCount(0);
        List<DeliveryAgent> agents = controller.getAllAgents();
        populateTable(agents);
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        tableModel.setRowCount(0);
        List<DeliveryAgent> agents = controller.searchAgents(keyword, status);
        populateTable(agents);
    }

    private void populateTable(List<DeliveryAgent> agents) {
        for (DeliveryAgent a : agents) {
            tableModel.addRow(new Object[]{
                a.getAgentId(),
                a.getFullName(),
                a.getPhone(),
                a.getEmail(),
                a.getVehicleType(),
                a.getVehicleRegNo(),
                a.getAvailabilityStatus()
            });
        }
    }

    private void openAddDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        DeliveryAgentDialog dlg = new DeliveryAgentDialog((Frame) parent, null, true);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadAgents();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an agent from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String agentId = (String) tableModel.getValueAt(selectedRow, 0);
        DeliveryAgent agent = controller.getAgent(agentId);
        if (agent != null) {
            Window parent = SwingUtilities.getWindowAncestor(this);
            DeliveryAgentDialog dlg = new DeliveryAgentDialog((Frame) parent, agent, false);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                loadAgents();
            }
        }
    }

    private void handleToggleAvailability() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an agent to toggle availability.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String agentId = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 6);

        String newStatus;
        if ("Available".equalsIgnoreCase(currentStatus)) {
            newStatus = "Unavailable";
        } else if ("Unavailable".equalsIgnoreCase(currentStatus)) {
            newStatus = "Available";
        } else {
            int conf = JOptionPane.showConfirmDialog(
                this,
                "Agent is currently marked as 'Assigned' to an active delivery. Reset status to 'Available'?",
                "Confirm Status Override",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (conf != JOptionPane.YES_OPTION) return;
            newStatus = "Available";
        }

        controller.saveAgent(new DeliveryAgent(agentId, "", "", "", "", "", newStatus), false);
        // Direct status update
        new com.econest.dao.DeliveryAgentDAO().updateAgentStatus(agentId, newStatus);
        loadAgents();
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an agent from the table to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String agentId = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to deactivate delivery agent '" + name + "' (" + agentId + ")?",
            "Confirm Agent Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = controller.deleteAgent(agentId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Agent removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAgents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete agent.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_DeliveryAgents.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportTableToCSV(table, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Delivery agents exported to CSV successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
