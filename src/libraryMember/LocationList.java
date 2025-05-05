<<<<<<< HEAD
package libraryMember;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LocationList {
    private ArrayList<Location> locations;
    private String sourceName = "locationList.txt";
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
        save();
    }
    
    public ItemList loadItems() {
        ItemList allItems = new ItemList(itemListType.Library, "");

        for (Location loc : locations) {
            ItemList inventory = loc.getInventory(); // returns Item[]
            if (inventory != null) {
                for (Item item : inventory.getIArray()) {
                    if (item != null) {
                        allItems.addItem(item);
                    }
                }
            }
        }

        return allItems;
    }
    
    public StaffMemberList loadStaff() {
    	StaffMemberList allStaff = new StaffMemberList("");

        for (Location loc : locations) {
        	StaffMemberList staff = loc.getStaffMembers(); // returns Item[]
            if (staff != null) {
                for (StaffMember s : staff.getMArray()) {
                    if (s != null) {
                        allStaff.addMember(s);
                    }
                }
            }
        }

        return allStaff;
    }
    
    public void save() {
        if (!modified) {
            return;
        }

        try (FileWriter writer = new FileWriter(sourceName)) {
            for (Location location : locations) {
                writer.write(location.toString() + "\n");
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

            Scanner scanner = new Scanner(file);
			scanner.useDelimiter(",|\n");
			

	    	//file format
			//locationName + "," + rentalItemList.sourcename() + ","
	    	//+ reserveItemList.sourcename() + "," + locationInventory.sourcename()
	    	//+ "," + locationStaff.sourcename();
			while(scanner.hasNext()) {
				String name = scanner.next();
				String rentalList = scanner.next();
				String reserveList = scanner.next();
				String locationList = scanner.next();
				String staffList = scanner.next();
				Location temp = new Location(name, rentalList, reserveList, locationList, staffList);
				
				locations.add(temp);
			}
			scanner.close();
			modified = false;
        } catch (Exception e) {
            System.out.println("Error loading location list: " + e.getMessage());
        }
    }
    
    public List<Location> getLocations() {
        return new ArrayList<>(locations); // Return a copy to prevent external modifications
    }
=======
package scmot;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LocationList {
    private ArrayList<Location> locations;
    private String sourceName = "locationList.txt";
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
            	System.out.println(location.toString());
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
        save();
    }
    
    public ItemList loadItems() {
        ItemList allItems = new ItemList(itemListType.Library, "");

        for (Location loc : locations) {
            ItemList inventory = loc.getInventory(); // returns Item[]
            if (inventory != null) {
                for (Item item : inventory.getIArray()) {
                    if (item != null) {
                        allItems.addItem(item);
                    }
                }
            }
        }

        return allItems;
    }
    
    public StaffMemberList loadStaff() {
    	StaffMemberList allStaff = new StaffMemberList("");

        for (Location loc : locations) {
        	StaffMemberList staff = loc.getStaffMembers(); // returns Item[]
            if (staff != null) {
                for (StaffMember s : staff.getMArray()) {
                    if (s != null) {
                        allStaff.addMember(s);
                    }
                }
            }
        }

        return allStaff;
    }
    
    public void save() {
    	
        try (FileWriter writer = new FileWriter(sourceName)) {
            for (Location location : locations) {
                writer.write(location.toString() + "\n");
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

            Scanner scanner = new Scanner(file);
			scanner.useDelimiter(",|\n");
			

	    	//file format
			//locationName + "," + rentalItemList.sourcename() + ","
	    	//+ reserveItemList.sourcename() + "," + locationInventory.sourcename()
	    	//+ "," + locationStaff.sourcename();
			while(scanner.hasNext()) {
				String name = scanner.next();
				String rentalList = scanner.next();
				String reserveList = scanner.next();
				String locationList = scanner.next();
				String staffList = scanner.next();
				Location temp = new Location(name, rentalList, reserveList, locationList, staffList);
				
				locations.add(temp);
			}
			scanner.close();
			modified = false;
        } catch (Exception e) {
            System.out.println("Error loading location list: " + e.getMessage());
        }
    }
    
    public List<Location> getLocations() {
        return new ArrayList<>(locations); // Return a copy to prevent external modifications
    }
>>>>>>> 4cdfb2fec0b8f15d4510487f382727fffdb2e88d
}