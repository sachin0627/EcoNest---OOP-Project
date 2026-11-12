package com.econest.interfaces;

/**
 * Interface representing any business party (Client, Delivery Agent, Admin)
 * eligible to receive automated email and system notifications.
 * 
 * Demonstrates: OOP Interface & Decoupling Notification Handlers.
 */
public interface Notifiable {
    /**
     * Target email address for notification delivery.
     * @return email address
     */
    String getRecipientEmail();

    /**
     * Recipient's full greeting name.
     * @return recipient name
     */
    String getRecipientName();

    /**
     * Category of notification (e.g. CLIENT, AGENT, ADMIN).
     * @return category string
     */
    String getNotificationCategory();
}
