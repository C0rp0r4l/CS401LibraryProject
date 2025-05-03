package scmot;

import java.util.Random;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Item implements Serializable {

	private static final long serialVersionUID = 1L;
	private static int count = 0;

	private String itemID;
	private String title;
	private String year;
	private String author;
	private String[] memberList;
	private int numMember;
	private int quantity;

	public Item(String t, String y, String a, Integer q) {
		title = t;
		year = y;
		author = a;
		quantity = q;
		numMember = 0;
		memberList = new String[100];

		String temp = t.substring(0, 2) + y.substring(0, 4) + a.substring(0, 2);
		Random rand = new Random();
		int randomDigits = rand.nextInt(30000) + 10000;
		itemID = temp + randomDigits + count;
		count++;
	}

	// Constructor ONLY for loading from file
	public Item(String i, String t, String y, String a, Integer q) {
		itemID = i;
		title = t;
		year = y;
		author = a;
		quantity = q;
		numMember = 0;
		memberList = new String[100];
	}

	public boolean isCheckedOut() {
		return numMember >= quantity;
	}

	public boolean addMember(String id) {
		if (isCheckedOut()) return false;

		memberList[numMember++] = id;
		return true;
	}

	public boolean removeMember(String id) {
		for (int i = 0; i < numMember; i++) {
			if (memberList[i].equals(id)) {
				memberList[i] = memberList[numMember - 1]; // swap with last
				numMember--;
				return true;
			}
		}
		return false;
	}

	public boolean isMemberHere(String id) {
		for (int i = 0; i < numMember; i++) {
			if (memberList[i].equals(id)) return true;
		}
		return false;
	}

	public String toString() {
		String temp = itemID + "," + title + "," + year + "," + author + "," + quantity + "," + numMember;
		for (int i = 0; i < numMember; i++) {
			temp += "," + memberList[i];
		}
		return temp;
	}

	// Accessors
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

	public int getQuantity() {
		return quantity;
	}

	public int getNumMembers() {
		return numMember;
	}

	public List<String> getMemberList() {
		List<String> members = new ArrayList<>();
		for (int i = 0; i < numMember; i++) {
			members.add(memberList[i]);
		}
		return members;
	}
}
