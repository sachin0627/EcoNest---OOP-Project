package com.econest.service;

import com.econest.dao.NotificationDAO;
import com.econest.model.Delivery;
import com.econest.model.EmailNotification;
import com.econest.model.Order;
import com.econest.util.EnvConfig;
import com.econest.util.UITheme;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Service managing automated transactional emails using Jakarta Mail.
 * 
 * Supports:
 * - Module 08: Client Order Dispatched Notifications
 * - Module 09: Delivery Agent Assignment Notifications
 * - Secure credential handling via EnvConfig (never hardcoded)
 * - Transparent error reporting and audit logging in database
 */
public class EmailService {
    private final NotificationDAO notificationDAO;

    public EmailService() {
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Sends an EcoNest branded HTML email notification to a Client when an order is dispatched.
     */
    public boolean sendOrderDispatchedEmail(Order order, String clientEmail, String clientName, String trackingNotes) {
        String subject = "Your EcoNest Order #" + order.getOrderId() + " Has Been Dispatched!";
        String htmlContent = buildClientDispatchEmail(order, clientName, trackingNotes);

        return sendEmail(clientEmail, clientName, "CLIENT", subject, htmlContent, order.getOrderId());
    }

    /**
     * Sends an EcoNest branded HTML email notification to a Delivery Agent when a delivery is assigned.
     */
    public boolean sendDeliveryAssignedEmail(Delivery delivery, String agentEmail, String agentName) {
        String subject = "New Delivery Assignment: Order #" + delivery.getOrderId() + " (" + delivery.getDeliveryId() + ")";
        String htmlContent = buildAgentAssignmentEmail(delivery, agentName);

        return sendEmail(agentEmail, agentName, "AGENT", subject, htmlContent, delivery.getOrderId());
    }

    /**
     * Core SMTP dispatcher with fallback logging.
     */
    private boolean sendEmail(String toEmail, String recipientName, String recipientType,
                              String subject, String htmlBody, String referenceId) {
        EnvConfig.init();
        String host = EnvConfig.get("SMTP_HOST", "smtp.gmail.com");
        int port = EnvConfig.getInt("SMTP_PORT", 587);
        String username = EnvConfig.get("SMTP_USERNAME", "");
        String password = EnvConfig.get("SMTP_PASSWORD", "");
        String fromEmail = EnvConfig.get("SMTP_FROM_EMAIL", username.isEmpty() ? "notifications@econest.lk" : username);
        String fromName = EnvConfig.get("SMTP_FROM_NAME", "EcoNest Retail Notifications");

        // Validate if SMTP credentials are provided
        if (username.isEmpty() || password.isEmpty() || password.equalsIgnoreCase("your_app_password_here")) {
            String errorMsg = "SMTP credentials not configured in .env (SMTP_USERNAME / SMTP_PASSWORD). " +
                              "Email notification recorded to database audit log.";
            System.err.println("[EmailService Warning] " + errorMsg);
            
            // Log as PENDING / SKIPPED with clear notification
            EmailNotification notif = new EmailNotification(
                toEmail, recipientName, recipientType, subject, htmlBody, referenceId, "PENDING", errorMsg
            );
            notificationDAO.logNotification(notif);
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);

            // Log successful delivery
            EmailNotification notif = new EmailNotification(
                toEmail, recipientName, recipientType, subject, htmlBody, referenceId, "SENT", null
            );
            notificationDAO.logNotification(notif);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailService Error] Failed to send email to " + toEmail + ": " + e.getMessage());
            // Log failed delivery
            EmailNotification notif = new EmailNotification(
                toEmail, recipientName, recipientType, subject, htmlBody, referenceId, "FAILED", e.getMessage()
            );
            notificationDAO.logNotification(notif);
            return false;
        }
    }

    private String buildClientDispatchEmail(Order order, String clientName, String trackingNotes) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #F7F8F2; margin: 0; padding: 20px; color: #1B2E24; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #FFFFFF; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.06); border: 1px solid #DEE2D9; }" +
                ".header { background: #245C43; color: #FFFFFF; padding: 24px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; letter-spacing: 1px; }" +
                ".header p { margin: 6px 0 0; color: #8FAF91; font-size: 13px; }" +
                ".content { padding: 28px 24px; }" +
                ".badge { display: inline-block; background: #E9A45B; color: #FFFFFF; padding: 6px 14px; border-radius: 20px; font-weight: bold; font-size: 12px; }" +
                ".card { background: #F7F8F2; border-left: 4px solid #245C43; padding: 14px 18px; border-radius: 6px; margin: 18px 0; }" +
                ".footer { background: #F1F3ED; text-align: center; padding: 18px; font-size: 12px; color: #6A7B72; border-top: 1px solid #DEE2D9; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "  <div class='header'>" +
                "    <h1>EcoNest</h1>" +
                "    <p>Sustainable Smart Home &amp; Garden Retail</p>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <span class='badge'>ORDER DISPATCHED</span>" +
                "    <h2 style='color:#245C43; margin-top:14px;'>Good news, " + clientName + "!</h2>" +
                "    <p>Your eco-friendly package for Order <strong>#" + order.getOrderId() + "</strong> has been carefully packed and dispatched with our delivery team.</p>" +
                "    <div class='card'>" +
                "      <p style='margin:4px 0;'><strong>Order ID:</strong> " + order.getOrderId() + "</p>" +
                "      <p style='margin:4px 0;'><strong>Total Amount:</strong> " + UITheme.formatCurrency(order.getTotalAmount()) + "</p>" +
                "      <p style='margin:4px 0;'><strong>Delivery Address:</strong> " + order.getDeliveryAddress() + "</p>" +
                "      <p style='margin:4px 0;'><strong>Tracking Remarks:</strong> " + (trackingNotes != null ? trackingNotes : "En-route for safe delivery") + "</p>" +
                "    </div>" +
                "    <p>Our delivery partner will reach your destination shortly. Please ensure someone is available at the premises to receive your plants.</p>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    EcoNest Retail Solutions &bull; 88, Green Park Way, Colombo 03, Sri Lanka<br>" +
                "    Telephone: +94 11 254 7890 &bull; Email: info@econest.lk" +
                "  </div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildAgentAssignmentEmail(Delivery delivery, String agentName) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #F7F8F2; margin: 0; padding: 20px; color: #1B2E24; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #FFFFFF; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.06); border: 1px solid #DEE2D9; }" +
                ".header { background: #245C43; color: #FFFFFF; padding: 24px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".header p { margin: 6px 0 0; color: #8FAF91; font-size: 13px; }" +
                ".content { padding: 28px 24px; }" +
                ".badge { display: inline-block; background: #245C43; color: #FFFFFF; padding: 6px 14px; border-radius: 20px; font-weight: bold; font-size: 12px; }" +
                ".card { background: #F7F8F2; border-left: 4px solid #E9A45B; padding: 14px 18px; border-radius: 6px; margin: 18px 0; }" +
                ".footer { background: #F1F3ED; text-align: center; padding: 18px; font-size: 12px; color: #6A7B72; border-top: 1px solid #DEE2D9; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "  <div class='header'>" +
                "    <h1>EcoNest Logistics</h1>" +
                "    <p>Delivery Personnel Dispatch System</p>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <span class='badge'>NEW DELIVERY ASSIGNED</span>" +
                "    <h2 style='color:#245C43; margin-top:14px;'>Hello, " + agentName + "!</h2>" +
                "    <p>You have been assigned to deliver order <strong>#" + delivery.getOrderId() + "</strong>. Please review the delivery details below:</p>" +
                "    <div class='card'>" +
                "      <p style='margin:4px 0;'><strong>Delivery Ref:</strong> " + delivery.getDeliveryId() + "</p>" +
                "      <p style='margin:4px 0;'><strong>Order ID:</strong> " + delivery.getOrderId() + "</p>" +
                "      <p style='margin:4px 0;'><strong>Client Name:</strong> " + delivery.getClientName() + "</p>" +
                "      <p style='margin:4px 0;'><strong>Contact Phone:</strong> " + (delivery.getClientPhone() != null ? delivery.getClientPhone() : "N/A") + "</p>" +
                "      <p style='margin:4px 0;'><strong>Delivery Address:</strong> " + delivery.getDeliveryAddress() + "</p>" +
                "      <p style='margin:4px 0;'><strong>Scheduled Date:</strong> " + UITheme.formatDate(delivery.getDeliveryDate()) + "</p>" +
                "    </div>" +
                "    <p>Please handle all live plants and ceramic planters with utmost care during transport.</p>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    EcoNest Logistics Department &bull; Internal Dispatch Coordination" +
                "  </div>" +
                "</div>" +
                "</body></html>";
    }
}
