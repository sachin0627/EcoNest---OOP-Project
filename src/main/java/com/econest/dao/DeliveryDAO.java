package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.Delivery;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Delivery Scheduling, Tracking, and Agent Assignment.
 */
public class DeliveryDAO {

    public List<Delivery> getAllDeliveries() {
        List<Delivery> list = new ArrayList<>();
        String sql = "SELECT d.*, a.full_name AS agent_name, c.client_name, c.phone AS client_phone, o.delivery_address " +
                     "FROM deliveries d " +
                     "JOIN delivery_agents a ON d.agent_id = a.agent_id " +
                     "JOIN orders o ON d.order_id = o.order_id " +
                     "JOIN clients c ON o.client_id = c.client_id " +
                     "ORDER BY d.assigned_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToDelivery(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving deliveries: " + e.getMessage(), e);
        }
        return list;
    }

    public Delivery getDeliveryById(String deliveryId) {
        String sql = "SELECT d.*, a.full_name AS agent_name, c.client_name, c.phone AS client_phone, o.delivery_address " +
                     "FROM deliveries d " +
                     "JOIN delivery_agents a ON d.agent_id = a.agent_id " +
                     "JOIN orders o ON d.order_id = o.order_id " +
                     "JOIN clients c ON o.client_id = c.client_id " +
                     "WHERE d.delivery_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, deliveryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDelivery(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving delivery [" + deliveryId + "]: " + e.getMessage(), e);
        }
        return null;
    }

    public Delivery getDeliveryByOrderId(String orderId) {
        String sql = "SELECT d.*, a.full_name AS agent_name, c.client_name, c.phone AS client_phone, o.delivery_address " +
                     "FROM deliveries d " +
                     "JOIN delivery_agents a ON d.agent_id = a.agent_id " +
                     "JOIN orders o ON d.order_id = o.order_id " +
                     "JOIN clients c ON o.client_id = c.client_id " +
                     "WHERE d.order_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDelivery(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving delivery for order [" + orderId + "]: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Assigns a delivery agent to an order within a database transaction.
     * Inserts into deliveries and updates agent availability to 'Assigned'.
     */
    public boolean assignDelivery(Delivery d) {
        String insertSql = "INSERT INTO deliveries (delivery_id, order_id, agent_id, delivery_date, delivery_status, tracking_notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        String updateAgentSql = "UPDATE delivery_agents SET availability_status = 'Assigned' WHERE agent_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, d.getDeliveryId());
                stmt.setString(2, d.getOrderId());
                stmt.setString(3, d.getAgentId());
                stmt.setDate(4, new java.sql.Date(d.getDeliveryDate().getTime()));
                stmt.setString(5, d.getDeliveryStatus() != null ? d.getDeliveryStatus() : "Assigned");
                stmt.setString(6, d.getTrackingNotes());
                stmt.executeUpdate();
            }

            try (PreparedStatement agentStmt = conn.prepareStatement(updateAgentSql)) {
                agentStmt.setString(1, d.getAgentId());
                agentStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Error assigning delivery: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateDeliveryStatus(String deliveryId, String status, String notes) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Get delivery info first
            String getSql = "SELECT order_id, agent_id FROM deliveries WHERE delivery_id = ?";
            String orderId = null;
            String agentId = null;
            try (PreparedStatement getStmt = conn.prepareStatement(getSql)) {
                getStmt.setString(1, deliveryId);
                try (ResultSet rs = getStmt.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getString("order_id");
                        agentId = rs.getString("agent_id");
                    }
                }
            }

            if (orderId == null) {
                throw new DatabaseException("Delivery [" + deliveryId + "] not found!");
            }

            // 2. Update delivery record
            String updateSql = "UPDATE deliveries SET delivery_status = ?, tracking_notes = ?, " +
                               "completed_date = CASE WHEN ? = 'Delivered' THEN NOW() ELSE completed_date END " +
                               "WHERE delivery_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, status);
                stmt.setString(2, notes);
                stmt.setString(3, status);
                stmt.setString(4, deliveryId);
                stmt.executeUpdate();
            }

            // 3. Sync Order Status & Agent Availability
            if ("Delivered".equalsIgnoreCase(status)) {
                // Free agent
                try (PreparedStatement agentStmt = conn.prepareStatement("UPDATE delivery_agents SET availability_status = 'Available' WHERE agent_id = ?")) {
                    agentStmt.setString(1, agentId);
                    agentStmt.executeUpdate();
                }
                // Mark order Delivered
                try (PreparedStatement orderStmt = conn.prepareStatement("UPDATE orders SET order_status = 'Delivered' WHERE order_id = ?")) {
                    orderStmt.setString(1, orderId);
                    orderStmt.executeUpdate();
                }
            } else if ("In Transit".equalsIgnoreCase(status) || "Picked Up".equalsIgnoreCase(status)) {
                // Mark order Dispatched
                try (PreparedStatement orderStmt = conn.prepareStatement("UPDATE orders SET order_status = 'Dispatched' WHERE order_id = ?")) {
                    orderStmt.setString(1, orderId);
                    orderStmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Error updating delivery status: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public int getActiveDeliveryCount() {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE delivery_status IN ('Assigned', 'Picked Up', 'In Transit')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    public String getNextDeliveryId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(delivery_id, 5) AS UNSIGNED)) AS max_id FROM deliveries";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int next = rs.getInt("max_id") + 1;
                return String.format("DEL-%04d", next > 5000 ? next : 5001);
            }
        } catch (SQLException ignored) {}
        return "DEL-5001";
    }

    private Delivery mapRowToDelivery(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setDeliveryId(rs.getString("delivery_id"));
        d.setOrderId(rs.getString("order_id"));
        d.setAgentId(rs.getString("agent_id"));
        d.setAgentName(rs.getString("agent_name"));
        d.setClientName(rs.getString("client_name"));
        d.setClientPhone(rs.getString("client_phone"));
        d.setDeliveryAddress(rs.getString("delivery_address"));
        d.setAssignedDate(rs.getTimestamp("assigned_date"));
        d.setDeliveryDate(rs.getDate("delivery_date"));
        d.setDeliveryStatus(rs.getString("delivery_status"));
        d.setTrackingNotes(rs.getString("tracking_notes"));
        d.setCompletedDate(rs.getTimestamp("completed_date"));
        return d;
    }
}
