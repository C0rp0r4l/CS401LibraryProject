package scmot;
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
	
	public Object attemptLogin(String u) {
		System.out.println(u);
		Member m = searchMember(u);
		if(m != null) {
				return m;
		}
		return null;
	}
	
	public Member getIndex(int i) {
		return mArray[i];
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
	
	public void setModified(boolean m) {
		modified = m;
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
	
	 public Boolean editMember(Member m) {
	        for(int i = 0; i < numMembers; i++) {
	            if(mArray[i].getUserID().compareTo(m.getUserID()) == 0) {
	                removeMember(mArray[i].getUserID());
	                Member temp = new Member(m.getName(), m.getUserID(), m.getStrikes(), String.valueOf(m.getAccountHold()));
	                mArray[i] = temp;
	    	        saveList();
	                return true;
	            }
	        }
	        saveList();
	        return false;
	    }

	public Boolean getAccountHold(String id) {
		for(int i = 0; i < numMembers; i++) {
			if(mArray[i].getUserID().compareTo(id) == 0) {
				return mArray[i].getAccountHold();
			}
		}
		return null;
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
			Member[] temp = new Member[numMembers*2];
			for(int i = 0; i < numMembers; i++) {
				temp[i] = mArray[i];
			}
			mArray = temp;
		}
		
		Member temp = new Member(name);
		mArray[numMembers] = temp;
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