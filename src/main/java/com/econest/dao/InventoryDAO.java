package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.Inventory;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Inventory & Stock Tracking.
 */
public class InventoryDAO {

    public List<Inventory> getAllInventory() {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT i.inventory_id, i.product_id, p.product_name, p.category, p.unit_price, " +
                     "i.available_quantity, i.reorder_level, i.last_updated " +
                     "FROM inventory i " +
                     "JOIN products p ON i.product_id = p.product_id " +
                     "WHERE p.is_active = TRUE " +
                     "ORDER BY i.available_quantity ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToInventory(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving inventory list: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Inventory> getLowStockItems() {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT i.inventory_id, i.product_id, p.product_name, p.category, p.unit_price, " +
                     "i.available_quantity, i.reorder_level, i.last_updated " +
                     "FROM inventory i " +
                     "JOIN products p ON i.product_id = p.product_id " +
                     "WHERE p.is_active = TRUE AND i.available_quantity <= i.reorder_level " +
                     "ORDER BY i.available_quantity ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToInventory(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving low stock items: " + e.getMessage(), e);
        }
        return list;
    }

    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM inventory i JOIN products p ON i.product_id = p.product_id " +
                     "WHERE p.is_active = TRUE AND i.available_quantity <= i.reorder_level";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting low stock: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateStockAndReorderLevel(String productId, int newQuantity, int newReorderLevel) {
        String updateInv = "UPDATE inventory SET available_quantity = ?, reorder_level = ? WHERE product_id = ?";
        String updatePrd = "UPDATE products SET available_quantity = ?, reorder_level = ? WHERE product_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(updateInv);
                 PreparedStatement stmt2 = conn.prepareStatement(updatePrd)) {

                stmt1.setInt(1, newQuantity);
                stmt1.setInt(2, newReorderLevel);
                stmt1.setString(3, productId);
                stmt1.executeUpdate();

                stmt2.setInt(1, newQuantity);
                stmt2.setInt(2, newReorderLevel);
                stmt2.setString(3, productId);
                stmt2.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Error updating inventory for [" + productId + "]: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Executes a Supplier Stock-In operation using database transactions.
     * Updates inventory, products, and logs a stock_movement record atomically.
     */
    public boolean recordStockIn(String productId, int addQuantity, String supplierId, String remarks) {
        if (addQuantity <= 0) return false;

        String updateInv = "UPDATE inventory SET available_quantity = available_quantity + ? WHERE product_id = ?";
        String updatePrd = "UPDATE products SET available_quantity = available_quantity + ? WHERE product_id = ?";
        String insertMovement = "INSERT INTO stock_movements (product_id, movement_type, quantity, supplier_id, remarks) " +
                                "VALUES (?, 'STOCK_IN', ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtInv = conn.prepareStatement(updateInv);
                 PreparedStatement stmtPrd = conn.prepareStatement(updatePrd);
                 PreparedStatement stmtMove = conn.prepareStatement(insertMovement)) {

                stmtInv.setInt(1, addQuantity);
                stmtInv.setString(2, productId);
                stmtInv.executeUpdate();

                stmtPrd.setInt(1, addQuantity);
                stmtPrd.setString(2, productId);
                stmtPrd.executeUpdate();

                stmtMove.setString(1, productId);
                stmtMove.setInt(2, addQuantity);
                stmtMove.setString(3, supplierId);
                stmtMove.setString(4, remarks);
                stmtMove.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Transaction failed: Stock-In for [" + productId + "]: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    private Inventory mapRowToInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("inventory_id"));
        inv.setProductId(rs.getString("product_id"));
        inv.setProductName(rs.getString("product_name"));
        inv.setCategory(rs.getString("category"));
        inv.setUnitPrice(rs.getDouble("unit_price"));
        inv.setAvailableQuantity(rs.getInt("available_quantity"));
        inv.setReorderLevel(rs.getInt("reorder_level"));
        inv.setLastUpdated(rs.getTimestamp("last_updated"));
        return inv;
    }
}
