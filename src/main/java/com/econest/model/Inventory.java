package com.econest.model;

import com.econest.interfaces.Manageable;
import com.econest.interfaces.Reportable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing current inventory stock status and reorder thresholds.
 * 
 * Demonstrates: Encapsulation, Business Rule Evaluation, Interface Implementation.
 */
public class Inventory implements Manageable, Reportable {
    private int inventoryId;
    private String productId;
    private String productName;
    private String category;
    private double unitPrice;
    private int availableQuantity;
    private int reorderLevel;
    private Date lastUpdated;

    public Inventory() {}

    public Inventory(int inventoryId, String productId, String productName, String category,
                     double unitPrice, int availableQuantity, int reorderLevel, Date lastUpdated) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.availableQuantity = availableQuantity;
        this.reorderLevel = reorderLevel;
        this.lastUpdated = lastUpdated;
    }

    public boolean isLowStock() {
        return this.availableQuantity <= this.reorderLevel;
    }

    public boolean isOutOfStock() {
        return this.availableQuantity <= 0;
    }

    public double getTotalStockValue() {
        return this.availableQuantity * this.unitPrice;
    }

    // --- Manageable Implementation ---

    @Override
    public String getId() {
        return productId;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s (%d units)", productName != null ? productName : productId, availableQuantity);
    }

    @Override
    public boolean isValid() {
        return productId != null && availableQuantity >= 0 && reorderLevel >= 0;
    }

    @Override
    public String getStatusDescription() {
        if (isOutOfStock()) return "OUT OF STOCK";
        if (isLowStock()) return "LOW STOCK ALERT";
        return "HEALTHY";
    }

    // --- Reportable Implementation ---

    @Override
    public String getReportIdentifier() {
        return productId;
    }

    @Override
    public double getMonetaryValue() {
        return getTotalStockValue();
    }

    @Override
    public Map<String, Object> toReportRow() {
        Map<String, Object> map = new HashMap<>();
        map.put("Product ID", productId);
        map.put("Product Name", productName);
        map.put("Category", category);
        map.put("Available Qty", availableQuantity);
        map.put("Reorder Level", reorderLevel);
        map.put("Unit Price", unitPrice);
        map.put("Total Valuation", getTotalStockValue());
        map.put("Status", getStatusDescription());
        return map;
    }

    // --- Getters & Setters ---

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
