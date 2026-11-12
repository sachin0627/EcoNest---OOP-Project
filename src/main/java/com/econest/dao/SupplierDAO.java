package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.Supplier;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Suppliers.
 */
public class SupplierDAO {

    public List<Supplier> getAllSuppliers() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY supplier_name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Supplier s = new Supplier();
                s.setSupplierId(rs.getString("supplier_id"));
                s.setSupplierName(rs.getString("supplier_name"));
                s.setContactPerson(rs.getString("contact_person"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setAddress(rs.getString("address"));
                s.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(s);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving suppliers: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addSupplier(Supplier s) {
        String sql = "INSERT INTO suppliers (supplier_id, supplier_name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getSupplierId());
            stmt.setString(2, s.getSupplierName());
            stmt.setString(3, s.getContactPerson());
            stmt.setString(4, s.getPhone());
            stmt.setString(5, s.getEmail());
            stmt.setString(6, s.getAddress());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error adding supplier: " + e.getMessage(), e);
        }
    }
}
