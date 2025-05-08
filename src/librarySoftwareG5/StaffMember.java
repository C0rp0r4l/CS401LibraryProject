package librarySoftwareG5;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StaffMember extends Member {
    
	private static final long serialVersionUID = 1L;
	private String password; // Persistent: Saved as parts[6], Loaded from parts[6]
    private String location; // Persistent: Saved as parts[5], Loaded from parts[5]
    private static final String STAFF_FILE = "staffmembers.txt";

   
    public StaffMember(String memberID, String name, int strikes, 
                      boolean accountHold, boolean accountBanned,
                      String location, String password) {
        // Calls super constructor to initialize inherited persistent attributes
        super(memberID, name, strikes, accountHold, accountBanned);
        this.location = location;
        this.password = password;
    }

    // Additional getters and setters for StaffMember specific persistent attributes
    public String getPassword() { return password; }
    public String getLocation() { return location; }

    public void setPassword(String password) { this.password = password; }
    public void setLocation(String location) { this.location = location; }

    // Generate random password (utility, not directly part of load/save of an existing password)
    public static String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

   //load staff from file 
    public static List<StaffMember> loadAllStaff() throws IOException {
        List<StaffMember> staff = new ArrayList<>();
        File file = new File(STAFF_FILE);
        
        if (!file.exists()) {
            file.createNewFile(); // Create file if it doesn't exist
            return staff; // Return empty list
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines
                String[] parts = line.split(",");
                if (parts.length == 7) { // Expecting 7 parts for a StaffMember
                    staff.add(new StaffMember(
                        parts[0].trim(), // memberID (inherited)
                        parts[1].trim(), // name (inherited)
                        Integer.parseInt(parts[2].trim()), // strikes (inherited)
                        Boolean.parseBoolean(parts[3].trim()), // accountHold (inherited)
                        Boolean.parseBoolean(parts[4].trim()), // accountBanned (inherited)
                        parts[5].trim(), // location (StaffMember specific)
                        parts[6].trim()  // password (StaffMember specific)
                    ));
                } else {
                     System.err.println("Warning: Skipped malformed line in staffmembers.txt: " + line);
                }
            }
        }
        return staff;
    }

    //save staff back to file
    public static void saveAllStaff(List<StaffMember> staff) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STAFF_FILE))) {
            for (StaffMember member : staff) {
                // Writing all 7 persistent attributes in order
                writer.write(String.format("%s,%s,%d,%b,%b,%s,%s\n",
                    member.getMemberID(),  
                    member.getName(),       
                    member.getStrikes(),   
                    member.isAccountHold(), 
                    member.isAccountBanned(),
                    member.location,        //StaffMember specific
                    member.password         //StaffMember specific
                ));
            }
        }
    }

    //return staffmember  info as string 
    @Override
    public String toString() {
        // Uses getters for inherited fields to ensure consistency if overridden
        return String.format("%s,%s,%d,%b,%b,%s,%s",
            getMemberID(),
            getName(),
            getStrikes(),
            isAccountHold(),
            isAccountBanned(),
            location,
            password);
    }

    
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}
