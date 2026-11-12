package com.econest.view;

import com.econest.controller.ClientController;
import com.econest.dao.OrderDAO;
import com.econest.model.Client;
import com.econest.model.Order;
import com.econest.util.CSVUtil;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * View panel for Module 02: Client Management.
 */
public class ClientPanel extends JPanel {
    private final ClientController controller;
    private final OrderDAO orderDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbType;

    public ClientPanel() {
        this.controller = new ClientController();
        this.orderDAO = new OrderDAO();
        initUI();
        loadClients();
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

        JLabel lblType = new JLabel("Type:");
        lblType.setFont(UITheme.FONT_BODY_BOLD);
        String[] types = {"All Types", "Household", "Office"};
        cmbType = new JComboBox<>(types);
        cmbType.setFont(UITheme.FONT_BODY);

        JButton btnSearch = UITheme.createSecondaryButton("Filter");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbType.setSelectedIndex(0);
            loadClients();
        });

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblType);
        filterPanel.add(cmbType);
        filterPanel.add(btnSearch);
        filterPanel.add(btnReset);
        topBar.add(filterPanel, BorderLayout.WEST);

        // Right Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("+ Register Client");
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnEdit = UITheme.createSecondaryButton("Edit");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnHistory = UITheme.createSecondaryButton("Order History");
        btnHistory.addActionListener(e -> viewOrderHistory());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnExport = UITheme.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCSV());

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnHistory);
        actionPanel.add(btnDelete);
        actionPanel.add(btnExport);
        topBar.add(actionPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"Client ID", "Client Name", "Type", "Email Address", "Phone Number", "Address", "City", "Registration Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
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

    public void loadClients() {
        tableModel.setRowCount(0);
        List<Client> clients = controller.getAllClients();
        populateTable(clients);
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String type = (String) cmbType.getSelectedItem();
        tableModel.setRowCount(0);
        List<Client> clients = controller.searchClients(keyword, type);
        populateTable(clients);
    }

    private void populateTable(List<Client> clients) {
        for (Client c : clients) {
            tableModel.addRow(new Object[]{
                c.getClientId(),
                c.getClientName(),
                c.getClientType(),
                c.getEmail(),
                c.getPhone(),
                c.getAddress(),
                c.getCity(),
                UITheme.formatDate(c.getRegistrationDate())
            });
        }
    }

    private void openAddDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        ClientDialog dlg = new ClientDialog((Frame) parent, null, true);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadClients();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a client from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String clientId = (String) tableModel.getValueAt(selectedRow, 0);
        Client client = controller.getClient(clientId);
        if (client != null) {
            Window parent = SwingUtilities.getWindowAncestor(this);
            ClientDialog dlg = new ClientDialog((Frame) parent, client, false);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                loadClients();
            }
        }
    }

    private void viewOrderHistory() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a client to view their order history.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String clientId = (String) tableModel.getValueAt(selectedRow, 0);
        String clientName = (String) tableModel.getValueAt(selectedRow, 1);

        List<Order> allOrders = orderDAO.getAllOrders();
        String[] cols = {"Order ID", "Date", "Items Subtotal", "Discount", "Total Amount", "Status", "Delivery Address"};
        DefaultTableModel historyModel = new DefaultTableModel(cols, 0);

        for (Order o : allOrders) {
            if (o.getClientId().equalsIgnoreCase(clientId)) {
                historyModel.addRow(new Object[]{
                    o.getOrderId(),
                    UITheme.formatDateTime(o.getOrderDate()),
                    UITheme.formatCurrency(o.getSubtotal()),
                    UITheme.formatCurrency(o.getDiscount()),
                    UITheme.formatCurrency(o.getTotalAmount()),
                    o.getOrderStatus(),
                    o.getDeliveryAddress()
                });
            }
        }

        JDialog historyDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Order History - " + clientName + " (" + clientId + ")", true);
        historyDialog.setSize(750, 400);
        historyDialog.setLocationRelativeTo(this);

        JTable historyTable = new JTable(historyModel);
        UITheme.styleTable(historyTable);

        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(new EmptyBorder(16, 16, 16, 16));
        pnl.add(new JLabel("Total Orders Found: " + historyModel.getRowCount()), BorderLayout.NORTH);
        pnl.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JButton btnClose = UITheme.createPrimaryButton("Close");
        btnClose.addActionListener(e -> historyDialog.dispose());
        JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPnl.add(btnClose);
        pnl.add(btnPnl, BorderLayout.SOUTH);

        historyDialog.add(pnl);
        historyDialog.setVisible(true);
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a client from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String clientId = (String) tableModel.getValueAt(selectedRow, 0);
        String clientName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to deactivate client '" + clientName + "' (" + clientId + ")?",
            "Confirm Client Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = controller.deleteClient(clientId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Client deactivated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadClients();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete client.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_Clients.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportTableToCSV(table, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Client registry exported to CSV successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
