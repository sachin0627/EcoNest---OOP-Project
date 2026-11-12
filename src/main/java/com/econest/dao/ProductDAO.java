package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.Product;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product catalogue management.
 */
public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE ORDER BY product_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving products: " + e.getMessage(), e);
        }
        return list;
    }

    public Product getProductById(String productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving product [" + productId + "]: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Product> searchProducts(String keyword, String category) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE is_active = TRUE");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(product_name) LIKE ? OR LOWER(product_id) LIKE ? OR LOWER(description) LIKE ?)");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All Categories")) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }

        sql.append(" ORDER BY product_id ASC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching products: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addProduct(Product p) {
        String insertProductSql = "INSERT INTO products (product_id, product_name, category, description, unit_price, " +
                "eco_rating, available_quantity, reorder_level, image_path, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertInventorySql = "INSERT INTO inventory (product_id, available_quantity, reorder_level) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE available_quantity = VALUES(available_quantity), reorder_level = VALUES(reorder_level)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(insertProductSql)) {
                stmt.setString(1, p.getProductId());
                stmt.setString(2, p.getProductName());
                stmt.setString(3, p.getCategory());
                stmt.setString(4, p.getDescription());
                stmt.setDouble(5, p.getUnitPrice());
                stmt.setInt(6, p.getEcoRating());
                stmt.setInt(7, p.getAvailableQuantity());
                stmt.setInt(8, p.getReorderLevel());
                stmt.setString(9, p.getImagePath());
                stmt.setBoolean(10, p.isActive());
                stmt.executeUpdate();
            }

            try (PreparedStatement invStmt = conn.prepareStatement(insertInventorySql)) {
                invStmt.setString(1, p.getProductId());
                invStmt.setInt(2, p.getAvailableQuantity());
                invStmt.setInt(3, p.getReorderLevel());
                invStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Error adding product: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateProduct(Product p) {
        String updateProductSql = "UPDATE products SET product_name = ?, category = ?, description = ?, " +
                "unit_price = ?, eco_rating = ?, available_quantity = ?, reorder_level = ?, image_path = ?, is_active = ? " +
                "WHERE product_id = ?";
        String updateInventorySql = "UPDATE inventory SET available_quantity = ?, reorder_level = ? WHERE product_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(updateProductSql)) {
                stmt.setString(1, p.getProductName());
                stmt.setString(2, p.getCategory());
                stmt.setString(3, p.getDescription());
                stmt.setDouble(4, p.getUnitPrice());
                stmt.setInt(5, p.getEcoRating());
                stmt.setInt(6, p.getAvailableQuantity());
                stmt.setInt(7, p.getReorderLevel());
                stmt.setString(8, p.getImagePath());
                stmt.setBoolean(9, p.isActive());
                stmt.setString(10, p.getProductId());
                stmt.executeUpdate();
            }

            try (PreparedStatement invStmt = conn.prepareStatement(updateInventorySql)) {
                invStmt.setInt(1, p.getAvailableQuantity());
                invStmt.setInt(2, p.getReorderLevel());
                invStmt.setString(3, p.getProductId());
                invStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Error updating product: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deleteProduct(String productId) {
        // Soft delete to preserve historical order integrity
        String sql = "UPDATE products SET is_active = FALSE WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting product [" + productId + "]: " + e.getMessage(), e);
        }
    }

    public String getNextProductId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(product_id, 5) AS UNSIGNED)) AS max_id FROM products";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int next = rs.getInt("max_id") + 1;
                return String.format("PRD-%04d", next > 1000 ? next : 1001);
            }
        } catch (SQLException ignored) {}
        return "PRD-1001";
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getString("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        p.setUnitPrice(rs.getDouble("unit_price"));
        p.setEcoRating(rs.getInt("eco_rating"));
        p.setAvailableQuantity(rs.getInt("available_quantity"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setImagePath(rs.getString("image_path"));
        p.setActive(rs.getBoolean("is_active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
