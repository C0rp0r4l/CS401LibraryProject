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

class LocationTest {

    // To test Location, we need to control its source file.
    // The Location class hardcodes "locationlist.txt".
    // We'll create/delete this file in the test working directory.
    private static final String TEST_LOCATION_FILE = "locationlist.txt";
    private Path testFilePath = Paths.get(TEST_LOCATION_FILE);
    private Location locationManager;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up and create a fresh Location instance for each test
        Files.deleteIfExists(testFilePath);
        locationManager = new Location(); // This will call load()
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up after each test
        Files.deleteIfExists(testFilePath);
    }

    @Test
    void testInitialLoad_FileDoesNotExist() {
        assertTrue(locationManager.getAllLocations().isEmpty(), "Initially, locations should be empty if file doesn't exist.");
        assertTrue(Files.exists(testFilePath), "Location file should be created by constructor if it doesn't exist.");
    }

    @Test
    void testAddLocation() {
        assertTrue(locationManager.addLocation("Fiction Section"), "Should add new location successfully.");
        assertEquals(1, locationManager.size());
        assertTrue(locationManager.contains("Fiction Section"));

        assertFalse(locationManager.addLocation("Fiction Section"), "Should not add duplicate location.");
        assertEquals(1, locationManager.size(), "Size should not change for duplicate add.");

        assertFalse(locationManager.addLocation("  "), "Should not add blank location.");
        assertEquals(1, locationManager.size());
        
        assertFalse(locationManager.addLocation(null), "Should not add null location.");
        assertEquals(1, locationManager.size());
    }

    @Test
    void testRemoveLocation() {
        locationManager.addLocation("Sci-Fi");
        locationManager.addLocation("History");

        assertTrue(locationManager.removeLocation("Sci-Fi"), "Should remove existing location successfully.");
        assertEquals(1, locationManager.size());
        assertFalse(locationManager.contains("Sci-Fi"));
        assertTrue(locationManager.contains("History"));

        assertFalse(locationManager.removeLocation("NonExistent"), "Should return false for non-existent location.");
        assertEquals(1, locationManager.size());
        
        assertFalse(locationManager.removeLocation(null), "Should return false for null location.");
        assertEquals(1, locationManager.size());
    }

    @Test
    void testContains() {
        locationManager.addLocation("  Main Shelf  "); // Test with spaces
        assertTrue(locationManager.contains("Main Shelf"), "Contains should trim input and find location.");
        assertFalse(locationManager.contains("Other Shelf"));
    }

    @Test
    void testGetAllLocations() {
        locationManager.addLocation("Location A");
        locationManager.addLocation("Location B");
        List<String> allLocations = locationManager.getAllLocations();
        assertEquals(2, allLocations.size());
        assertTrue(allLocations.contains("Location A"));
        assertTrue(allLocations.contains("Location B"));

        // Ensure it's a copy
        allLocations.add("Location C_TestCopy");
        assertEquals(2, locationManager.getAllLocations().size(), "Modifying returned list should not affect original.");
    }

    @Test
    void testSize() {
        assertEquals(0, locationManager.size());
        locationManager.addLocation("L1");
        assertEquals(1, locationManager.size());
        locationManager.addLocation("L2");
        assertEquals(2, locationManager.size());
    }

    @Test
    void testClear() {
        locationManager.addLocation("L1");
        locationManager.addLocation("L2");
        locationManager.clear();
        assertEquals(0, locationManager.size());
        assertTrue(locationManager.getAllLocations().isEmpty());
        // Also check if file is empty after save (implicitly tested by next load)
        locationManager.save(); // Force save
        Location newManager = new Location(); // This will load
        assertTrue(newManager.getAllLocations().isEmpty(), "After clear and save, new load should be empty.");
    }

    @Test
    void testSaveAndLoad() throws IOException {
        locationManager.addLocation("Shelf 1");
        locationManager.addLocation("Shelf 2");
        locationManager.save(); // Persist changes

        assertTrue(Files.exists(testFilePath) && Files.size(testFilePath) > 0, "Location file should exist and not be empty after save.");

        // Create a new instance to trigger load()
        Location newLocationManager = new Location();
        assertEquals(2, newLocationManager.size());
        assertTrue(newLocationManager.contains("Shelf 1"));
        assertTrue(newLocationManager.contains("Shelf 2"));
    }
    
    @Test
    void testLoad_WithEmptyLinesAndSpacesInFile() throws IOException {
        Files.writeString(testFilePath, "  Location One  \n\nLocation Two\n   \nLocation Three");
        Location newManager = new Location(); // Triggers load
        List<String> locations = newManager.getAllLocations();
        assertEquals(3, locations.size(), "Should load 3 valid locations, ignoring empty/blank lines.");
        assertTrue(locations.contains("Location One"));
        assertTrue(locations.contains("Location Two"));
        assertTrue(locations.contains("Location Three"));
    }

    @Test
    void testToStringMethod() {
        locationManager.addLocation("Alpha");
        locationManager.addLocation("Beta");
        String expected = "Alpha\nBeta";
        assertEquals(expected, locationManager.toString());

        locationManager.clear();
        assertEquals("", locationManager.toString());
    }
}
