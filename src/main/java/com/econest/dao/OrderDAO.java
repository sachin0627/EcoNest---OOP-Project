package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.exception.InsufficientStockException;
import com.econest.model.Order;
import com.econest.model.OrderItem;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for Client Orders.
 * 
 * Demonstrates:
 * - Transaction Management: Atomically verifies stock, deducts inventory, records movements, and confirms order.
 * - Rollback on InsufficientStockException.
 * - Aggregate reporting queries for JFreeChart and financial analytics.
 */
public class OrderDAO {

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.client_name FROM orders o " +
                     "JOIN clients c ON o.client_id = c.client_id " +
                     "ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving orders: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.client_name FROM orders o " +
                     "JOIN clients c ON o.client_id = c.client_id " +
                     "ORDER BY o.order_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving recent orders: " + e.getMessage(), e);
        }
        return list;
    }

    public Order getOrderById(String orderId) {
        String sql = "SELECT o.*, c.client_name FROM orders o " +
                     "JOIN clients c ON o.client_id = c.client_id " +
                     "WHERE o.order_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapRowToOrder(rs);
                    order.setItems(getOrderItems(conn, orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving order [" + orderId + "]: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Order> searchOrders(String keyword, String status) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT o.*, c.client_name FROM orders o " +
            "JOIN clients c ON o.client_id = c.client_id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(o.order_id) LIKE ? OR LOWER(c.client_name) LIKE ? OR LOWER(o.delivery_address) LIKE ?)");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All Statuses")) {
            sql.append(" AND o.order_status = ?");
            params.add(status.trim());
        }

        sql.append(" ORDER BY o.order_date DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching orders: " + e.getMessage(), e);
        }
        return list;
    }

    public List<OrderItem> getOrderItems(Connection conn, String orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.product_name FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setOrderId(rs.getString("order_id"));
                    item.setProductId(rs.getString("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public boolean createOrder(Order order) {
        String insertOrderSql = "INSERT INTO orders (order_id, client_id, subtotal, discount, total_amount, " +
                "delivery_address, order_status, payment_method, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(insertOrderSql)) {
                stmt.setString(1, order.getOrderId());
                stmt.setString(2, order.getClientId());
                stmt.setDouble(3, order.getSubtotal());
                stmt.setDouble(4, order.getDiscount());
                stmt.setDouble(5, order.getTotalAmount());
                stmt.setString(6, order.getDeliveryAddress());
                stmt.setString(7, order.getOrderStatus());
                stmt.setString(8, order.getPaymentMethod());
                stmt.setString(9, order.getNotes());
                stmt.executeUpdate();
            }

            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                for (OrderItem item : order.getItems()) {
                    itemStmt.setString(1, order.getOrderId());
                    itemStmt.setString(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getUnitPrice());
                    itemStmt.setDouble(5, item.getSubtotal());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Error creating order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Confirms an order within a database transaction:
     * 1. Validates available stock for every line item.
     * 2. Throws InsufficientStockException and rolls back if stock is deficient.
     * 3. Deducts available inventory and products quantity.
     * 4. Logs audit record in stock_movements.
     * 5. Updates order status to 'Confirmed'.
     */
    public boolean confirmOrder(String orderId) throws InsufficientStockException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            List<OrderItem> items = getOrderItems(conn, orderId);
            if (items.isEmpty()) {
                throw new DatabaseException("Order [" + orderId + "] contains no items!");
            }

            // Step 1: Check and reserve stock
            String checkStockSql = "SELECT available_quantity FROM inventory WHERE product_id = ? FOR UPDATE";
            String deductInvSql = "UPDATE inventory SET available_quantity = available_quantity - ? WHERE product_id = ?";
            String deductPrdSql = "UPDATE products SET available_quantity = available_quantity - ? WHERE product_id = ?";
            String logMovementSql = "INSERT INTO stock_movements (product_id, movement_type, quantity, reference_order_id, remarks) " +
                    "VALUES (?, 'ORDER_OUT', ?, ?, 'Stock deduction on order confirmation')";

            for (OrderItem item : items) {
                int available = 0;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql)) {
                    checkStmt.setString(1, item.getProductId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            available = rs.getInt("available_quantity");
                        } else {
                            throw new DatabaseException("Product not found in inventory: " + item.getProductId());
                        }
                    }
                }

                if (item.getQuantity() > available) {
                    conn.rollback();
                    throw new InsufficientStockException(item.getProductId(), item.getQuantity(), available);
                }

                // Deduct from inventory
                try (PreparedStatement deductStmt = conn.prepareStatement(deductInvSql)) {
                    deductStmt.setInt(1, item.getQuantity());
                    deductStmt.setString(2, item.getProductId());
                    deductStmt.executeUpdate();
                }

                // Deduct from products
                try (PreparedStatement deductPrdStmt = conn.prepareStatement(deductPrdSql)) {
                    deductPrdStmt.setInt(1, item.getQuantity());
                    deductPrdStmt.setString(2, item.getProductId());
                    deductPrdStmt.executeUpdate();
                }

                // Log movement
                try (PreparedStatement moveStmt = conn.prepareStatement(logMovementSql)) {
                    moveStmt.setString(1, item.getProductId());
                    moveStmt.setInt(2, item.getQuantity());
                    moveStmt.setString(3, orderId);
                    moveStmt.executeUpdate();
                }
            }

            // Step 2: Mark order Confirmed
            String updateOrderSql = "UPDATE orders SET order_status = 'Confirmed' WHERE order_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateOrderSql)) {
                updateStmt.setString(1, orderId);
                updateStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Transaction rollback during order confirmation: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateOrderStatus(String orderId, String newStatus) {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating order status: " + e.getMessage(), e);
        }
    }

    public boolean cancelOrder(String orderId) {
        // Update order status to Cancelled
        String sql = "UPDATE orders SET order_status = 'Cancelled' WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error cancelling order: " + e.getMessage(), e);
        }
    }

    public String getNextOrderId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(order_id, 5) AS UNSIGNED)) AS max_id FROM orders";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int next = rs.getInt("max_id") + 1;
                return String.format("ORD-%04d", next > 4000 ? next : 4001);
            }
        } catch (SQLException ignored) {}
        return "ORD-4001";
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM orders WHERE order_status NOT IN ('Cancelled', 'Pending')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException ignored) {}
        return 0.0;
    }

    public int getTotalOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    /**
     * Retrieves monthly sales aggregates for JFreeChart.
     * @return Map of Month string (e.g. "2026-10", "2026-11") to revenue
     */
    public Map<String, Double> getMonthlySalesData() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(order_date, '%Y-%m') AS sale_month, SUM(total_amount) AS revenue " +
                     "FROM orders WHERE order_status NOT IN ('Cancelled') " +
                     "GROUP BY sale_month ORDER BY sale_month ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("sale_month"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting monthly sales: " + e.getMessage());
        }
        return map;
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getString("order_id"));
        o.setClientId(rs.getString("client_id"));
        o.setClientName(rs.getString("client_name"));
        o.setOrderDate(rs.getTimestamp("order_date"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setDiscount(rs.getDouble("discount"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setOrderStatus(rs.getString("order_status"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setNotes(rs.getString("notes"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        return o;
    }
}
