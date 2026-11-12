package com.econest.controller;

import com.econest.model.Delivery;
import com.econest.service.DeliveryService;

import java.util.List;

/**
 * Controller for Module 06: Delivery Agent Assignment & Tracking.
 */
public class DeliveryController {
    private final DeliveryService deliveryService;

    public DeliveryController() {
        this.deliveryService = new DeliveryService();
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    public boolean assignDelivery(Delivery delivery) {
        if (delivery == null || !delivery.isValid()) {
            return false;
        }
        return deliveryService.scheduleDelivery(delivery);
    }

    public boolean updateStatus(String deliveryId, String newStatus, String notes) {
        return deliveryService.updateDeliveryStatus(deliveryId, newStatus, notes);
    }

    public String generateNextId() {
        return deliveryService.generateNextDeliveryId();
    }

    public int getActiveCount() {
        return deliveryService.getActiveDeliveriesCount();
    }
}
