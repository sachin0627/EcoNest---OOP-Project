package com.econest.view;

import com.econest.controller.OrderController;
import com.econest.dao.ClientDAO;
import com.econest.dao.ProductDAO;
import com.econest.model.Client;
import com.econest.model.Order;
import com.econest.model.OrderItem;
import com.econest.model.Product;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modal dialog for Creating a New Client Order with Multi-Product selection
 * and Polymorphic Tier Discount calculation.
 */
public class CreateOrderDialog extends JDialog {
    private final OrderController orderController;
    private final ClientDAO clientDAO;
    private final ProductDAO productDAO;

    private boolean completed = false;

    private JTextField txtOrderId;
    private JComboBox<Client> cmbClient;
    private JTextField txtDeliveryAddress;
    private JComboBox<String> cmbPaymentMethod;
    private JTextField txtNotes;

    // Line items builder
    private JComboBox<Product> cmbProduct;
    private JLabel lblAvailableStock;
    private JSpinner spQuantity;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;

    // Financial summaries
    private JLabel lblSubtotal;
    private JTextField txtDiscount;
    private JLabel lblGrandTotal;

    private double currentSubtotal = 0.0;
    private double currentDiscount = 0.0;
    private double currentGrandTotal = 0.0;

    private final List<OrderItem> currentItems = new ArrayList<>();

    public CreateOrderDialog(Frame parent) {
        super(parent, "Create New Client Order", true);
        this.orderController = new OrderController();
        this.clientDAO = new ClientDAO();
        this.productDAO = new ProductDAO();

        initUI();
    }

    private void initUI() {
        setSize(850, 680);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("EcoNest Order Processing Terminal");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content Split
        JPanel contentPanel = new JPanel(new BorderLayout(14, 14));
        contentPanel.setBackground(UITheme.COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Order Info Card
        JPanel infoCard = UITheme.createCardPanel();
        infoCard.setLayout(new GridLayout(3, 4, 10, 8));

        infoCard.add(new JLabel("Order ID:"));
        txtOrderId = new JTextField(orderController.generateNextId());
        txtOrderId.setEditable(false);
        infoCard.add(txtOrderId);

        infoCard.add(new JLabel("Select Client:"));
        cmbClient = new JComboBox<>();
        List<Client> clients = clientDAO.getAllClients();
        for (Client c : clients) cmbClient.addItem(c);
        cmbClient.addActionListener(e -> onClientSelected());
        infoCard.add(cmbClient);

        infoCard.add(new JLabel("Delivery Address:"));
        txtDeliveryAddress = new JTextField();
        infoCard.add(txtDeliveryAddress);

        infoCard.add(new JLabel("Payment Method:"));
        String[] payments = {"Cash on Delivery", "Credit Card (Online)", "Bank Transfer", "Company Cheque"};
        cmbPaymentMethod = new JComboBox<>(payments);
        infoCard.add(cmbPaymentMethod);

        infoCard.add(new JLabel("Order Remarks:"));
        txtNotes = new JTextField("Eco-friendly retail packaging");
        infoCard.add(txtNotes);

        infoCard.add(new JLabel("Client Tier Discount:"));
        JLabel lblTierNotice = new JLabel("Auto-calculated on items");
        lblTierNotice.setFont(UITheme.FONT_SMALL);
        lblTierNotice.setForeground(UITheme.COLOR_PRIMARY);
        infoCard.add(lblTierNotice);

        contentPanel.add(infoCard, BorderLayout.NORTH);

        // Center: Item Selection & Items Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Add Product Form Bar
        JPanel itemBar = UITheme.createCardPanel();
        itemBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        itemBar.add(new JLabel("Product:"));
        cmbProduct = new JComboBox<>();
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) cmbProduct.addItem(p);
        cmbProduct.addActionListener(e -> updateStockLabel());
        itemBar.add(cmbProduct);

        lblAvailableStock = new JLabel("Stock: -");
        lblAvailableStock.setFont(UITheme.FONT_SMALL);
        lblAvailableStock.setForeground(UITheme.COLOR_TEXT_MUTED);
        itemBar.add(lblAvailableStock);

        itemBar.add(new JLabel("Qty:"));
        spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        itemBar.add(spQuantity);

        JButton btnAddItem = UITheme.createPrimaryButton("+ Add to Order");
        btnAddItem.addActionListener(e -> addItemToOrder());
        itemBar.add(btnAddItem);

        JButton btnRemoveItem = UITheme.createDangerButton("Remove Item");
        btnRemoveItem.addActionListener(e -> removeItemFromOrder());
        itemBar.add(btnRemoveItem);

        centerPanel.add(itemBar, BorderLayout.NORTH);

        // Order Items Table
        String[] cols = {"#", "Product ID", "Product Name", "Unit Price", "Qty", "Subtotal (LKR)"};
        itemsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsTableModel);
        UITheme.styleTable(itemsTable);
        JScrollPane scrollTable = new JScrollPane(itemsTable);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollTable, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom Totals Card
        JPanel totalsCard = UITheme.createCardPanel();
        totalsCard.setLayout(new BorderLayout());

        JPanel totalsGrid = new JPanel(new GridLayout(3, 2, 8, 4));
        totalsGrid.setOpaque(false);

        totalsGrid.add(new JLabel("Subtotal:"));
        lblSubtotal = new JLabel("LKR 0.00");
        lblSubtotal.setFont(UITheme.FONT_BODY_BOLD);
        totalsGrid.add(lblSubtotal);

        totalsGrid.add(new JLabel("Discount (Polymorphic Client Tier / Promo):"));
        txtDiscount = new JTextField("0.00", 8);
        txtDiscount.addActionListener(e -> recalculateGrandTotal());
        totalsGrid.add(txtDiscount);

        totalsGrid.add(new JLabel("Grand Total:"));
        lblGrandTotal = new JLabel("LKR 0.00");
        lblGrandTotal.setFont(UITheme.FONT_TITLE);
        lblGrandTotal.setForeground(UITheme.COLOR_PRIMARY);
        totalsGrid.add(lblGrandTotal);

        totalsCard.add(totalsGrid, BorderLayout.WEST);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        actionPanel.setOpaque(false);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("Create & Save Order");
        btnSubmit.addActionListener(e -> handleSubmitOrder());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSubmit);
        totalsCard.add(actionPanel, BorderLayout.EAST);

        contentPanel.add(totalsCard, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Initialize state
        onClientSelected();
        updateStockLabel();
    }

    private void onClientSelected() {
        Client c = (Client) cmbClient.getSelectedItem();
        if (c != null) {
            txtDeliveryAddress.setText(c.getAddress() + ", " + c.getCity());
            recalculateGrandTotal();
        }
    }

    private void updateStockLabel() {
        Product p = (Product) cmbProduct.getSelectedItem();
        if (p != null) {
            lblAvailableStock.setText(String.format("Stock: %d units | LKR %,.2f", p.getAvailableQuantity(), p.getUnitPrice()));
        }
    }

    private void addItemToOrder() {
        Product p = (Product) cmbProduct.getSelectedItem();
        int qty = (Integer) spQuantity.getValue();

        if (p == null) return;

        if (qty > p.getAvailableQuantity()) {
            JOptionPane.showMessageDialog(this,
                "Requested quantity (" + qty + ") exceeds available stock (" + p.getAvailableQuantity() + ") for " + p.getProductName() + "!",
                "Stock Insufficient",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Check if already in list
        for (OrderItem existing : currentItems) {
            if (existing.getProductId().equalsIgnoreCase(p.getProductId())) {
                int newQty = existing.getQuantity() + qty;
                if (newQty > p.getAvailableQuantity()) {
                    JOptionPane.showMessageDialog(this,
                        "Total requested (" + newQty + ") exceeds available stock (" + p.getAvailableQuantity() + ")!",
                        "Stock Insufficient",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                existing.setQuantity(newQty);
                refreshItemsTable();
                return;
            }
        }

        OrderItem item = new OrderItem(p.getProductId(), p.getProductName(), qty, p.getUnitPrice());
        currentItems.add(item);
        refreshItemsTable();
    }

    private void removeItemFromOrder() {
        int row = itemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentItems.remove(row);
        refreshItemsTable();
    }

    private void refreshItemsTable() {
        itemsTableModel.setRowCount(0);
        currentSubtotal = 0.0;
        int idx = 1;
        for (OrderItem item : currentItems) {
            itemsTableModel.addRow(new Object[]{
                idx++,
                item.getProductId(),
                item.getProductName(),
                UITheme.formatCurrency(item.getUnitPrice()),
                item.getQuantity(),
                UITheme.formatCurrency(item.getSubtotal())
            });
            currentSubtotal += item.getSubtotal();
        }

        lblSubtotal.setText(UITheme.formatCurrency(currentSubtotal));

        // Calculate Polymorphic Client Discount
        Client c = (Client) cmbClient.getSelectedItem();
        if (c != null) {
            currentDiscount = c.calculateDiscount(currentSubtotal);
            txtDiscount.setText(String.format("%.2f", currentDiscount));
        }

        recalculateGrandTotal();
    }

    private void recalculateGrandTotal() {
        try {
            currentDiscount = Double.parseDouble(txtDiscount.getText().trim());
        } catch (NumberFormatException e) {
            currentDiscount = 0.0;
        }

        currentGrandTotal = Math.max(0.0, currentSubtotal - currentDiscount);
        lblGrandTotal.setText(UITheme.formatCurrency(currentGrandTotal));
    }

    private void handleSubmitOrder() {
        if (currentItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one product to the order.", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Client c = (Client) cmbClient.getSelectedItem();
        String address = txtDeliveryAddress.getText().trim();
        String payment = (String) cmbPaymentMethod.getSelectedItem();
        String notes = txtNotes.getText().trim();

        if (c == null || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide complete client and delivery address details.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        recalculateGrandTotal();

        Order order = new Order();
        order.setOrderId(txtOrderId.getText().trim());
        order.setClientId(c.getClientId());
        order.setDeliveryAddress(address);
        order.setPaymentMethod(payment);
        order.setNotes(notes);
        order.setSubtotal(currentSubtotal);
        order.setDiscount(currentDiscount);
        order.setTotalAmount(currentGrandTotal);
        order.setOrderStatus("Pending");
        order.setItems(currentItems);

        try {
            boolean ok = orderController.placeOrder(order);
            if (ok) {
                completed = true;
                JOptionPane.showMessageDialog(this,
                    "Order #" + order.getOrderId() + " created successfully in 'Pending' status!\n" +
                    "Use 'Confirm Order' to deduct inventory and schedule delivery.",
                    "Order Created",
                    JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Order error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
