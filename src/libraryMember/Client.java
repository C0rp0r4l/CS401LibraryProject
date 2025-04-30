package libraryMember;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String serverAddress = "localhost"; // this is where we’re trying to connect to
    private static final int Port = 7777; // the server is expected to be listening on this port
    private static boolean isConnected = false; // just a simple flag to keep track of our connection status

    public static void main(String[] args) {
        try (Socket socket = new Socket(serverAddress, Port);
             ObjectOutputStream outSocket = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inSocket = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            isConnected = true; // we made it we’re connected
            System.out.println("Connected to server at " + serverAddress + ":" + Port);

            // we send a login message as soon as the client starts
            Message loginMessage = new Message(HeaderEnum.login, CommandEnum.login, StatusEnum.toServer, "login request");
            outSocket.writeObject(loginMessage);

            // now we wait to see if the server says we’re good to go
            Message inboundMessage = (Message) inSocket.readObject();
            if (!inboundMessage.getStatus().equals(StatusEnum.success)) {
                System.out.println("Login Failure: " + inboundMessage.getText()); // nope didn’t work
                isConnected = false;
                return;
            }

            System.out.println("Login Successful: " + inboundMessage.getText()); // nice we’re in

            boolean loggedIn = true; // we stay in this loop until the user logs out
            while (loggedIn) {
                displayMenu(); // show the menu
                int choice = getValidChoice(scanner); // grab user input and validate it

                switch (choice) {
                    case 1: {
                        System.out.print("Enter member name: "); // ask for a new member’s name
                        String name = scanner.nextLine();
                        Message createMsg = new Message(HeaderEnum.member, CommandEnum.add, StatusEnum.toServer, name);
                        outSocket.writeObject(createMsg); // send it off
                        inboundMessage = (Message) inSocket.readObject(); // wait for reply
                        System.out.println(inboundMessage.getText()); // show result
                        break;
                    }

                    case 2: {
                        System.out.print("Enter member ID to delete: "); // ask who to remove
                        String memberId = scanner.nextLine();
                        Message deleteMsg = new Message(HeaderEnum.member, CommandEnum.remove, StatusEnum.toServer, memberId);
                        outSocket.writeObject(deleteMsg);
                        inboundMessage = (Message) inSocket.readObject();
                        System.out.println(inboundMessage.getText());
                        break;
                    }

                    case 3: {
                        System.out.print("Enter member ID to check standing: "); // check if a member is in good standing
                        String memberId = scanner.nextLine();
                        Message standingMsg = new Message(HeaderEnum.member, CommandEnum.getStanding, StatusEnum.toServer, memberId);
                        outSocket.writeObject(standingMsg);
                        inboundMessage = (Message) inSocket.readObject();
                        System.out.println(inboundMessage.getText());
                        break;
                    }

                    case 4: {
                        System.out.print("Enter member ID to view rented items: "); // what’s currently rented
                        String memberId = scanner.nextLine();
                        Message rentedMsg = new Message(HeaderEnum.member, CommandEnum.viewItems, StatusEnum.toServer, memberId);
                        outSocket.writeObject(rentedMsg);
                        inboundMessage = (Message) inSocket.readObject();
                        System.out.println(inboundMessage.getText());
                        break;
                    }

                    case 5: {
                        System.out.print("Enter member ID to view waitlisted items: "); // any items the member is waiting for
                        String memberId = scanner.nextLine();
                        Message waitlistMsg = new Message(HeaderEnum.member, CommandEnum.getStanding, StatusEnum.toServer, memberId);
                        outSocket.writeObject(waitlistMsg);
                        inboundMessage = (Message) inSocket.readObject();
                        System.out.println(inboundMessage.getText());
                        break;
                    }

                    case 6: {
                        Message logoutMsg = new Message(HeaderEnum.login, CommandEnum.logout, StatusEnum.toServer, "logout request");
                        outSocket.writeObject(logoutMsg); // tell server we’re leaving
                        inboundMessage = (Message) inSocket.readObject();
                        System.out.println(inboundMessage.getText());
                        loggedIn = false; // end loop
                        isConnected = false;
                        break;
                    }

                    default: {
                        System.out.println("Invalid option. Try again."); // just in case somehow it’s wrong
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

    private static void displayMenu() {
        System.out.println("\nLibrary Member System - Select an option:");
        System.out.println("1. Create a new Member");
        System.out.println("2. Delete a Member");
        System.out.println("3. Check Member standing");
        System.out.println("4. View Member's rented items");
        System.out.println("5. View Member's waitlisted items");
        System.out.println("6. Logout");
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
