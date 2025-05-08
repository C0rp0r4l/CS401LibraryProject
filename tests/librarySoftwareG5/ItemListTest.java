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

class ItemListTest {

    private static final String TEST_ITEM_FILE = "masterItemList.txt";
    private Path testFilePath = Paths.get(TEST_ITEM_FILE);
    private ItemList itemList;

    // Dummy location file needed for addItem if location validation is strict
    private static final String TEST_LOCATION_FILE = "locationlist.txt";
    private Path testLocationFilePath = Paths.get(TEST_LOCATION_FILE);
    private Location locationList;


    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(testFilePath);
        Files.deleteIfExists(testLocationFilePath); // Clean up dummy location file
        
        // Create a dummy location file with a valid location for tests
        locationList = new Location(); // This will create 'locationlist.txt'
        locationList.addLocation("Shelf A");
        locationList.save();

        itemList = new ItemList(); // This will call loadItems()
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFilePath);
        Files.deleteIfExists(testLocationFilePath);
    }

    @Test
    void testInitialLoad_FileDoesNotExist() {
        assertTrue(itemList.getAllItems().isEmpty(), "Initially, items should be empty if file doesn't exist.");
        assertTrue(Files.exists(testFilePath), "Item file should be created by constructor if it doesn't exist.");
    }

    @Test
    void testAddItem() {
        String title = "Test Book";
        String year = "2023";
        String author = "Test Author";
        String location = "Shelf A"; // Must exist in our dummy location file

        String itemId = itemList.addItem(title, year, author, location);
        assertNotNull(itemId);
        assertFalse(itemId.isEmpty());

        Item retrievedItem = itemList.getItem(itemId);
        assertNotNull(retrievedItem);
        assertEquals(title, retrievedItem.getTitle());
        assertEquals(author, retrievedItem.getAuthor());
        assertEquals(location, retrievedItem.getLocation());
        assertEquals(1, itemList.getAllItems().size());
    }
    
    @Test
    void testAddItem_GeneratedIdUniqueness() {
        String id1 = itemList.addItem("Title1", "2020", "Auth1", "Shelf A");
        String id2 = itemList.addItem("Title2", "2021", "Auth2", "Shelf A");
        assertNotEquals(id1, id2, "Generated item IDs should be unique.");
    }


    @Test
    void testRemoveItem() {
        String itemId = itemList.addItem("Book to Remove", "2020", "Author", "Shelf A");
        assertTrue(itemList.removeItem(itemId), "Should remove existing item successfully.");
        assertNull(itemList.getItem(itemId), "Item should be null after removal.");
        assertEquals(0, itemList.getAllItems().size());

        assertFalse(itemList.removeItem("NONEXISTENT_ID"), "Should return false for non-existent item ID.");
    }

    @Test
    void testGetItem() {
        String itemId = itemList.addItem("Specific Book", "2021", "Specific Author", "Shelf A");
        Item item = itemList.getItem(itemId);
        assertNotNull(item);
        assertEquals("Specific Book", item.getTitle());

        assertNull(itemList.getItem("NONEXISTENT_ID"), "Should return null for non-existent item ID.");
    }

    @Test
    void testSearchByTitle() {
        itemList.addItem("The Great Test", "2020", "Tester", "Shelf A");
        itemList.addItem("Another Test Book", "2021", "Tester", "Shelf A");
        itemList.addItem("No Match", "2022", "Someone", "Shelf A");

        List<Item> results = itemList.searchByTitle("Test");
        assertEquals(2, results.size(), "Should find two books with 'Test' in title.");
        assertTrue(results.stream().anyMatch(item -> item.getTitle().equals("The Great Test")));
        assertTrue(results.stream().anyMatch(item -> item.getTitle().equals("Another Test Book")));

        List<Item> noResults = itemList.searchByTitle("NonExistentTitle");
        assertTrue(noResults.isEmpty(), "Should return empty list for non-matching title.");
        
        List<Item> caseInsensitiveResults = itemList.searchByTitle("test");
        assertEquals(2, caseInsensitiveResults.size(), "Search should be case-insensitive.");
    }

    @Test
    void testGetAllItems() {
        itemList.addItem("Book 1", "Y1", "A1", "Shelf A");
        itemList.addItem("Book 2", "Y2", "A2", "Shelf A");
        List<Item> allItems = itemList.getAllItems();
        assertEquals(2, allItems.size());

        // Ensure it's a copy
        allItems.add(new Item("temp", "temp", "t", "t", "t", null, 0, new ArrayList<>()));
        assertEquals(2, itemList.getAllItems().size(), "Modifying returned list should not affect original.");
    }

    @Test
    void testCheckoutItem() {
        String itemId = itemList.addItem("Checkoutable Book", "2023", "Author", "Shelf A");
        String memberId = "MEM001";
        String memberName = "John Doe";

        assertTrue(itemList.checkoutItem(itemId, memberId, memberName), "Should checkout available item successfully.");
        Item item = itemList.getItem(itemId);
        assertEquals(memberId, item.getOwnedBy());
        assertEquals(memberName, item.getOwnedByName());

        assertFalse(itemList.checkoutItem(itemId, "MEM002", "Jane Doe"), "Should not checkout already owned item.");
        assertFalse(itemList.checkoutItem("NONEXISTENT_ID", memberId, memberName), "Should not checkout non-existent item.");
    }

    @Test
    void testReturnItem() {
        String itemId = itemList.addItem("Returnable Book", "2023", "Author", "Shelf A");
        itemList.checkoutItem(itemId, "MEM001", "John Doe");

        assertTrue(itemList.returnItem(itemId), "Should return checked-out item successfully.");
        Item item = itemList.getItem(itemId);
        assertNull(item.getOwnedBy(), "Item should not be owned after return.");
        assertNull(item.getOwnedByName());


        assertFalse(itemList.returnItem(itemId), "Should not return an already available item.");
        assertFalse(itemList.returnItem("NONEXISTENT_ID"), "Should not return non-existent item.");
    }

    @Test
    void testReserveItem() {
        String itemId = itemList.addItem("Reservable Book", "2023", "Author", "Shelf A");
        String memberId = "MEM001";
        String memberName = "John Doe";

        assertTrue(itemList.reserveItem(itemId, memberId, memberName), "Should reserve item successfully.");
        Item item = itemList.getItem(itemId);
        assertEquals(1, item.getReservedCount());
        assertTrue(item.getReservedBy().contains(memberId));
        // Name check can be tricky due to formatting, check if list of names contains the memberName part
        assertTrue(item.getReservedByNames().stream().anyMatch(name -> name.startsWith(memberName)));


        assertFalse(itemList.reserveItem(itemId, memberId, memberName), "Should not reserve item again by same member.");
        assertFalse(itemList.reserveItem("NONEXISTENT_ID", memberId, memberName), "Should not reserve non-existent item.");
    }

    @Test
    void testCancelReservation() {
        String itemId = itemList.addItem("Book With Reservation", "2023", "Author", "Shelf A");
        String memberId = "MEM001";
        String memberName = "John Doe";
        itemList.reserveItem(itemId, memberId, memberName);

        assertTrue(itemList.cancelReservation(itemId, memberId), "Should cancel reservation successfully.");
        Item item = itemList.getItem(itemId);
        assertEquals(0, item.getReservedCount());
        assertFalse(item.getReservedBy().contains(memberId));
        assertTrue(item.getReservedByNames().isEmpty());


        assertFalse(itemList.cancelReservation(itemId, memberId), "Should not cancel non-existent reservation.");
        assertFalse(itemList.cancelReservation("NONEXISTENT_ID", memberId), "Should not cancel reservation for non-existent item.");
    }

    @Test
    void testSaveAndLoadItems() throws IOException {
        // Add a few items
        String id1 = itemList.addItem("Book Alpha", "2000", "AuthX", "Shelf A");
        String id2 = itemList.addItem("Book Beta", "2001", "AuthY", "Shelf A");
        itemList.checkoutItem(id1, "M001", "Alice");
        itemList.reserveItem(id2, "M002", "Bob");
        itemList.reserveItem(id2, "M003", "Charlie");

        // itemList.saveItems() is called internally by add, checkout, reserve etc.
        // To be absolutely sure for the test, we can call it if it were public, or rely on operations.
        // For this test, the operations above should have saved.

        ItemList newItemsList = new ItemList(); // This will load from the file
        assertEquals(2, newItemsList.getAllItems().size(), "Should load the correct number of items.");

        Item loadedItem1 = newItemsList.getItem(id1);
        assertNotNull(loadedItem1);
        assertEquals("Book Alpha", loadedItem1.getTitle());
        assertEquals("M001", loadedItem1.getOwnedBy());
        // ownedByName is transient, so it won't be directly loaded from file in this simple setup.
        // It would be populated by server-side enrichment.

        Item loadedItem2 = newItemsList.getItem(id2);
        assertNotNull(loadedItem2);
        assertEquals("Book Beta", loadedItem2.getTitle());
        assertNull(loadedItem2.getOwnedBy());
        assertEquals(2, loadedItem2.getReservedCount());
        assertTrue(loadedItem2.getReservedBy().containsAll(List.of("M002", "M003")));
        // reservedByNames is transient.
    }
    
    @Test
    void testLoadItems_MalformedLine() throws IOException {
        // itemID,Title,Year,Author,Location,ownedBy,reservedCount,reservedID1,reservedID2,...
        String goodLine = "ITM001,Good Book,2020,Good Author,Shelf A,null,0,";
        String malformedLine = "ITM002,Bad Book Data,2021"; // Too few parts
        String anotherGoodLine = "ITM003,Another Fine Book,2022,Nice Author,Shelf A,M005,1,M006";

        List<String> lines = List.of(goodLine, malformedLine, anotherGoodLine);
        Files.write(testFilePath, lines);

        ItemList freshItemList = new ItemList(); // This calls loadItems
        assertEquals(2, freshItemList.getAllItems().size(), "Should skip malformed lines and load valid ones.");
        assertNotNull(freshItemList.getItem("ITM001"));
        assertNotNull(freshItemList.getItem("ITM003"));
        assertNull(freshItemList.getItem("ITM002"));
    }
}
