package scmot;
import java.io.*;
import java.util.Scanner;

public class ItemList {
	private Integer numItems;
	private Item[] iArray;
	private String sourceName;
	private itemListType type;
	private boolean modified = false;
	
	
	// Two types of item lists, one will be for keeping track of rentals, the other for reservations
	public ItemList(itemListType t) {
		numItems = 0;
		iArray = new Item[1000];
		type = t;
		if(type == itemListType.Rental) {
			sourceName = "itemRentalList.txt";
		}
		else if(type == itemListType.Reservation) {
			sourceName = "itemReservationList.txt";
		}
		else if(type == itemListType.Library) {
			sourceName = "itemsInLibrary.txt";
		}
	}

	// If member isn't here then an empty string should be returned
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

	// If list is of type library you can not add owners to it
	// Get memberID and ItemID, look for the matching itemID
	// if no matches or item is out of copies return false
	// otherwise return true
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

	// Same thing as add member to Item but remove instead 
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
	
	// Add an item with title, year, author, quantity 
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
		save();
	}
	
	//NEW FUNCTION ADDED BY JORDAN add item as item
	public void addItem(Item item) {
		System.out.println(item.toString());
		
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
	
	// Get the type of list this is
	public itemListType getType() {
		return type;
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
	
	//NEW FUNCTION ADDED BY JORDAN
	public Item getItemFromTitle(String title) {
		for(int i = 0; i < numItems; i++) {
			if(iArray[i].getTitle().compareTo(title) == 0) {
				return iArray[i];
			}
		}
		return null;
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

	public boolean load() {
		//System.out.println("Attempting to load items from: " + sourceName);

	    File file = new File(sourceName);
	    numItems = 0;

	    try {
	        if (!file.exists()) {
	            file.createNewFile(); // create it if needed
	            return false;         // optional: no data to load
	        }

	        Scanner scanner = new Scanner(file);
	        scanner.useDelimiter(",|\n");

	        while (scanner.hasNext()) {
	            String id = scanner.next();
	            String title = scanner.next();
	            String year = scanner.next();
	            String author = scanner.next();
	            int quantity = Integer.parseInt(scanner.next());
	            int numOwners = Integer.parseInt(scanner.next());

	            Item temp = new Item(id, title, year, author, quantity);

	            for (int i = 0; i < numOwners; i++) {
	                if (scanner.hasNext()) {
	                    temp.addMember(scanner.next());
	                }
	            }

	            iArray[numItems++] = temp;
	        }

	        scanner.close();
	        return true;
	    } catch (Exception e) {
	        System.out.println("Error reading file: " + e.getMessage());
	        return false;
	    }
	}


	public void save() {
	    if (!modified) return;

	    try {
	        FileWriter out = new FileWriter(sourceName);

	        for (int i = 0; i < numItems; i++) {
	            Item item = iArray[i];

	            // Format: ID,Title,Year,Author,Quantity,NumOwners,Owner1,Owner2,...
	            out.write(item.getID() + "," +
	                      item.getTitle() + "," +
	                      item.getYear() + "," +
	                      item.getAuthor() + "," +
	                      item.getQuantity() + "," +
	                      item.getNumMembers());

	            for (String member : item.getMemberList()) {
	                out.write("," + member);
	            }
	            out.write("\n");
	        }

	        out.close();
	        modified = false;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
}