package com.econest.util;

import com.econest.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton Database Connection Manager for MySQL.
 * 
 * Demonstrates:
 * - Singleton Design Pattern (private constructor, static instance, getInstance)
 * - Encapsulation of JDBC credentials and connection lifecycle
 * - Transaction management helpers (commit, rollback, autoCommit)
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnection() {
        // Initialize environment configuration
        EnvConfig.init();

        String host = EnvConfig.get("DB_HOST", "localhost");
        String port = EnvConfig.get("DB_PORT", "3306");
        String dbName = EnvConfig.get("DB_NAME", "econest_db");
        
        String defaultUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            host, port, dbName
        );
        this.url = EnvConfig.get("DB_URL", defaultUrl);
        this.username = EnvConfig.get("DB_USER", "root");
        this.password = EnvConfig.get("DB_PASSWORD", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC Driver 'com.mysql.cj.jdbc.Driver' not found on classpath!", e);
        }
    }

    /**
     * Thread-safe Singleton access point.
     * @return DatabaseConnection singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Gets a live database connection, automatically reconnecting if closed.
     * @return active java.sql.Connection
     * @throws DatabaseException if connection fails
     */
    public synchronized Connection getConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = DriverManager.getConnection(url, username, password);
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException(
                "Failed to connect to MySQL database (" + url + "). Please ensure MySQL Server is running!\nDetails: " + e.getMessage(), 
                e
            );
        }
    }

    /**
     * Checks if the database is online and reachable.
     * @return true if connected successfully
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely closes the current connection if open.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }
}
