package com.inventory;

import com.inventory.model.Item;
import com.inventory.repository.InventoryRepository;
import com.inventory.service.InventoryService;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        InventoryService inventoryService = new InventoryService();

        boolean deleted =
                inventoryService.deleteItem("I002");

        System.out.println(deleted);

        inventoryService.viewInventory();
    }
}