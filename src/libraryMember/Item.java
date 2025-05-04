package libraryMember;

import java.io.Serializable;
import java.util.Random;

public class Item implements Serializable {
    private static int count = 0;
    private String itemID;
    private String title;
    private String year;
    private String author;
    private String[] memberList;
    private int numMember;
    private int quantity;

    // Constructor for new items
    public Item(String t, String y, String a, int q) {
        this.title = t;
        this.year = y;
        this.author = a;
        this.quantity = q;
        this.memberList = new String[100];
        this.numMember = 0;

        String temp = t.substring(0, Math.min(2, t.length())) +
                      y.substring(0, Math.min(4, y.length())) +
                      a.substring(0, Math.min(2, a.length()));

        Random rand = new Random();
        int randomDigits = rand.nextInt(30000) + 10000;
        this.itemID = temp + randomDigits + count;
        count++;
    }

    // Constructor for loading from file (no ID generation)
    public Item(String i, String t, String y, String a, int q) {
        this.itemID = i;
        this.title = t;
        this.year = y;
        this.author = a;
        this.quantity = q;
        this.memberList = new String[100];
        this.numMember = 0;
    }

    // Add a member ID if space available
    public boolean addMember(String id) {
        if (numMember >= quantity) return false;

        memberList[numMember++] = id;
        return true;
    }

    // Remove a member ID from list
    public boolean removeMember(String id) {
        for (int i = 0; i < numMember; i++) {
            if (memberList[i].equals(id)) {
                memberList[i] = memberList[numMember - 1]; // replace with last
                memberList[numMember - 1] = null;
                numMember--;
                return true;
            }
        }
        return false;
    }

    // Check if a member has this item
    public boolean isMemberHere(String id) {
        for (int i = 0; i < numMember; i++) {
            if (memberList[i].equals(id)) return true;
        }
        return false;
    }

    // ✅ Define isCheckedOut: if any members currently hold it
    public boolean isCheckedOut() {
        return numMember > 0;
    }

    // ✅ To string for saving
    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder(
            itemID + "," + title + "," + year + "," + author + "," + quantity + "," + numMember
        );
        for (int i = 0; i < numMember; i++) {
            temp.append(",").append(memberList[i]);
        }
        return temp.toString();
    }

    // ✅ Getters
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

    public int getNumCheckedOut() {
        return numMember;
    }
}