package com.econest.model;

import com.econest.interfaces.Manageable;
import com.econest.interfaces.Notifiable;
import com.econest.util.ValidationUtil;

import java.util.Date;

/**
 * Entity representing a scheduled or completed Delivery Assignment.
 * 
 * Demonstrates:
 * - Association: Links an Order with a DeliveryAgent.
 * - Encapsulation & Multiple Interface Implementation.
 */
public class Delivery implements Manageable, Notifiable {
    private String deliveryId;
    private String orderId;
    private String agentId;
    private String agentName; // Joined/transient
    private String clientName; // Joined/transient
    private String clientPhone; // Joined/transient
    private String deliveryAddress; // Joined/transient
    private Date assignedDate;
    private Date deliveryDate;
    private String deliveryStatus; // "Assigned", "Picked Up", "In Transit", "Delivered", "Failed"
    private String trackingNotes;
    private Date completedDate;

    public Delivery() {
        this.assignedDate = new Date();
        this.deliveryStatus = "Assigned";
    }

    public Delivery(String deliveryId, String orderId, String agentId, Date deliveryDate) {
        this();
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.agentId = agentId;
        this.deliveryDate = deliveryDate;
    }

    // --- Manageable Implementation ---

    @Override
    public String getId() {
        return deliveryId;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s (Order: %s -> Agent: %s)", deliveryId, orderId, agentName != null ? agentName : agentId);
    }

    @Override
    public boolean isValid() {
        return ValidationUtil.isNotEmpty(deliveryId) &&
               ValidationUtil.isNotEmpty(orderId) &&
               ValidationUtil.isNotEmpty(agentId) &&
               deliveryDate != null;
    }

    @Override
    public String getStatusDescription() {
        return deliveryStatus;
    }

    // --- Notifiable Implementation ---

    @Override
    public String getRecipientEmail() {
        return null; // Routed via agent or client
    }

    @Override
    public String getRecipientName() {
        return agentName != null ? agentName : agentId;
    }

    @Override
    public String getNotificationCategory() {
        return "DELIVERY";
    }

    // --- Getters & Setters ---

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Date getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Date assignedDate) {
        this.assignedDate = assignedDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getTrackingNotes() {
        return trackingNotes;
    }

    public void setTrackingNotes(String trackingNotes) {
        this.trackingNotes = trackingNotes;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
