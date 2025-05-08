package librarySoftwareG5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class MemberListTest {

    private static final String TEST_MEMBERS_FILE = "members.txt";
    private static final String TEST_STAFF_FILE = "staffmembers.txt";
    private Path testMembersFilePath = Paths.get(TEST_MEMBERS_FILE);
    private Path testStaffFilePath = Paths.get(TEST_STAFF_FILE);

    private MemberList memberList;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up files before each test
        Files.deleteIfExists(testMembersFilePath);
        Files.deleteIfExists(testStaffFilePath);
        memberList = new MemberList(); // This calls loadAll()
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up files after each test
        Files.deleteIfExists(testMembersFilePath);
        Files.deleteIfExists(testStaffFilePath);
    }

    @Test
    void testInitialLoad_FilesDoNotExist() {
        assertTrue(memberList.getAllMembers().isEmpty(), "Initially, members list should be empty.");
        assertTrue(memberList.getAllStaffMembers().isEmpty(), "Initially, staff list should be empty.");
        assertTrue(Files.exists(testMembersFilePath), "Members file should be created.");
        assertTrue(Files.exists(testStaffFilePath), "Staff file should be created.");
    }

    @Test
    void testAddMember() {
        String memberId = memberList.addMember("Regular User");
        assertNotNull(memberId);
        assertFalse(memberId.isEmpty());

        Member retrievedMember = memberList.getMember(memberId);
        assertNotNull(retrievedMember);
        assertEquals("Regular User", retrievedMember.getName());
        assertEquals(0, retrievedMember.getStrikes());
        assertFalse(retrievedMember.isAccountHold());
        assertEquals(1, memberList.getAllMembers().size());

        // Check if file was saved (by trying to load into a new instance)
        MemberList newMemberList = new MemberList();
        assertNotNull(newMemberList.getMember(memberId));
    }

    @Test
    void testRemoveMember() {
        String memberId = memberList.addMember("User ToDelete");
        assertTrue(memberList.removeMember(memberId), "Should remove existing member.");
        assertNull(memberList.getMember(memberId));
        assertEquals(0, memberList.getAllMembers().size());

        assertFalse(memberList.removeMember("NONEXISTENT_ID"), "Should return false for non-existent ID.");
    }

    @Test
    void testGetMember() {
        String memberId = memberList.addMember("Specific User");
        Member member = memberList.getMember(memberId);
        assertNotNull(member);
        assertEquals("Specific User", member.getName());
        assertNull(memberList.getMember("NONEXISTENT_ID"));
    }

    @Test
    void testAddStaffMember() {
        String staffId = memberList.addStaffMember("Staff Person", "Main Desk");
        assertNotNull(staffId);
        assertFalse(staffId.isEmpty());

        StaffMember retrievedStaff = memberList.getStaffMember(staffId);
        assertNotNull(retrievedStaff);
        assertEquals("Staff Person", retrievedStaff.getName());
        assertEquals("Main Desk", retrievedStaff.getLocation());
        assertNotNull(retrievedStaff.getPassword()); // Password should be generated
        assertEquals(1, memberList.getAllStaffMembers().size());

        // Check save
        MemberList newMemberList = new MemberList();
        assertNotNull(newMemberList.getStaffMember(staffId));
    }

    @Test
    void testRemoveStaffMember() {
        String staffId = memberList.addStaffMember("Staff ToDelete", "Back Office");
        assertTrue(memberList.removeStaffMember(staffId), "Should remove existing staff member.");
        assertNull(memberList.getStaffMember(staffId));
        assertEquals(0, memberList.getAllStaffMembers().size());

        assertFalse(memberList.removeStaffMember("NONEXISTENT_ID"));
    }

    @Test
    void testGetStaffMember() {
        String staffId = memberList.addStaffMember("Specific Staff", "Archives");
        StaffMember staff = memberList.getStaffMember(staffId);
        assertNotNull(staff);
        assertEquals("Specific Staff", staff.getName());
        assertNull(memberList.getStaffMember("NONEXISTENT_ID"));
    }
    
    @Test
    void testSearchAllMembers_Found() {
        String memberId = memberList.addMember("Searchable Member");
        String staffId = memberList.addStaffMember("Searchable Staff", "Desk");

        assertTrue(memberList.searchAllMembers(memberId), "Should find existing regular member.");
        assertTrue(memberList.searchAllMembers(staffId), "Should find existing staff member.");
    }

    @Test
    void testSearchAllMembers_NotFound() {
        assertFalse(memberList.searchAllMembers("NON_EXISTENT_ID"), "Should not find non-existent ID.");
    }


    @Test
    void testSearchByName() {
        memberList.addMember("Alice Wonderland");
        memberList.addStaffMember("Bob The Builder", "Construction");
        memberList.addMember("Alicia Keys"); // Another Alice

        List<Member> resultsAlice = memberList.searchByName("Alice");
        assertEquals(2, resultsAlice.size(), "Should find two members with 'Alice'.");
        assertTrue(resultsAlice.stream().anyMatch(m -> m.getName().equals("Alice Wonderland")));
        assertTrue(resultsAlice.stream().anyMatch(m -> m.getName().equals("Alicia Keys")));


        List<Member> resultsBob = memberList.searchByName("Bob");
        assertEquals(1, resultsBob.size());
        assertEquals("Bob The Builder", resultsBob.get(0).getName());
        assertTrue(resultsBob.get(0) instanceof StaffMember);

        List<Member> resultsNonExistent = memberList.searchByName("Charlie");
        assertTrue(resultsNonExistent.isEmpty());
        
        List<Member> caseInsensitiveResults = memberList.searchByName("alice");
        assertEquals(2, caseInsensitiveResults.size(), "Search by name should be case-insensitive.");

    }

    @Test
    void testGetAllMembers() {
        memberList.addMember("M1");
        memberList.addMember("M2");
        assertEquals(2, memberList.getAllMembers().size());
    }

    @Test
    void testGetAllStaffMembers() {
        memberList.addStaffMember("S1", "L1");
        memberList.addStaffMember("S2", "L2");
        assertEquals(2, memberList.getAllStaffMembers().size());
    }
    
    @Test
    void testGenerateMemberIdUniqueness() {
        String id1 = memberList.addMember("User One");
        String id2 = memberList.addMember("User Two");
        assertNotEquals(id1, id2, "Generated member IDs should be unique.");
    }

    @Test
    void testGenerateStaffIdUniqueness() {
        String id1 = memberList.addStaffMember("Staff One", "Loc A");
        String id2 = memberList.addStaffMember("Staff Two", "Loc B");
        assertNotEquals(id1, id2, "Generated staff IDs should be unique.");
    }

    @Test
    void testSaveAll_CalledByAddOperations() throws IOException {
        // Add a member and a staff, which should trigger saveAll()
        String memberId = memberList.addMember("Persistent Member");
        String staffId = memberList.addStaffMember("Persistent Staff", "Main");

        // Create new MemberList instance, which will load from files
        MemberList newMemberList = new MemberList();
        assertNotNull(newMemberList.getMember(memberId), "Member should be persisted and loaded.");
        assertNotNull(newMemberList.getStaffMember(staffId), "Staff member should be persisted and loaded.");
    }
}
