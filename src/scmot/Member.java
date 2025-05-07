package scmot;

import java.io.Serializable;
import java.util.Random;

public class Member implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//variables
	static private int count = 0;
	private String userID;
	//private String userPassword;
	boolean accountHold;
	private String name;
	private int strikes;
	private boolean banned;

	
	public Member() {
	    this.name = "";
	    this.userID = "";
	    this.accountHold = false;
	    this.strikes = 0;
	    this.banned = false;
	}
	
	//methods
	//constructor
	public Member(String name) {
		//take first 3 characters of new members name, or use the whole thing if less than 3 characters
		String namePrefix = name.length() >= 3 ? name.substring(0, 3) : name;
		//now generate 3 random digits
		Random rand = new Random();
		//gives a random number between 10000 - 40000
		int randomDigits = rand.nextInt(30000) + 10000;
		//now append the random digits onto the name prefix for our new userID
		this.userID = namePrefix + randomDigits + count;
		//now construct the rest of the variables
		this.accountHold = false;
		this.name = name;
		this.strikes = 0;
		
		count++;
		//REMEMBER THE MAIN WAY TO FIND A MEMBER IS WITH THE USERID
	}
	
	// Constructor ONLY for loading already existing members from a file since ID is randomly generated
	// name, userID, strikes, accountHold
	public Member(String n, String uID, String s, String a) {
		name = n;
		userID = uID;
		strikes = Integer.valueOf(s);
		accountHold = Boolean.valueOf(a);
	}
	
	//NEW FUNCTION BY JORDAN
	public boolean addStrike() {
		strikes++;
		if(strikes == 3) {
			banned = true;
		}
		return banned;
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
		return (name + "," + userID + "," + String.valueOf(strikes) + "," + String.valueOf(accountHold));
	}
	
	// Returns the # of strikes as a string
	public String getStrikes() {
		return String.valueOf(strikes);
	}
	
	// Returns true/false of the account hold as a string
	public Boolean getAccountHold() {		
		return accountHold;
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
}