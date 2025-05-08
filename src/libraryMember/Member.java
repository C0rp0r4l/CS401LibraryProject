package libraryMember;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Member implements Serializable {
	private static final long serialVersionUID = 1L;
    private String memberID;
    private String name;
    private int strikes;
    private boolean accountHold;
    private boolean accountBanned;
    private static final String MEMBERS_FILE = "members.txt";

    public Member(String memberID, String name, int strikes, boolean accountHold, boolean accountBanned) {
        this.memberID = memberID;
        this.name = name;
        this.strikes = strikes;
        this.accountHold = accountHold;
        this.accountBanned = accountBanned;
        checkBanStatus();
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

    // File operations
    public static List<Member> loadAllMembers() throws IOException {
        List<Member> members = new ArrayList<>();
        File file = new File(MEMBERS_FILE);
        
        if (!file.exists()) {
            file.createNewFile();
            return members;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    members.add(new Member(
                        parts[0].trim(),
                        parts[1].trim(),
                        Integer.parseInt(parts[2].trim()),
                        Boolean.parseBoolean(parts[3].trim()),
                        Boolean.parseBoolean(parts[4].trim())
                    ));
                }
            }
        }
        return members;
    }

    public static void saveAllMembers(List<Member> members) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member member : members) {
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