package libraryMember;
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
			sourceName = "itemRentalList";
		}
		else if(type == itemListType.Reservation) {
			sourceName = "itemReservationList";
		}
		else if(type == itemListType.Library) {
			sourceName = "itemsInLibrary";
		}
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
		// Save writes out the attributes seperated by comma and the number of owners + list of members
		// number of owners is important because we need to somehow know the number of strings to parse
		// while saving or loading.
		
		File file = new File(sourceName);
		try {
			if(file.createNewFile()) {
				return false;
			}
			
			Scanner scanner = new Scanner(file);
			scanner.useDelimiter(",|\n");
			
			// Format of each item 
			// itemID + "," + title + "," + year + "," + author + "," + quantity + "," + numOwners + "," ownerList[] 
			while(scanner.hasNext()) {
				String id = scanner.next();
				String title = scanner.next();
				String year = scanner.next();
				String author = scanner.next();
				//I don't know if this works? 
				Integer quantity = Integer.valueOf(scanner.next());
				Item temp = new Item(id, title, year, author, quantity);
				
				Integer numOwners = Integer.valueOf(scanner.next());
				
				// Get the number of owners, repeatedly scan and call addOwner until done then repeat the process
				// for each item
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
		//False return in case item loading was successful but it's an empty file
		return false;
	}
	
}
