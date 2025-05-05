package libraryMember;

import java.util.Comparator;

public class Location {
    private String locationName;
    private ItemList locationInventory;
    private StaffMemberList locationStaff;
    
    // Constructor for new location
    public Location(String name) {
        this.locationName = name;
        this.locationInventory = new ItemList(itemListType.Library);
        this.locationStaff = new StaffMemberList();
        this.locationInventory.load();
        this.locationStaff.loadList();
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
    public void addItem(String title, String year, String author, int quantity) {
        locationInventory.addItem(title, year, author, quantity);
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
        
        destination.addItem(title, year, author, quantity);
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