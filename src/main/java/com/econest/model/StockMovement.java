package com.econest.model;

import java.util.Date;

/**
 * Entity representing an immutable historical log of stock movement.
 * (Stock-In from supplier, Order-Out for dispatch, Adjustment, Return).
 */
public class StockMovement {
    private int movementId;
    private String productId;
    private String productName;
    private String movementType; // "STOCK_IN", "ORDER_OUT", "ADJUSTMENT", "RETURN"
    private int quantity;
    private String supplierId;
    private String supplierName;
    private String referenceOrderId;
    private String remarks;
    private Date movementDate;

    public StockMovement() {
        this.movementDate = new Date();
    }

    public StockMovement(int movementId, String productId, String productName, String movementType,
                         int quantity, String supplierId, String referenceOrderId, String remarks, Date movementDate) {
        this.movementId = movementId;
        this.productId = productId;
        this.productName = productName;
        this.movementType = movementType;
        this.quantity = quantity;
        this.supplierId = supplierId;
        this.referenceOrderId = referenceOrderId;
        this.remarks = remarks;
        this.movementDate = movementDate;
    }

    public int getMovementId() {
        return movementId;
    }

    public void setMovementId(int movementId) {
        this.movementId = movementId;
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

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getReferenceOrderId() {
        return referenceOrderId;
    }

    public void setReferenceOrderId(String referenceOrderId) {
        this.referenceOrderId = referenceOrderId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }
}
