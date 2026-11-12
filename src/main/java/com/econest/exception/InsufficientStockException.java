package com.econest.exception;

/**
 * Custom Exception thrown when an order quantity exceeds the current stock
 * available in inventory, or when minimum reorder constraints are breached.
 * 
 * Demonstrates: Custom Exception Handling & Business Constraint Enforcement.
 */
public class InsufficientStockException extends Exception {
    private final String productId;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String productId, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for Product '%s'! Requested: %d, Available: %d", 
                productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(String message) {
        super(message);
        this.productId = "UNKNOWN";
        this.requestedQuantity = 0;
        this.availableQuantity = 0;
    }

    public String getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
