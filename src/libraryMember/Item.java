package libraryMember;

import java.util.Random;

public class Item {
	static private int count = 0;
	private String itemID;
	private String title;
	private String year;
	private String author;
	private String[] memberList;
	private Integer numMember;
	private Integer quantity;
	
	public Item(String t, String y, String a, Integer q) {
		title = t;
		year = y;
		author = a;
		quantity = q;
		memberList = new String[100];
		
		// An issue with just using random is that it's posssible for two items to get the same ID
		// by appending a count to it this assures even if the first X digits are the same the last two are always
		// different
		String temp = t.substring(0, 2) + y.substring(0,4) + a.substring(0,2);
		Random rand = new Random();
		int randomDigits = rand.nextInt(30000) + 10000;
		itemID = temp + randomDigits + count;
		count++;
	}
	
	// Constructor ONLY for loading items into an item list
	public Item(String i, String t, String y, String a, Integer q) {
		title = t;
		year = y;
		author = a;
		quantity = q;
		memberList = new String[100];
	}
	
	// Check if the number of owners is higher than the available quantity
	// If it is return false, if not return true (indicating a succesful operation)
	public Boolean addMember(String id) {
		if(memberList.length >= quantity) {
			return false;
		}
		else {
			memberList[numMember] = id;
			return true;
		}
	}

	public Boolean removeMember(String id){
		for(int i = 0; i < numMember; i++) {
			if(memberList[i].compareTo(id) == 0){
				memberList[i] = memberList[numMember];
				return true;
			}
		}
		return false;
	}
	
	// Print out the attributes seperated by comma and the number of owners + list of members
	// number of owners is important because we need to somehow know the number of strings to parse
	// while saving or loading.
	public String toString() {
		String temp = itemID + "," + title + "," + year + "," + author + "," + quantity + "," + numMember;
		for(int i = 0; i < memberList.length; i++) {
			String list = "," + memberList[i];
			temp.concat(list);
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
