package libraryMember;

public enum Header {
    // Inventory
	INV,
    SEARCH,     // Search - String Query (Search ItemList for Item based on query)
    TRANSFER,   // Transfer - Location LocationB (Edit item data to set location from to loc b)
    ADD,        // Add - Item ItemA (Add data for item to itemList)
    REMOVE,     // Remove - String ItemID (Remove item associated with item id)
    EDIT,		// Edit
    GET,        // Get - String ItemID (Get item associated to itemID)
    DATA,       // Data - Item ItemA (Passing of ItemA)

    MAKESTAFF,  // MakeStaff - To make this user a staff member
    // Account Management
    ACCT,
    LOGIN,
    LOGOUT,
    CREATE,     // Create - Account AccountA (Create account with message object)
    DELETE,     // Delete - String AccountID (Delete Account assoc. with ID)
    STATUS,     // Status - Account Account A Status Status (Set status of accountA to status)
    //EDIT,     // Edit - Account AccountA (Replace account with AccountA, which is a new Account object with changes implemented)
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
    CHECKOUT,   // CheckOut - String ItemID, String MemberID (Check out item associated with item ID --  meaning it is unavailable for checkout)
    CHECKIN,    // CheckIn - String ItemID (Check in item associated with item ID -- meaning it is avaliable for checkout)
    RESERVE,    // Reserve - String ItemID, String MemberID (Set item assoc w itemid as reserved by member assoc w memberID
    //GET,         Get - String ItemID (Get item associated to itemID)
    //DATA,        Data - Item ItemA (Passing of ItemA)

    // Network
    NET,
    ACK, // Acknowledge - String AckMsg (A message of acknowledgement related to received message)
	ERR; // Error - String ErrMsg (An error message)
}