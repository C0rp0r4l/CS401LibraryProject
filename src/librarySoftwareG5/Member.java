package librarySoftwareG5;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Member implements Serializable{

	//Follow Format:
	//MEM001,John Doe,0,false,false
	//MemberID,name,strikes,accountHold,accountBanned
	private static final long serialVersionUID = 1L;
	private String memberID; //Saved as parts[0], Loaded from parts[0]
    private String name; //Saved as parts[1], Loaded from parts[1]
    private int strikes; //Saved as parts[2], Loaded from parts[2]
    private boolean accountHold; //Saved as parts[3], Loaded from parts[3]
    private boolean accountBanned; //Saved as parts[4], Loaded from parts[4]
    private static final String MEMBERS_FILE = "members.txt";
    
    //member constructor
    public Member(String memberID, String name, int strikes, boolean accountHold, boolean accountBanned) {
        this.memberID = memberID;
        this.name = name;
        this.strikes = strikes;
        this.accountHold = accountHold;
        this.accountBanned = accountBanned; // Initial state, can be updated by checkBanStatus
        checkBanStatus(); // Ensures accountBanned is consistent with strikes on creation/load
    }

    // Getters and Setters
    public String getMemberID() { return memberID; }
    public String getName() { return name; }
    public int getStrikes() { return strikes; }
    public boolean isAccountHold() { return accountHold; }
    public boolean isAccountBanned() { return accountBanned; }

    public void setName(String name) { this.name = name; }
    public void setAccountHold(boolean hold) { this.accountHold = hold; }

    public void addStrike() {
        strikes++;
        checkBanStatus();
    }

    public void removeStrike() {
        if (strikes > 0) strikes--;
        checkBanStatus();
    }

    private void checkBanStatus() {
        accountBanned = strikes >= 3;
    }

    //Loads all members from the MEMBERS_FILE.
    public static List<Member> loadAllMembers() throws IOException {
        List<Member> members = new ArrayList<>();
        File file = new File(MEMBERS_FILE);
        
        if (!file.exists()) {
            file.createNewFile(); // Create file if it doesn't exist
            return members; // Return empty list
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines
                String[] parts = line.split(",");
                if (parts.length == 5) { // Expecting 5 parts for a Member
                    members.add(new Member(
                        parts[0].trim(), // memberID
                        parts[1].trim(), // name
                        Integer.parseInt(parts[2].trim()), // strikes
                        Boolean.parseBoolean(parts[3].trim()), // accountHold
                        Boolean.parseBoolean(parts[4].trim())  // accountBanned
                    ));
                } else {
                    System.err.println("Warning: Skipped malformed line in members.txt: " + line);
                }
            }
        }
        return members;
    }

    //Saves all members to the MEMBERS_FILE.
    public static void saveAllMembers(List<Member> members) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member member : members) {
                //Writing all 5 persistent attributes in order
                writer.write(String.format("%s,%s,%d,%b,%b\n",
                    member.memberID,
                    member.name,
                    member.strikes,
                    member.accountHold,
                    member.accountBanned
                ));
            }
        }
    }

   
    @Override
    public String toString() {
        return String.format("%s,%s,%d,%b,%b",
            memberID, name, strikes, accountHold, accountBanned);
    }
}
