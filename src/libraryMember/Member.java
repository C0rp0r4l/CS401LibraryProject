package scmot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.Serializable;

public class Member implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//variables
	private String userID;
	private String userPassword = generateRandomPass(5);
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
		System.out.println("New Member: " + this.userID + " " + this.userPassword);
	}
	
	// Constructor ONLY for loading already existing members from a file since ID is randomly generated
	// name, userID, strikes, accountHold
	public Member(String n, String uID, String userPass, String s, String a) {
		name = n;
		userID = uID;
		userPassword = userPass;
		strikes = Integer.valueOf(s);
		accountHold = Boolean.valueOf(a);
	}
	
	// get userID
	public String getUserID() {
	    return this.userID;
	}
	
	public String getName() {
		return name;
	}
	
	// Returns a string with format Name,userID,Strikes,accountHold 
	// Example: Ricky Ip,RIC412,0,false
	public String toString() {
		return (name + "," + userID + "," + userPassword + "," + String.valueOf(strikes) + "," + String.valueOf(accountHold));
	}
	
	/*
	//get account standing
	public String getStanding() {
		String standingOutput = "Member account " + this.userID + " has " + this.strikes + " strikes.\n";
		return standingOutput;
	}*/
	
	// Returns the # of strikes as a string
	public String getStrikes() {
		return String.valueOf(strikes);
	}
	
	// Returns true/false of the account hold as a string
	public String getAccountHold() {
		/*
		if (accountHold == true) {
			String accountHoldStatus = "Account " + this.userID + " is on hold.\n";
			return accountHoldStatus;
		}
		else {
			String accountHoldStatus = "Account " + this.userID + " is not on hold.\n";
			return accountHoldStatus;
		}*/
		
		return String.valueOf(accountHold);
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
	
	public String getPassword() {
		return userPassword;
	}
	
	//add items to accounts Checked out Items
	public void checkoutItem() {
		
	}
	
	//add items to accounts reserved Items
	public void reserveItem() {

	}
	
	
	//get account checked out items
	public String getCheckedItems() {
		return "";
	}
	
    // Method to generate a random ID of specified length
    private static String generateRandomPass(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String result = "";

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            result += chars.charAt(index);
        }

        return result;
    }
	
}