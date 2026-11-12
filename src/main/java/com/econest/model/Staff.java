package com.econest.model;

import java.util.Date;

/**
 * Concrete class representing a Staff member with operational system privileges.
 * 
 * Demonstrates:
 * - Inheritance: Extends abstract parent class User.
 * - Polymorphism: Overrides `canAccessModule` and `getRolePermissions` with role-based restriction.
 */
public class Staff extends User {

    public Staff() {
        super();
        setRole("STAFF");
    }

    public Staff(int userId, String username, String passwordHash, String salt,
                 String fullName, String email, String phone, String status, Date createdAt) {
        super(userId, username, passwordHash, salt, fullName, email, "STAFF", phone, status, createdAt);
    }

    @Override
    public boolean canAccessModule(String module) {
        if (module == null) return false;
        // Staff can access operational modules, but cannot manage users or change system settings
        switch (module.toUpperCase()) {
            case "USER_MANAGEMENT":
            case "SETTINGS":
            case "DELETE_AUDIT":
                return false;
            default:
                return true;
        }
    }

    @Override
    public String getRolePermissions() {
        return "Operational Staff Privileges (Catalogue, Clients, Orders, Inventory, Deliveries)";
    }
}
