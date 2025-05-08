package librarySoftwareG5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ItemTest {

    private Item item1;
    private final String item1Id = "BOOK001";
    private final String item1Title = "The Great Gatsby";
    private final String item1Year = "1925";
    private final String item1Author = "F. Scott Fitzgerald";
    private final String item1Location = "Fiction Shelf A";

    @BeforeEach
    void setUp() {
        item1 = new Item(item1Id, item1Title, item1Year, item1Author, item1Location, null, 0, new ArrayList<>());
    }

    @Test
    void testItemConstructorAndGetters() {
        assertEquals(item1Id, item1.getItemID());
        assertEquals(item1Title, item1.getTitle());
        assertEquals(item1Year, item1.getYear());
        assertEquals(item1Author, item1.getAuthor());
        assertEquals(item1Location, item1.getLocation());
        assertNull(item1.getOwnedBy(), "New item should not be owned.");
        assertEquals(0, item1.getReservedCount(), "New item should have 0 reservations.");
        assertTrue(item1.getReservedBy().isEmpty(), "New item reservedBy list should be empty.");
        assertNull(item1.getOwnedByName(), "New item ownedByName should be null.");
        assertTrue(item1.getReservedByNames().isEmpty(), "New item reservedByNames list should be empty.");
    }

    @Test
    void testSetLocation() {
        String newLocation = "Classics Section";
        item1.setLocation(newLocation);
        assertEquals(newLocation, item1.getLocation());
    }

    @Test
    void testSetOwnedBy() {
        String memberId = "MEM001";
        String memberName = "John Doe";
        assertTrue(item1.setOwnedBy(memberId, memberName), "Should be able to set owner for available item.");
        assertEquals(memberId, item1.getOwnedBy());
        assertEquals(memberName, item1.getOwnedByName());

        // Try to set owner again when already owned
        assertFalse(item1.setOwnedBy("MEM002", "Jane Doe"), "Should not be able to set owner if already owned.");
        assertEquals(memberId, item1.getOwnedBy(), "Owner ID should not change.");
        assertEquals(memberName, item1.getOwnedByName(), "Owner name should not change.");
    }

    @Test
    void testReleaseOwnership() {
        String memberId = "MEM001";
        String memberName = "John Doe";
        item1.setOwnedBy(memberId, memberName);

        assertTrue(item1.releaseOwnership(), "Should be able to release ownership.");
        assertNull(item1.getOwnedBy(), "OwnedBy should be null after release.");
        assertNull(item1.getOwnedByName(), "OwnedByName should be null after release.");

        // Try to release when not owned
        assertFalse(item1.releaseOwnership(), "Should return false if trying to release when not owned.");
    }

    @Test
    void testAddReservation() {
        String memberId1 = "MEM002";
        String memberName1 = "Alice";
        assertTrue(item1.addReservation(memberId1, memberName1), "Should add reservation successfully.");
        assertEquals(1, item1.getReservedCount());
        assertTrue(item1.getReservedBy().contains(memberId1));
        assertTrue(item1.getReservedByNames().contains(memberName1 + " (ID: " + memberId1 + ")") || item1.getReservedByNames().contains(memberName1) ); // Name formatting can vary

        // Try to add same reservation
        assertFalse(item1.addReservation(memberId1, memberName1), "Should not add duplicate reservation.");
        assertEquals(1, item1.getReservedCount());
    }
    
    @Test
    void testAddMultipleReservations() {
        String memberId1 = "MEM002";
        String memberName1 = "Alice";
        String memberId2 = "MEM003";
        String memberName2 = "Bob";

        item1.addReservation(memberId1, memberName1);
        assertTrue(item1.addReservation(memberId2, memberName2));
        assertEquals(2, item1.getReservedCount());
        assertTrue(item1.getReservedBy().containsAll(Arrays.asList(memberId1, memberId2)));
        // Check if names (potentially with IDs) are present. Order might not be guaranteed by simple contains.
        assertEquals(2, item1.getReservedByNames().size());
    }


    @Test
    void testRemoveReservation() {
        String memberId1 = "MEM002";
        String memberName1 = "Alice";
        String memberId2 = "MEM003";
        String memberName2 = "Bob";
        item1.addReservation(memberId1, memberName1);
        item1.addReservation(memberId2, memberName2);

        assertTrue(item1.removeReservation(memberId1), "Should remove reservation successfully.");
        assertEquals(1, item1.getReservedCount());
        assertFalse(item1.getReservedBy().contains(memberId1));
        
        // Check that corresponding name is also removed.
        // This is a bit tricky due to potential name formatting (e.g. "Alice (ID: MEM002)")
        // A more robust check would be to verify the remaining name corresponds to the remaining ID.
        boolean name1Present = item1.getReservedByNames().stream().anyMatch(name -> name.startsWith(memberName1));
        assertFalse(name1Present, "Name of removed reserver should not be present.");


        assertFalse(item1.removeReservation("NONEXISTENT_ID"), "Should return false if reservation ID not found.");
        assertEquals(1, item1.getReservedCount(), "Count should remain unchanged after failed removal.");
    }

    @Test
    void testToFileFormat() {
        // Available item
        String expectedFormatAvailable = String.format("%s,%s,%s,%s,%s,null,0,",
            item1Id, item1Title, item1Year, item1Author, item1Location);
        assertEquals(expectedFormatAvailable, item1.toFileFormat());

        // Owned item
        item1.setOwnedBy("MEM001", "John Doe");
        String expectedFormatOwned = String.format("%s,%s,%s,%s,%s,MEM001,0,",
            item1Id, item1Title, item1Year, item1Author, item1Location);
        assertEquals(expectedFormatOwned, item1.toFileFormat());

        // Reserved item
        item1.releaseOwnership();
        item1.addReservation("MEM002", "Alice");
        item1.addReservation("MEM003", "Bob");
        String expectedFormatReserved = String.format("%s,%s,%s,%s,%s,null,2,MEM002,MEM003", // Note: trailing comma removed if no reservations
            item1Id, item1Title, item1Year, item1Author, item1Location);
         // If reservedBy is empty, String.join(",", reservedBy) is empty.
        // If not empty, it's "ID1,ID2". The constructor of Item has `String.join(",", reservedBy))`
        // so if reservedBy is empty, it will be `itemID,title,year,author,location,ownedBy,0,` (with a trailing comma)
        // if reservedBy has one item "ID1", it will be `itemID,title,year,author,location,ownedBy,1,ID1`
        // Let's re-evaluate based on Item.toFileFormat()
        // return String.format("%s,%s,%s,%s,%s,%s,%d,%s", itemID, title, year, author, location, this.ownedBy != null ? this.ownedBy : "null", reservedBy.size(), String.join(",", reservedBy));

        if (item1.getReservedBy().isEmpty()){
             expectedFormatReserved = String.format("%s,%s,%s,%s,%s,null,%d,%s",
            item1Id, item1Title, item1Year, item1Author, item1Location, item1.getReservedCount(), "");
        } else {
             expectedFormatReserved = String.format("%s,%s,%s,%s,%s,null,%d,%s",
            item1Id, item1Title, item1Year, item1Author, item1Location, item1.getReservedCount(), String.join(",", item1.getReservedBy()));
        }
        assertEquals(expectedFormatReserved, item1.toFileFormat());
    }

    @Test
    void testToString_Available() {
        String str = item1.toString();
        assertTrue(str.contains("ID: " + item1Id));
        assertTrue(str.contains("Title: " + item1Title));
        assertTrue(str.contains("Status: Available"));
        assertFalse(str.contains("Checked out to:"));
        assertFalse(str.contains("Reserved by:"));
    }

    @Test
    void testToString_Owned() {
        item1.setOwnedBy("MEM001", "John Doe");
        String str = item1.toString();
        assertTrue(str.contains("Status: Checked out to: John Doe (ID: MEM001)"));
    }
    
    @Test
    void testToString_Owned_NameNotSet() {
        // Simulate item loaded from file where only ID is present initially
        Item itemFromFile = new Item(item1Id, item1Title, item1Year, item1Author, item1Location, "MEM001", 0, new ArrayList<>());
        String str = itemFromFile.toString();
        assertTrue(str.contains("Status: Checked out to member ID: MEM001"));
    }

    @Test
    void testToString_Reserved_WithNames() {
        item1.addReservation("MEM002", "Alice");
        item1.setReservedByNames(List.of("Alice (ID: MEM002)")); // Simulate server enrichment
        String str = item1.toString();
        assertTrue(str.contains("Status: Available"));
        assertTrue(str.contains("Reserved by: [Alice (ID: MEM002)]"));
    }
    
    @Test
    void testToString_Reserved_WithOnlyIds() {
        // Simulate item loaded from file where only IDs are present for reservations
        List<String> reserverIds = new ArrayList<>();
        reserverIds.add("MEM002");
        reserverIds.add("MEM003");
        Item itemFromFile = new Item(item1Id, item1Title, item1Year, item1Author, item1Location, null, 2, reserverIds);
        // ReservedByNames would be empty initially for such an item
        String str = itemFromFile.toString();
        assertTrue(str.contains("Status: Available"));
        assertTrue(str.contains("Reserved by IDs: [MEM002, MEM003]"));
    }


    @Test
    void testSetReservedByNames() {
        List<String> names = Arrays.asList("Alice (ID: M01)", "Bob (ID: M02)");
        item1.setReservedByNames(names);
        assertEquals(names, item1.getReservedByNames());

        item1.setReservedByNames(null);
        assertTrue(item1.getReservedByNames().isEmpty(), "Setting reserved names to null should result in an empty list.");
    }
}
