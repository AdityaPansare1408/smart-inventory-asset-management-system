package com.inventory.repository;

import com.inventory.model.Asset;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AssetRepository {
    private final String FILE_PATH = "data/assets.csv";

    public List<Asset> loadAll() {
        List<Asset> assets = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return assets;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length == 7) {
                    // New Format
                    assets.add(new Asset(p[0], p[1], p[2], Double.parseDouble(p[3]), p[4], p[5], Integer.parseInt(p[6])));
                } else if (p.length == 6) {
                    // Auto-Migrate Old Format: Set Last Maintenance = Purchase Date
                    assets.add(new Asset(p[0], p[1], p[2], Double.parseDouble(p[3]), p[4], p[2], Integer.parseInt(p[5])));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return assets;
    }

    public void saveAll(List<Asset> assets) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Asset a : assets) { bw.write(a.toCSV()); bw.newLine(); }
        } catch (IOException e) { e.printStackTrace(); }
    }
}