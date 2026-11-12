package com.econest.model;

import com.econest.interfaces.Manageable;
import com.econest.util.ValidationUtil;

import java.util.Date;

/**
 * Entity class representing a Product in the EcoNest Catalogue.
 * 
 * Demonstrates:
 * - Encapsulation: Private attributes with getters/setters and domain validation.
 * - Interface Implementation: Implements Manageable.
 */
public class Product implements Manageable {
    private String productId;
    private String productName;
    private String category;
    private String description;
    private double unitPrice;
    private int ecoRating; // 1 to 5
    private int availableQuantity;
    private int reorderLevel;
    private String imagePath;
    private boolean active;
    private Date createdAt;
    private Date updatedAt;

    public Product() {
        this.active = true;
        this.ecoRating = 5;
        this.reorderLevel = 10;
    }

    public Product(String productId, String productName, String category, String description,
                   double unitPrice, int ecoRating, int availableQuantity, int reorderLevel) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.description = description;
        this.unitPrice = unitPrice;
        this.ecoRating = ecoRating;
        this.availableQuantity = availableQuantity;
        this.reorderLevel = reorderLevel;
        this.active = true;
    }

    // --- Business Logic Methods ---

    /**
     * Checks if product quantity is at or below its defined reorder threshold.
     * @return true if low stock alert should trigger
     */
    public boolean isLowStock() {
        return this.availableQuantity <= this.reorderLevel;
    }

    // --- Manageable Interface Implementation ---

    @Override
    public String getId() {
        return productId;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s - %s (LKR %,.2f)", productId, productName, unitPrice);
    }

    @Override
    public boolean isValid() {
        return ValidationUtil.isNotEmpty(productId) &&
               ValidationUtil.isNotEmpty(productName) &&
               ValidationUtil.isNotEmpty(category) &&
               ValidationUtil.isPositive(unitPrice) &&
               ValidationUtil.isValidEcoRating(ecoRating) &&
               ValidationUtil.isNonNegative(availableQuantity) &&
               ValidationUtil.isNonNegative(reorderLevel);
    }

    @Override
    public String getStatusDescription() {
        if (!active) return "Inactive";
        if (availableQuantity <= 0) return "Out of Stock";
        if (isLowStock()) return "Low Stock (" + availableQuantity + " left)";
        return "In Stock (" + availableQuantity + ")";
    }

    // --- Encapsulated Getters & Setters ---

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getEcoRating() {
        return ecoRating;
    }

    public void setEcoRating(int ecoRating) {
        this.ecoRating = ecoRating;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    @Override
    public String toString() {
        return productName;
    }
}
