package libraryMember;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;


public class Client {
	private static String serverAddress = "localhost";
	private static int port = 7777;
	
	public static void main(String[] args) {
		//establish connection with host address and port number
		try (Socket socket = new Socket(serverAddress, port)) {
			//create a scanner object
			Scanner scanner = new Scanner(System.in);
			//create output stream socket
			ObjectOutputStream outSocket = new ObjectOutputStream(socket.getOutputStream());
			//create input stream socket
			ObjectInputStream inSocket = new ObjectInputStream(socket.getInputStream());
			
			//create our login message
			Message loginMessage = new Message(HeaderEnum.login, CommandEnum.login, StatusEnum.toServer, "login request");
			//send off login message through outSocket to server
			outSocket.writeObject(loginMessage);
		
			//wait to receive login response 'success'
			Message inboundMessage = (Message) inSocket.readObject();
			
			//check for login failure and end the program
			if (!inboundMessage.getStatus().equals(StatusEnum.success)) {
				System.out.println("Login Failure");
				scanner.close();
				return;
			}
			
			//print login success message
			System.out.println(inboundMessage.getText());
			
			 boolean loggedIn = true;
	            while (loggedIn) {
	                System.out.println("\nPlease select an option:");
	                System.out.println("1. Create a new Member");
	                System.out.println("2. Delete a Member");
	                System.out.println("3. Check Member standing");
	                System.out.println("4. View Member's rented items");
	                System.out.println("5. Check Member's waitlisted items");
	                System.out.println("6. Logout");
	                
	                int choice = 0;
	                while (true) {
	                    System.out.print("Enter choice (1-6): ");
	                    if (scanner.hasNextInt()) {
	                        choice = scanner.nextInt();
	                        scanner.nextLine(); // consume newline
	                        if (choice >= 1 && choice <= 6) {
	                            break;
	                        } else {
	                            System.out.println("Invalid selection, number must be between 1 and 6.");
	                        }
	                    } else {
	                        System.out.println("Invalid input. Please enter a number.");
	                        scanner.next(); // consume invalid input
	                    }
	                }
	                
	                switch (choice) {
	                    case 1: // Create new member
	                        System.out.print("Enter member name: ");
	                        String name = scanner.nextLine();
	                        Message createMsg = new Message(HeaderEnum.member, CommandEnum.add, StatusEnum.toServer, name);
	                        outSocket.writeObject(createMsg);
	                        inboundMessage = (Message) inSocket.readObject();
	                        System.out.println(inboundMessage.getText());
	                        break;
	                        
	                    case 2: // Delete member
	                        System.out.print("Enter member ID to delete: ");
	                        String memberId = scanner.nextLine();
	                        Message deleteMsg = new Message(HeaderEnum.member, CommandEnum.remove, StatusEnum.toServer, memberId);
	                        outSocket.writeObject(deleteMsg);
	                        inboundMessage = (Message) inSocket.readObject();
	                        System.out.println(inboundMessage.getText());
	                        break;
	                        
	                    case 3: // Check standing
	                        System.out.print("Enter member ID to check standing: ");
	                        memberId = scanner.nextLine();
	                        Message standingMsg = new Message(HeaderEnum.member, CommandEnum.getStanding, StatusEnum.toServer, memberId);
	                        outSocket.writeObject(standingMsg);
	                        inboundMessage = (Message) inSocket.readObject();
	                        System.out.println(inboundMessage.getText());
	                        break;
	                        
	                    case 4: // View rented items
	                        System.out.print("Enter member ID to view rented items: ");
	                        memberId = scanner.nextLine();
	                        Message rentedMsg = new Message(HeaderEnum.member, CommandEnum.viewItems, StatusEnum.toServer, memberId);
	                        outSocket.writeObject(rentedMsg);
	                        inboundMessage = (Message) inSocket.readObject();
	                        System.out.println(inboundMessage.getText());
	                        break;
	                        
	                    case 5: // View waitlisted items
	                        System.out.print("Enter member ID to view waitlisted items: ");
	                        memberId = scanner.nextLine();
	                        Message waitlistMsg = new Message(HeaderEnum.member, CommandEnum.getStanding, StatusEnum.toServer, memberId);
	                        outSocket.writeObject(waitlistMsg);
	                        inboundMessage = (Message) inSocket.readObject();
	                        System.out.println(inboundMessage.getText());
	                        break;
	                        
	                    case 6: // Logout
	                        Message logoutMsg = new Message(HeaderEnum.login, CommandEnum.logout, StatusEnum.toServer, "logout request");
	                        outSocket.writeObject(logoutMsg);
	                        inboundMessage = (Message) inSocket.readObject();
	                        System.out.println(inboundMessage.getText());
	                        loggedIn = false;
	                        break;
	                }
	            }
	            
	            scanner.close();
	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        }
	        System.exit(0);
	    }
}
