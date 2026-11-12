package com.econest.model;

import com.econest.interfaces.Manageable;
import com.econest.interfaces.Notifiable;
import com.econest.util.ValidationUtil;

import java.util.Date;

/**
 * Base class representing a Client (Customer) of EcoNest.
 * 
 * Demonstrates:
 * - Encapsulation: Private fields with validated accessors.
 * - Abstraction & Inheritance: Base class extended by HouseholdClient and OfficeClient.
 * - Interfaces: Implements Manageable and Notifiable.
 * - Polymorphism: Abstract discount calculation method `calculateDiscount(double subtotal)`.
 */
public abstract class Client implements Manageable, Notifiable {
    private String clientId;
    private String clientName;
    private String clientType; // "Household" or "Office"
    private String email;
    private String phone;
    private String address;
    private String city;
    private Date registrationDate;
    private boolean active;

    public Client() {
        this.active = true;
        this.registrationDate = new Date();
    }

    public Client(String clientId, String clientName, String clientType, String email,
                  String phone, String address, String city, Date registrationDate) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientType = clientType;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.registrationDate = registrationDate != null ? registrationDate : new Date();
        this.active = true;
    }

    /**
     * Polymorphic discount calculation based on client category and order value.
     * @param subtotal raw order amount in LKR
     * @return discount amount in LKR
     */
    public abstract double calculateDiscount(double subtotal);

    // --- Manageable Implementation ---

    @Override
    public String getId() {
        return clientId;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s - %s (%s)", clientId, clientName, clientType);
    }

    @Override
    public boolean isValid() {
        return ValidationUtil.isNotEmpty(clientId) &&
               ValidationUtil.isNotEmpty(clientName) &&
               ValidationUtil.isValidEmail(email) &&
               ValidationUtil.isValidPhone(phone) &&
               ValidationUtil.isNotEmpty(address);
    }

    @Override
    public String getStatusDescription() {
        return active ? "Active Client" : "Inactive Client";
    }

    // --- Notifiable Implementation ---

    @Override
    public String getRecipientEmail() {
        return email;
    }

    @Override
    public String getRecipientName() {
        return clientName;
    }

    @Override
    public String getNotificationCategory() {
        return "CLIENT";
    }

    // --- Encapsulated Getters & Setters ---

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", clientId, clientName, city);
    }
}
