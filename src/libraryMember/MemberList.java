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
	
	// INCOMPLETE ! Not sure how to format our save/load 
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
		
			myWriter.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// INCOMPLETE ! Not sure how to format our save/load for items as well
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
