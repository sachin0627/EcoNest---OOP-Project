package com.econest.model;

import java.util.Date;

/**
 * Concrete class representing an Administrator user with full system privileges.
 * 
 * Demonstrates:
 * - Inheritance: Extends abstract parent class User.
 * - Polymorphism: Overrides `canAccessModule` and `getRolePermissions`.
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int userId, String username, String passwordHash, String salt,
                 String fullName, String email, String phone, String status, Date createdAt) {
        super(userId, username, passwordHash, salt, fullName, email, "ADMIN", phone, status, createdAt);
    }

    @Override
    public boolean canAccessModule(String module) {
        // Administrator has unrestricted access to all modules
        return true;
    }

    @Override
    public String getRolePermissions() {
        return "Full Administrative Privileges (All Modules, Settings, Reports, Deletion)";
    }
}
