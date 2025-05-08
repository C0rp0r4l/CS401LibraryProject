package libraryMember;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Item implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String itemID;
    private String title;
    private String year;
    private String author;
    private String location;
    private String ownedBy;
    private int reserved;
    private List<String> reservedBy;
    
    public Item(String itemID, String title, String year, String author, 
               String location, String ownedBy, int reserved, List<String> reservedBy) {
        this.itemID = itemID;
        this.title = title;
        this.year = year;
        this.author = author;
        this.location = location;
        this.ownedBy = ownedBy;
        this.reserved = reserved;
        this.reservedBy = new ArrayList<>(reservedBy);
    }

    // Getters
    public String getItemID() { return itemID; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getAuthor() { return author; }
    public String getLocation() { return location; }
    public String getOwnedBy() { return ownedBy; }
    public int getReservedCount() { return reserved; }
    public List<String> getReservedBy() { return new ArrayList<>(reservedBy); }

    // Setters
    public void setLocation(String location) { this.location = location; }
    
    // Ownership management
    public boolean setOwnedBy(String memberID) {
        if (ownedBy == null || ownedBy.isEmpty()) {
            ownedBy = memberID;
            return true;
        }
        return false;
    }
    
    public boolean releaseOwnership() {
        if (ownedBy != null && !ownedBy.isEmpty()) {
            ownedBy = null;
            return true;
        }
        return false;
    }

    // Reservation management
    public boolean addReservation(String memberID) {
        if (!reservedBy.contains(memberID)) {
            reservedBy.add(memberID);
            reserved = reservedBy.size();
            return true;
        }
        return false;
    }
    
    public boolean removeReservation(String memberID) {
        boolean removed = reservedBy.remove(memberID);
        if (removed) {
            reserved = reservedBy.size();
        }
        return removed;
    }

    // File format: itemID,Title,Year,Author,Location,ownedBy,reservedCount,reservedID1,reservedID2,...
    public String toFileFormat() {
        return String.format("%s,%s,%s,%s,%s,%s,%d,%s",
            itemID, title, year, author, location, 
            ownedBy != null ? ownedBy : "null",
            reserved,
            String.join(",", reservedBy));
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Title: %s | Author: %s | Location: %s | %s | Reservations: %d",
            itemID, title, author, location,
            ownedBy != null ? "Checked out by: " + ownedBy : "Available",
            reserved);
    }
}