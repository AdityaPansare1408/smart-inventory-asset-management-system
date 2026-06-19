package com.inventory.model;

public class Item {

    private String itemId;
    private String itemName;
    private int quantity;
    private double price;
    private String category;
    private int minimumStockLevel;

    public Item(String itemId,
                String itemName,
                int quantity,
                double price,
                String category,
                int minimumStockLevel) {

        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.minimumStockLevel = minimumStockLevel;

    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public int getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setMinimumStockLevel(int minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", minimumStockLevel=" + minimumStockLevel +
                '}';
    }
}
