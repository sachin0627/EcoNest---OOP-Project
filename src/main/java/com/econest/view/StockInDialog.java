package com.econest.view;

import com.econest.controller.InventoryController;
import com.econest.dao.ProductDAO;
import com.econest.model.Product;
import com.econest.model.Supplier;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for Recording Supplier Stock-In.
 */
public class StockInDialog extends JDialog {
    private final InventoryController controller;
    private final ProductDAO productDAO;
    private boolean completed = false;

    private JComboBox<Product> cmbProduct;
    private JSpinner spQuantity;
    private JComboBox<Supplier> cmbSupplier;
    private JTextField txtRemarks;

    public StockInDialog(Frame parent, String preselectedProductId) {
        super(parent, "Record Supplier Stock-In (Replenishment)", true);
        this.controller = new InventoryController();
        this.productDAO = new ProductDAO();

        initUI(preselectedProductId);
    }

    private void initUI(String preselectedProductId) {
        setSize(500, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Supplier Stock Intake Entry");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.COLOR_BACKGROUND);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Product Selector
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Target Product:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbProduct = new JComboBox<>();
        List<Product> products = productDAO.getAllProducts();
        Product toSelect = null;
        for (Product p : products) {
            cmbProduct.addItem(p);
            if (preselectedProductId != null && p.getProductId().equalsIgnoreCase(preselectedProductId)) {
                toSelect = p;
            }
        }
        if (toSelect != null) cmbProduct.setSelectedItem(toSelect);
        form.add(cmbProduct, gbc);

        // Quantity Received
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Received Quantity:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        spQuantity = new JSpinner(new SpinnerNumberModel(10, 1, 5000, 1));
        form.add(spQuantity, gbc);

        // Supplier Selector
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbSupplier = new JComboBox<>();
        List<Supplier> suppliers = controller.getAllSuppliers();
        for (Supplier s : suppliers) {
            cmbSupplier.addItem(s);
        }
        form.add(cmbSupplier, gbc);

        // Remarks / Batch Note
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(new JLabel("Remarks / Batch No:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtRemarks = new JTextField("Nursery restock batch");
        form.add(txtRemarks, gbc);

        add(form, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actionPanel.setBackground(UITheme.COLOR_BACKGROUND);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("Record Stock-In");
        btnSubmit.addActionListener(e -> handleSubmit());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSubmit);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleSubmit() {
        Product p = (Product) cmbProduct.getSelectedItem();
        Supplier s = (Supplier) cmbSupplier.getSelectedItem();
        int qty = (Integer) spQuantity.getValue();
        String remarks = txtRemarks.getText().trim();

        if (p == null || s == null || qty <= 0) {
            JOptionPane.showMessageDialog(this, "Please select valid product, supplier, and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = controller.recordStockIn(p.getProductId(), qty, s.getSupplierId(), remarks);
        if (ok) {
            completed = true;
            JOptionPane.showMessageDialog(this, "Stock successfully recorded! " + qty + " units added to " + p.getProductName() + ".", "Stock Updated", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to record stock intake.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
