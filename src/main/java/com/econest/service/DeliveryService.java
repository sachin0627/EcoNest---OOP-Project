package com.econest.service;

import com.econest.dao.ClientDAO;
import com.econest.dao.DeliveryAgentDAO;
import com.econest.dao.DeliveryDAO;
import com.econest.dao.OrderDAO;
import com.econest.model.Client;
import com.econest.model.Delivery;
import com.econest.model.DeliveryAgent;
import com.econest.model.Order;

import java.util.List;

/**
 * Service managing delivery scheduling, tracking, and automated email triggers.
 * 
 * Enforces Requirements:
 * - Module 06 & 09: When delivery is assigned, trigger automated email to delivery agent.
 * - Module 06 & 08: When order is dispatched / in-transit, trigger automated email to client.
 */
public class DeliveryService {
    private final DeliveryDAO deliveryDAO;
    private final DeliveryAgentDAO agentDAO;
    private final OrderDAO orderDAO;
    private final ClientDAO clientDAO;
    private final EmailService emailService;

    public DeliveryService() {
        this.deliveryDAO = new DeliveryDAO();
        this.agentDAO = new DeliveryAgentDAO();
        this.orderDAO = new OrderDAO();
        this.clientDAO = new ClientDAO();
        this.emailService = new EmailService();
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryDAO.getAllDeliveries();
    }

    public int getActiveDeliveriesCount() {
        return deliveryDAO.getActiveDeliveryCount();
    }

    public String generateNextDeliveryId() {
        return deliveryDAO.getNextDeliveryId();
    }

    /**
     * Schedules a delivery assignment and triggers an automated email notification to the agent.
     */
    public boolean scheduleDelivery(Delivery delivery) {
        boolean success = deliveryDAO.assignDelivery(delivery);
        if (success) {
            // Trigger Agent Email Notification (Module 09)
            new Thread(() -> {
                try {
                    DeliveryAgent agent = agentDAO.getAgentById(delivery.getAgentId());
                    Order order = orderDAO.getOrderById(delivery.getOrderId());
                    Client client = clientDAO.getClientById(order.getClientId());

                    delivery.setAgentName(agent.getFullName());
                    delivery.setClientName(client.getClientName());
                    delivery.setClientPhone(client.getPhone());
                    delivery.setDeliveryAddress(order.getDeliveryAddress());

                    emailService.sendDeliveryAssignedEmail(delivery, agent.getEmail(), agent.getFullName());
                } catch (Exception e) {
                    System.err.println("[DeliveryService] Agent notification trigger failed: " + e.getMessage());
                }
            }).start();
        }
        return success;
    }

    /**
     * Updates delivery tracking progress. If status transitions to In Transit / Dispatched,
     * triggers an automated email notification to the client.
     */
    public boolean updateDeliveryStatus(String deliveryId, String newStatus, String trackingNotes) {
        boolean success = deliveryDAO.updateDeliveryStatus(deliveryId, newStatus, trackingNotes);
        if (success) {
            if ("In Transit".equalsIgnoreCase(newStatus) || "Picked Up".equalsIgnoreCase(newStatus)) {
                // Trigger Client Email Notification (Module 08)
                new Thread(() -> {
                    try {
                        Delivery d = deliveryDAO.getDeliveryById(deliveryId);
                        if (d != null) {
                            Order order = orderDAO.getOrderById(d.getOrderId());
                            Client client = clientDAO.getClientById(order.getClientId());
                            emailService.sendOrderDispatchedEmail(order, client.getEmail(), client.getClientName(), trackingNotes);
                        }
                    } catch (Exception e) {
                        System.err.println("[DeliveryService] Client notification trigger failed: " + e.getMessage());
                    }
                }).start();
            }
        }
        return success;
    }
}
