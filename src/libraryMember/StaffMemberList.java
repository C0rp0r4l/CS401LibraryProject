package libraryMember;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class StaffMemberList {
    private Integer numMembers;
	private StaffMember[] mArray;
	private String sourceName = "memberList";
	private boolean modified = false;
	
	
	public StaffMemberList() {
		numMembers = 0;
		mArray = new StaffMember[200];
	}
	
	public void setFilename(String filename) {
		sourceName = filename;
	}
	
	public String toString() {
		String list = "";
		
		for(int i = 0; i < numMembers; i++) {
			String temp = mArray[i].getName() + "," + mArray[i].getUserID()
					+ "," + mArray[i].getStrikes() + "," + mArray[i].getAccountHold() + "\n";
			list = list.concat(temp);
		}
		return list;
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

	public Integer getNumMembers() {
		return numMembers;
	}

	public String getStrikes(String id) {
		for(int i = 0; i < numMembers; i++) {
			if(mArray[i].getUserID().compareTo(id) == 0) {
				return mArray[i].getStrikes();
			}
		}
		return "User not found";
	}

	public String getAccountHold(String id) {
		for(int i = 0; i < numMembers; i++) {
			if(mArray[i].getUserID().compareTo(id) == 0) {
				return mArray[i].getAccountHold();
			}
		}
		return "User not found";
	}
	
	public void removeMember(String id) {
		
		for(int i = 0; i < numMembers; i++) {
			if(mArray[i].getUserID().compareTo(id) == 0) {
				mArray[i] = mArray[numMembers];
				numMembers--;
			}
		}
	}
	
	public void addMember(String name) {
		
		if(mArray.length == numMembers) {
			StaffMember[] temp = new StaffMember[numMembers*2];
			for(int i = 0; i < numMembers; i++) {
				temp[i] = mArray[i];
			}
			mArray = temp;
		}
		
		StaffMember temp = new StaffMember(name);
		mArray[numMembers] = temp;
	}
	
	public void saveList() {
		if(modified == false) {
			return;
		}

	    System.out.println("Saving List");
		
		try {
			FileWriter myWriter = new FileWriter(sourceName);
			
			// Save format can be changed according to the implementation of toString()
			// with a newline to make text files readable for devs
			for(int i = 0; i < numMembers; i++) {
				myWriter.write(mArray[i].toString() + "\n");
			}
		
			modified = false;
			myWriter.close();
		} 
		catch (Exception e) {
			System.out.println("Error saving member list: " + e.getMessage());
		}
	}
	
	public void loadList() {
		File file = new File(sourceName);
		try {
			if(file.createNewFile()) {
				return;
			}
		}
		catch (Exception e) {
			System.out.println("Error creating file: " + e.getMessage());
		}
		
		try {
			Scanner scanner = new Scanner(file);
			scanner.useDelimiter(",|\n");
			
			//name + "," + userID + "," + String.valueOf(strikes) + "," + String.valueOf(accountHold)
			while(scanner.hasNext()) {
				String name = scanner.next();
				String userID = scanner.next();
				String strikes = scanner.next();
				String accountHold = scanner.next();
				StaffMember temp = new StaffMember(name, userID, strikes, accountHold);
				
				mArray[numMembers] = temp;
				numMembers++;
			}
			scanner.close();
			modified = false;
		}
		catch (Exception e) {
			System.out.println("Error reading file");
		}
	}
}