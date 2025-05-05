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
    private String location = null;
    private String ownedBy = null;
    private ArrayList<String> reserved = new ArrayList<String>();
    
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
    
    public Item(String i, String t, String y, String a, String loc, String own) {
    	itemID = i;
        title = t;
        year = y;
        author = a;
        location = (loc == "null" ? null : loc);
        ownedBy = (own == "null" ? null : own);
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
    	return (ownedBy != null && ownedBy != "null");
    }
    
    public void setOwner(String id) {
        ownedBy = id;
    }

    public void removeOwner() {
    	ownedBy = null;
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

    public String toString() {
        return itemID + "," + title + "," + year + "," + author + "," + location + "," + ownedBy; //need to write reserved list too
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