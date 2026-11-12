package com.econest.dao;

import com.econest.exception.DatabaseException;
import com.econest.model.EmailNotification;
import com.econest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Email Notification Auditing.
 */
public class NotificationDAO {

    public boolean logNotification(EmailNotification n) {
        String sql = "INSERT INTO email_notifications (recipient_email, recipient_name, recipient_type, subject, content, reference_id, status, error_message) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, n.getRecipientEmail());
            stmt.setString(2, n.getRecipientName());
            stmt.setString(3, n.getRecipientType());
            stmt.setString(4, n.getSubject());
            stmt.setString(5, n.getContent());
            stmt.setString(6, n.getReferenceId());
            stmt.setString(7, n.getStatus());
            stmt.setString(8, n.getErrorMessage());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to log notification: " + e.getMessage());
            return false;
        }
    }

    public List<EmailNotification> getRecentNotifications(int limit) {
        List<EmailNotification> list = new ArrayList<>();
        String sql = "SELECT * FROM email_notifications ORDER BY sent_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmailNotification n = new EmailNotification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setRecipientEmail(rs.getString("recipient_email"));
                    n.setRecipientName(rs.getString("recipient_name"));
                    n.setRecipientType(rs.getString("recipient_type"));
                    n.setSubject(rs.getString("subject"));
                    n.setContent(rs.getString("content"));
                    n.setReferenceId(rs.getString("reference_id"));
                    n.setStatus(rs.getString("status"));
                    n.setErrorMessage(rs.getString("error_message"));
                    n.setSentAt(rs.getTimestamp("sent_at"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving notifications: " + e.getMessage(), e);
        }
        return list;
    }
}
