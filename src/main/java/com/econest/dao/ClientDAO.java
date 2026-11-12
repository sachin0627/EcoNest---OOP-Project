package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.Client;
import com.econest.model.HouseholdClient;
import com.econest.model.OfficeClient;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Client (Customer) management.
 * 
 * Demonstrates:
 * - Polymorphic mapping: instantiates HouseholdClient or OfficeClient based on database column.
 */
public class ClientDAO {

    public List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE is_active = TRUE ORDER BY client_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToClient(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving clients: " + e.getMessage(), e);
        }
        return list;
    }

    public Client getClientById(String clientId) {
        String sql = "SELECT * FROM clients WHERE client_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToClient(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving client [" + clientId + "]: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Client> searchClients(String keyword, String clientType) {
        List<Client> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM clients WHERE is_active = TRUE");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(client_name) LIKE ? OR LOWER(client_id) LIKE ? OR LOWER(email) LIKE ? OR LOWER(phone) LIKE ? OR LOWER(city) LIKE ?)");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            for (int i = 0; i < 5; i++) params.add(term);
        }

        if (clientType != null && !clientType.trim().isEmpty() && !clientType.equalsIgnoreCase("All Types")) {
            sql.append(" AND client_type = ?");
            params.add(clientType.trim());
        }

        sql.append(" ORDER BY client_id ASC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToClient(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching clients: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addClient(Client c) {
        String sql = "INSERT INTO clients (client_id, client_name, client_type, email, phone, address, city, registration_date, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getClientId());
            stmt.setString(2, c.getClientName());
            stmt.setString(3, c.getClientType());
            stmt.setString(4, c.getEmail());
            stmt.setString(5, c.getPhone());
            stmt.setString(6, c.getAddress());
            stmt.setString(7, c.getCity());
            stmt.setDate(8, new java.sql.Date(c.getRegistrationDate().getTime()));
            stmt.setBoolean(9, c.isActive());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error adding client: " + e.getMessage(), e);
        }
    }

    public boolean updateClient(Client c) {
        String sql = "UPDATE clients SET client_name = ?, client_type = ?, email = ?, phone = ?, address = ?, city = ?, is_active = ? " +
                "WHERE client_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getClientName());
            stmt.setString(2, c.getClientType());
            stmt.setString(3, c.getEmail());
            stmt.setString(4, c.getPhone());
            stmt.setString(5, c.getAddress());
            stmt.setString(6, c.getCity());
            stmt.setBoolean(7, c.isActive());
            stmt.setString(8, c.getClientId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating client: " + e.getMessage(), e);
        }
    }

    public boolean deleteClient(String clientId) {
        // Soft delete to protect relational order history
        String sql = "UPDATE clients SET is_active = FALSE WHERE client_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting client: " + e.getMessage(), e);
        }
    }

    public String getNextClientId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(client_id, 5) AS UNSIGNED)) AS max_id FROM clients";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int next = rs.getInt("max_id") + 1;
                return String.format("CLT-%04d", next > 2000 ? next : 2001);
            }
        } catch (SQLException ignored) {}
        return "CLT-2001";
    }

    /**
     * Polymorphic instantiation based on client_type column.
     */
    private Client mapRowToClient(ResultSet rs) throws SQLException {
        String type = rs.getString("client_type");
        Client client;

        if ("Office".equalsIgnoreCase(type)) {
            client = new OfficeClient();
        } else {
            client = new HouseholdClient();
        }

        client.setClientId(rs.getString("client_id"));
        client.setClientName(rs.getString("client_name"));
        client.setClientType(type);
        client.setEmail(rs.getString("email"));
        client.setPhone(rs.getString("phone"));
        client.setAddress(rs.getString("address"));
        client.setCity(rs.getString("city"));
        client.setRegistrationDate(rs.getDate("registration_date"));
        client.setActive(rs.getBoolean("is_active"));

        return client;
    }
}
