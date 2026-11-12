package com.econest.model;

import java.util.Date;

/**
 * Entity representing an audit log entry for system email notifications.
 */
public class EmailNotification {
    private int notificationId;
    private String recipientEmail;
    private String recipientName;
    private String recipientType; // "CLIENT", "AGENT", "ADMIN"
    private String subject;
    private String content;
    private String referenceId;
    private String status; // "SENT", "PENDING", "FAILED"
    private String errorMessage;
    private Date sentAt;

    public EmailNotification() {
        this.status = "SENT";
        this.sentAt = new Date();
    }

    public EmailNotification(String recipientEmail, String recipientName, String recipientType,
                             String subject, String content, String referenceId, String status, String errorMessage) {
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.recipientType = recipientType;
        this.subject = subject;
        this.content = content;
        this.referenceId = referenceId;
        this.status = status;
        this.errorMessage = errorMessage;
        this.sentAt = new Date();
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }
}
