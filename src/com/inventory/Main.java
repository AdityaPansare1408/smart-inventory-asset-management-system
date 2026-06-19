package com.inventory;

import com.inventory.model.Item;
import com.inventory.service.InventoryService;

public class Main {

    public static void main(String[] args) {
        InventoryService inventoryService = new InventoryService();

        inventoryService.displayInventorySummary();
    }
}