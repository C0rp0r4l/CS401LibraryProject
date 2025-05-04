package libraryMember;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String serverAddress = "localhost";
    private static final int Port = 7777;
    private static boolean isConnected = false;
    private static Member member = null;

    public static void main(String[] args) {
        try (Socket socket = new Socket(serverAddress, Port);
             ObjectOutputStream outSocket = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inSocket = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            isConnected = true;
            System.out.println("Connected to server at " + serverAddress + ":" + Port);

            Message inboundMessage = (Message) inSocket.readObject();
            processMessage(inboundMessage, outSocket, inSocket, scanner);

            while (member == null) {
                System.out.println("Would you like to... \n1. Log in\n2. Sign up");
                String choice = scanner.nextLine().trim();

                if ("1".equals(choice)) {
                    System.out.print("Username: ");
                    String u = scanner.nextLine();
                    System.out.print("Password: ");
                    String p = scanner.nextLine();

                    Message loginMsg = new Message(
                            Header.ACCT, Header.LOGIN, u + "," + p,
                            "client", "login", "server", "client"
                    );
                    outSocket.writeObject(loginMsg);
                    outSocket.flush();
                    inboundMessage = (Message) inSocket.readObject();
                    processMessage(inboundMessage, outSocket, inSocket, scanner);

                } else if ("2".equals(choice)) {
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    Message createMsg = new Message(
                            Header.ACCT, Header.CREATE, name,
                            "client", "new", "server", "client"
                    );
                    outSocket.writeObject(createMsg);
                    outSocket.flush();
                    inboundMessage = (Message) inSocket.readObject();
                    processMessage(inboundMessage, outSocket, inSocket, scanner);
                }
            }

            // Now you're logged in
            while (member != null) {
                displayMenu();
                int choice = getValidChoice(scanner, 6);

                switch (choice) {
                    case 1:
                        System.out.print("Enter member name: ");
                        String name = scanner.nextLine();
                        Message addMsg = new Message(
                                Header.ACCT, Header.CREATE, name,
                                "client", "server", "server", "client"
                        );
                        outSocket.writeObject(addMsg);
                        processMessage((Message) inSocket.readObject(), outSocket, inSocket, scanner);
                        break;

                    case 2:
                        System.out.print("Enter member ID to search: ");
                        String id = scanner.nextLine();
                        Message getMsg = new Message(
                                Header.ACCT, Header.GET, id,
                                "client", id, "server", "client"
                        );
                        outSocket.writeObject(getMsg);
                        processMessage((Message) inSocket.readObject(), outSocket, inSocket, scanner);
                        break;

                    case 3:
                    case 4:
                        System.out.println("Not Yet Implemented");
                        break;

                    case 5:
                        System.out.print("Title: ");
                        String title = scanner.nextLine();
                        System.out.print("Year: ");
                        String year = scanner.nextLine();
                        System.out.print("Author: ");
                        String author = scanner.nextLine();
                        System.out.print("Quantity: ");
                        String quantity = scanner.nextLine();
                        Message itemMsg = new Message(
                                Header.INV, Header.CREATE, title + "," + year + "," + author + "," + quantity,
                                "client", "server", "server", "client"
                        );
                        outSocket.writeObject(itemMsg);
                        Object response = inSocket.readObject();
                        if (response instanceof Message) {
                            processMessage((Message) response, outSocket, inSocket, scanner);
                        } else {
                            System.out.println("Server sent null or invalid response.");
                        }
                        break;

                    case 6:
                        System.out.print("Title of item: ");
                        String search = scanner.nextLine();
                        Message searchItemMsg = new Message(
                                Header.INV, Header.GET, search,
                                "client", "server", "server", "client"
                        );
                        outSocket.writeObject(searchItemMsg);
                        Object obj = inSocket.readObject();
                        if (obj instanceof Message msgResponse) {
                            processMessage(msgResponse, outSocket, inSocket, scanner);
                        } else {
                            System.out.println("Invalid response from server.");
                        }
                        break;

                    default:
                        System.out.println("Invalid option.");
                }
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static void processMessage(Message msg, ObjectOutputStream out, ObjectInputStream in, Scanner scanner) {
        switch (msg.getPrimaryHeader()) {
            case NET:
                if (Header.ACK.equals(msg.getSecondaryHeader())) {
                    if (msg.getData() instanceof Member m) {
                        member = m;
                        System.out.println("Login successful. Welcome, " + m.getName() + "!");
                    } else {
                        System.out.println("ACK: " + msg.getData());
                    }
                } else if (Header.ERR.equals(msg.getSecondaryHeader())) {
                    System.out.println("Error: " + msg.getData());
                }
                break;

            case ACCT:
                if (Header.CREATE.equals(msg.getSecondaryHeader())) {
                    System.out.println("Account created: " + msg.getData());
                } else if (Header.DATA.equals(msg.getSecondaryHeader())) {
                    System.out.println("Account info: " + msg.getData());
                }
                break;

            case INV:
                if (Header.DATA.equals(msg.getSecondaryHeader())) {
                    System.out.println("Inventory: " + msg.getData());
                }
                break;

            default:
                System.out.println("Unknown response.");
        }
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

    private static int getValidChoice(Scanner scanner, int limit) {
        while (true) {
            System.out.print("Enter choice (1-" + limit + "): ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); 
                if (choice >= 1 && choice <= limit) return choice;
            } else {
                scanner.next(); // toss invalid input
            }
            System.out.println("Invalid option.");
        }
    }
}