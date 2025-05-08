package librarySoftwareG5;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static Scanner scanner; // Used for all console input

    public static void main(String[] args) {
        // Try-with-resources for Socket and main Scanner
        try (Socket socket = new Socket("localhost", 7777);
             Scanner mainScanner = new Scanner(System.in)) {

            // Initialize streams and static scanner
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            scanner = mainScanner;

            System.out.println("Connected to server: " + socket.getRemoteSocketAddress());

            // Handle login
            if (!handleLogin()) {
                System.out.println("Login failed. Exiting client.");
                return; // Exit if login fails
            }

            // Main menu loop
            boolean running = true;
            while (running) {
                printMainMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1": // Member Operations
                        handleMemberOperations();
                        break;
                    case "2": // Staff Member Operations
                        handleStaffMemberOperations();
                        break;
                    case "3": // Item Operations
                        handleItemOperations();
                        break;
                    case "4": // Location Operations
                        handleLocationOperations();
                        break;
                    case "5": // Logout
                        Message logoutMsg = new Message(Message.ActionType.LOGOUT, null);
                        sendRequest(logoutMsg, false); // Expect simple ACK
                        System.out.println("Logged out from server.");
                        running = false; // Exit main loop
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SocketException e) {
            System.err.println("Connection error: Could not connect to the server or connection lost. " + e.getMessage());
            System.err.println("Please ensure the server is running and accessible.");
        } catch (EOFException e) {
             System.err.println("Connection closed by server unexpectedly.");
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Client communication error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Exiting client application.");
            // scanner.close(); // Closing System.in scanner can cause issues if other parts of JVM expect it.
            // Typically not closed here if application might be extended or run in certain environments.
        }
    }

    private static boolean handleLogin() throws IOException, ClassNotFoundException {
        System.out.print("Staff ID: ");
        String staffId = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Message loginMsg = new Message(Message.ActionType.LOGIN, new String[]{staffId, password});
        out.writeObject(loginMsg);
        out.flush(); // Ensure login message is sent immediately

        Message response = (Message) in.readObject();
        if (!response.isSuccess()) {
            System.out.println("Login failed: " + response.getErrorMessage());
            return false;
        }

        // Assuming server sends StaffMember object on successful login
        StaffMember loggedInStaff = (StaffMember) response.getData();
        System.out.println("Logged in successfully as " + loggedInStaff.getName() + " (ID: " + loggedInStaff.getMemberID() + ")");
        return true;
    }

    private static void printMainMenu() {
        System.out.println("\n--- Library Management System ---");
        System.out.println("1. Member Operations");
        System.out.println("2. Staff Member Operations");
        System.out.println("3. Item Operations");
        System.out.println("4. Location Operations");
        System.out.println("5. Logout");
        System.out.print("Choose an option: ");
    }

    /**
     * Sends a request to the server and handles the basic response.
     * @param msg The Message object to send.
     * @param expectComplexData True if the response data is expected to be an object/list for displayGenericResponse.
     * False if a simple string ACK is expected.
     * @return The Message object received from the server.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Message sendRequest(Message msg, boolean expectComplexData) throws IOException, ClassNotFoundException {
        out.writeObject(msg);
        out.flush(); // Ensure message is sent

        Message response = (Message) in.readObject();
        if (response.isSuccess()) {
            if (expectComplexData) {
                // displayGenericResponse will be called by the handler method for complex data
            } else if (response.getData() instanceof String) { // For simple ACK messages with a string payload
                System.out.println("Server: " + response.getData());
            } else if (response.getData() == null && msg.getAction() == Message.ActionType.LOGOUT) {
                 System.out.println("Server: Logout Acknowledged"); // Specific for logout ACK
            }
             else { // For other simple ACKs without a specific string payload (but not expecting complex data)
                if (msg.getAction() != Message.ActionType.GET_ALL_MEMBERS &&
                    msg.getAction() != Message.ActionType.GET_ALL_STAFF &&
                    msg.getAction() != Message.ActionType.GET_ALL_ITEMS &&
                    msg.getAction() != Message.ActionType.GET_LOCATIONS &&
                    msg.getAction() != Message.ActionType.SEARCH_MEMBERS &&
                    msg.getAction() != Message.ActionType.SEARCH_ITEMS &&
                    msg.getAction() != Message.ActionType.GET_MEMBER &&
                    msg.getAction() != Message.ActionType.GET_STAFF &&
                    msg.getAction() != Message.ActionType.GET_ITEM) {
                    System.out.println("Operation successful."); // Generic success for non-data-fetching ops
                }
            }
        } else {
            System.out.println("Error from server: " + response.getErrorMessage());
        }
        return response;
    }

    /**
     * Displays various types of payloads received from the server in a user-friendly format.
     * @param payload The data object from the server's response.
     * @param actionType The type of action that led to this response (for context).
     */
    private static void displayGenericResponse(Object payload, Message.ActionType actionType) {
        if (payload == null) {
            // Contextual "not found" messages based on action type
             if (actionType == Message.ActionType.GET_ALL_MEMBERS || actionType == Message.ActionType.GET_ALL_STAFF ||
                actionType == Message.ActionType.GET_ALL_ITEMS || actionType == Message.ActionType.GET_LOCATIONS ||
                actionType == Message.ActionType.SEARCH_MEMBERS || actionType == Message.ActionType.SEARCH_ITEMS) {
                System.out.println("No records found matching your criteria.");
            } else if (actionType == Message.ActionType.GET_MEMBER || actionType == Message.ActionType.GET_STAFF || actionType == Message.ActionType.GET_ITEM) {
                 System.out.println("The requested record was not found.");
            } else {
                // This case might occur if an operation was successful but returned no specific data object,
                // and it wasn't a simple string ACK handled by sendRequest.
                // System.out.println("Operation completed, no specific data to display.");
            }
            return;
        }

        if (payload instanceof String) { // Should be handled by sendRequest, but as a fallback
            System.out.println(payload);
        } else if (payload instanceof Member && !(payload instanceof StaffMember)) {
            Member member = (Member) payload;
            System.out.println("\n--- Member Details ---");
            System.out.println("ID: " + member.getMemberID());
            System.out.println("Name: " + member.getName());
            System.out.println("Strikes: " + member.getStrikes());
            System.out.println("Account Hold: " + (member.isAccountHold() ? "Yes" : "No"));
            System.out.println("Account Banned: " + (member.isAccountBanned() ? "Yes" : "No"));
            System.out.println("Status: " + (member.isAccountBanned() ? "Banned" :
                                          member.isAccountHold() ? "On Hold" : "Active"));
        } else if (payload instanceof StaffMember) {
            StaffMember staff = (StaffMember) payload;
            System.out.println("\n--- Staff Member Details ---");
            System.out.println("ID: " + staff.getMemberID());
            System.out.println("Name: " + staff.getName());
            System.out.println("Location: " + staff.getLocation());
            System.out.println("Strikes: " + staff.getStrikes()); // Staff can also have strikes
            System.out.println("Account Hold: " + (staff.isAccountHold() ? "Yes" : "No"));
            System.out.println("Account Banned: " + (staff.isAccountBanned() ? "Yes" : "No"));
            System.out.println("Status: " + (staff.isAccountBanned() ? "Banned" :
                                           staff.isAccountHold() ? "On Hold" : "Active"));
        } else if (payload instanceof Item) {
            Item item = (Item) payload;
            System.out.println("\n--- Item Details ---");
            System.out.println(item.toString()); // Relies on Item's comprehensive toString()
        }
        else if (payload instanceof List) {
            List<?> list = (List<?>) payload;
            if (list.isEmpty()) {
                System.out.println("No records found in the list.");
                return;
            }
            System.out.println("\n--- List Results (" + list.size() + " record(s)) ---");
            for (Object itemInList : list) {
                if (itemInList instanceof Member && !(itemInList instanceof StaffMember)) {
                    Member m = (Member) itemInList;
                    System.out.printf("Member | ID: %-10s | Name: %-20s | Strikes: %d | Status: %s%n",
                        m.getMemberID(), m.getName(), m.getStrikes(),
                        (m.isAccountBanned() ? "Banned" : m.isAccountHold() ? "On Hold" : "Active"));
                } else if (itemInList instanceof StaffMember) {
                    StaffMember s = (StaffMember) itemInList;
                    System.out.printf("Staff  | ID: %-10s | Name: %-20s | Location: %-15s | Status: %s%n",
                        s.getMemberID(), s.getName(), s.getLocation(),
                        (s.isAccountBanned() ? "Banned" : s.isAccountHold() ? "On Hold" : "Active"));
                } else if (itemInList instanceof Item) {
                    System.out.println("Item   | " + itemInList.toString()); // Item.toString() handles detailed item display
                } else if (itemInList instanceof String) { // For list of locations
                    System.out.println("Location: " + itemInList);
                } else {
                    System.out.println("Unknown list item: " + itemInList.toString());
                }
            }
        } else {
             System.out.println("Received unhandled data type for display: " + payload.getClass().getName());
        }
    }

    // --- Menu Handler Methods ---
    // (handleMemberOperations, handleStaffMemberOperations, handleItemOperations, handleLocationOperations)
    // These methods remain structurally similar to previous versions,
    // relying on sendRequest and displayGenericResponse.

    private static void handleMemberOperations() throws IOException, ClassNotFoundException {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Member Operations ---");
            System.out.println("1. Add Member");
            System.out.println("2. Remove Member");
            System.out.println("3. Search Member by ID");
            System.out.println("4. Search Members by Name");
            System.out.println("5. List All Members");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            Message requestMsg = null;
            Object data = null;
            Message.ActionType action = null;
            boolean expectDataForDisplay = false;

            switch (choice) {
                case "1":
                    System.out.print("Enter member name: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.ADD_MEMBER;
                    break;
                case "2":
                    System.out.print("Enter member ID to remove: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.REMOVE_MEMBER;
                    break;
                case "3":
                    System.out.print("Enter member ID to search: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.GET_MEMBER;
                    expectDataForDisplay = true;
                    break;
                case "4":
                    System.out.print("Enter name (or part of name) to search: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.SEARCH_MEMBERS;
                    expectDataForDisplay = true;
                    break;
                case "5":
                    action = Message.ActionType.GET_ALL_MEMBERS;
                    expectDataForDisplay = true;
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    continue; // Loop back to menu
            }

            if (action != null) {
                requestMsg = new Message(action, data);
                Message response = sendRequest(requestMsg, expectDataForDisplay);
                if (expectDataForDisplay && response.isSuccess()) {
                    displayGenericResponse(response.getData(), action);
                }
            }
        }
    }

    private static void handleStaffMemberOperations() throws IOException, ClassNotFoundException {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Staff Member Operations ---");
            System.out.println("1. Add Staff Member");
            System.out.println("2. Remove Staff Member");
            System.out.println("3. Search Staff Member by ID");
            System.out.println("4. List All Staff Members");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            Message requestMsg = null;
            Object data = null;
            Message.ActionType action = null;
            boolean expectDataForDisplay = false;

            switch (choice) {
                case "1":
                    System.out.print("Enter staff member name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Enter staff member location: ");
                    String location = scanner.nextLine().trim();
                    data = new String[]{name, location}; // Server expects {name, location}
                    action = Message.ActionType.ADD_STAFF;
                    break;
                case "2":
                    System.out.print("Enter staff ID to remove: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.REMOVE_STAFF;
                    break;
                case "3":
                    System.out.print("Enter staff ID to search: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.GET_STAFF;
                    expectDataForDisplay = true;
                    break;
                case "4":
                    action = Message.ActionType.GET_ALL_STAFF;
                    expectDataForDisplay = true;
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    continue;
            }

            if (action != null) {
                requestMsg = new Message(action, data);
                Message response = sendRequest(requestMsg, expectDataForDisplay);
                 if (expectDataForDisplay && response.isSuccess()) {
                    displayGenericResponse(response.getData(), action);
                }
            }
        }
    }

    private static void handleItemOperations() throws IOException, ClassNotFoundException {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Item Operations ---");
            System.out.println("1. Add Item");
            System.out.println("2. Remove Item");
            System.out.println("3. Search Item by ID");
            System.out.println("4. Search Items by Title");
            System.out.println("5. List All Items");
            System.out.println("6. Checkout Item");
            System.out.println("7. Return Item");
            System.out.println("8. Reserve Item");
            System.out.println("9. Cancel Reservation");
            System.out.println("10. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            Message requestMsg = null;
            Object data = null;
            Message.ActionType action = null;
            boolean expectDataForDisplay = false;
            String title, year, author, location, itemId, memberId; // Re-declare for local scope

            switch (choice) {
                case "1": // Add Item
                    System.out.print("Enter item title: ");
                    title = scanner.nextLine().trim();
                    System.out.print("Enter publication year: ");
                    year = scanner.nextLine().trim();
                    System.out.print("Enter author: ");
                    author = scanner.nextLine().trim();
                    System.out.print("Enter location (must exist): ");
                    location = scanner.nextLine().trim();
                    data = new String[]{title, year, author, location};
                    action = Message.ActionType.ADD_ITEM;
                    break;
                case "2": // Remove Item
                    System.out.print("Enter item ID to remove: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.REMOVE_ITEM;
                    break;
                case "3": // Search Item by ID
                    System.out.print("Enter item ID to search: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.GET_ITEM;
                    expectDataForDisplay = true;
                    break;
                case "4": // Search Items by Title
                    System.out.print("Enter title (or part of title) to search: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.SEARCH_ITEMS;
                    expectDataForDisplay = true;
                    break;
                case "5": // List All Items
                    action = Message.ActionType.GET_ALL_ITEMS;
                    expectDataForDisplay = true;
                    break;
                case "6": // Checkout Item
                    System.out.print("Enter item ID to checkout: ");
                    itemId = scanner.nextLine().trim();
                    System.out.print("Enter member ID for checkout: ");
                    memberId = scanner.nextLine().trim();
                    data = new String[]{itemId, memberId}; // Server expects {itemId, memberId}
                    action = Message.ActionType.CHECKOUT;
                    break;
                case "7": // Return Item
                    System.out.print("Enter item ID to return: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.RETURN;
                    break;
                case "8": // Reserve Item
                    System.out.print("Enter item ID to reserve: ");
                    itemId = scanner.nextLine().trim();
                    System.out.print("Enter member ID for reservation: ");
                    memberId = scanner.nextLine().trim();
                    data = new String[]{itemId, memberId}; // Server expects {itemId, memberId}
                    action = Message.ActionType.RESERVE;
                    break;
                case "9": // Cancel Reservation
                    System.out.print("Enter item ID to cancel reservation for: ");
                    itemId = scanner.nextLine().trim();
                    System.out.print("Enter member ID whose reservation to cancel: ");
                    memberId = scanner.nextLine().trim();
                    data = new String[]{itemId, memberId}; // Server expects {itemId, memberId}
                    action = Message.ActionType.CANCEL_RESERVATION;
                    break;
                case "10": // Back
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    continue;
            }

            if (action != null) {
                requestMsg = new Message(action, data);
                Message response = sendRequest(requestMsg, expectDataForDisplay);
                 if (expectDataForDisplay && response.isSuccess()) {
                    displayGenericResponse(response.getData(), action);
                }
            }
        }
    }

    private static void handleLocationOperations() throws IOException, ClassNotFoundException {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Location Operations ---");
            System.out.println("1. Add Location");
            System.out.println("2. Remove Location");
            System.out.println("3. List All Locations");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            Message requestMsg = null;
            Object data = null;
            Message.ActionType action = null;
            boolean expectDataForDisplay = false;

            switch (choice) {
                case "1":
                    System.out.print("Enter location name to add: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.ADD_LOCATION;
                    break;
                case "2":
                    System.out.print("Enter location name to remove: ");
                    data = scanner.nextLine().trim();
                    action = Message.ActionType.REMOVE_LOCATION;
                    break;
                case "3":
                    action = Message.ActionType.GET_LOCATIONS;
                    expectDataForDisplay = true;
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    continue;
            }

            if (action != null) {
                requestMsg = new Message(action, data);
                Message response = sendRequest(requestMsg, expectDataForDisplay);
                 if (expectDataForDisplay && response.isSuccess()) {
                    displayGenericResponse(response.getData(), action);
                }
            }
        }
    }
}
