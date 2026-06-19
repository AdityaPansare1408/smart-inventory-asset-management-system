package com.inventory.repository;

import com.inventory.model.Item;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class InventoryRepository {
    public void saveItems(List<Item> items) {
        try (FileWriter writer = new FileWriter("data/inventory.csv")) {

            writer.write(
                    "itemId,itemName,quantity,price,category,minimumStockLevel\n"
            );

            for (Item item : items) {

                writer.write(
                        item.getItemId() + "," +
                                item.getItemName() + "," +
                                item.getQuantity() + "," +
                                item.getPrice() + "," +
                                item.getCategory() + "," +
                                item.getMinimumStockLevel() + "\n"
                );

            }

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public List<Item> loadItems() {

        List<Item> items = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader("data/inventory.csv"))) {

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",");

                Item item = new Item(
                        data[0],
                        data[1],
                        Integer.parseInt(data[2]),
                        Double.parseDouble(data[3]),
                        data[4],
                        Integer.parseInt(data[5])
                );

                items.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }
}
