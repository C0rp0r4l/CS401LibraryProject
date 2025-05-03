package scmot;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class MemberList {
	private int numMembers;
	private Member[] mArray;
	private String sourceName = "memberList.txt";
	private boolean modified = false;
	
	
	public MemberList() {
		numMembers = 0;
		mArray = new Member[200];
	}
	
	public void setFilename(String filename) {
		sourceName = filename;
	}
	
	public String toString() {
		String list = "";
		
		for(int i = 0; i < numMembers; i++) {
			String temp = mArray[i].getName() + " " + mArray[i].getUserID()
		                  + mArray[i].getStrikes() + " " + mArray[i].getAccountHold() + "\n";
			list = list.concat(temp);
		}
		
		return list;
	}
	
	public Object attemptLogin(String u, String p) {
		System.out.println(u + " " + p);
		Member m = searchMember(u);
		if(m != null) {
			if(m.getPassword().equals(p)) {
				return m;
			}
			return null;
		}
		
		return 1;
	}
	
	public void addMember(Object obj) {
	    Member newMember = null;

	    // Determine the input type
	    if (obj instanceof String) {
	        newMember = new Member((String) obj);
	    } else if (obj instanceof Member) {
	        newMember = (Member) obj;
	    } else {
	        System.out.println("Invalid type passed to addMember.");
	        return;
	    }

	    // Resize array if needed
	    if (numMembers == mArray.length) {
	        Member[] temp = new Member[numMembers * 2];
	        for (int i = 0; i < numMembers; i++) {
	            temp[i] = mArray[i];
	        }
	        mArray = temp;
	    }

	    // Find insert position based on first name
	    int insertPos = 0;
	    while (insertPos < numMembers && mArray[insertPos].getUserID().compareToIgnoreCase(newMember.getName()) < 0) {
	        insertPos++;
	    }

	    // Shift elements to the right to make room
	    for (int i = numMembers; i > insertPos; i--) {
	        mArray[i] = mArray[i - 1];
	    }

	    // Insert new member
	    mArray[insertPos] = newMember;
	    numMembers++;
	    
	    setModified(true);
	    saveList();
	}
	
	public int getNumMembers() {
		return numMembers;
	}
	
	public Member searchMember(String memberId) {
	    int low = 0;
	    int high = numMembers - 1;

	    while (low <= high) {
	        int mid = (low + high) / 2;
	        int cmp = mArray[mid].getUserID().compareTo(memberId);

	        if (cmp == 0) {
	            return mArray[mid]; // Match found
	        } else if (cmp < 0) {
	            low = mid + 1; // Search right half
	        } else {
	            high = mid - 1; // Search left half
	        }
	    }

	    return null; // Not found
	}

	
	public Member getIndex(int i) {
		return mArray[i];
	}
	
	public void saveList() {
	    if (!modified) {
	        return;
	    }
	    
	    System.out.println("Saving List");

	    File file = new File(sourceName);

	    try {
	        // Ensure the file exists
	        if (!file.exists()) {
	        	System.out.println("file exists");
	            file.createNewFile();  // Create the file if it doesn't exist
	        }

	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
	            for (int i = 0; i < numMembers; i++) {
	                String line = mArray[i].toString();
	                writer.write(line);
	                writer.newLine();  // Adds a newline after each entry
	            }
	            modified = false;
	        }
	    } catch (IOException e) {
	        System.out.println("Error saving member list: " + e.getMessage());
	    }
	}

	
	public void setModified(boolean m) {
		modified = m;
	}
	
	// INCOMPLETE ! Not sure how to format our save/load for items as well
	public void loadList() {
	    File file = new File(sourceName);
	    
	    try {
	        if (!file.exists()) {
	            file.createNewFile();
	            // No data to load, but file now exists
	            return;
	        }
	    } catch (IOException e) {
	        System.out.println("Error creating file: " + e.getMessage());
	        return;
	    }

	    try (Scanner scanner = new Scanner(file)) {
	        scanner.useDelimiter(",|\n");

	        while (scanner.hasNext()) {
	            String name = scanner.next();
	            String userID = scanner.next();
	            String userPassword = scanner.next();
	            String strikes = scanner.next();
	            String accountHold = scanner.next();

	            Member temp = new Member(name, userID, userPassword, strikes, accountHold);
	            mArray[numMembers++] = temp;
	        }

	        modified = false;
	    } catch (Exception e) {
	        System.out.println("Error reading file: " + e.getMessage());
	    }
	}

}
