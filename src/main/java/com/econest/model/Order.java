package com.econest.model;

import com.econest.interfaces.Manageable;
import com.econest.interfaces.Reportable;
import com.econest.util.UITheme;
import com.econest.util.ValidationUtil;

import java.util.*;

/**
 * Entity representing a Customer Order in the EcoNest Retail Management System.
 * 
 * Demonstrates:
 * - Composition & Aggregation: Order contains a collection of OrderItem objects.
 * - Encapsulation: Internal total calculations and order state transitions.
 * - Method Overloading: Multiple `addItem` and `applyDiscount` signatures.
 * - Interfaces: Implements Manageable and Reportable.
 */
public class Order implements Manageable, Reportable {
    private String orderId;
    private String clientId;
    private String clientName; // Joined/transient
    private Date orderDate;
    private double subtotal;
    private double discount;
    private double totalAmount;
    private String deliveryAddress;
    private String orderStatus; // "Pending", "Confirmed", "Processing", "Dispatched", "Delivered", "Cancelled"
    private String paymentMethod;
    private String notes;
    private Date createdAt;
    private Date updatedAt;

    // Composition: Order owns a list of OrderItems
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        this.orderDate = new Date();
        this.orderStatus = "Pending";
        this.paymentMethod = "Cash on Delivery";
        this.subtotal = 0.0;
        this.discount = 0.0;
        this.totalAmount = 0.0;
    }

    public Order(String orderId, String clientId, String deliveryAddress) {
        this();
        this.orderId = orderId;
        this.clientId = clientId;
        this.deliveryAddress = deliveryAddress;
    }

    // --- Business Logic & Method Overloading ---

    /**
     * Adds a product to the order by constructing an OrderItem.
     * Demonstrates: Method Overloading (Signature 1)
     */
    public void addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) return;
        OrderItem item = new OrderItem(product.getProductId(), product.getProductName(), quantity, product.getUnitPrice());
        addItem(item);
    }

    /**
     * Adds an existing OrderItem directly to the order.
     * Demonstrates: Method Overloading (Signature 2)
     */
    public void addItem(OrderItem item) {
        if (item == null) return;
        // If product already in list, increment quantity
        for (OrderItem existing : items) {
            if (existing.getProductId().equalsIgnoreCase(item.getProductId())) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                recalculateTotals();
                return;
            }
        }
        items.add(item);
        recalculateTotals();
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProductId().equalsIgnoreCase(productId));
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotal = 0.0;
        for (OrderItem item : items) {
            this.subtotal += item.getSubtotal();
        }
        this.totalAmount = Math.max(0.0, this.subtotal - this.discount);
    }

    /**
     * Applies a flat discount amount in LKR.
     * Demonstrates: Method Overloading (Signature 1)
     */
    public void applyDiscount(double amount) {
        this.discount = Math.max(0.0, amount);
        recalculateTotals();
    }

    /**
     * Applies a percentage discount based on the current subtotal.
     * Demonstrates: Method Overloading (Signature 2)
     */
    public void applyDiscount(int percentage) {
        if (percentage > 0 && percentage <= 100) {
            this.discount = Math.round(this.subtotal * (percentage / 100.0) * 100.0) / 100.0;
        } else {
            this.discount = 0.0;
        }
        recalculateTotals();
    }

    // --- Manageable Implementation ---

    @Override
    public String getId() {
        return orderId;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s - %s (%s)", orderId, clientName != null ? clientName : clientId, UITheme.formatCurrency(totalAmount));
    }

    @Override
    public boolean isValid() {
        return ValidationUtil.isNotEmpty(orderId) &&
               ValidationUtil.isNotEmpty(clientId) &&
               ValidationUtil.isNotEmpty(deliveryAddress) &&
               !items.isEmpty() &&
               totalAmount >= 0.0;
    }

    @Override
    public String getStatusDescription() {
        return orderStatus;
    }

    // --- Reportable Implementation ---

    @Override
    public String getReportIdentifier() {
        return orderId;
    }

    @Override
    public double getMonetaryValue() {
        return totalAmount;
    }

    @Override
    public Map<String, Object> toReportRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("Order ID", orderId);
        row.put("Client", clientName != null ? clientName : clientId);
        row.put("Date", UITheme.formatDateTime(orderDate));
        row.put("Items Count", items.size());
        row.put("Subtotal", subtotal);
        row.put("Discount", discount);
        row.put("Total Amount", totalAmount);
        row.put("Status", orderStatus);
        return row;
    }

    // --- Encapsulated Getters & Setters ---

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        recalculateTotals();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        recalculateTotals();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
