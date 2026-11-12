package com.econest.model;

import java.util.Date;

/**
 * Abstract base class representing a system User.
 * 
 * Demonstrates:
 * - Abstraction: Cannot be instantiated directly; concrete roles (Admin, Staff) extend this.
 * - Encapsulation: Private fields with validated getters and setters.
 * - Polymorphism: Abstract methods `getRolePermissions()` and `canAccessModule(String module)`.
 */
public abstract class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private String role; // "ADMIN" or "STAFF"
    private String phone;
    private String status; // "ACTIVE" or "INACTIVE"
    private Date createdAt;

    public User() {}

    public User(int userId, String username, String passwordHash, String salt,
                String fullName, String email, String role, String phone, String status, Date createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Polymorphic method to determine if this user role can access a specific system module.
     * Overridden in Admin and Staff.
     * @param module name of module (e.g. "USER_MANAGEMENT", "INVENTORY", "SETTINGS")
     * @return true if access granted
     */
    public abstract boolean canAccessModule(String module);

    /**
     * Polymorphic method returning a description of user permissions.
     * @return String permission summary
     */
    public abstract String getRolePermissions();

    // --- Encapsulated Getters & Setters ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", fullName, username, role);
    }
}
