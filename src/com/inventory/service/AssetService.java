package com.inventory.service;

import com.inventory.model.Asset;
import com.inventory.repository.AssetRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class AssetService {
    private final AssetRepository repository;
    private final List<Asset> assets;

    public AssetService() {
        this.repository = new AssetRepository();
        this.assets = repository.loadAll();
    }

    public List<Asset> getAllAssets() { return assets; }

    public void addAsset(Asset a) throws Exception {
        if (assets.stream().anyMatch(as -> as.getId().equalsIgnoreCase(a.getId()))) throw new Exception("ID already exists.");
        assets.add(a);
        repository.saveAll(assets);
    }

    public void updateAsset(Asset updated) {
        for (int i = 0; i < assets.size(); i++) {
            if (assets.get(i).getId().equals(updated.getId())) { assets.set(i, updated); break; }
        }
        repository.saveAll(assets);
    }

    public void deleteAsset(String id) {
        assets.removeIf(a -> a.getId().equals(id));
        repository.saveAll(assets);
    }

    public List<Asset> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return assets;
        String kw = keyword.toLowerCase();
        return assets.stream()
                .filter(a -> a.getId().toLowerCase().contains(kw) || a.getName().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    // --- NEW MAINTENANCE ENGINES ---

    public long calculateAssetAgeInMonths(Asset a) {
        try { return Math.max(0, ChronoUnit.MONTHS.between(LocalDate.parse(a.getPurchaseDate()), LocalDate.now())); }
        catch (Exception e) { return 0; }
    }

    public LocalDate calculateNextMaintenanceDue(Asset a) {
        try {
            LocalDate lastMaint = LocalDate.parse(a.getLastMaintenanceDate());
            return lastMaint.plusMonths(a.getMaintenanceInterval());
        } catch (Exception e) { return LocalDate.now(); }
    }

    public String calculateMaintenanceStatus(Asset a) {
        LocalDate nextDue = calculateNextMaintenanceDue(a);
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), nextDue);

        if (daysBetween < 0) return "Overdue";
        if (daysBetween <= 30) return "Due Soon";
        return "On Schedule";
    }

    public int calculateHealthScore(Asset a) {
        // 1. Base Score from Physical Condition (Highest Influence)
        int base;
        switch (a.getCurrentCondition().toLowerCase()) {
            case "excellent": base = 100; break;
            case "good": base = 85; break;
            case "fair": base = 65; break;
            default: base = 40;
        }

        // 2. Age Deduction (Moderate Influence)
        long age = calculateAssetAgeInMonths(a);
        int ageDed = Math.min((int)(age / 3), 20); // Max 20 point deduction for age

        // 3. Realistic Maintenance Compliance (Significant Influence)
        int maintDed = 0;
        String maintStatus = calculateMaintenanceStatus(a);
        if (maintStatus.equals("Overdue")) maintDed = 25;
        else if (maintStatus.equals("Due Soon")) maintDed = 5;

        return Math.max(0, Math.min(100, base - ageDed - maintDed));
    }

    public String calculateHealthStatus(int score) {
        if (score >= 85) return "Excellent";
        if (score >= 65) return "Good";
        if (score >= 45) return "Fair";
        return "Needs Maintenance";
    }
}