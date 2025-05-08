package scmot;

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

    // Account Management
    ACCT,
    LOGIN,
    LOGOUT,
    CREATE,     // Create - Member MemberA (Create Member with message object)
    DELETE,     // Delete - String AccountID (Delete Member assoc. with ID)
    STATUS,     // Status - Member MemberA Status Status (Set status of MemberA to status)
    MAKESTAFF,  //MakeStaff - String MemberID (Make Member assoc with MemberID into a StaffMember)
    //EDIT,     // Edit - Member MemberA (Replace Member with MemberA, which is a new Member object with changes implemented)
    // GET		// Get - String MemberID (Get Member associated to MemberID)
    // DATA		// Data - Member MemberA (Passing of MemberA)

    // Location Management
    LOC,
    // CREATE   //String name (Create a location with name name)
    // ADD		//String StaffMemberID (Add staff to Location)
    // GET		// Get - String LocationID (Get item associated to LocationID)
    // DATA		// Data - Location (Passing of ItemA)

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
