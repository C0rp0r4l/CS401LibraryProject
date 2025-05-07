package libraryMember;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Item implements Serializable {
    static private int count = 0;
    private String itemID;
    private String title;
    private String year;
    private String author;
    private String location = "null";
    private String ownedBy = "null";
    private ArrayList<String> reserved = new ArrayList<String>();
    
    // Item creation
    public Item(String t, String y, String a) {
        title = t;
        year = y;
        author = a;
        
        String temp = t.substring(0, 2) + y.substring(0,4) + a.substring(0,2);
        Random rand = new Random();
        int randomDigits = rand.nextInt(30000) + 10000;
        itemID = temp + randomDigits + count;
        count++;
    }
    
    // For saving and loading an item 
    public Item(String i, String t, String y, String a, String loc, String own, ArrayList<String> r) {
    	itemID = i;
        title = t;
        year = y;
        author = a;
        location = (loc == "null" ? null : loc);
        ownedBy = (own == "null" ? null : own);
        reserved = r;
    }
    
    public void handleReservation(String id) {
    	if(reserved.contains(id)) {
    		reserved.remove(id);
    	}
    	else {
    		reserved.add(id);
    	}
    }
    
    public boolean hasReservations() {
    	return reserved.size() > 0;
    }
    
    public ArrayList<String> getReservations() {
    	return reserved;
    }
    
    public boolean isCheckedOut() {
    	return (ownedBy != null && !("null".equals(ownedBy)));
    }
    
    public void setOwner(String id) {
        ownedBy = id;
    }

    public void removeOwner() {
    	ownedBy = "null";
    }
    
    public String getOwner() {
    	return ownedBy;
    }
    
    public Boolean isOwner(String id) {
        return ownedBy == id;
    }
    
    public void setLoc(String id) {
        location = id;
    }

    public void removeLoc() {
    	location = null;
    }
    
    public String getLoc() {
    	return location;
    }
    
    public Boolean isAt(String id) {
        return location == id;
    }

    // itemID,Title,Year,Author,Location,RenterID,int of reserved,reservedID's
    public String toString() {
        // Print out the information that always exists first according to the specified format
        // Check if the reserved.size() is bigger than zero or not
        // if it is print out a list of IDs of the members that are reserving this item
        // if not just return temp
        String temp = itemID + "," + title + "," + author + "," + location + "," + ownedBy + "," + reserved.size();
        if(reserved.size() > 0) {
            String tArray[] = (String[]) reserved.toArray();
            for(int i = 0; i < reserved.size(); i++) {
                String reserveID = "," + tArray[i];
                temp = temp.concat(reserveID);
            }
        }

        return temp;
    }
    
    public String getID() {
        return itemID;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getYear() {
        return year;
    }
    
    public String getAuthor() {
        return author;
    }
}