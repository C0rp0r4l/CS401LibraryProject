package scmot;

import java.io.Serializable;
import java.util.Random;

public class Item implements Serializable {
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
        
        String temp = t.substring(0, 2) + y.substring(0,4) + a.substring(0,2);
        Random rand = new Random();
        int randomDigits = rand.nextInt(30000) + 10000;
        itemID = temp + randomDigits + count;
        count++;
    }
    
    public Item(String i, String t, String y, String a, Integer q) {
        title = t;
        year = y;
        author = a;
        quantity = q;
        memberList = new String[100];
    }
    
    public boolean isCheckedOut() {
    	if(numMember >= quantity) {
    		return true;
    	}
    	return false;
    }
    
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
    
    public Boolean isMemberHere(String id) {
        for(int i = 0; i < numMember; i++) { 
            if(memberList[i].compareTo(id) == 0) {
                return true;
            }
        }
        return false;
    }

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