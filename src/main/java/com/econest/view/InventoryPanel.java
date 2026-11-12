package com.econest.view;

import com.econest.controller.InventoryController;
import com.econest.model.Inventory;
import com.econest.model.StockMovement;
import com.econest.util.CSVUtil;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * View panel for Module 03: Inventory & Stock Management.
 */
public class InventoryPanel extends JPanel {
    private final InventoryController controller;

    private JTable invTable;
    private DefaultTableModel invTableModel;
    private JTable moveTable;
    private DefaultTableModel moveTableModel;

    private JLabel lblAlertBanner;
    private JToggleButton btnToggleLowStock;

    public InventoryPanel() {
        this.controller = new InventoryController();
        initUI();
        loadInventory(false);
        loadMovements();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Alert Banner & Action Bar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Low Stock Alert Banner
        lblAlertBanner = new JLabel(" ");
        lblAlertBanner.setFont(UITheme.FONT_BODY_BOLD);
        lblAlertBanner.setOpaque(true);
        lblAlertBanner.setBackground(new Color(0xFD, 0xED, 0xEC));
        lblAlertBanner.setForeground(UITheme.COLOR_DANGER);
        lblAlertBanner.setBorder(new EmptyBorder(8, 16, 8, 16));
        lblAlertBanner.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Control Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout(14, 0));
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left Controls
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);

        btnToggleLowStock = new JToggleButton("Show Low Stock Alerts Only");
        btnToggleLowStock.setFont(UITheme.FONT_BODY_BOLD);
        btnToggleLowStock.setFocusPainted(false);
        btnToggleLowStock.addActionListener(e -> loadInventory(btnToggleLowStock.isSelected()));

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            loadInventory(btnToggleLowStock.isSelected());
            loadMovements();
        });

        leftControls.add(btnToggleLowStock);
        leftControls.add(btnRefresh);
        topBar.add(leftControls, BorderLayout.WEST);

        // Right Actions
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JButton btnStockIn = UITheme.createPrimaryButton("+ Stock-In from Supplier");
        btnStockIn.addActionListener(e -> openStockInDialog());

        JButton btnUpdateLevel = UITheme.createSecondaryButton("Update Reorder Level");
        btnUpdateLevel.addActionListener(e -> openUpdateReorderDialog());

        JButton btnExport = UITheme.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCSV());

        rightControls.add(btnStockIn);
        rightControls.add(btnUpdateLevel);
        rightControls.add(btnExport);
        topBar.add(rightControls, BorderLayout.EAST);

        topContainer.add(lblAlertBanner);
        topContainer.add(Box.createVerticalStrut(10));
        topContainer.add(topBar);

        add(topContainer, BorderLayout.NORTH);

        // Tabbed Pane (Tab 1: Stock Inventory, Tab 2: Movement Logs)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        // Tab 1: Current Inventory
        JPanel invPanel = UITheme.createCardPanel();
        invPanel.setLayout(new BorderLayout());

        String[] invCols = {"Product ID", "Product Name", "Category", "Unit Price", "Available Qty", "Reorder Level", "Total Value (LKR)", "Stock Status"};
        invTableModel = new DefaultTableModel(invCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        invTable = new JTable(invTableModel);
        UITheme.styleTable(invTable);

        JScrollPane scrollInv = new JScrollPane(invTable);
        scrollInv.setBorder(BorderFactory.createEmptyBorder());
        invPanel.add(scrollInv, BorderLayout.CENTER);
        tabbedPane.addTab("Current Stock & Levels", invPanel);

        // Tab 2: Stock Movements
        JPanel movePanel = UITheme.createCardPanel();
        movePanel.setLayout(new BorderLayout());

        String[] moveCols = {"Log ID", "Product ID", "Product Name", "Movement Type", "Quantity", "Supplier / Order Ref", "Remarks", "Date / Time"};
        moveTableModel = new DefaultTableModel(moveCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        moveTable = new JTable(moveTableModel);
        UITheme.styleTable(moveTable);

        JScrollPane scrollMove = new JScrollPane(moveTable);
        scrollMove.setBorder(BorderFactory.createEmptyBorder());
        movePanel.add(scrollMove, BorderLayout.CENTER);
        tabbedPane.addTab("Stock Movement History", movePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void loadInventory(boolean lowStockOnly) {
        invTableModel.setRowCount(0);
        List<Inventory> list = lowStockOnly ? controller.getLowStockAlerts() : controller.getAllInventory();

        for (Inventory i : list) {
            invTableModel.addRow(new Object[]{
                i.getProductId(),
                i.getProductName(),
                i.getCategory(),
                UITheme.formatCurrency(i.getUnitPrice()),
                i.getAvailableQuantity(),
                i.getReorderLevel(),
                UITheme.formatCurrency(i.getTotalStockValue()),
                i.getStatusDescription()
            });
        }

        int lowStockCount = controller.getLowStockCount();
        if (lowStockCount > 0) {
            lblAlertBanner.setText("⚠ LOW STOCK ALERT: " + lowStockCount + " product(s) are currently at or below their reorder threshold! Replenishment required.");
            lblAlertBanner.setVisible(true);
        } else {
            lblAlertBanner.setText("✔ All inventory levels are healthy and above reorder thresholds.");
            lblAlertBanner.setBackground(new Color(0xEA, 0xFA, 0xEA));
            lblAlertBanner.setForeground(UITheme.COLOR_SUCCESS);
            lblAlertBanner.setVisible(true);
        }
    }

    public void loadMovements() {
        moveTableModel.setRowCount(0);
        List<StockMovement> list = controller.getRecentMovements(50);
        for (StockMovement sm : list) {
            String ref = sm.getSupplierName() != null ? "Supplier: " + sm.getSupplierName() :
                         (sm.getReferenceOrderId() != null ? "Order: " + sm.getReferenceOrderId() : "N/A");

            moveTableModel.addRow(new Object[]{
                sm.getMovementId(),
                sm.getProductId(),
                sm.getProductName() != null ? sm.getProductName() : sm.getProductId(),
                sm.getMovementType(),
                sm.getQuantity(),
                ref,
                sm.getRemarks() != null ? sm.getRemarks() : "",
                UITheme.formatDateTime(sm.getMovementDate())
            });
        }
    }

    private void openStockInDialog() {
        String preselectedId = null;
        int row = invTable.getSelectedRow();
        if (row >= 0) {
            preselectedId = (String) invTableModel.getValueAt(row, 0);
        }

        Window parent = SwingUtilities.getWindowAncestor(this);
        StockInDialog dlg = new StockInDialog((Frame) parent, preselectedId);
        dlg.setVisible(true);
        if (dlg.isCompleted()) {
            loadInventory(btnToggleLowStock.isSelected());
            loadMovements();
        }
    }

    private void openUpdateReorderDialog() {
        int row = invTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an inventory item from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) invTableModel.getValueAt(row, 0);
        String productName = (String) invTableModel.getValueAt(row, 1);
        int currentQty = (Integer) invTableModel.getValueAt(row, 4);
        int currentReorder = (Integer) invTableModel.getValueAt(row, 5);

        JTextField txtNewQty = new JTextField(String.valueOf(currentQty));
        JTextField txtNewReorder = new JTextField(String.valueOf(currentReorder));

        JPanel pnl = new JPanel(new GridLayout(2, 2, 8, 8));
        pnl.add(new JLabel("Available Quantity:"));
        pnl.add(txtNewQty);
        pnl.add(new JLabel("Minimum Reorder Level:"));
        pnl.add(txtNewReorder);

        int result = JOptionPane.showConfirmDialog(
            this,
            pnl,
            "Update Stock & Reorder Threshold: " + productName,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int q = Integer.parseInt(txtNewQty.getText().trim());
                int r = Integer.parseInt(txtNewReorder.getText().trim());
                if (q < 0 || r < 0) throw new NumberFormatException();

                boolean ok = controller.updateStockAndThreshold(productId, q, r);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Inventory thresholds updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadInventory(btnToggleLowStock.isSelected());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update inventory.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid non-negative integer numbers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_Inventory.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportTableToCSV(invTable, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Inventory data exported to CSV successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
