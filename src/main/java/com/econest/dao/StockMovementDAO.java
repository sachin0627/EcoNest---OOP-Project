package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.StockMovement;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Stock Movement history.
 */
public class StockMovementDAO {

    public List<StockMovement> getRecentMovements(int limit) {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT m.*, p.product_name, s.supplier_name " +
                     "FROM stock_movements m " +
                     "LEFT JOIN products p ON m.product_id = p.product_id " +
                     "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                     "ORDER BY m.movement_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StockMovement sm = new StockMovement();
                    sm.setMovementId(rs.getInt("movement_id"));
                    sm.setProductId(rs.getString("product_id"));
                    sm.setProductName(rs.getString("product_name"));
                    sm.setMovementType(rs.getString("movement_type"));
                    sm.setQuantity(rs.getInt("quantity"));
                    sm.setSupplierId(rs.getString("supplier_id"));
                    sm.setSupplierName(rs.getString("supplier_name"));
                    sm.setReferenceOrderId(rs.getString("reference_order_id"));
                    sm.setRemarks(rs.getString("remarks"));
                    sm.setMovementDate(rs.getTimestamp("movement_date"));
                    list.add(sm);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving stock movements: " + e.getMessage(), e);
        }
        return list;
    }
}
