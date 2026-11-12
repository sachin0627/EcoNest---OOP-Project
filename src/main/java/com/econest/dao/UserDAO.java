package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.Admin;
import com.econest.model.Staff;
import com.econest.model.User;
import com.econest.util.DatabaseConnection;
import com.econest.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User management and authentication.
 * 
 * Demonstrates:
 * - Data Access Layer abstraction
 * - Polymorphic object creation (instantiates Admin or Staff based on role)
 * - PreparedStatements for SQL injection prevention
 */
public class UserDAO {

    /**
     * Authenticates a user given username/email and plain text password.
     * @param identifier username or email
     * @param plainPassword input password
     * @return User object (Admin or Staff) if valid, null otherwise
     */
    public User authenticate(String identifier, String plainPassword) {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, identifier.trim());
            stmt.setString(2, identifier.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String salt = rs.getString("salt");
                    String storedHash = rs.getString("password_hash");

                    if (PasswordUtil.verifyPassword(plainPassword, salt, storedHash)) {
                        return mapRowToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error during user authentication: " + e.getMessage(), e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving users: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addUser(User user, String plainPassword) {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, email, role, phone, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, hash);
            stmt.setString(3, salt);
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.getPhone());
            stmt.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating user: " + e.getMessage(), e);
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, phone = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getStatus());
            stmt.setInt(6, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user: " + e.getMessage(), e);
        }
    }

    public boolean updatePassword(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE user_id = ?";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(newPlainPassword, salt);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hash);
            stmt.setString(2, salt);
            stmt.setInt(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating password: " + e.getMessage(), e);
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting user: " + e.getMessage(), e);
        }
    }

    /**
     * Polymorphic Factory mapping: instantiates Admin or Staff based on role column.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;

        if ("ADMIN".equalsIgnoreCase(role)) {
            user = new Admin();
        } else {
            user = new Staff();
        }

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));

        return user;
    }
}
