package libraryMember;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//implements a facade design pattern
//allow us to access the features of Member and StaffMember
public class MemberList {
	private List<Member> members;
    private List<StaffMember> staffMembers;
    private static final String MEMBERS_FILE = "members.txt";
    private static final String STAFF_FILE = "staffmembers.txt";

    public MemberList() {
        this.members = new ArrayList<>();
        this.staffMembers = new ArrayList<>();
        loadAll();
    }

    //Loading and saving-----------------------------------------------
    public void loadAll() {
        try {
            this.members = Member.loadAllMembers();
            this.staffMembers = StaffMember.loadAllStaff();
        } catch (IOException e) {
            System.err.println("Error loading members: " + e.getMessage());
        }
    }

    public void saveAll() {
        try {
            Member.saveAllMembers(members);
            StaffMember.saveAllStaff(staffMembers);
        } catch (IOException e) {
            System.err.println("Error saving members: " + e.getMessage());
        }
    }

    //Member Management-----------------------------------------------
    public String addMember(String name) {
        String newId = generateMemberId();
        Member newMember = new Member(newId, name, 0, false, false);
        members.add(newMember);
        saveAll();
        return newId;
    }

    public boolean removeMember(String memberId) {
        boolean removed = members.removeIf(m -> m.getMemberID().equals(memberId));
        if (removed) saveAll();
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
        StaffMember newStaff = new StaffMember(
            newId, name, 0, false, false, 
            location, StaffMember.generatePassword()
        );
        staffMembers.add(newStaff);
        saveAll();
        return newId;
    }

    public boolean removeStaffMember(String staffId) {
        boolean removed = staffMembers.removeIf(s -> s.getMemberID().equals(staffId));
        if (removed) saveAll();
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
        results.addAll(members.stream()
            .filter(m -> m.getName().toLowerCase().contains(name.toLowerCase()))
            .collect(Collectors.toList()));
        results.addAll(staffMembers.stream()
            .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
            .collect(Collectors.toList()));
        return results;
    }

    //ID Creation----------------------------------------------------
    private String generateMemberId() {
        return "MEM" + (members.size() + 1000);
    }

    private String generateStaffId() {
        return "STAFF" + (staffMembers.size() + 100);
    }

    //Getters-----------------------------------------------------------
    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public List<StaffMember> getAllStaffMembers() {
        return new ArrayList<>(staffMembers);
    }
    
    //ID getters
    public Member getMemberByID(String id) {
        for (Member m : members) {
            if (m.getMemberID().equals(id)) {
                return m;
            }
        }
        for (StaffMember s : staffMembers) {
            if (s.getMemberID().equals(id)) {
                return s;
            }
        }
        return null;
    }
    
    //Authentication
    public Member authenticate(String id, String password) {
        for (StaffMember s : staffMembers) {
            if (s.getMemberID().equals(id) && s.getPassword().equals(password)) {
                return s;
            }
        }
        return null;
    }
}