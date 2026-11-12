package com.econest.interfaces;

/**
 * Interface representing any business entity that can be managed
 * via CRUD operations in the EcoNest Retail Management System.
 * 
 * Demonstrates: OOP Interface & Abstraction.
 */
public interface Manageable {
    /**
     * Returns the unique business identifier of the entity (e.g. PRD-1001, CLT-2001).
     * @return unique entity ID
     */
    String getId();

    /**
     * Returns a human-friendly display name or label for UI tables and dropdowns.
     * @return display label
     */
    String getDisplayName();

    /**
     * Validates whether all mandatory business rules for this entity are satisfied.
     * @return true if valid, false otherwise
     */
    boolean isValid();

    /**
     * Returns a textual summary of current status (e.g. In Stock, Dispatched, Active).
     * @return status description
     */
    String getStatusDescription();
}
