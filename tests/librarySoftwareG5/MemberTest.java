package librarySoftwareG5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class MemberTest {

    private static final String TEST_MEMBERS_FILE = "members.txt"; // Using the original filename
    private Path testFilePath = Paths.get(TEST_MEMBERS_FILE);

    private Member member;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up before each test to ensure a fresh state
        Files.deleteIfExists(testFilePath);
        member = new Member("MEM001", "John Doe", 0, false, false);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up after each test
        Files.deleteIfExists(testFilePath);
    }

    @Test
    void testMemberConstructorAndGetters() {
        assertEquals("MEM001", member.getMemberID());
        assertEquals("John Doe", member.getName());
        assertEquals(0, member.getStrikes());
        assertFalse(member.isAccountHold());
        assertFalse(member.isAccountBanned());
    }

    @Test
    void testSetName() {
        member.setName("Jane Doe");
        assertEquals("Jane Doe", member.getName());
    }

    @Test
    void testSetAccountHold() {
        member.setAccountHold(true);
        assertTrue(member.isAccountHold());
        member.setAccountHold(false);
        assertFalse(member.isAccountHold());
    }

    @Test
    void testAddStrike() {
        member.addStrike();
        assertEquals(1, member.getStrikes());
        assertFalse(member.isAccountBanned());

        member.addStrike();
        assertEquals(2, member.getStrikes());
        assertFalse(member.isAccountBanned());

        member.addStrike();
        assertEquals(3, member.getStrikes());
        assertTrue(member.isAccountBanned(), "Account should be banned after 3 strikes.");

        member.addStrike(); // Add another strike
        assertEquals(4, member.getStrikes());
        assertTrue(member.isAccountBanned(), "Account should remain banned with more than 3 strikes.");
    }

    @Test
    void testRemoveStrike() {
        member.addStrike(); // 1
        member.addStrike(); // 2
        member.addStrike(); // 3 (banned)
        assertTrue(member.isAccountBanned());

        member.removeStrike(); // 2
        assertEquals(2, member.getStrikes());
        assertFalse(member.isAccountBanned(), "Account should not be banned after removing a strike from 3.");

        member.removeStrike(); // 1
        assertEquals(1, member.getStrikes());
        assertFalse(member.isAccountBanned());

        member.removeStrike(); // 0
        assertEquals(0, member.getStrikes());
        assertFalse(member.isAccountBanned());

        member.removeStrike(); // Still 0
        assertEquals(0, member.getStrikes(), "Strikes should not go below 0.");
    }
    
    @Test
    void testCheckBanStatusOnCreation() {
        Member bannedMember = new Member("MEM002", "Bad User", 3, false, false);
        assertTrue(bannedMember.isAccountBanned(), "Member created with 3 strikes should be banned.");

        Member notBannedMember = new Member("MEM003", "Good User", 2, false, false);
        assertFalse(notBannedMember.isAccountBanned(), "Member created with 2 strikes should not be banned.");
    }

    @Test
    void testToString() {
        String expected = "MEM001,John Doe,0,false,false";
        assertEquals(expected, member.toString());

        member.addStrike();
        member.setAccountHold(true);
        expected = "MEM001,John Doe,1,true,false";
        assertEquals(expected, member.toString());
    }

    @Test
    void testSaveAndLoadAllMembers() throws IOException {
        // Create some members
        Member member1 = new Member("M001", "Alice Smith", 0, false, false);
        Member member2 = new Member("M002", "Bob Johnson", 2, true, false);
        Member member3 = new Member("M003", "Charlie Brown", 3, true, true); // Banned
        List<Member> originalMembers = new ArrayList<>(List.of(member1, member2, member3));

        // Save them
        Member.saveAllMembers(originalMembers);
        assertTrue(Files.exists(testFilePath), "Members file should be created.");

        // Load them
        List<Member> loadedMembers = Member.loadAllMembers();
        assertNotNull(loadedMembers);
        assertEquals(originalMembers.size(), loadedMembers.size(), "Number of loaded members should match original.");

        // Verify details (order might not be guaranteed by file read, so check content)
        for (Member original : originalMembers) {
            assertTrue(loadedMembers.stream().anyMatch(loaded ->
                original.getMemberID().equals(loaded.getMemberID()) &&
                original.getName().equals(loaded.getName()) &&
                original.getStrikes() == loaded.getStrikes() &&
                original.isAccountHold() == loaded.isAccountHold() &&
                original.isAccountBanned() == loaded.isAccountBanned()
            ), "Loaded members should contain member: " + original.getMemberID());
        }
    }

    @Test
    void testLoadAllMembers_EmptyFile() throws IOException {
        // Create an empty file
        Files.createFile(testFilePath);
        List<Member> loadedMembers = Member.loadAllMembers();
        assertNotNull(loadedMembers);
        assertTrue(loadedMembers.isEmpty(), "Loading from an empty file should result in an empty list.");
    }

    @Test
    void testLoadAllMembers_FileNotExists() throws IOException {
        // Ensure file does not exist (handled by @BeforeEach, but good to be explicit)
        Files.deleteIfExists(testFilePath);
        List<Member> loadedMembers = Member.loadAllMembers();
        assertNotNull(loadedMembers);
        assertTrue(loadedMembers.isEmpty(), "Loading when file doesn't exist should result in an empty list and create the file.");
        assertTrue(Files.exists(testFilePath), "Members file should be created if it didn't exist.");
    }
     @Test
    void testLoadAllMembers_MalformedLine() throws IOException {
        String goodLine = "M001,Good Member,0,false,false";
        String malformedLine = "M002,BadMemberData,1,true"; // Missing one field
        String anotherGoodLine = "M003,Another Good,2,true,false";

        List<String> lines = List.of(goodLine, malformedLine, anotherGoodLine);
        Files.write(testFilePath, lines);

        List<Member> loadedMembers = Member.loadAllMembers();
        assertEquals(2, loadedMembers.size(), "Should skip malformed lines and load valid ones.");
        assertTrue(loadedMembers.stream().anyMatch(m -> m.getMemberID().equals("M001")));
        assertTrue(loadedMembers.stream().anyMatch(m -> m.getMemberID().equals("M003")));
        assertFalse(loadedMembers.stream().anyMatch(m -> m.getMemberID().equals("M002"))); // The malformed one
    }
}
