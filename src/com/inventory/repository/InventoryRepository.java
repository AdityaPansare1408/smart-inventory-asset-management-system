package com.inventory.repository;

import com.inventory.model.Item;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryRepository {
    private final String FILE_PATH = "data/inventory.csv";

    public List<Item> loadAll() {
        List<Item> items = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return items;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    items.add(new Item(parts[0], parts[1], Integer.parseInt(parts[2]),
                            Double.parseDouble(parts[3]), parts[4], Integer.parseInt(parts[5])));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return items;
    }

    public void saveAll(List<Item> items) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Item item : items) {
                bw.write(item.toCSV());
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}