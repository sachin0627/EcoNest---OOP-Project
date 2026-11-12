package com.econest.view;

import com.econest.controller.OrderController;
import com.econest.exception.InsufficientStockException;
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
 * View panel for Module 05: Client Order Management.
 */
public class OrderPanel extends JPanel {
    private final OrderController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatus;

    public OrderPanel() {
        this.controller = new OrderController();
        initUI();
        loadOrders();
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
        txtSearch = new JTextField(14);
        txtSearch.setFont(UITheme.FONT_BODY);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BODY_BOLD);
        String[] statuses = {"All Statuses", "Pending", "Confirmed", "Processing", "Dispatched", "Delivered", "Cancelled"};
        cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setFont(UITheme.FONT_BODY);

        JButton btnSearch = UITheme.createSecondaryButton("Filter");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            loadOrders();
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

        JButton btnCreate = UITheme.createPrimaryButton("+ Create Order");
        btnCreate.addActionListener(e -> openCreateOrderDialog());

        JButton btnConfirm = UITheme.createAccentButton("Confirm Order (Deduct Stock)");
        btnConfirm.addActionListener(e -> handleConfirmOrder());

        JButton btnInvoice = UITheme.createSecondaryButton("View Invoice / PDF");
        btnInvoice.addActionListener(e -> openInvoicePreview());

        JButton btnCancel = UITheme.createDangerButton("Cancel Order");
        btnCancel.addActionListener(e -> handleCancelOrder());

        JButton btnExport = UITheme.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCSV());

        actionPanel.add(btnCreate);
        actionPanel.add(btnConfirm);
        actionPanel.add(btnInvoice);
        actionPanel.add(btnCancel);
        actionPanel.add(btnExport);
        topBar.add(actionPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"Order ID", "Client Name", "Order Date", "Subtotal", "Discount", "Total Amount", "Status", "Payment Method", "Delivery Address"};
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

    public void loadOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = controller.getAllOrders();
        populateTable(orders);
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        tableModel.setRowCount(0);
        List<Order> orders = controller.searchOrders(keyword, status);
        populateTable(orders);
    }

    private void populateTable(List<Order> orders) {
        for (Order o : orders) {
            tableModel.addRow(new Object[]{
                o.getOrderId(),
                o.getClientName(),
                UITheme.formatDateTime(o.getOrderDate()),
                UITheme.formatCurrency(o.getSubtotal()),
                UITheme.formatCurrency(o.getDiscount()),
                UITheme.formatCurrency(o.getTotalAmount()),
                o.getOrderStatus(),
                o.getPaymentMethod(),
                o.getDeliveryAddress()
            });
        }
    }

    private void openCreateOrderDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        CreateOrderDialog dlg = new CreateOrderDialog((Frame) parent);
        dlg.setVisible(true);
        if (dlg.isCompleted()) {
            loadOrders();
        }
    }

    private void handleConfirmOrder() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to confirm.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 6);

        if (!"Pending".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "Order is already in '" + currentStatus + "' status. Only 'Pending' orders can be confirmed.",
                "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Confirm Order #" + orderId + "?\n\n" +
            "This will verify inventory, atomically deduct stock, log movement history, and change status to 'Confirmed'.",
            "Confirm Order & Stock Deduction",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = controller.confirmOrder(orderId);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                        "Order #" + orderId + " confirmed successfully!\nStock has been deducted from inventory.",
                        "Order Confirmed",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    loadOrders();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to confirm order.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (InsufficientStockException ex) {
                JOptionPane.showMessageDialog(this,
                    "Cannot confirm order due to insufficient stock!\n\n" + ex.getMessage(),
                    "Stock Shortage Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error during confirmation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openInvoicePreview() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to view its invoice.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        Order order = controller.getOrderDetails(orderId);
        if (order != null) {
            Window parent = SwingUtilities.getWindowAncestor(this);
            InvoicePreviewDialog dlg = new InvoicePreviewDialog((Frame) parent, order);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Could not load order details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelOrder() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 6);

        if ("Delivered".equalsIgnoreCase(currentStatus) || "Cancelled".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Cannot cancel an order that is already '" + currentStatus + "'.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Order #" + orderId + "?",
            "Confirm Order Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = controller.cancelOrder(orderId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Order #" + orderId + " has been cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_Orders.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportTableToCSV(table, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Orders exported to CSV successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
