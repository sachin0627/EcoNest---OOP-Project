package com.econest.controller;

import com.econest.dao.StockMovementDAO;
import com.econest.dao.SupplierDAO;
import com.econest.model.Inventory;
import com.econest.model.StockMovement;
import com.econest.model.Supplier;
import com.econest.service.InventoryService;

import java.util.List;

/**
 * Controller for Module 03: Inventory & Stock Management.
 */
public class InventoryController {
    private final InventoryService inventoryService;
    private final SupplierDAO supplierDAO;
    private final StockMovementDAO stockMovementDAO;

    public InventoryController() {
        this.inventoryService = new InventoryService();
        this.supplierDAO = new SupplierDAO();
        this.stockMovementDAO = new StockMovementDAO();
    }

    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    public List<Inventory> getLowStockAlerts() {
        return inventoryService.getLowStockAlerts();
    }

    public int getLowStockCount() {
        return inventoryService.getLowStockCount();
    }

    public boolean updateStockAndThreshold(String productId, int newQuantity, int newReorderLevel) {
        return inventoryService.updateStockAndThreshold(productId, newQuantity, newReorderLevel);
    }

    public boolean recordStockIn(String productId, int addQuantity, String supplierId, String remarks) {
        return inventoryService.replenishStockFromSupplier(productId, addQuantity, supplierId, remarks);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierDAO.getAllSuppliers();
    }

    public List<StockMovement> getRecentMovements(int limit) {
        return stockMovementDAO.getRecentMovements(limit);
    }
}
