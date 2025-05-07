package scmot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String serverAddress = "localhost"; // this is where we’re trying to connect to
    private static final int Port = 7777; // the server is expected to be listening on this port
    private static boolean isConnected = false; // just a simple flag to keep track of our connection status
    private static Member member = null;
    

    public static void main(String[] args) {
    	boolean shouldExit = false;
        try (Socket socket = new Socket(serverAddress, Port);
             ObjectOutputStream outSocket = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inSocket = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            isConnected = true; // we made it we’re connected
            System.out.println("Connected to server at " + serverAddress + ":" + Port);

            Message inboundMessage;
            while (!shouldExit) {
            	inboundMessage = (Message) inSocket.readObject();
                if(inboundMessage != null) {
                	processMessage(inboundMessage, outSocket, inSocket, scanner);
                }
                
                while (member != null && !shouldExit) {
                	displayMenu(); // show the menu
                    int choice = getValidChoice(scanner, 7); // grab user input and validate it

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
                            System.out.print("Enter member ID to search: "); // ask who to search
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
                            System.out.print("Enter location name: "); // ask for a new member’s name
                            String name = scanner.nextLine();
                            Message createMsg = new Message(
                            		Header.LOC, 
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

                        case 4: {//search a specific location
                            System.out.print("Enter location name to search: "); // ask who to search
                            String name = scanner.nextLine();
                            Message searchMsg = new Message(
                            		Header.LOC, 
                            		Header.GET, 
                            		name,
                            		"client",
                            		"server",
                            		"server",
                            		"client");
                            outSocket.writeObject(searchMsg);
                            inboundMessage = (Message) inSocket.readObject();
                            processMessage(inboundMessage, outSocket, inSocket, scanner);
                            break;
                        }

                        case 5: {//add an item
                            System.out.print("Title: "); // ask for a new item title
                            String title = scanner.nextLine();
                            System.out.print("Year: "); // ask for a new item year
                            String year = scanner.nextLine();
                            System.out.print("Author: "); // ask for a new item author
                            String author = scanner.nextLine();
                            System.out.print("Quantity: "); // ask for a new item quantity
                            String quant = scanner.nextLine();
                            Message createMsg = new Message(
                            		Header.INV, 
                            		Header.CREATE, 
                            		title + "," + year + "," + author + "," + quant,
                            		"client",
                            		"server",
                            		"server",
                            		"client");
                            outSocket.writeObject(createMsg); // send it off
                            inboundMessage = (Message) inSocket.readObject(); // wait for reply
                            processMessage(inboundMessage, outSocket, inSocket, scanner);
                            break;
                        }

                        case 6: {//search a specific item
                        	System.out.println("Search by title or ID? : "); // ask for a new item's name
                        	System.out.println("1. Title");
                        	System.out.println("2. ID");
                        	
                        	int choice1 = getValidChoice(scanner, 2);

                	        switch(choice1) {
                	        case 1:
                	        	System.out.println("Title: ");
                	        	String title = scanner.nextLine();
                                Message titleMsg = new Message(
                                		Header.INV, 
                                		Header.GET, 
                                		"title," + title,
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                outSocket.writeObject(titleMsg); // send it off
                                inboundMessage = (Message) inSocket.readObject(); // wait for reply
                                processMessage(inboundMessage, outSocket, inSocket, scanner);
                                break;
                	        case 2:
                	        	System.out.println("ID: ");
                	        	String id = scanner.nextLine();
                                Message idMsg = new Message(
                                		Header.INV, 
                                		Header.GET, 
                                		"id," + id,
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                outSocket.writeObject(idMsg); // send it off
                                inboundMessage = (Message) inSocket.readObject(); // wait for reply
                                processMessage(inboundMessage, outSocket, inSocket, scanner);
                                break;
                	        }
                	        break;
                        }
                        case 7:
                            Message discMsg = new Message(
                                    Header.NET, 
                                    Header.ACK, 
                                    "Disconnecting...",
                                    "client",
                                    "server",
                                    "server",
                                    "client");
                            outSocket.writeObject(discMsg);
                            
                            // OPTIONAL: wait for server confirmation (recommended for clean exit)
                            Message response = (Message) inSocket.readObject();
                            if (response != null && response.getSecondaryHeader() == Header.ACK) {
                                System.out.println("Server acknowledged disconnect.");
                            }

                            // Let socket shutdown cleanly
                            shouldExit = true;
                            break; // exit switch, loop ends naturally and resources are closed in try-with-resources

                        default: {
                            System.out.println("Invalid option. Try again."); // just in case somehow it’s wrong
                        }
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            isConnected = false;
            System.err.println("Exception caught:");
            e.printStackTrace(); // <-- This will show the real reason
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
                        System.out.println("Please Log in");
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
                    	break;

                    case Header.CREATE:
                        System.out.println("Account creation response received: " + msg.getData());
                        break;
                        
                    case Header.DATA:
                    	if(msg.getData() == null) {
                			System.out.println("The Member you're looking for doesn't exist");
                		}
                		else if (msg.getData() instanceof Member) {
                			Member member = (Member) msg.getData();
                			System.out.println(member.toString());
                			System.out.println("What would you like to do with this Member?");
                	        System.out.println("1. Add a Strike");
                	        Boolean held = member.getAccountHold();
                	        if(held) {
                    	        System.out.println("2. Remove Hold");
                	        }
                	        else {
                	        	System.out.println("2. Add Hold");
                	        }
                	        System.out.println("3. Make Staff Member");
                	        int choice = getValidChoice(scanner, 3);

                	        switch(choice) {
                	        case 1:
                	        		member.addStrike();
                                    Message editMsg = new Message(
                                    		Header.ACCT, 
                                    		Header.EDIT, 
                                    		member,
                                    		"client",
                                    		"server",
                                    		"server",
                                    		"client");
                                    try {
        								out.writeObject(editMsg);
        	                            Message inboundMessage = (Message) in.readObject(); // wait for reply
        	                            processMessage(inboundMessage, out, in, scanner);
        							} catch (IOException | ClassNotFoundException e) {
        								e.printStackTrace();
        							}
                	        	break;
                	        case 2:
                	        	if(held) {
                	        		member.setAccountHold("F");
                	        	}
                	        	else {
                	        		member.setAccountHold("T");
                	        	}
                                Message holdMsg = new Message(
                                		Header.ACCT, 
                                		Header.EDIT, 
                                		member,
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                try {
    								out.writeObject(holdMsg);
    								Message inboundMessage = (Message) in.readObject(); // wait for reply
    	                            processMessage(inboundMessage, out, in, scanner);
    							} catch (IOException | ClassNotFoundException e) {
    								e.printStackTrace();
    							}
                	        	break;
                	        case 3:
                	        	Message staffMsg = new Message(
                                		Header.ACCT, 
                                		Header.MAKESTAFF, 
                                		member.getUserID(),
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                try {
    								out.writeObject(staffMsg);
    								Message inboundMessage = (Message) in.readObject(); // wait for reply
    	                            processMessage(inboundMessage, out, in, scanner);
    							} catch (IOException | ClassNotFoundException e) {
    								e.printStackTrace();
    							}
                	        default:
                	        	System.out.println("Please choose a valid menu option.");
                	        	break;
                	        }
                		}
                    	break;

                    default:
                        System.out.println("Unknown account action received.");
                        break;
                }
                break;
                
            case Header.LOC:
            	switch(msg.getSecondaryHeader()) {
            	case Header.DATA:
            		System.out.println(msg.getData().toString());
            		if(msg.getData() == null) {
            			System.out.println("The Location you're looking for doesn't exist");
            		}
            		else if (msg.getData() instanceof Location) {
            			Location loc = (Location) msg.getData();
            			System.out.println(loc.toString());
            			System.out.println("What would you like to do with this Location?");
            	        System.out.println("1. Add Staff");
            	        System.out.println("2. Remove Staff");
            	        System.out.println("3. Delete Location");
            	        int choice = getValidChoice(scanner, 1);

            	        switch(choice) {
            	        case 1:
            	        	System.out.println("ID of Staff Member to add: ");
            	        	String memberID = scanner.nextLine();
                                Message staffMsg = new Message(
                                		Header.LOC, 
                                		Header.ADD, 
                                		memberID + "," + loc.getLocationName(),
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                try {
    								out.writeObject(staffMsg);
    								Message inboundMessage = (Message) in.readObject(); // wait for reply
    	                            processMessage(inboundMessage, out, in, scanner);
    							} catch (IOException | ClassNotFoundException e) {
    								e.printStackTrace();
    							}
            	        	break;
            	        default:
            	        	System.out.println("Please choose a valid menu option.");
            	        	break;
            	        }
            		}
                	break;
            	}
                
            case Header.INV:
            	//Handle inventory-related messages
            	switch(msg.getSecondaryHeader()) {
            	case Header.DATA:
            		if(msg.getData() == null) {
            			System.out.println("No inventory with that title");
            		}
            		else if (msg.getData() instanceof ItemList) {
            			Item[] items = ((ItemList) msg.getData()).getIArray();
            			System.out.println("Search Results: ");
            			for(int i = 0; i < items.length; i++) {
            				System.out.println((i + 1) + ". " + items[i].getID() + " - " + items[i].getTitle());
            			}
            			
            			System.out.println("Choose an Item: ");
            			int choice = getValidChoice(scanner, items.length);

            	        Item item = items[choice - 1];
            	        	
            			boolean checkedOut = item.isCheckedOut();
            			System.out.println(item.toString());
            			System.out.println("What would you like to do with this item?");
            			if(checkedOut) {
            				System.out.println("1. Check in Item");
            			}
            			else {
            				System.out.println("1. Check out Item");
            			}
            	        System.out.println("2. Reserve Item");
            	        System.out.println("3. Remove Reservation");
            	        System.out.println("4. Change Location");
            	        choice = getValidChoice(scanner, 4);

            	        String memberID = "";
            	        switch(choice) {
            	        case 1:
            	        	if(checkedOut) {
            	        		System.out.println("ID of Member to check Item in from: ");
            	        		memberID = scanner.nextLine();
                                Message checkInMsg = new Message(
                                		Header.ITEM, 
                                		Header.CHECKIN, 
                                		item.getID() + "," + memberID,
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                try {
    								out.writeObject(checkInMsg);
    								Message inboundMessage = (Message) in.readObject(); // wait for reply
    	                            processMessage(inboundMessage, out, in, scanner);
    							} catch (IOException | ClassNotFoundException e) {
    								e.printStackTrace();
    							}
            	        	}
            	        	else {
            	        		System.out.println("ID of Member to check Item out for: ");
            	        		memberID = scanner.nextLine();
                                Message checkOutMsg = new Message(
                                		Header.ITEM, 
                                		Header.CHECKOUT, 
                                		item.getID() + "," + memberID,
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                try {
    								out.writeObject(checkOutMsg);
    								Message inboundMessage = (Message) in.readObject(); // wait for reply
    	                            processMessage(inboundMessage, out, in, scanner);
    							} catch (IOException | ClassNotFoundException e) {
    								e.printStackTrace();
    							}
            	        		
            	        	}
            	        	break;
            	        case 2:
            	        	System.out.println("ID of Member to reserve item for: ");
        	        		memberID = scanner.nextLine();
            	        	if(checkedOut) {
                                Message reserveMsg = new Message(
                                		Header.ITEM, 
                                		Header.RESERVE, 
                                		item.getID() + "," + memberID,
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                try {
    								out.writeObject(reserveMsg);
    								Message inboundMessage = (Message) in.readObject(); // wait for reply
    	                            processMessage(inboundMessage, out, in, scanner);
    							} catch (IOException | ClassNotFoundException e) {
    								e.printStackTrace();
    							}
            	        	}
            	        	break;
            	        case 3:
            	        	System.out.println("ID of Member to remove reservation from: ");
        	        		memberID = scanner.nextLine();
            	        	if(checkedOut) {
                                Message reserveMsg = new Message(
                                		Header.ITEM, 
                                		Header.RESERVE, 
                                		item.getID() + "," + memberID,
                                		"client",
                                		"server",
                                		"server",
                                		"client");
                                try {
    								out.writeObject(reserveMsg);
    								Message inboundMessage = (Message) in.readObject(); // wait for reply
    	                            processMessage(inboundMessage, out, in, scanner);
    							} catch (IOException | ClassNotFoundException e) {
    								e.printStackTrace();
    							}
            	        	}
            	        	break;
            	        case 4:
            	        	System.out.println("Name of Location to add Item to: ");
        	        		String name = scanner.nextLine();
        	        		Message reserveMsg = new Message(
                            		Header.INV, 
                            		Header.TRANSFER, 
                            		item.getID() + "," + name,
                            		"client",
                            		"server",
                            		"server",
                            		"client");
                            try {
								out.writeObject(reserveMsg);
								Message inboundMessage = (Message) in.readObject(); // wait for reply
	                            processMessage(inboundMessage, out, in, scanner);
							} catch (IOException | ClassNotFoundException e) {
								e.printStackTrace();
							}
            	        break;

            	        default:
            	        	System.out.println("Please choose a valid menu option.");
            	        	break;
            	        }
            		}
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
                        else if (msg.getData() instanceof StaffMember) {
                            member = (StaffMember) msg.getData();
                            System.out.println("Login successful. Welcome, " + member.getName() + "!");
                        }
                        break;

                    case Header.ERR:
                        System.out.println("ERR: " + msg.getData());
                        break;

                    default:
                        System.out.println("Unknown network action received. " + msg.getPrimaryHeader() + " " + msg.getSecondaryHeader());
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
        System.out.println("7. Exit Program");
    }

    private static int getValidChoice(Scanner scanner, int limit) {
        int choice = -1; // start invalid
        while (true) {
            System.out.print("Enter choice (1-" + limit + "): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // eat newline so next input is clean
                if (choice >= 1 && choice <= limit) {
                    break; // we got a good number
                } else {
                    System.out.println("Invalid option. Please choose between 1 and " + limit + ".");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // toss the bad input
            }
        }
        return choice;
    }
}