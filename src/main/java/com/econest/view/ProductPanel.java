package com.econest.view;

import com.econest.controller.ProductController;
import com.econest.model.Product;
import com.econest.util.CSVUtil;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * View panel for Module 01: Product Catalogue Management.
 */
public class ProductPanel extends JPanel {
    private final ProductController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbCategory;

    public ProductPanel() {
        this.controller = new ProductController();
        initUI();
        loadProducts();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Control Bar (Search, Category Filter, Actions)
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout(14, 0));

        // Left Controls: Search + Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        txtSearch = new JTextField(15);
        txtSearch.setFont(UITheme.FONT_BODY);

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(UITheme.FONT_BODY_BOLD);
        String[] cats = {"All Categories", "Indoor Plants", "Outdoor Plants", "Planters", "Gardening Kits"};
        cmbCategory = new JComboBox<>(cats);
        cmbCategory.setFont(UITheme.FONT_BODY);

        JButton btnSearch = UITheme.createSecondaryButton("Filter");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            loadProducts();
        });

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblCat);
        filterPanel.add(cmbCategory);
        filterPanel.add(btnSearch);
        filterPanel.add(btnReset);
        topBar.add(filterPanel, BorderLayout.WEST);

        // Right Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("+ Add Product");
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnEdit = UITheme.createSecondaryButton("Edit");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnDelete = UITheme.createDangerButton("Delete");
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnExport = UITheme.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> exportCSV());

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(btnExport);
        topBar.add(actionPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"Product ID", "Product Name", "Category", "Unit Price", "Eco Rating", "Available Qty", "Reorder Level", "Stock Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table view
            }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = controller.getAllProducts();
        populateTable(products);
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();
        tableModel.setRowCount(0);
        List<Product> products = controller.searchProducts(keyword, category);
        populateTable(products);
    }

    private void populateTable(List<Product> products) {
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getProductId(),
                p.getProductName(),
                p.getCategory(),
                UITheme.formatCurrency(p.getUnitPrice()),
                p.getEcoRating() + " ★",
                p.getAvailableQuantity(),
                p.getReorderLevel(),
                p.getStatusDescription()
            });
        }
    }

    private void openAddDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        ProductDialog dlg = new ProductDialog((Frame) parent, null, true);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadProducts();
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        Product product = controller.getProduct(productId);
        if (product != null) {
            Window parent = SwingUtilities.getWindowAncestor(this);
            ProductDialog dlg = new ProductDialog((Frame) parent, product, false);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                loadProducts();
            }
        }
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove product '" + productName + "' (" + productId + ")?",
            "Confirm Product Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = controller.deleteProduct(productId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Product removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_Products.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportTableToCSV(table, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Product catalogue exported to CSV successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
