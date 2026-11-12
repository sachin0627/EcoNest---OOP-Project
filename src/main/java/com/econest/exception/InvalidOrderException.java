package com.econest.exception;

/**
 * Custom Exception thrown when an order validation fails, such as missing items,
 * zero quantities, non-existent client, or illegal status transitions.
 * 
 * Demonstrates: Custom Exception Class for Domain Rule Validation.
 */
public class InvalidOrderException extends Exception {
    private final String orderId;

    public InvalidOrderException(String message) {
        super(message);
        this.orderId = null;
    }

    public InvalidOrderException(String orderId, String message) {
        super(String.format("Order [%s] validation failed: %s", orderId, message));
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
