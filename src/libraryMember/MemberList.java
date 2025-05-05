package libraryMember;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class MemberList {
	private Integer numMembers;
	private Member[] mArray;
	private String sourceName = "memberList";
	private boolean modified = false;
	
	
	public MemberList() {
		numMembers = 0;
		mArray = new Member[200];
	}
	
	public void setFilename(String filename) {
		sourceName = sourceName.concat(filename);
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
	
	//returns userID
	public String addMember(String name) {
		
		if(mArray.length == numMembers) {
			Member[] temp = new Member[numMembers*2];
			for(int i = 0; i < numMembers; i++) {
				temp[i] = mArray[i];
			}
			mArray = temp;
		}

		
		Member temp = new Member(name);
		mArray[numMembers] = temp;
		return temp.getUserID();
	}
	
	public void saveList() {
		if(modified == false) {
			return;
		}
		
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
			e.printStackTrace();
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
			System.out.println("Error opening file");
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
				Member temp = new Member(name, userID, strikes, accountHold);
				
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
