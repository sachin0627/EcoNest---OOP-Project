package com.econest.model;

import com.econest.interfaces.Manageable;
import com.econest.interfaces.Notifiable;
import com.econest.util.ValidationUtil;

import java.util.Date;

/**
 * Entity representing an EcoNest delivery driver / logistics agent.
 * 
 * Demonstrates:
 * - Encapsulation
 * - Multiple Interface Implementation (Manageable, Notifiable)
 */
public class DeliveryAgent implements Manageable, Notifiable {
    private String agentId;
    private String fullName;
    private String phone;
    private String email;
    private String vehicleType;
    private String vehicleRegNo;
    private String availabilityStatus; // "Available", "Assigned", "Unavailable"
    private boolean active;
    private Date createdAt;

    public DeliveryAgent() {
        this.availabilityStatus = "Available";
        this.active = true;
        this.createdAt = new Date();
    }

    public DeliveryAgent(String agentId, String fullName, String phone, String email,
                         String vehicleType, String vehicleRegNo, String availabilityStatus) {
        this.agentId = agentId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.vehicleType = vehicleType;
        this.vehicleRegNo = vehicleRegNo;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : "Available";
        this.active = true;
        this.createdAt = new Date();
    }

    public boolean isAvailable() {
        return active && "Available".equalsIgnoreCase(this.availabilityStatus);
    }

    // --- Manageable Implementation ---

    @Override
    public String getId() {
        return agentId;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s - %s (%s - %s)", agentId, fullName, vehicleType, availabilityStatus);
    }

    @Override
    public boolean isValid() {
        return ValidationUtil.isNotEmpty(agentId) &&
               ValidationUtil.isNotEmpty(fullName) &&
               ValidationUtil.isValidPhone(phone) &&
               ValidationUtil.isValidEmail(email) &&
               ValidationUtil.isNotEmpty(vehicleType) &&
               ValidationUtil.isNotEmpty(vehicleRegNo);
    }

    @Override
    public String getStatusDescription() {
        return availabilityStatus;
    }

    // --- Notifiable Implementation ---

    @Override
    public String getRecipientEmail() {
        return email;
    }

    @Override
    public String getRecipientName() {
        return fullName;
    }

    @Override
    public String getNotificationCategory() {
        return "AGENT";
    }

    // --- Encapsulated Getters & Setters ---

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleRegNo() {
        return vehicleRegNo;
    }

    public void setVehicleRegNo(String vehicleRegNo) {
        this.vehicleRegNo = vehicleRegNo;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", agentId, fullName, availabilityStatus);
    }
}
