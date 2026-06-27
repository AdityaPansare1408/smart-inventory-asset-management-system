package com.inventory.model;

public class Item {
    private String id;
    private String name;
    private int quantity;
    private double price;
    private String category;
    private int minimumStock;

    public Item(String id, String name, int quantity, double price, String category, int minimumStock) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.minimumStock = minimumStock;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }

    public String toCSV() {
        return id + "," + name + "," + quantity + "," + price + "," + category + "," + minimumStock;
    }
}