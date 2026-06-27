package com.inventory.model;

public class Asset {
    private String id;
    private String name;
    private String purchaseDate;
    private double purchaseCost;
    private String currentCondition;
    private String lastMaintenanceDate; // NEW FIELD
    private int maintenanceInterval;

    public Asset(String id, String name, String purchaseDate, double purchaseCost, String currentCondition, String lastMaintenanceDate, int maintenanceInterval) {
        this.id = id;
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.purchaseCost = purchaseCost;
        this.currentCondition = currentCondition;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.maintenanceInterval = maintenanceInterval;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    public double getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(double purchaseCost) { this.purchaseCost = purchaseCost; }
    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }
    public String getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(String lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    public int getMaintenanceInterval() { return maintenanceInterval; }
    public void setMaintenanceInterval(int maintenanceInterval) { this.maintenanceInterval = maintenanceInterval; }

    public String toCSV() {
        return id + "," + name + "," + purchaseDate + "," + purchaseCost + "," + currentCondition + "," + lastMaintenanceDate + "," + maintenanceInterval;
    }
}