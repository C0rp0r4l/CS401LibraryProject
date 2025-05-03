package scmot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    private static final String serverAddress = "localhost"; // this is where we’re trying to connect to
    private static final int Port = 7777; // the server is expected to be listening on this port
    private static boolean isConnected = false; // just a simple flag to keep track of our connection status
    private static Member member = null;

    public static void main(String[] args) {
        try (Socket socket = new Socket(serverAddress, Port);
             ObjectOutputStream outSocket = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inSocket = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            isConnected = true; // we made it we’re connected
            System.out.println("Connected to server at " + serverAddress + ":" + Port);

            Message inboundMessage;
            while (true) {
            	inboundMessage = (Message) inSocket.readObject();
                if(inboundMessage != null) {
                	processMessage(inboundMessage, outSocket, inSocket, scanner);
                }
                
                while (member != null) {
                	displayMenu(); // show the menu
                    int choice = getValidChoice(scanner); // grab user input and validate it

                    switch (choice) {
                        case 1: { //add member
                            System.out.print("Enter member name: "); // ask for a new member’s name
                            String name = scanner.nextLine();
                            Message createMsg = new Message(
                            		Header.ACCT, 
                            		Header.CREATE, 
                            		name,
                            		"client",
                            		"server",
                            		"server",
                            		"client");
                            outSocket.writeObject(createMsg); // send it off
                            inboundMessage = (Message) inSocket.readObject(); // wait for reply
                            processMessage(inboundMessage, outSocket, inSocket, scanner);
                            break;
                        }

                        case 2: {//search member
                            System.out.print("Enter member ID to search: "); // ask who to remove
                            String memberId = scanner.nextLine();
                            Message searchMsg = new Message(
                            		Header.ACCT, 
                            		Header.GET, 
                            		memberId,
                            		"client",
                            		memberId,
                            		"server",
                            		"client");
                            outSocket.writeObject(searchMsg);
                            inboundMessage = (Message) inSocket.readObject();
                            processMessage(inboundMessage, outSocket, inSocket, scanner);
                            break;
                        }

                        case 3: {//add location
                            System.out.print("Not Yet Implemented");
                            break;
                        }

                        case 4: {//search a specific location
                            System.out.print("Not Yet Implemented");
                            break;
                        }

                        case 5: {//add an item
                            System.out.print("Not Yet Implemented");
                            break;
                        }

                        case 6: {//search a specific item
                            System.out.print("Not Yet Implemented");
                            break;
                        }

                        default: {
                            System.out.println("Invalid option. Try again."); // just in case somehow it’s wrong
                        }
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            isConnected = false; // we lost connection or had some issue
            System.err.println("Error: " + e.getMessage());
        } finally {
            System.out.println("Client shutting down."); // always say goodbye
            System.exit(0); // peace out
        }
    }
    
    private static void processMessage(Message msg, ObjectOutputStream out, ObjectInputStream in, Scanner scanner) {
        switch (msg.getPrimaryHeader()) {
            case Header.ACCT:
                // Handle account-related messages
                switch (msg.getSecondaryHeader()) {
                    case Header.GET:
                    	while (member == null) {
                        System.out.println("Would you like to... \n1. Log in\n2. Sign up");
                        String continueChoice = scanner.nextLine().trim();
                        if ("1".equalsIgnoreCase(continueChoice)) {
                        	System.out.println("Username: ");
                            String u = scanner.nextLine();
                            System.out.println("Password: ");
                            String p = scanner.nextLine();

                            Message loginMsg = new Message(
                                Header.ACCT, 
                                Header.LOGIN, 
                                u + "," + p,  // Send user and password in the format "user,pass"
                                "client",
                                "login to existing user",
                                "server",
                                "client");

                            try {
                                // Send login message to server
                                out.writeObject(loginMsg);
                                out.flush();  // Ensure it's sent immediately
                                System.out.println("Login request sent. Waiting for server response...");

                                // Wait for server's acknowledgement message
                                Message inboundMessage = (Message) in.readObject(); // Block until a response is received
	                            processMessage(inboundMessage, out, in, scanner);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        }
                        else if ("2".equalsIgnoreCase(continueChoice)) {
                        	System.out.println("Name: ");
                        	String name = scanner.nextLine();
                        	
                            Message createMsg = new Message(
                            		Header.ACCT, 
                            		Header.CREATE, 
                            		name,
                            		"client",
                            		"new user",
                            		"server",
                            		"client");

                            try {
								out.writeObject(createMsg);
							} catch (IOException e) {
								e.printStackTrace();
							}
                        }
                    	}
                    	break;

                    case Header.CREATE:
                        System.out.println("Account creation response received: " + msg.getData());
                        break;
                        
                    case Header.DATA:
                    	System.out.println(msg.getData().toString());
                    	break;

                    default:
                        System.out.println("Unknown account action received.");
                        break;
                }
                break;

            case Header.NET:
                // Handle network-related messages (e.g., ACK, ERR)
                switch (msg.getSecondaryHeader()) {
                    case Header.ACK:
                    	System.out.println("Ack msg recieved");
                        if (msg.getData() instanceof String) {
                            System.out.println(msg.getData().toString());
                        }
                        else if (msg.getData() instanceof Member) {
                            member = (Member) msg.getData();
                            System.out.println("Login successful. Welcome, " + member.getName() + "!");
                        }
                        break;

                    case Header.ERR:
                        System.out.println("ERR: " + msg.getData());
                        break;

                    default:
                        System.out.println("Unknown network action received.");
                        break;
                }
                break;

            default:
                System.out.println("Unknown primary header received.");
                break;
        }
        return;
    }

    private static void displayMenu() {
        System.out.println("\nLibrary Member System - Select an option:");
        System.out.println("1. Add Member");
        System.out.println("2. Search Member");
        System.out.println("3. Add Location");
        System.out.println("4. Search Location");
        System.out.println("5. Add Item");
        System.out.println("6. Search Item");
    }

    private static int getValidChoice(Scanner scanner) {
        int choice = -1; // start invalid
        while (true) {
            System.out.print("Enter choice (1-6): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // eat newline so next input is clean
                if (choice >= 1 && choice <= 6) {
                    break; // we got a good number
                } else {
                    System.out.println("Invalid option. Please choose between 1 and 6.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // toss the bad input
            }
        }
        return choice;
    }
}