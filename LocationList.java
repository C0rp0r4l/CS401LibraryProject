package scmot;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LocationList {
    private ArrayList<Location> locations;
    private String sourceName = "locationlist.txt";
    private boolean modified = false;
    
    public LocationList() {
        locations = new ArrayList<>();
    }
    
    public void setFilename(String filename) {
        sourceName = filename;
    }
    
    public String toString() {
        StringBuilder list = new StringBuilder();
        for (Location location : locations) {
            list.append(location.getLocationName()).append("\n");
        }
        return list.toString();
    }

    public Location searchLocation(String locationName) {
        for (Location location : locations) {
            if (location.getLocationName().equalsIgnoreCase(locationName)) {
                return location;
            }
        }
        return null;
    }

    public int getNumLocations() {
        return locations.size();
    }
    
    public boolean removeLocation(String name) {
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).getLocationName().equals(name)) {
                locations.remove(i);
                modified = true;
                return true;
            }
        }
        return false;
    }
    
    public void addLocation(String name) {
        locations.add(new Location(name));
        modified = true;
    }
    
    public void save() {
        if (!modified) {
            return;
        }

        try (FileWriter writer = new FileWriter(sourceName)) {
            for (Location location : locations) {
                writer.write(location.getLocationName() + "\n");
            }
            modified = false;
        } catch (Exception e) {
            System.out.println("Error saving location list: " + e.getMessage());
        }
    }
    
    public void load() {
        try {
            File file = new File(sourceName);
            if (file.createNewFile()) {
                return;
            }

            try (Scanner scanner = new Scanner(file)) {
                locations.clear(); // Clear existing locations before loading
                while (scanner.hasNextLine()) {
                    String name = scanner.nextLine().trim();
                    if (!name.isEmpty()) {
                        addLocation(name);
                    }
                }
            }
            modified = false;
        } catch (Exception e) {
            System.out.println("Error loading location list: " + e.getMessage());
        }
    }
    
    public List<Location> getLocations() {
        return new ArrayList<>(locations); // Return a copy to prevent external modifications
    }
}