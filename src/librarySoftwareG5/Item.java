package librarySoftwareG5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Item implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String itemID;
    private String title;
    private String year;
    private String author;
    private String location;
    private String ownedBy; // Stores the memberID of the current owner
    private List<String> reservedBy; // List of memberIDs who reserved the item

    // Transient fields for display purposes, populated by the server
    private transient String ownedByName;
    private transient List<String> reservedByNames;

    public Item(String itemID, String title, String year, String author,
               String location, String ownedByFromFile, int reservedCountFromFile, List<String> reservedByIdsFromFile) {
        this.itemID = itemID;
        this.title = title;
        this.year = year;
        this.author = author;
        this.location = location;
        this.ownedBy = ownedByFromFile; //This is the ID from the file
        this.reservedBy = new ArrayList<>(reservedByIdsFromFile);
        this.reservedByNames = new ArrayList<>(); // Initialize as empty, to be populated
        // ownedByName will be populated by enrichItemDetails or during checkout
    }

    // Getters
    public String getItemID() { return itemID; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getAuthor() { return author; }
    public String getLocation() { return location; }
    public String getOwnedBy() { return ownedBy; }
    public int getReservedCount() { return reservedBy.size(); }
    public List<String> getReservedBy() { return new ArrayList<>(reservedBy); }

    // Getters for transient display fields (might be useful for server-side logic too)
    public String getOwnedByName() { return ownedByName; }
    public List<String> getReservedByNames() {
        return reservedByNames != null ? new ArrayList<>(reservedByNames) : new ArrayList<>();
    }

    // Setters for transient display fields (used by enrichItemDetails or direct updates)
    public void setOwnedByName(String name) { this.ownedByName = name; }
    public void setReservedByNames(List<String> names) {
        this.reservedByNames = (names != null) ? new ArrayList<>(names) : new ArrayList<>();
    }


    // Setters for core data
    public void setLocation(String location) { this.location = location; }

    // Ownership management
    public boolean setOwnedBy(String memberId, String memberName) {
        if (this.ownedBy == null || this.ownedBy.isEmpty()) {
            this.ownedBy = memberId;
            this.ownedByName = memberName; // Directly set the name
            return true;
        }
        return false;
    }

    public boolean releaseOwnership() {
        if (this.ownedBy != null && !this.ownedBy.isEmpty()) {
            this.ownedBy = null;
            this.ownedByName = null; // Clear the name as well
            return true;
        }
        return false;
    }

    // Reservation management
    public boolean addReservation(String memberId, String memberName) {
        if (!reservedBy.contains(memberId)) {
            reservedBy.add(memberId);
            if (this.reservedByNames == null) { // Ensure list is initialized
                this.reservedByNames = new ArrayList<>();
            }
            this.reservedByNames.add(memberName); // Add name in corresponding order
            return true;
        }
        return false;
    }

    public boolean removeReservation(String memberId) {
        int index = reservedBy.indexOf(memberId);
        if (index != -1) { // If memberId is found in the reservation list
            reservedBy.remove(index);
            if (this.reservedByNames != null && index < this.reservedByNames.size()) {
                this.reservedByNames.remove(index); // Remove name at the same position
            }
            return true;
        }
        return false;
    }

    // File format: itemID,Title,Year,Author,Location,ownedBy,reservedCount,reservedID1,reservedID2,...
    public String toFileFormat() {
        // Note: ownedByName and reservedByNames are transient and NOT saved to the file.
        // The file only stores IDs.
        return String.format("%s,%s,%s,%s,%s,%s,%d,%s",
            itemID, title, year, author, location,
            this.ownedBy != null ? this.ownedBy : "null",
            reservedBy.size(),
            String.join(",", reservedBy));
    }

    @Override
    public String toString() {
        String statusDetails;
        if (ownedByName != null && !ownedByName.isEmpty()) {
            statusDetails = "Checked out to: " + ownedByName + " (ID: " + ownedBy + ")";
        } else if (ownedBy != null && !ownedBy.isEmpty()) {
            statusDetails = "Checked out to member ID: " + ownedBy;
        } else {
            statusDetails = "Available";
        }

        String reservationDetails = "";
        if (reservedByNames != null && !reservedByNames.isEmpty()) {
            // Display names if available
            String reservedDisplayString = reservedByNames.stream()
                .map(name -> (name != null && name.contains("(ID:")) ? name : name + " (ID: " + reservedBy.get(reservedByNames.indexOf(name)) +")" ) // Ensure ID is appended if not already
                .collect(Collectors.joining(", "));
            reservationDetails = " | Reserved by: [" + reservedDisplayString + "]";
        } else if (reservedBy != null && !reservedBy.isEmpty()) {
            // Fallback to IDs if names list is empty but ID list is not
            String reservedIdsString = String.join(", ", reservedBy);
            reservationDetails = " | Reserved by IDs: [" + reservedIdsString + "]";
        }

        return String.format("ID: %s | Title: %s | Author: %s | Location: %s | Status: %s%s",
            itemID, title, author, location,
            statusDetails,
            reservationDetails);
    }
}
