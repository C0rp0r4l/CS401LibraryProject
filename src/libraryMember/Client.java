package libraryMember;

import java.io.*;
import java.net.*;
import java.util.*;


 //Library Client 
 //Connects to the library management server and provides a user-friendly interface
 
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 7777;
    private static boolean isConnected = false;
    private static Member currentUser = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==== Library Management System Client ====");
        System.out.println("Connecting to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
        
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream())) {

            isConnected = true;
            System.out.println("Connected successfully to the library server!");
            
            // Start the communication loop with the server
            communicateWithServer(outToServer, inFromServer);
            
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Data error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isConnected = false;
            System.out.println("Disconnected from server. Goodbye!");
            scanner.close();
        }
    }


    //Main communication loop with the server

    private static void communicateWithServer(ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        Message serverMessage;
        
        // Handle initial server messages (usually login request)
        serverMessage = (Message) in.readObject();
        processServerMessage(serverMessage, out, in);
        
        // Main application loop
        while (isConnected) {
            if (currentUser == null) {
                // Not logged in yet, wait for login response
                if (in.available() > 0) {
                    serverMessage = (Message) in.readObject();
                    processServerMessage(serverMessage, out, in);
                }
                
                // If still not logged in, try again
                if (currentUser == null) {
                    handleLogin(out, in);
                }
            } else {
                // Already logged in, show the main menu
                displayMainMenu();
                int choice = getValidChoice(1, 7);
                
                switch (choice) {
                    case 1: // Search for books
                        handleBookSearch(out, in);
                        break;
                    case 2: // Search for members
                        handleMemberSearch(out, in);
                        break;
                    case 3: // Add new book
                        handleAddBook(out, in);
                        break;
                    case 4: // Add new member
                        handleAddMember(out, in);
                        break;
                    case 5: // Manage locations
                        handleLocationManagement(out, in);
                        break;
                    case 6: // View my account
                        System.out.println("\nMy Account Information:");
                        System.out.println(currentUser.toString());
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine();
                        break;
                    case 7: // Exit
                        System.out.println("Logging out...");
                        isConnected = false;
                        break;
                }
            }
        }
    }


    //Handle the login process

    private static void handleLogin(ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        System.out.println("\n==== Login ====");
        System.out.print("Username/Member ID: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        Message loginMessage = new Message(
            Header.ACCT,
            Header.LOGIN,
            username + "," + password,
            "client",
            "server",
            "server",
            "client"
        );
        
        out.writeObject(loginMessage);
        out.flush();
        
        // Wait for server response
        Message response = (Message) in.readObject();
        processServerMessage(response, out, in);
    }

    //Handle book search functionality
    private static void handleBookSearch(ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        System.out.println("\n==== Search Books ====");
        System.out.println("1. Search by Title");
        System.out.println("2. Search by ID");
        System.out.print("Choose search method: ");
        
        int searchChoice = getValidChoice(1, 2);
        String searchType, searchValue;
        
        if (searchChoice == 1) {
            System.out.print("Enter book title: ");
            searchValue = scanner.nextLine().trim();
            searchType = "title";
        } else {
            System.out.print("Enter book ID: ");
            searchValue = scanner.nextLine().trim();
            searchType = "id";
        }
        
        Message searchMessage = new Message(
            Header.INV,
            Header.GET,
            searchType + "," + searchValue,
            "client",
            "server",
            "server",
            "client"
        );
        
        out.writeObject(searchMessage);
        out.flush();
        
        // Wait for server response
        Message response = (Message) in.readObject();
        processServerMessage(response, out, in);
        
        // If we found items, show action menu
        if (response.getSecondaryHeader() == Header.DATA && response.getData() instanceof ItemList) {
            handleItemActions((ItemList) response.getData(), out, in);
        }
    }


    //Handle actions for a specific item
    private static void handleItemActions(ItemList itemList, ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        List<Item> items = itemList.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }
        
        System.out.println("\n==== Book Results ====");
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).getTitle() + " by " + items.get(i).getAuthor() + 
                    " (" + items.get(i).getYear() + ") - ID: " + items.get(i).getItemID());
        }
        
        System.out.print("Select a book (1-" + items.size() + ") or 0 to go back: ");
        int selection = getValidChoice(0, items.size());
        
        if (selection == 0) return;
        
        Item selectedItem = items.get(selection - 1);
        System.out.println("\n==== Book Details ====");
        System.out.println(selectedItem.toString());
        
        System.out.println("\n==== Book Actions ====");
        System.out.println("1. " + (selectedItem.getOwnedBy() == null ? "Check out book" : "Return book"));
        System.out.println("2. " + (selectedItem.getReservedBy().contains(currentUser.getMemberID()) ? 
                "Cancel my reservation" : "Reserve book"));
        System.out.println("3. Change location");
        System.out.println("4. Back to main menu");
        
        int actionChoice = getValidChoice(1, 4);
        Message actionMessage = null;
        
        switch (actionChoice) {
            case 1: // Check out/return
                if (selectedItem.getOwnedBy() == null) {
                    // Check out
                    actionMessage = new Message(
                        Header.ITEM,
                        Header.CHECKOUT,
                        selectedItem.getItemID() + "," + currentUser.getMemberID(),
                        "client",
                        "server",
                        "server",
                        "client"
                    );
                } else {
                    // Return
                    actionMessage = new Message(
                        Header.ITEM,
                        Header.CHECKIN,
                        selectedItem.getItemID() + "," + currentUser.getMemberID(),
                        "client",
                        "server",
                        "server",
                        "client"
                    );
                }
                break;
                
            case 2: // Reserve/Cancel reservation
                actionMessage = new Message(
                    Header.ITEM,
                    Header.RESERVE,
                    selectedItem.getItemID() + "," + currentUser.getMemberID(),
                    "client",
                    "server",
                    "server",
                    "client"
                );
                break;
                
            case 3: // Change location
                System.out.print("Enter new location name: ");
                String newLocation = scanner.nextLine().trim();
                
                actionMessage = new Message(
                    Header.INV,
                    Header.TRANSFER,
                    selectedItem.getItemID() + "," + newLocation,
                    "client",
                    "server",
                    "server",
                    "client"
                );
                break;
                
            case 4: // Back
                return;
        }
        
        if (actionMessage != null) {
            out.writeObject(actionMessage);
            out.flush();
            
            // Wait for server response
            Message response = (Message) in.readObject();
            processServerMessage(response, out, in);
        }
    }


    //Handle member search functionality
    private static void handleMemberSearch(ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        System.out.println("\n==== Search Members ====");
        System.out.print("Enter member ID: ");
        String memberId = scanner.nextLine().trim();
        
        Message searchMessage = new Message(
            Header.ACCT,
            Header.GET,
            memberId,
            "client",
            memberId,
            "server",
            "client"
        );
        
        out.writeObject(searchMessage);
        out.flush();
        
        // Wait for server response
        Message response = (Message) in.readObject();
        processServerMessage(response, out, in);
        
        if (response.getSecondaryHeader() == Header.DATA && response.getData() instanceof Member) {
            Member foundMember = (Member) response.getData();
            
            System.out.println("\n==== Member Actions ====");
            System.out.println("1. " + (foundMember.isAccountHold() ? "Remove account hold" : "Place account hold"));
            System.out.println("2. " + (foundMember instanceof StaffMember ? "Change location" : "Make staff member"));
            System.out.println("3. Back to main menu");
            
            int actionChoice = getValidChoice(1, 3);
            Message actionMessage = null;
            
            switch (actionChoice) {
                case 1: // Toggle hold
                    foundMember.setAccountHold(!foundMember.isAccountHold());
                    actionMessage = new Message(
                        Header.ACCT,
                        Header.EDIT,
                        foundMember,
                        "client",
                        "server",
                        "server",
                        "client"
                    );
                    break;
                    
                case 2: // Make staff/change location
                    if (foundMember instanceof StaffMember) {
                        System.out.print("Enter new location: ");
                        String newLocation = scanner.nextLine().trim();
                        ((StaffMember) foundMember).setLocation(newLocation);
                        
                        actionMessage = new Message(
                            Header.ACCT,
                            Header.EDIT,
                            foundMember,
                            "client",
                            "server",
                            "server",
                            "client"
                        );
                    } else {
                        actionMessage = new Message(
                            Header.ACCT,
                            Header.MAKESTAFF,
                            foundMember.getMemberID(),
                            "client",
                            "server",
                            "server",
                            "client"
                        );
                    }
                    break;
                    
                case 3: // Back
                    return;
            }
            
            if (actionMessage != null) {
                out.writeObject(actionMessage);
                out.flush();
                
                // Wait for server response
                response = (Message) in.readObject();
                processServerMessage(response, out, in);
            }
        }
    }


    //Handle adding a new book
    private static void handleAddBook(ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        System.out.println("\n==== Add New Book ====");
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        
        System.out.print("Year: ");
        String year = scanner.nextLine().trim();
        
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        
        System.out.print("Quantity: ");
        int quantity = getValidIntInput();
        
        Message addMessage = new Message(
            Header.INV,
            Header.CREATE,
            title + "," + year + "," + author + "," + quantity,
            "client",
            "server",
            "server",
            "client"
        );
        
        out.writeObject(addMessage);
        out.flush();
        
        // Wait for server response
        Message response = (Message) in.readObject();
        processServerMessage(response, out, in);
    }


    //Handle adding a new member
    private static void handleAddMember(ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        System.out.println("\n==== Add New Member ====");
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        
        Message addMessage = new Message(
            Header.ACCT,
            Header.CREATE,
            name,
            "client",
            "server",
            "server",
            "client"
        );
        
        out.writeObject(addMessage);
        out.flush();
        
        // Wait for server response
        Message response = (Message) in.readObject();
        processServerMessage(response, out, in);
    }


    //Handle location management
    private static void handleLocationManagement(ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        System.out.println("\n==== Location Management ====");
        System.out.println("1. Add new location");
        System.out.println("2. Search location");
        System.out.println("3. Back to main menu");
        
        int choice = getValidChoice(1, 3);
        
        switch (choice) {
            case 1: // Add location
                System.out.print("Enter new location name: ");
                String locationName = scanner.nextLine().trim();
                
                Message addMessage = new Message(
                    Header.LOC,
                    Header.CREATE,
                    locationName,
                    "client",
                    "server",
                    "server",
                    "client"
                );
                
                out.writeObject(addMessage);
                out.flush();
                
                // Wait for server response
                Message response = (Message) in.readObject();
                processServerMessage(response, out, in);
                break;
                
            case 2: // Search location
                System.out.print("Enter location name to search: ");
                String searchName = scanner.nextLine().trim();
                
                Message searchMessage = new Message(
                    Header.LOC,
                    Header.GET,
                    searchName,
                    "client",
                    "server",
                    "server",
                    "client"
                );
                
                out.writeObject(searchMessage);
                out.flush();
                
                // Wait for server response
                response = (Message) in.readObject();
                processServerMessage(response, out, in);
                
                if (response.getSecondaryHeader() == Header.DATA && response.getData() instanceof String) {
                    String foundLocation = (String) response.getData();
                    
                    System.out.println("\n==== Location Actions ====");
                    System.out.println("1. Add staff to location");
                    System.out.println("2. Back to main menu");
                    
                    int actionChoice = getValidChoice(1, 2);
                    
                    if (actionChoice == 1) {
                        System.out.print("Enter staff member ID to add: ");
                        String staffId = scanner.nextLine().trim();
                        
                        Message staffMessage = new Message(
                            Header.LOC,
                            Header.ADD,
                            staffId + "," + foundLocation,
                            "client",
                            "server",
                            "server",
                            "client"
                        );
                        
                        out.writeObject(staffMessage);
                        out.flush();
                        
                        // Wait for server response
                        response = (Message) in.readObject();
                        processServerMessage(response, out, in);
                    }
                }
                break;
                
            case 3: // Back
                return;
        }
    }


    //Process messages received from the server
    private static void processServerMessage(Message msg, ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        if (msg == null) {
            return;
        }
        
        // Handle different message types
        switch (msg.getPrimaryHeader()) {
            case Header.ACCT:
                handleAccountMessage(msg, out, in);
                break;
                
            case Header.INV:
                handleInventoryMessage(msg);
                break;
                
            case Header.LOC:
                handleLocationMessage(msg);
                break;
                
            case Header.NET:
                handleNetworkMessage(msg);
                break;
                
            default:
                System.out.println("Unknown message type received: " + msg.getPrimaryHeader());
        }
    }

    
    //Handle account-related messages
    private static void handleAccountMessage(Message msg, ObjectOutputStream out, ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        
        switch (msg.getSecondaryHeader()) {
            case Header.GET:
                // We received a login request
                if (currentUser == null) {
                    handleLogin(out, in);
                }
                break;
                
            case Header.DATA:
                // We received member data
                if (msg.getData() instanceof Member) {
                    Member foundMember = (Member) msg.getData();
                    System.out.println("\nMember found: " + foundMember.getName() + " (ID: " + foundMember.getMemberID() + ")");
                    System.out.println("Account status: " + 
                            (foundMember.isAccountBanned() ? "BANNED" : 
                            (foundMember.isAccountHold() ? "ON HOLD" : "ACTIVE")));
                    System.out.println("Strikes: " + foundMember.getStrikes());
                    
                    if (foundMember instanceof StaffMember) {
                        System.out.println("Staff member at location: " + ((StaffMember)foundMember).getLocation());
                    }
                } else {
                    System.out.println("Member not found.");
                }
                break;
                
            default:
                System.out.println("Unknown account action: " + msg.getSecondaryHeader());
        }
    }


    //Handle inventory-related messages
    private static void handleInventoryMessage(Message msg) {
        switch (msg.getSecondaryHeader()) {
            case Header.DATA:
                if (msg.getData() == null) {
                    System.out.println("No items found matching the search criteria.");
                } else if (msg.getData() instanceof ItemList) {
                    ItemList items = (ItemList) msg.getData();
                    if (items.getAllItems().isEmpty()) {
                        System.out.println("No items found matching the search criteria.");
                    } else {
                        System.out.println("\nFound " + items.getAllItems().size() + " items:");
                    }
                }
                break;
                
            default:
                System.out.println("Unknown inventory action: " + msg.getSecondaryHeader());
        }
    }


    //Handle location-related messages
    private static void handleLocationMessage(Message msg) {
        switch (msg.getSecondaryHeader()) {
            case Header.DATA:
                if (msg.getData() == null) {
                    System.out.println("Location not found.");
                } else if (msg.getData() instanceof String) {
                    System.out.println("Location found: " + msg.getData());
                }
                break;
                
            default:
                System.out.println("Unknown location action: " + msg.getSecondaryHeader());
        }
    }


    //Handle network-related messages
    private static void handleNetworkMessage(Message msg) {
        switch (msg.getSecondaryHeader()) {
            case Header.ACK:
                if (msg.getData() instanceof String) {
                    System.out.println("✓ " + msg.getData());
                } else if (msg.getData() instanceof Member) {
                    currentUser = (Member) msg.getData();
                    System.out.println("Login successful! Welcome, " + currentUser.getName() + "!");
                    
                    if (currentUser instanceof StaffMember) {
                        System.out.println("You are logged in as staff at location: " + 
                                ((StaffMember)currentUser).getLocation());
                    }
                }
                break;
                
            case Header.ERR:
                System.out.println("✗ Error: " + msg.getData());
                break;
                
            default:
                System.out.println("Unknown network action: " + msg.getSecondaryHeader());
        }
    }


    //Display the main menu
    private static void displayMainMenu() {
        System.out.println("\n==== Library Management System ====");
        System.out.println("User: " + currentUser.getName() + " (" + currentUser.getMemberID() + ")");
        System.out.println("1. Search for books");
        System.out.println("2. Search for members");
        System.out.println("3. Add new book");
        System.out.println("4. Add new member");
        System.out.println("5. Manage locations");
        System.out.println("6. View my account");
        System.out.println("7. Exit");
    }


    //Get a valid integer choice from the user

    private static int getValidChoice(int min, int max) {
        int choice = -1;
        boolean validInput = false;
        
        while (!validInput) {
            System.out.print("Enter your choice (" + min + "-" + max + "): ");
            
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= min && choice <= max) {
                    validInput = true;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        
        return choice;
    }

   
    //Get a valid integer input from the user
    private static int getValidIntInput() {
        int value = -1;
        boolean validInput = false;
        
        while (!validInput) {
            try {
                value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= 0) {
                    validInput = true;
                } else {
                    System.out.print("Please enter a non-negative number: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
        
        return value;
    }
}