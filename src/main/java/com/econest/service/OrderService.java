package com.econest.service;

import com.econest.dao.ClientDAO;
import com.econest.dao.OrderDAO;
import com.econest.exception.InsufficientStockException;
import com.econest.exception.InvalidOrderException;
import com.econest.model.Client;
import com.econest.model.Order;

import java.util.List;

/**
 * Service managing client order creation, polymorphic discount calculation,
 * and transactional stock confirmation.
 * 
 * Demonstrates:
 * - Polymorphism: Calls `client.calculateDiscount(subtotal)` on runtime instance of HouseholdClient or OfficeClient.
 * - Business validation and exception delegation.
 */
public class OrderService {
    private final OrderDAO orderDAO;
    private final ClientDAO clientDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.clientDAO = new ClientDAO();
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public Order getOrderDetails(String orderId) {
        return orderDAO.getOrderById(orderId);
    }

    /**
     * Calculates the automatic client tier discount polymorphically.
     * @param clientId ID of the client placing order
     * @param subtotal raw items total
     * @return calculated discount in LKR
     */
    public double calculateClientDiscount(String clientId, double subtotal) {
        if (clientId == null || subtotal <= 0) return 0.0;
        Client client = clientDAO.getClientById(clientId);
        if (client != null) {
            // Polymorphic call: HouseholdClient or OfficeClient logic executes
            return client.calculateDiscount(subtotal);
        }
        return 0.0;
    }

    public boolean placeNewOrder(Order order) throws InvalidOrderException {
        if (order == null || !order.isValid()) {
            throw new InvalidOrderException("Invalid order data! Ensure items are selected and delivery address is provided.");
        }
        return orderDAO.createOrder(order);
    }

    public boolean confirmOrder(String orderId) throws InsufficientStockException {
        return orderDAO.confirmOrder(orderId);
    }

    public boolean cancelOrder(String orderId) {
        return orderDAO.cancelOrder(orderId);
    }

    public boolean updateStatus(String orderId, String newStatus) {
        return orderDAO.updateOrderStatus(orderId, newStatus);
    }

    public String generateNextOrderId() {
        return orderDAO.getNextOrderId();
    }
}
