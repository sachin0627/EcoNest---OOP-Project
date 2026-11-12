package com.econest.controller;

import com.econest.dao.OrderDAO;
import com.econest.exception.InsufficientStockException;
import com.econest.exception.InvalidOrderException;
import com.econest.model.Order;
import com.econest.service.OrderService;

import java.util.List;

/**
 * Controller for Module 05: Client Order Management.
 */
public class OrderController {
    private final OrderService orderService;
    private final OrderDAO orderDAO;

    public OrderController() {
        this.orderService = new OrderService();
        this.orderDAO = new OrderDAO();
    }

    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    public List<Order> getRecentOrders(int limit) {
        return orderDAO.getRecentOrders(limit);
    }

    public Order getOrderDetails(String orderId) {
        return orderService.getOrderDetails(orderId);
    }

    public List<Order> searchOrders(String keyword, String status) {
        return orderDAO.searchOrders(keyword, status);
    }

    public double calculateDiscount(String clientId, double subtotal) {
        return orderService.calculateClientDiscount(clientId, subtotal);
    }

    public boolean placeOrder(Order order) throws InvalidOrderException {
        return orderService.placeNewOrder(order);
    }

    public boolean confirmOrder(String orderId) throws InsufficientStockException {
        return orderService.confirmOrder(orderId);
    }

    public boolean cancelOrder(String orderId) {
        return orderService.cancelOrder(orderId);
    }

    public boolean updateStatus(String orderId, String newStatus) {
        return orderService.updateStatus(orderId, newStatus);
    }

    public String generateNextId() {
        return orderService.generateNextOrderId();
    }

    public double getTotalRevenue() {
        return orderDAO.getTotalRevenue();
    }

    public int getTotalOrderCount() {
        return orderDAO.getTotalOrderCount();
    }
}
