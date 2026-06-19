package com.inventory.service;

import com.inventory.model.Item;
import com.inventory.repository.InventoryRepository;
import java.util.ArrayList;
import java.util.List;

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
}
