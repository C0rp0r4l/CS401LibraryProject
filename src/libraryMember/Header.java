package scmot;

public enum Header {
    // Inventory
	INV,
    SEARCH,     // Search - String Query
    TRANSFER,   // Transfer - Location LocationA Location LocationB
    ADD,        // Add - Item ItemA
    REMOVE,     // Remove - Item ItemA
    GET,        // Get - String ItemID
    DATA,       // Data - Item ItemA

    // Account Management
    ACCT,
    LOGIN,
    LOGOUT,
    CREATE,     // Create - Account AccountA (Create account with message object)
    DELETE,     // Delete - String AccountID (Delete Account assoc. with ID)
    STATUS,     // Status - Account Account A Status Status (Set status of accountA to status)
    EDIT,       // Edit - Account AccountA (Replace account with AccountA, which is a new Account object with changes implemented)
    // GET already defined above (reuse GET)
    // DATA already defined above (reuse DATA)

    // Location Management
    LOC,
    // CREATE already defined above (reuse CREATE)
    // ADD already defined above (reuse ADD)
    // GET already defined above (reuse GET)
    // DATA already defined above (reuse DATA)

    // Item Attainment
    ITEM,
    CHECKOUT,   // CheckOut - String ItemID
    CHECKIN,    // CheckIn - String ItemID
    MOVELOCATION, // MoveLocation - String ItemID, Location LocationA
    RESERVE,    // Reserve - String ItemID
    // GET already defined above (reuse GET)
    // DATA already defined above (reuse DATA)

    // Network
    NET,
    ACK, // Acknowledge - String AckMsg (A message of acknowledgement related to received message)
	ERR; // Error - String ErrMsg (An error message)
}
