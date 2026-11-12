package com.econest.service;

import com.econest.dao.InventoryDAO;
import com.econest.model.Inventory;

import java.util.List;

/**
 * Service orchestrating inventory queries, stock replenishment, and low-stock alerts.
 */
public class InventoryService {
    private final InventoryDAO inventoryDAO;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
    }

    public List<Inventory> getAllInventory() {
        return inventoryDAO.getAllInventory();
    }

    public List<Inventory> getLowStockAlerts() {
        return inventoryDAO.getLowStockItems();
    }

    public int getLowStockCount() {
        return inventoryDAO.getLowStockCount();
    }

    public boolean updateStockAndThreshold(String productId, int newQuantity, int newReorderLevel) {
        return inventoryDAO.updateStockAndReorderLevel(productId, newQuantity, newReorderLevel);
    }

    public boolean replenishStockFromSupplier(String productId, int addQuantity, String supplierId, String remarks) {
        return inventoryDAO.recordStockIn(productId, addQuantity, supplierId, remarks);
    }
}
