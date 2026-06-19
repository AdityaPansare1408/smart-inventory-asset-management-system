package com.inventory.service;

import com.inventory.model.Item;
import com.inventory.repository.InventoryRepository;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

public class InventoryService {
    private List<Item> inventory;
    private InventoryRepository repository;

    public InventoryService() {
        repository = new InventoryRepository();
        inventory = repository.loadItems();
    }

    public void addItem(Item item){
        if (item.getQuantity() < 0) {
            System.out.println("Invalid quantity");
            return;
        }

        inventory.add(item);
        repository.saveItems(inventory);
    }

    public void viewInventory(){
        for (Item item : inventory){
            System.out.println(item);
        }
    }

    public Item searchItemById(String itemId){
        for (Item item : inventory){
            if (item.getItemId().equalsIgnoreCase(itemId)){
                return item;
            }
        }
        return null;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public boolean updateQuantity(
            String itemId,
            int newQuantity) {

        Item item = searchItemById(itemId);

        if (item == null) {
            return false;
        }

        item.setQuantity(newQuantity);

        repository.saveItems(inventory);

        return true;
    }

    public boolean updateItem(
            String itemId,
            String itemName,
            int quantity,
            double price,
            String category,
            int minimumStockLevel) {

        Item item = searchItemById(itemId);

        if (item == null) {
            return false;
        }

        item.setItemName(itemName);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setCategory(category);
        item.setMinimumStockLevel(minimumStockLevel);

        repository.saveItems(inventory);

        return true;
    }

    public boolean deleteItem(String itemId) {

        Item item = searchItemById(itemId);

        if (item == null) {
            return false;
        }

        inventory.remove(item);

        repository.saveItems(inventory);

        return true;
    }

    public void checkLowStockItems() {

        boolean lowStockFound = false;

        for (Item item : inventory) {

            if (item.getQuantity() <= item.getMinimumStockLevel()) {

                System.out.println(
                        "LOW STOCK ALERT: "
                                + item.getItemName()
                                + " has only "
                                + item.getQuantity()
                                + " units remaining."
                );

                lowStockFound = true;
            }
        }

        if (!lowStockFound) {
            System.out.println("No low stock items found.");
        }
    }

    public void generateRestockPriorityList() {

        PriorityQueue<Item> restockQueue =
                new PriorityQueue<>(
                        Comparator.comparingInt(Item::getQuantity)
                );

        for (Item item : inventory) {

            if (item.getQuantity() <= item.getMinimumStockLevel()) {
                restockQueue.offer(item);
            }
        }

        System.out.println("\nRESTOCK PRIORITY LIST");

        while (!restockQueue.isEmpty()) {

            Item item = restockQueue.poll();

            System.out.println(
                    item.getItemName()
                            + " | Quantity: "
                            + item.getQuantity()
            );
        }
    }

    public double getTotalInventoryValue() {

        double totalValue = 0;

        for (Item item : inventory) {

            totalValue += item.getQuantity() * item.getPrice();

        }

        return totalValue;
    }

    public Item getMostValuableInventoryItem() {

        if (inventory.isEmpty()) {
            return null;
        }

        Item MostValuableInventoryItem = inventory.get(0);

        for (Item item : inventory) {

            double currentValue =
                    item.getQuantity() * item.getPrice();

            double highestValue =
                    MostValuableInventoryItem.getQuantity()
                            * MostValuableInventoryItem.getPrice();

            if (currentValue > highestValue) {
                MostValuableInventoryItem = item;
            }
        }

        return MostValuableInventoryItem;
    }

    public Item getHighestPricedItem() {

        if (inventory.isEmpty()) {
            return null;
        }

        Item highestPricedItem = inventory.get(0);

        for (Item item : inventory) {

            if (item.getPrice() > highestPricedItem.getPrice()) {
                highestPricedItem = item;
            }
        }

        return highestPricedItem;
    }

    public Item getLowestStockItem() {

        if (inventory.isEmpty()) {
            return null;
        }

        Item lowestStockItem = inventory.get(0);

        for (Item item : inventory) {

            if (item.getQuantity() < lowestStockItem.getQuantity()) {
                lowestStockItem = item;
            }
        }

        return lowestStockItem;
    }

    public void displayInventorySummary() {

        System.out.println("\n========== INVENTORY SUMMARY ==========");

        System.out.println(
                "Total Inventory Value: ₹"
                        + getTotalInventoryValue()
        );

        System.out.println(
                "\nMost Valuable Inventory Item:"
        );
        System.out.println(
                getMostValuableInventoryItem()
        );

        System.out.println(
                "\nHighest Priced Item:"
        );
        System.out.println(
                getHighestPricedItem()
        );

        System.out.println(
                "\nLowest Stock Item:"
        );
        System.out.println(
                getLowestStockItem()
        );

        System.out.println(
                "\n======================================="
        );
    }
}
