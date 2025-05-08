package libraryMember;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.Serializable;

public class StaffMember extends Member implements Serializable {
	private static final long serialVersionUID = 1L;
    private String password;
    private String location;
    private static final String STAFF_FILE = "staffmembers.txt";

    public StaffMember(String memberID, String name, int strikes, 
                      boolean accountHold, boolean accountBanned,
                      String location, String password) {
        super(memberID, name, strikes, accountHold, accountBanned);
        this.location = location;
        this.password = password;
    }

    // Additional getters and setters
    public String getPassword() { return password; }
    public String getLocation() { return location; }

    public void setPassword(String password) { this.password = password; }
    public void setLocation(String location) { this.location = location; }

    // Generate random password
    public static String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // File operations
    public static List<StaffMember> loadAllStaff() throws IOException {
        List<StaffMember> staff = new ArrayList<>();
        File file = new File(STAFF_FILE);
        
        if (!file.exists()) {
            file.createNewFile();
            return staff;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    staff.add(new StaffMember(
                        parts[0].trim(),
                        parts[1].trim(),
                        Integer.parseInt(parts[2].trim()),
                        Boolean.parseBoolean(parts[3].trim()),
                        Boolean.parseBoolean(parts[4].trim()),
                        parts[5].trim(),
                        parts[6].trim()
                    ));
                }
            }
        }
        return staff;
    }

    public static void saveAllStaff(List<StaffMember> staff) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STAFF_FILE))) {
            for (StaffMember member : staff) {
                writer.write(String.format("%s,%s,%d,%b,%b,%s,%s\n",
                    member.getMemberID(),
                    member.getName(),
                    member.getStrikes(),
                    member.isAccountHold(),
                    member.isAccountBanned(),
                    member.location,
                    member.password
                ));
            }
        }
    }

    @Override
    public String toString() {
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