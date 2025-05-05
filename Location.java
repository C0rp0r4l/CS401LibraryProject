package scmot;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Location {
    private String locationName;
    private static ItemList rentalItemList;
    private static ItemList reserveItemList;
    private static ItemList locationInventory;
    private static StaffMemberList locationStaff;
    
    // Constructor for new location
    public Location(String name) {
        this.locationName = name;
        this.locationInventory = new ItemList(itemListType.Library, name);
        this.reserveItemList = new ItemList(itemListType.Reservation, name);
        this.rentalItemList = new ItemList(itemListType.Rental, name);
        this.locationStaff = new StaffMemberList(name);

        locationInventory.load();
        reserveItemList.load();
        rentalItemList.load();
        locationStaff.loadList();
    }
    
    public Location(String n, String locList, String resList, String rentList, String staffList) {
        this.locationName = n;
        this.locationInventory = new ItemList(locList, itemListType.Library);
        this.reserveItemList = new ItemList(resList, itemListType.Reservation);
        this.rentalItemList = new ItemList(rentList, itemListType.Rental);
        this.locationStaff = new StaffMemberList(staffList);

        locationInventory.load();
        reserveItemList.load();
        rentalItemList.load();
        locationStaff.loadList();
    }

    // Getters
    public String getLocationName() {
        return locationName;
    }
    
    public ItemList getInventory() {
        return locationInventory;
    }
    
    public StaffMemberList getStaffMembers() {
        return locationStaff;
    }
    
    // Inventory Management Methods
    public void addItem(String title, String year, String author) {
    	locationInventory.addItem(title, year, author);
    	locationInventory.save();
    }
    
    public void removeItem(String itemID) {
    	locationInventory.removeItem(itemID);
    	locationInventory.save();
    }
    
    public boolean transferItem(String itemID, Location destination) {
        if (locationInventory.getTitle(itemID).equals("Book not found")) {
            return false;
        }
        
        String title = locationInventory.getTitle(itemID);
        String year = locationInventory.getYear(itemID);
        String author = locationInventory.getAuthor(itemID);
        int quantity = 1;
        
        destination.addItem(title, year, author);
        locationInventory.removeItem(itemID);
        
        locationInventory.save();
        destination.getInventory().save();
        
        return true;
    }
    
    // Staff Management
    public void addStaffMember(String name) {
        locationStaff.addMember(name);
        locationStaff.saveList();
    }
    
    public void addStaffMember(StaffMember s) {
        locationStaff.addMember(s);
        locationStaff.saveList();
    }
    
    public boolean removeStaffMember(String userID) {
        StaffMember staff = (StaffMember) locationStaff.searchMember(userID);
        if (staff != null) {
            locationStaff.removeMember(userID);
            locationStaff.saveList();
            return true;
        }
        return false;
    }
    
    public boolean transferStaff(String userID, Location destination) {
        StaffMember staff = (StaffMember) locationStaff.searchMember(userID);
        if (staff != null) {
            destination.getStaffMembers().addMember(staff.getName());
            locationStaff.removeMember(userID);
            locationStaff.saveList();
            destination.getStaffMembers().saveList();
            return true;
        }
        return false;
    }
    
    // Sorting Methods
    private void sortItems(Comparator<Item> comparator) {
    	Item[] items = locationInventory.getIArray();
        int numItems = locationInventory.getNumItems();
        
        // Simple bubble sort implementation
        for (int i = 0; i < numItems - 1; i++) {
            for (int j = 0; j < numItems - i - 1; j++) {
                if (comparator.compare(items[j], items[j + 1]) > 0) {
                    // Swap items
                    Item temp = items[j];
                    items[j] = items[j + 1];
                    items[j + 1] = temp;
                }
            }
        }
        locationInventory.save();
    }
    
    public void sortByTitle() {
        sortItems(Comparator.comparing(Item::getTitle));
    }
    
    public void sortByAuthor() {
        sortItems(Comparator.comparing(Item::getAuthor));
    }
    
    public void sortByYear() {
        sortItems(Comparator.comparing(Item::getYear));
    }
    
    public void sortByTitleThenAuthor() {
        sortItems(Comparator.comparing(Item::getTitle)
                  .thenComparing(Item::getAuthor));
    }
    
    public String toString() {
        return locationName + "," + rentalItemList.sourcename() + "," + reserveItemList.sourcename() + "," + locationInventory.sourcename() + "," + locationStaff.sourcename();
    }
    
    public String displaySortedInventory() {
        StringBuilder sb = new StringBuilder();
        Item[] items = locationInventory.getIArray();
        int numItems = locationInventory.getNumItems();
        
        for (int i = 0; i < numItems; i++) {
            Item item = items[i];
            sb.append(String.format("%s by %s (%s)\n", 
                item.getTitle(), item.getAuthor(), item.getYear()));
        }
        return sb.toString();
    }
    
}