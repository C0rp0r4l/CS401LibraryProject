package libraryMember;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class ItemList {
    private Integer numItems;
    private Item[] iArray;
    private String sourceName;
    private itemListType type;
    private boolean modified = false;
    
    public ItemList(itemListType t) {
        numItems = 0;
        iArray = new Item[1000];
        type = t;
        if(type == itemListType.Rental) {
            sourceName = "itemRentalList";
        }
        else if(type == itemListType.Reservation) {
            sourceName = "itemReservationList";
        }
        else if(type == itemListType.Library) {
            sourceName = "itemsInLibrary";
        }
    }

    public String listOfItemRentedByMember(String id) {
        String list = "";
        for(int i = 0; i < numItems; i++) {
            if(iArray[i].isMemberHere(id) == true) {
                list.concat(iArray[i].getID());
            }
        }
        return list;
    }
    
    public String toString() {
        String list = "";
        for(int i = 0; i < numItems; i++) {
            String temp = iArray[i].toString() + "\n";
            list = list.concat(temp);
        }
        return list;
    }

    public Boolean addMemberToItem(String memberID, String itemID) {
        if(this.type == itemListType.Library) {
            return false;
        }
        else {
            for(int i = 0; i < numItems; i++) {
                if(iArray[i].getID().compareTo(itemID) == 0){
                    Boolean success = iArray[i].addMember(memberID);
                    if(success == false){
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public Boolean removeMemberFromItem(String memberID, String itemID) {
        if(this.type == itemListType.Library) {
            return false;
        }
        else {
            for(int i = 0; i < numItems; i++) {
                if(iArray[i].getID().compareTo(itemID) == 0){
                    Boolean success = iArray[i].removeMember(memberID);
                    if(success == false) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
    
	//NEW FUNCTION ADDED BY JORDAN
	public Item getItemFromTitle(String title) {
		for(int i = 0; i < numItems; i++) {
			if(iArray[i].getTitle().compareTo(title) == 0) {
				return iArray[i];
			}
		}
		return null;
	}
    
    public void addItem(String t, String y, String a, int q) {
        Item temp = new Item(t, y, a, q);
        
        if(numItems == iArray.length) {
            Item[] newArray = new Item[numItems *2];
            for(int i = 0; i < (numItems*2); i++) {
                newArray[i] = iArray[i];
            }
            iArray = newArray;
        }
        iArray[numItems] = temp;
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
        if(modified == false) {
            return;
        }
        
        try {
            FileWriter out = new FileWriter(sourceName);
            
            for(int i = 0; i < numItems; i++) {
                out.write(iArray.toString() + "\n");
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
                Integer quantity = Integer.valueOf(scanner.next());
                Item temp = new Item(id, title, year, author, quantity);
                
                Integer numOwners = Integer.valueOf(scanner.next());
                
                for(int i = 0; i < numOwners; i++) {
                    String scan = scanner.next();
                    temp.addMember(scan);
                }
                scanner.close();
                return true;
            }
        }
        catch (Exception e) {
            System.out.println("Error reading file");
        }
        return false;
    }
}