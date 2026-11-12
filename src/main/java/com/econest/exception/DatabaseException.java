package com.econest.exception;

/**
 * Custom Exception thrown when database connectivity fails, SQL constraints are violated,
 * or transaction rollback occurs in the Data Access Layer (DAO).
 * 
 * Demonstrates: Encapsulation of low-level SQLExceptions into domain-level unchecked/checked exceptions.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
