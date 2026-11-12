package com.econest.model;

import java.util.Date;

/**
 * Concrete class representing a Household (Residential) Client.
 * 
 * Demonstrates:
 * - Inheritance: Extends Client base class.
 * - Polymorphism: Overrides `calculateDiscount` with household loyalty rules.
 */
public class HouseholdClient extends Client {

    public HouseholdClient() {
        super();
        setClientType("Household");
    }

    public HouseholdClient(String clientId, String clientName, String email,
                           String phone, String address, String city, Date registrationDate) {
        super(clientId, clientName, "Household", email, phone, address, city, registrationDate);
    }

    /**
     * Household clients receive a 5% loyalty discount on orders above 5,000 LKR.
     */
    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal >= 5000.0) {
            return Math.round(subtotal * 0.05 * 100.0) / 100.0;
        }
        return 0.0;
    }
}
