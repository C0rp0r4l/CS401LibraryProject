package scmot;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LocationList {
    //variables
	private List<String> locations;
    private String filepath = "locationlist.txt";
    private boolean modified = false;
    
    //methods
    //constructor
    public LocationList() {
        locations = new ArrayList<>();
        load();
    }

    //LOADS LOCATIONS FROM THE TEXT FILE
    public void load() {
        locations.clear();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
                return;
            }

            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String location = scanner.nextLine().trim();
                    if (!location.isEmpty()) {
                        locations.add(location);
                    }
                }
            }
            modified = false;
        } catch (IOException e) {
            System.err.println("Error loading locations: " + e.getMessage());
        }
    }


    
    //SAVES LOCATIONS TO THE TEXT FILE IF MODIFICATIONS WERE MADE 
    public void save() {
        if (!modified) return;

        try (FileWriter writer = new FileWriter(filePath)) {
            for (String location : locations) {
                writer.write(location + "\n");
            }
            modified = false;
        } catch (IOException e) {
            System.err.println("Error saving locations: " + e.getMessage());
        }
    }

    
    //ADDS A NEW LOCATION TO THE LIST
    //return true if added successfully, false if location already exists 
    public boolean addLocation(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = locationName.trim();
        if (!locations.contains(trimmedName)) {
            locations.add(trimmedName);
            modified = true;
            return true;
        }
        return false;
    }

    //REMOVES A LOCATION FROM THE LIST
    //return true if removed successfully, false if location wasn't found
    public boolean removeLocation(String locationName) {
        if (locationName == null) {
            return false;
        }
        
        boolean removed = locations.remove(locationName.trim());
        if (removed) {
            modified = true;
        }
        return removed;
    }
    
    //CHECK IF A LOCATION EXISTS IN THE LIST
    public boolean contains(String locationName) {
        return locations.contains(locationName.trim());
    }

    //RETURN LIST OF ALL LOCATION NAMES
    public List<String> getAllLocations() {
        return new ArrayList<>(locations); // Return a copy to prevent external modification
    }

    //GETS THE NUMBER OF LOCATIONS
    public int size() {
        return locations.size();
    }

    //CLEARS ALL LOCATIONS FROM THE LIST
    public void clear() {
        if (!locations.isEmpty()) {
            locations.clear();
            modified = true;
        }
    }

    @Override
    public String toString() {
        return String.join("\n", locations);
    }
    
}