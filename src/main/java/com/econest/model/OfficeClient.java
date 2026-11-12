package com.econest.model;

import java.util.Date;

/**
 * Concrete class representing an Office / Corporate Client.
 * 
 * Demonstrates:
 * - Inheritance: Extends Client base class.
 * - Polymorphism: Overrides `calculateDiscount` with corporate bulk discount tiers.
 */
public class OfficeClient extends Client {

    public OfficeClient() {
        super();
        setClientType("Office");
    }

    public OfficeClient(String clientId, String clientName, String email,
                        String phone, String address, String city, Date registrationDate) {
        super(clientId, clientName, "Office", email, phone, address, city, registrationDate);
    }

    /**
     * Office clients receive tiered volume discounts:
     * - 8% discount on orders above 25,000 LKR
     * - 5% discount on orders above 10,000 LKR
     */
    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal >= 25000.0) {
            return Math.round(subtotal * 0.08 * 100.0) / 100.0;
        } else if (subtotal >= 10000.0) {
            return Math.round(subtotal * 0.05 * 100.0) / 100.0;
        }
        return 0.0;
    }
}
