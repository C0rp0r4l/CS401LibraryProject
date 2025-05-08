package librarySoftwareG5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//implements a facade design pattern
//allow us to access the features of Member and StaffMember
public class MemberList {
	private List<Member> members; // In-memory list of regular members
    private List<StaffMember> staffMembers; // In-memory list of staff members
    // File names are defined in Member and StaffMember classes respectively
    // private static final String MEMBERS_FILE = "members.txt"; // Not needed here
    // private static final String STAFF_FILE = "staffmembers.txt"; // Not needed here

    public MemberList() {
        this.members = new ArrayList<>();
        this.staffMembers = new ArrayList<>();
        loadAll(); // Load data from files when MemberList is instantiated
    }

    //Loading and saving-----------------------------------------------
    private void loadAll() {
        try {
            // Delegate loading to the Member class
            this.members = Member.loadAllMembers();
            // Delegate loading to the StaffMember class
            this.staffMembers = StaffMember.loadAllStaff();
        } catch (IOException e) {
            // Handle potential IOExceptions during loading
            System.err.println("Error loading members or staff from file: " + e.getMessage());
            // Consider if more robust error handling is needed, e.g., exiting or using empty lists
        }
    }

    public void saveAll() {
        try {
            // Delegate saving members to the Member class
            Member.saveAllMembers(members);
            // Delegate saving staff members to the StaffMember class
            StaffMember.saveAllStaff(staffMembers);
        } catch (IOException e) {
            // Handle potential IOExceptions during saving
            System.err.println("Error saving members or staff to file: " + e.getMessage());
        }
    }

    //Member Management-----------------------------------------------
    public String addMember(String name) {
        String newId = generateMemberId();
        // Create new Member with default values for strikes and account status
        Member newMember = new Member(newId, name, 0, false, false);
        members.add(newMember);
        saveAll(); // Persist changes
        return newId;
    }

    public boolean removeMember(String memberId) {
        boolean removed = members.removeIf(m -> m.getMemberID().equals(memberId));
        if (removed) saveAll(); // Persist changes
        return removed;
    }

    public Member getMember(String memberId) {
        return members.stream()
            .filter(m -> m.getMemberID().equals(memberId))
            .findFirst()
            .orElse(null);
    }

    //Staff Member Management-----------------------------------------
    public String addStaffMember(String name, String location) {
        String newId = generateStaffId();
        String password = StaffMember.generatePassword(); // Generate a password for the new staff
        // Create new StaffMember with default values for strikes and account status
        StaffMember newStaff = new StaffMember(
            newId, name, 0, false, false, 
            location, password // Staff-specific attributes
        );
        staffMembers.add(newStaff);
        saveAll(); // Persist changes
        return newId;
    }

    public boolean removeStaffMember(String staffId) {
        boolean removed = staffMembers.removeIf(s -> s.getMemberID().equals(staffId));
        if (removed) saveAll(); // Persist changes
        return removed;
    }

    public StaffMember getStaffMember(String staffId) {
        return staffMembers.stream()
            .filter(s -> s.getMemberID().equals(staffId))
            .findFirst()
            .orElse(null);
    }

    //Search Operation-------------------------------------------------
    public boolean searchAllMembers(String id) {
        return getMember(id) != null || getStaffMember(id) != null;
    }

    public List<Member> searchByName(String name) {
        List<Member> results = new ArrayList<>();
        String lowerCaseName = name.toLowerCase(); // For case-insensitive search

        results.addAll(members.stream()
            .filter(m -> m.getName().toLowerCase().contains(lowerCaseName))
            .collect(Collectors.toList()));
        results.addAll(staffMembers.stream()
            .filter(s -> s.getName().toLowerCase().contains(lowerCaseName)) // StaffMembers are also Members
            .collect(Collectors.toList()));
        return results;
    }

    //ID Creation----------------------------------------------------
    private String generateMemberId() {
        // Simple ID generation, ensure uniqueness if scaling
        return "MEM" + (members.size() + staffMembers.size() + 1000); // Make it more unique across both lists
    }

    private String generateStaffId() {
        // Simple ID generation
        return "STAFF" + (staffMembers.size() + members.size() + 100); // Make it more unique
    }

    //Getters-----------------------------------------------------------
    public List<Member> getAllMembers() {
        return new ArrayList<>(members); // Return a copy to prevent external modification
    }

    public List<StaffMember> getAllStaffMembers() {
        return new ArrayList<>(staffMembers); // Return a copy
    }
}
