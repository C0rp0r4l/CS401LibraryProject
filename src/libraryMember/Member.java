package libraryMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Member {
	//variables
	private String userID;
	//private String userPassword;
	boolean accountHold;
	private String name;
	private int strikes;
	private List<Item> checkedOutItems;
	private List<Item> reservedItems;
	
	//methods
	//constructor
	public Member(String name) {
		//take first 3 characters of new members name, or use the whole thing if less than 3 characters
		String namePrefix = name.length() >= 3 ? name.substring(0, 3) : name;
		//now generate 3 random digits
		Random rand = new Random();
		//gives a random number between 100 and 999
		int randomDigits = rand.nextInt(900) + 100;
		//now append the random digits onto the name prefix for our new userID
		this.userID = namePrefix + randomDigits;
		//now construct the rest of the variables
		this.accountHold = false;
		this.name = name;
		this.strikes = 0;
		this.checkedOutItems = new ArrayList<>();
		this.reservedItems = new ArrayList<>();
		//REMEMBER THE MAIN WAY TO FIND A MEMBER IS WITH THE USERID
	}
	
	//get userID
	public String getUserID() {
	    return this.userID;
	}
	
	//get account standing
	public String getStanding() {
		String standingOutput = "Member account " + this.userID + " has " + this.strikes + " strikes.\n";
		return standingOutput;
	}
	
	//get account hold status
	public String getAccountHold() {
		if (accountHold == true) {
			String accountHoldStatus = "Account " + this.userID + " is on hold.\n";
			return accountHoldStatus;
		}
		else {
			String accountHoldStatus = "Account " + this.userID + " is not on hold.\n";
			return accountHoldStatus;
		}
	}
	
	//set account hold status
	//have client prompt user for 'T' or 'F' to set state
	public void setAccountHold(String hold) {
		if (hold.equalsIgnoreCase("t")) {
			this.accountHold = true;
			return;
		}
		
		else {
			this.accountHold = false;
		}
	}
	
	//add items to accounts Checked out Items
	public void checkoutItem() {
		//takes itemID and puts that under the account, itemID is used to find item info (author, title etc)
		Item newItem = new Item();
		//add the new item to the list of checked out items
		this.checkedOutItems.add(newItem);
	}
	
	//add items to accounts reserved Items
		public void reserveItem() {
			//takes itemID and puts that under the account, itemID is used to find item info (author, title etc)
			Item newItem = new Item();
			//add the new item to the list of checked out items
			this.reservedItems.add(newItem);
		}
	
	
	//get account checked out items
	public String getCheckedItems() {
	//for all items in checkedOutItems, iterate and add each item to allcheckeditems string to send
	String allCheckedOutItems = "";
		for (Item ci : this.checkedOutItems) {
			allCheckedOutItems += ci.getItem() + "\n";
		}
		return allCheckedOutItems;
	}
}
