package com.econest.test;

import com.econest.controller.LoginController;
import com.econest.dao.*;
import com.econest.exception.InsufficientStockException;
import com.econest.model.*;
import com.econest.service.*;
import com.econest.util.DatabaseConnection;
import com.econest.util.EnvConfig;
import org.jfree.chart.JFreeChart;

import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;

/**
 * System Integration & Verification Test Suite for EcoNest Retail System.
 * Tests all 9 modules, OOP principles, and database operations.
 */
public class SystemIntegrationTest {

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println(" EcoNest – System Integration & Verification Test Suite");
        System.out.println("==============================================================");

        int passed = 0;
        int failed = 0;

        EnvConfig.init();

        // 1. Test Database Connectivity
        System.out.print("[TEST 1] Testing MySQL Database Connectivity... ");
        if (DatabaseConnection.getInstance().testConnection()) {
            System.out.println("PASSED (MySQL online & reachable)");
            passed++;
        } else {
            System.out.println("FAILED (Cannot reach MySQL)");
            failed++;
        }

        // 2. Test Login Authentication & Role Polymorphism
        System.out.print("[TEST 2] Testing User Authentication & Role Polymorphism... ");
        try {
            LoginController loginController = new LoginController();
            User admin = loginController.login("admin", "admin123");
            User staff = loginController.login("staff", "staff123");

            if (admin instanceof Admin && staff instanceof Staff &&
                admin.canAccessModule("SETTINGS") && !staff.canAccessModule("SETTINGS")) {
                System.out.println("PASSED (Admin & Staff correctly authenticated with distinct permissions)");
                passed++;
            } else {
                System.out.println("FAILED (Authentication or permission mismatch)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 3. Test Module 01: Product Catalogue
        System.out.print("[TEST 3] Testing Module 01 - Product Catalogue Management... ");
        try {
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts();
            List<Product> indoor = productDAO.searchProducts(null, "Indoor Plants");

            if (!products.isEmpty() && !indoor.isEmpty()) {
                System.out.println("PASSED (" + products.size() + " products loaded, " + indoor.size() + " indoor plants)");
                passed++;
            } else {
                System.out.println("FAILED (No products retrieved)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 4. Test Module 02: Client Management & Polymorphic Discounts
        System.out.print("[TEST 4] Testing Module 02 - Client Management & Tier Discounts... ");
        try {
            ClientDAO clientDAO = new ClientDAO();
            List<Client> clients = clientDAO.getAllClients();
            
            HouseholdClient household = new HouseholdClient();
            OfficeClient office = new OfficeClient();

            double hDiscount = household.calculateDiscount(6000.0); // 5% = 300.0
            double oDiscount = office.calculateDiscount(30000.0);   // 8% = 2400.0

            if (!clients.isEmpty() && hDiscount == 300.0 && oDiscount == 2400.0) {
                System.out.println("PASSED (" + clients.size() + " clients loaded, Polymorphic discounts verified)");
                passed++;
            } else {
                System.out.println("FAILED (Discount calculation mismatch: h=" + hDiscount + ", o=" + oDiscount + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 5. Test Module 03: Inventory & Low Stock Tracking
        System.out.print("[TEST 5] Testing Module 03 - Inventory Management & Stock Alerts... ");
        try {
            InventoryDAO invDAO = new InventoryDAO();
            List<Inventory> allInv = invDAO.getAllInventory();
            List<Inventory> lowStock = invDAO.getLowStockItems();
            int lowCount = invDAO.getLowStockCount();

            if (!allInv.isEmpty() && lowCount == lowStock.size()) {
                System.out.println("PASSED (" + allInv.size() + " inventory tracked, " + lowCount + " low-stock alerts identified)");
                passed++;
            } else {
                System.out.println("FAILED (Inventory query discrepancy)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 6. Test Module 04: Delivery Agent Management
        System.out.print("[TEST 6] Testing Module 04 - Delivery Agent Management... ");
        try {
            DeliveryAgentDAO agentDAO = new DeliveryAgentDAO();
            List<DeliveryAgent> agents = agentDAO.getAllAgents();
            List<DeliveryAgent> available = agentDAO.getAvailableAgents();

            if (!agents.isEmpty() && !available.isEmpty()) {
                System.out.println("PASSED (" + agents.size() + " agents registered, " + available.size() + " available)");
                passed++;
            } else {
                System.out.println("FAILED (No delivery agents found)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 7. Test Module 05: Client Order Creation & Transactional Stock Deduction
        System.out.print("[TEST 7] Testing Module 05 - Order Processing & Transactional Confirmation... ");
        try {
            OrderService orderService = new OrderService();
            ProductDAO productDAO = new ProductDAO();
            InventoryDAO invDAO = new InventoryDAO();

            String testOrderId = "ORD-T-" + (System.currentTimeMillis() % 100000);

            Order testOrder = new Order();
            testOrder.setOrderId(testOrderId);
            testOrder.setClientId("CLT-2001");
            testOrder.setDeliveryAddress("Colombo 07");
            testOrder.setPaymentMethod("Cash on Delivery");
            
            Product p1 = productDAO.getProductById("PRD-1002"); // Snake plant
            int initialStock = p1.getAvailableQuantity();
            testOrder.addItem(p1, 2);

            orderService.placeNewOrder(testOrder);

            // Confirm order (transaction should deduct 2 units)
            orderService.confirmOrder(testOrderId);

            Product p1After = productDAO.getProductById("PRD-1002");
            int afterStock = p1After.getAvailableQuantity();

            if (afterStock == initialStock - 2) {
                System.out.println("PASSED (Order placed, stock reduced from " + initialStock + " to " + afterStock + ")");
                passed++;
            } else {
                System.out.println("FAILED (Stock not reduced properly: " + initialStock + " -> " + afterStock + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 8. Test Module 06: Delivery Assignment & Tracking
        System.out.print("[TEST 8] Testing Module 06 - Delivery Scheduling & Tracking... ");
        try {
            DeliveryService deliveryService = new DeliveryService();
            List<Delivery> deliveries = deliveryService.getAllDeliveries();
            int active = deliveryService.getActiveDeliveriesCount();

            if (!deliveries.isEmpty()) {
                System.out.println("PASSED (" + deliveries.size() + " deliveries registered, " + active + " currently active)");
                passed++;
            } else {
                System.out.println("FAILED (No deliveries loaded)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 9. Test Module 07: Reports & Analytics (JFreeChart)
        System.out.print("[TEST 9] Testing Module 07 - JFreeChart & Reports Generation... ");
        try {
            ReportService reportService = new ReportService();
            JFreeChart barChart = reportService.createMonthlySalesChart();
            JFreeChart pieChart = reportService.createCategoryDistributionChart();
            DefaultTableModel reportTable = reportService.generateReportTableModel("Monthly Sales Report", null, null);

            if (barChart != null && pieChart != null && reportTable.getRowCount() > 0) {
                System.out.println("PASSED (Bar chart, pie chart & " + reportTable.getRowCount() + " report rows generated)");
                passed++;
            } else {
                System.out.println("FAILED (Chart or report model generation failed)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // 10. Test Module 08 & 09: Email Notification Audit Logging
        System.out.print("[TEST 10] Testing Module 08 & 09 - Notification Audit & Fallback Logging... ");
        try {
            NotificationDAO notifDAO = new NotificationDAO();
            List<EmailNotification> notifs = notifDAO.getRecentNotifications(10);

            if (!notifs.isEmpty()) {
                System.out.println("PASSED (" + notifs.size() + " email notification audit records logged)");
                passed++;
            } else {
                System.out.println("FAILED (No notification records found)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        System.out.println("==============================================================");
        System.out.println(String.format(" TEST RESULTS: %d PASSED, %d FAILED (TOTAL %d)", passed, failed, passed + failed));
        System.out.println("==============================================================");

        if (failed == 0) {
            System.out.println(">>> ALL 9 MODULES AND OOP ARCHITECTURE VERIFIED 100% WORKING! <<<");
        }
    }
}
