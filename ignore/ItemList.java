package scmot;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ItemList implements Serializable{
	//variables
	private static final long serialVersionUID = 1L;
	private Integer numItems;
    private Item[] iArray;
    private String sourceName;
    private itemListType type;
    private boolean modified = false;
    
    //methods
    public ItemList(itemListType t, String name) {
        numItems = 0;
        iArray = new Item[1000];
        type = t;
        if(type == itemListType.Rental) {
            sourceName = name + "ItemRentalList.txt";
        }
        else if(type == itemListType.Reservation) {
            sourceName = name + "ItemReservationList.txt";
        }
        else if(type == itemListType.Library) {
            sourceName = name + "ItemsInLibrary.txt";
        }
    }
    
    public ItemList(String sourcename, itemListType t) {
        numItems = 0;
        iArray = new Item[1000];
        type = t;
        sourceName = sourcename;
    }
    
    public String toString() {
        String list = "";
        for(int i = 0; i < numItems; i++) {
            String temp = iArray[i].toString() + "\n";
            list = list.concat(temp);
        }
        return list;
    }
    
    public String sourcename() {
    	return sourceName;
    }

    public Boolean addOwner(String memberID, String itemID) {
        try {
        	System.out.println(itemID);
            Item item = getItemFromID(itemID);
        	System.out.println(item);
            item.setOwner(memberID);
        	System.out.println(item.getOwner() + " Owns " + item);
            save();
            return true; // success
        } catch (Exception e) {
            return false; // something went wrong
        }
     }

    public Boolean removeOwner(String itemID) {
            try {
                Item item = getItemFromID(itemID);
                item.removeOwner();
                save();
                return true; // success
            } catch (Exception e) {
                return false; // something went wrong
            }
    }
    
    public Boolean addLoc(String locID, String itemID) {
            try {
                Item item = getItemFromID(itemID);
                item.setLoc(locID);
                save();
                return true; // success
            } catch (Exception e) {
                return false; // something went wrong
            }
        }

    public Boolean removeLoc(String itemID) {
            try {
                Item item = getItemFromID(itemID);
                item.removeLoc();
                save();
                return true; // success
            } catch (Exception e) {
                return false; // something went wrong
            }
        }
    
    public Boolean handleReservation(String itemID, String memberID) {
            try {
                Item item = getItemFromID(itemID);
                item.handleReservation(memberID);
                save();
                return true; // success
            } catch (Exception e) {
                return false; // something went wrong
            }
        }
    
	//NEW FUNCTION ADDED BY JORDAN
	public ItemList getItemsFromTitle(String title) {
		ItemList items = new ItemList(itemListType.Library, "");
		for(int i = 0; i < numItems; i++) {
			if(iArray[i].getTitle().compareTo(title) == 0) {
				items.addItem(iArray[i]);
			}
		}
		if(items.getNumItems() > 0) {
			return items;
		}
		else return null;
	}
	
	public Item getItemFromID(String id) {
		for(int i = 0; i < numItems; i++) {
			if(iArray[i].getID().compareTo(id) == 0) {
				return iArray[i];
			}
		}
		return null;
	}
    
    public void addItem(String t, String y, String a) {
        Item temp = new Item(t, y, a);
        
        if(numItems == iArray.length) {
            Item[] newArray = new Item[numItems *2];
            for(int i = 0; i < (numItems*2); i++) {
                newArray[i] = iArray[i];
            }
            iArray = newArray;
        }
        iArray[numItems] = temp;
    	System.out.println("Added " + iArray[numItems] + " to Array");
        numItems++;
        modified = true;
        save();
    }
    
    //NEW FUNCTION FROM JORDAN ADD ITEM BY ITEM
    public void addItem(Item item) {
        
        if(numItems == iArray.length) {
            Item[] newArray = new Item[numItems *2];
            for(int i = 0; i < (numItems*2); i++) {
                newArray[i] = iArray[i];
            }
            iArray = newArray;
        }
        iArray[numItems] = item;
        numItems++;
        modified = true;
    }
    
    public itemListType getType() {
        return type;
    }
    
    public int getNumItems() {
        return numItems;
    }

    public Item[] getIArray() {
        return Arrays.copyOf(iArray, numItems); // Safe copy
    }
    
    public void removeItem(String id) {
        for(int i = 0; i < numItems; i++) {
            if(iArray[i].getID().compareTo(id) == 0) {
                iArray[i] = iArray[numItems];
                numItems--;
            }
        }
    }
    
    public String getTitle(String bookID) {
        for(int i = 0; i < numItems; i++) {
            if(iArray[i].getID().compareTo(bookID) == 0) {
                return iArray[i].getTitle();
            }
        }
        return "Book not found";
    }

    public String getYear(String bookID) {
        for(int i = 0; i < numItems; i++) {
            if(iArray[i].getID().compareTo(bookID) == 0) {
                return iArray[i].getYear();
            }
        }
        return "Book not found";
    }
    
    public String getAuthor(String bookID) {
        for(int i = 0; i < numItems; i++) {
            if(iArray[i].getID().compareTo(bookID) == 0) {
                return iArray[i].getAuthor();
            }
        }
        return "Book not found";
    }

    public void save() {
        try {
            FileWriter out = new FileWriter(sourceName);
            
            for(int i = 0; i < numItems; i++) {
            	System.out.println(iArray[i].toString());
                out.write(iArray[i].toString() + "\n");
            }
            
            out.close();
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean load() {
        File file = new File(sourceName);
        try {
            if(file.createNewFile()) {
                return false;
            }
            
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter(",|\n");
            
            while(scanner.hasNext()) {
                String id = scanner.next();
                String title = scanner.next();
                String year = scanner.next();
                String author = scanner.next();
                String loc = scanner.next();
                String own = scanner.next();
                //need to parse reserved list
                Item temp = new Item(id, title, year, author, loc, own);//change constructor once resereve list is added
                
                addItem(temp);
            }
            
            scanner.close();
            return true;
        }
        catch (Exception e) {
            System.out.println("Error reading file");
        }
        return false;
    }
}