package libraryMember;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class ItemList implements Serializable{
	private static final long serialVersionUID = 1L;
    private List<Item> items;
    private static final String ITEM_FILE = "masterItemList.txt";

    public ItemList() {
        items = new ArrayList<>();
        loadItems();
    }

    //Loading and Saving----------------------------------------------------------------------
    public void loadItems() {
        items.clear();
        File file = new File(ITEM_FILE);
        
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating item file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String itemID = parts[0].trim();
                    String title = parts[1].trim();
                    String year = parts[2].trim();
                    String author = parts[3].trim();
                    String location = parts[4].trim();
                    String ownedBy = parts[5].trim().equals("null") ? null : parts[5].trim();
                    int reserved = Integer.parseInt(parts[6].trim());
                    
                    List<String> reservedBy = new ArrayList<>();
                    for (int i = 7; i < parts.length; i++) {
                        reservedBy.add(parts[i].trim());
                    }
                    
                    items.add(new Item(itemID, title, year, author, location, 
                                     ownedBy, reserved, reservedBy));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading items: " + e.getMessage());
        }
    }

    public void saveItems() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ITEM_FILE))) {
            for (Item item : items) {
                writer.write(item.toFileFormat() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving items: " + e.getMessage());
        }
    }

    //Item management------------------------------------------------------------------------
    public String addItem(String title, String year, String author, String location) {
        String newId = generateItemId(title, year, author);
        Item newItem = new Item(newId, title, year, author, location, null, 0, new ArrayList<>());
        items.add(newItem);
        saveItems();
        return newId;
    }

    public boolean removeItem(String itemId) {
        boolean removed = items.removeIf(item -> item.getItemID().equals(itemId));
        if (removed) saveItems();
        return removed;
    }

    //Item Accessing---------------------------------------------------------
    public Item getItem(String itemId) {
        return items.stream()
            .filter(item -> item.getItemID().equals(itemId))
            .findFirst()
            .orElse(null);
    }

    public List<Item> searchByTitle(String title) {
        List<Item> results = new ArrayList<>();
        for (Item item : items) {
            if (item.getTitle().equalsIgnoreCase(title)) {
                results.add(item);
            }
        }
        return results;
    }

    //Checkout/Reserve Items-----------------------------------------
    public boolean checkoutItem(String itemId, String memberId) {
        Item item = getItem(itemId);
        if (item != null && item.getOwnedBy() == null) {
            boolean success = item.setOwnedBy(memberId);
            if (success) saveItems();
            return success;
        }
        return false;
    }

    public boolean returnItem(String itemId) {
        Item item = getItem(itemId);
        if (item != null && item.getOwnedBy() != null) {
            boolean success = item.releaseOwnership();
            if (success) saveItems();
            return success;
        }
        return false;
    }

    public boolean reserveItem(String itemId, String memberId) {
        Item item = getItem(itemId);
        if (item != null) {
            boolean success = item.addReservation(memberId);
            if (success) saveItems();
            return success;
        }
        return false;
    }

    public boolean cancelReservation(String itemId, String memberId) {
        Item item = getItem(itemId);
        if (item != null) {
            boolean success = item.removeReservation(memberId);
            if (success) saveItems();
            return success;
        }
        return false;
    }

    //ID Creation--------------------------------------------------------
    private String generateItemId(String title, String year, String author) {
        String prefix = title.substring(0, Math.min(3, title.length())) + 
                      year.substring(Math.max(0, year.length()-2)) + 
                      author.substring(0, Math.min(2, author.length()));
        return prefix + (items.size() + 1000);
    }
    
    //Get All Items--------------------------------------------------------
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }
}