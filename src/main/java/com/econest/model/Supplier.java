package com.econest.model;

import com.econest.interfaces.Manageable;
import com.econest.util.ValidationUtil;

import java.util.Date;

/**
 * Entity representing an agricultural or equipment Supplier.
 */
public class Supplier implements Manageable {
    private String supplierId;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private Date createdAt;

    public Supplier() {}

    public Supplier(String supplierId, String supplierName, String contactPerson,
                    String phone, String email, String address) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = new Date();
    }

    @Override
    public String getId() {
        return supplierId;
    }

    @Override
    public String getDisplayName() {
        return supplierName + " (" + contactPerson + ")";
    }

    @Override
    public boolean isValid() {
        return ValidationUtil.isNotEmpty(supplierId) &&
               ValidationUtil.isNotEmpty(supplierName) &&
               ValidationUtil.isValidPhone(phone);
    }

    @Override
    public String getStatusDescription() {
        return "Active Supplier";
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return supplierName;
    }
}
