package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.DeliveryAgent;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Delivery Agents.
 */
public class DeliveryAgentDAO {

    public List<DeliveryAgent> getAllAgents() {
        List<DeliveryAgent> list = new ArrayList<>();
        String sql = "SELECT * FROM delivery_agents WHERE is_active = TRUE ORDER BY agent_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToAgent(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving delivery agents: " + e.getMessage(), e);
        }
        return list;
    }

    public List<DeliveryAgent> getAvailableAgents() {
        List<DeliveryAgent> list = new ArrayList<>();
        String sql = "SELECT * FROM delivery_agents WHERE is_active = TRUE AND availability_status = 'Available' ORDER BY full_name ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToAgent(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving available delivery agents: " + e.getMessage(), e);
        }
        return list;
    }

    public DeliveryAgent getAgentById(String agentId) {
        String sql = "SELECT * FROM delivery_agents WHERE agent_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, agentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAgent(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving agent [" + agentId + "]: " + e.getMessage(), e);
        }
        return null;
    }

    public List<DeliveryAgent> searchAgents(String keyword, String status) {
        List<DeliveryAgent> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM delivery_agents WHERE is_active = TRUE");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(full_name) LIKE ? OR LOWER(agent_id) LIKE ? OR LOWER(phone) LIKE ? OR LOWER(vehicle_reg_no) LIKE ?)");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            for (int i = 0; i < 4; i++) params.add(term);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All Statuses")) {
            sql.append(" AND availability_status = ?");
            params.add(status.trim());
        }

        sql.append(" ORDER BY agent_id ASC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAgent(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching delivery agents: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addAgent(DeliveryAgent a) {
        String sql = "INSERT INTO delivery_agents (agent_id, full_name, phone, email, vehicle_type, vehicle_reg_no, availability_status, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getAgentId());
            stmt.setString(2, a.getFullName());
            stmt.setString(3, a.getPhone());
            stmt.setString(4, a.getEmail());
            stmt.setString(5, a.getVehicleType());
            stmt.setString(6, a.getVehicleRegNo());
            stmt.setString(7, a.getAvailabilityStatus());
            stmt.setBoolean(8, a.isActive());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error adding delivery agent: " + e.getMessage(), e);
        }
    }

    public boolean updateAgent(DeliveryAgent a) {
        String sql = "UPDATE delivery_agents SET full_name = ?, phone = ?, email = ?, vehicle_type = ?, vehicle_reg_no = ?, " +
                     "availability_status = ?, is_active = ? WHERE agent_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getFullName());
            stmt.setString(2, a.getPhone());
            stmt.setString(3, a.getEmail());
            stmt.setString(4, a.getVehicleType());
            stmt.setString(5, a.getVehicleRegNo());
            stmt.setString(6, a.getAvailabilityStatus());
            stmt.setBoolean(7, a.isActive());
            stmt.setString(8, a.getAgentId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating delivery agent: " + e.getMessage(), e);
        }
    }

    public boolean updateAgentStatus(String agentId, String newStatus) {
        String sql = "UPDATE delivery_agents SET availability_status = ? WHERE agent_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, agentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating agent status: " + e.getMessage(), e);
        }
    }

    public boolean deleteAgent(String agentId) {
        String sql = "UPDATE delivery_agents SET is_active = FALSE WHERE agent_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, agentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting agent: " + e.getMessage(), e);
        }
    }

    public String getNextAgentId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(agent_id, 5) AS UNSIGNED)) AS max_id FROM delivery_agents";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int next = rs.getInt("max_id") + 1;
                return String.format("AGT-%04d", next > 3000 ? next : 3001);
            }
        } catch (SQLException ignored) {}
        return "AGT-3001";
    }

    private DeliveryAgent mapRowToAgent(ResultSet rs) throws SQLException {
        DeliveryAgent a = new DeliveryAgent();
        a.setAgentId(rs.getString("agent_id"));
        a.setFullName(rs.getString("full_name"));
        a.setPhone(rs.getString("phone"));
        a.setEmail(rs.getString("email"));
        a.setVehicleType(rs.getString("vehicle_type"));
        a.setVehicleRegNo(rs.getString("vehicle_reg_no"));
        a.setAvailabilityStatus(rs.getString("availability_status"));
        a.setActive(rs.getBoolean("is_active"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
