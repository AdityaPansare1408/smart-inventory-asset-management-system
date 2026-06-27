package com.inventory.service;

import com.inventory.model.Item;
import com.inventory.repository.InventoryRepository;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {
    private final InventoryRepository repository;
    private final List<Item> items;

    public InventoryService() {
        this.repository = new InventoryRepository();
        this.items = repository.loadAll();
    }

    public List<Item> getAllItems() { return items; }

    public void addItem(Item item) throws Exception {
        if (items.stream().anyMatch(i -> i.getId().equalsIgnoreCase(item.getId()))) {
            throw new Exception("Duplicate Item ID detected.");
        }
        items.add(item);
        repository.saveAll(items);
    }

    public void updateItem(Item updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updatedItem.getId())) {
                items.set(i, updatedItem);
                break;
            }
        }
        repository.saveAll(items);
    }

    public void deleteItem(String id) {
        items.removeIf(i -> i.getId().equals(id));
        repository.saveAll(items);
    }

    public List<Item> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return items;
        String kw = keyword.toLowerCase();
        return items.stream()
                .filter(i -> i.getId().toLowerCase().contains(kw) ||
                        i.getName().toLowerCase().contains(kw) ||
                        i.getCategory().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public double getTotalValue() {
        return items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }

    public double getAverageItemPrice() {
        if (items.isEmpty()) return 0.0;
        return getTotalValue() / items.size();
    }

    public double getAverageStockQuantity() {
        if (items.isEmpty()) return 0.0;
        return items.stream().mapToInt(Item::getQuantity).average().orElse(0.0);
    }

    public String getCategoryBreakdown() {
        return items.stream()
                .collect(Collectors.groupingBy(Item::getCategory, Collectors.counting()))
                .entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(" | "));
    }

    public Item getHighestPricedItem() {
        return items.stream().max((i1, i2) -> Double.compare(i1.getPrice(), i2.getPrice())).orElse(null);
    }

    public Item getMostValuableItem() {
        return items.stream().max((i1, i2) -> Double.compare(i1.getPrice() * i1.getQuantity(), i2.getPrice() * i2.getQuantity())).orElse(null);
    }

    public Item getLowestStockItem() {
        return items.stream().min((i1, i2) -> Integer.compare(i1.getQuantity(), i2.getQuantity())).orElse(null);
    }

    public List<Item> getLowStockItems() {
        return items.stream().filter(i -> i.getQuantity() <= i.getMinimumStock()).collect(Collectors.toList());
    }

    public String calculatePriorityLevel(Item item) {
        if (item.getQuantity() == 0) return "Critical";
        int deficit = item.getMinimumStock() - item.getQuantity();
        double deficitRatio = (double) deficit / item.getMinimumStock();
        if (deficitRatio >= 0.75) return "High";
        if (deficitRatio >= 0.30) return "Medium";
        return "Low";
    }
}