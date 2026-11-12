package com.econest.view;

import com.econest.controller.ProductController;
import com.econest.model.Product;
import com.econest.util.UITheme;
import com.econest.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for Adding / Editing a Product in the Catalogue.
 */
public class ProductDialog extends JDialog {
    private final ProductController controller;
    private final Product product;
    private final boolean isNew;
    private boolean saved = false;

    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<String> cmbCategory;
    private JTextArea txtDescription;
    private JTextField txtPrice;
    private JComboBox<Integer> cmbEcoRating;
    private JSpinner spQuantity;
    private JSpinner spReorderLevel;

    public ProductDialog(Frame parent, Product product, boolean isNew) {
        super(parent, isNew ? "Add New Product" : "Edit Product: " + product.getProductId(), true);
        this.controller = new ProductController();
        this.product = product != null ? product : new Product();
        this.isNew = isNew;

        initUI();
    }

    private void initUI() {
        setSize(520, 580);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel(isNew ? "Add New EcoNest Product" : "Update Product Details");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.COLOR_BACKGROUND);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: ID & Category
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtId = new JTextField(isNew ? controller.generateNextId() : product.getProductId());
        txtId.setEnabled(isNew);
        form.add(txtId, gbc);

        // Row 1: Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtName = new JTextField(product.getProductName() != null ? product.getProductName() : "");
        form.add(txtName, gbc);

        // Row 2: Category
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] categories = {"Indoor Plants", "Outdoor Plants", "Planters", "Gardening Kits"};
        cmbCategory = new JComboBox<>(categories);
        if (product.getCategory() != null) cmbCategory.setSelectedItem(product.getCategory());
        form.add(cmbCategory, gbc);

        // Row 3: Description
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDescription = new JTextArea(product.getDescription() != null ? product.getDescription() : "", 3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDescription), gbc);

        // Row 4: Unit Price (LKR)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(new JLabel("Unit Price (LKR):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPrice = new JTextField(product.getUnitPrice() > 0 ? String.valueOf(product.getUnitPrice()) : "");
        form.add(txtPrice, gbc);

        // Row 5: Eco Rating (1-5 Stars)
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        form.add(new JLabel("Eco Rating (1-5):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        Integer[] ratings = {5, 4, 3, 2, 1};
        cmbEcoRating = new JComboBox<>(ratings);
        if (product.getEcoRating() > 0) cmbEcoRating.setSelectedItem(product.getEcoRating());
        form.add(cmbEcoRating, gbc);

        // Row 6: Initial / Current Quantity
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        form.add(new JLabel("Available Quantity:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        spQuantity = new JSpinner(new SpinnerNumberModel(product.getAvailableQuantity(), 0, 10000, 1));
        form.add(spQuantity, gbc);

        // Row 7: Reorder Level
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.3;
        form.add(new JLabel("Reorder Alert Level:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        spReorderLevel = new JSpinner(new SpinnerNumberModel(product.getReorderLevel() > 0 ? product.getReorderLevel() : 10, 1, 1000, 1));
        form.add(spReorderLevel, gbc);

        add(form, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actionPanel.setBackground(UITheme.COLOR_BACKGROUND);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UITheme.createPrimaryButton(isNew ? "Add Product" : "Save Changes");
        btnSave.addActionListener(e -> handleSave());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleSave() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();
        String desc = txtDescription.getText().trim();
        String priceStr = txtPrice.getText().trim();
        int rating = (Integer) cmbEcoRating.getSelectedItem();
        int qty = (Integer) spQuantity.getValue();
        int reorder = (Integer) spReorderLevel.getValue();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid product name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive unit price (e.g. 2500.00).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        product.setProductId(id);
        product.setProductName(name);
        product.setCategory(category);
        product.setDescription(desc);
        product.setUnitPrice(price);
        product.setEcoRating(rating);
        product.setAvailableQuantity(qty);
        product.setReorderLevel(reorder);
        product.setActive(true);

        boolean ok = controller.saveProduct(product, isNew);
        if (ok) {
            saved = true;
            JOptionPane.showMessageDialog(this, "Product successfully saved to catalogue!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save product to database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
