package libraryMember;

import java.io.*;
import java.net.*;
import java.util.List;

public class Server {
    
    private static MemberList memberList = new MemberList();
    private static ItemList itemList = new ItemList();
    private static Location locationList = new Location();
    
    public static void main(String[] args) {
        ServerSocket server = null;

        try {
            System.out.println("Starting server");
            server = new ServerSocket(7777);
            server.setReuseAddress(true);
            System.out.println("Server socket opened on port 7777");

            try {
                locationList.load();
                System.out.println("Location list loaded successfully");
            } catch (Exception e) {
                System.err.println("Failed to load location list:");
                e.printStackTrace();
            }

            try {
                itemList.loadItems();
                System.out.println("Item list loaded successfully");
            } catch (Exception e) {
                System.err.println("Failed to load item list:");
                e.printStackTrace();
            }

            try {
                memberList.loadAll();
                System.out.println("Member list loaded successfully");
            } catch (Exception e) {
                System.err.println("Failed to load member list:");
                e.printStackTrace();
            }

            System.out.println("System fully initialized. Waiting for clients to connect to the server!");

            // Running infinite loop for getting client requests
            while (true) {
                // Socket object to receive incoming client requests
                Socket client = server.accept();
                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

                // Create a new thread object to handle the client separately
                ClientHandler clientSock = new ClientHandler(client, memberList);

                // Start the client handler thread
                new Thread(clientSock).start();
            }

        } catch (IOException e) {
            System.err.println("Server crashed with IOException:");
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    System.err.println("Failed to close server socket:");
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final MemberList memberList;

        public ClientHandler(Socket socket, MemberList memberList) {
            this.clientSocket = socket;
            this.memberList = memberList;
        }

        public void run() {
            ObjectOutputStream out = null;
            ObjectInputStream in = null;

            try {
                // Get the output stream of the client
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();  // Ensure the output stream is flushed

                // Get the input stream of the client
                in = new ObjectInputStream(clientSocket.getInputStream());

                // Send a message requesting login information from the client
                Message requestLoginMessage = new Message(
                    Header.ACCT,
                    Header.GET,
                    "Please provide login credentials",
                    "server",
                    "client",
                    "client",
                    "server"
                );
                out.writeObject(requestLoginMessage);
                out.flush();
                System.out.println("Sent login request to client.");
                
                while (true) {
                    if (clientSocket.getInputStream().available() > 0) {
                        Object obj = in.readObject();
                        if (!(obj instanceof Message)) {
                            System.out.println("Received unknown object type");
                            continue;
                        }
                        
                        Message msg = (Message) obj;
                        System.out.println("Received Message " + msg.getPrimaryHeader());
                        Message response;

                        // Process the message based on its primary header
                        switch (msg.getPrimaryHeader()) {
                            case Header.INV:
                                response = handleInventoryActions(msg);
                                break;
                            case Header.ACCT:
                                response = handleAccountActions(msg);
                                break;
                            case Header.LOC:
                                response = handleLocationActions(msg);
                                break;
                            case Header.ITEM:
                                response = handleItemActions(msg);
                                break;
                            default:
                                System.out.println("Unknown primary header received.");
                                response = new Message(Header.NET, Header.ERR, "Unknown request", "server", "client", "client", "server");
                        }
                        
                        out.writeObject(response);
                        out.flush();
                    } else {
                        try {
                            Thread.sleep(100); // reduce CPU usage
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Client handler encountered an error:");
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    clientSocket.close();
                    System.out.println("Client disconnected.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private Message handleInventoryActions(Message msg) {
            Message response = null;
            
            switch(msg.getSecondaryHeader()) {
                case Header.CREATE:
                    String[] parts = ((String) msg.getData()).split(",");
                    String title = parts[0];             // Title
                    String year = parts[1];              // Year
                    String author = parts[2];            // Author
                    int quantity = Integer.parseInt(parts[3]); // Quantity
                    
                    for(int i = 0; i < quantity; i++) {
                        itemList.addItem(title, year, author, "Library");
                    }
                    
                    itemList.saveItems();
                    response = new Message(
                        Header.INV, 
                        Header.ACK, 
                        "Items created", 
                        "server", 
                        "client", 
                        "client", 
                        "server");
                    break;
                    
                case Header.GET:
                    ItemList resultList = new ItemList(false);
                    String[] searchParams = ((String) msg.getData()).split(",");
                    String searchType = searchParams[0];
                    String searchValue = searchParams[1];
                    
                    if(searchType.equals("title")) {
                        List<Item> foundItems = itemList.searchByTitle(searchValue);
                        for (Item item : foundItems) {
                            resultList.add(item);
                        }
                    } else if(searchType.equals("id")) {
                        Item found = itemList.getItem(searchValue);
                        if (found != null) {
                            resultList.add(found);
                        }
                    }
                    
                    response = new Message(
                        Header.INV, 
                        Header.DATA, 
                        resultList, 
                        "server", 
                        "client", 
                        "client", 
                        "server");
                    break;
                    
                case Header.TRANSFER:
                    String[] transferParams = ((String) msg.getData()).split(",");
                    String itemId = transferParams[0];
                    String newLocation = transferParams[1];
                    
                    Item item = itemList.getItem(itemId);
                    if (item != null) {
                        item.setLocation(newLocation);
                        itemList.saveItems();
                        response = new Message(
                            Header.NET, 
                            Header.ACK, 
                            "Item transferred to " + newLocation, 
                            "server", 
                            "client", 
                            "client", 
                            "server");
                    } else {
                        response = new Message(
                            Header.NET, 
                            Header.ERR, 
                            "Item not found", 
                            "server", 
                            "client", 
                            "client", 
                            "server");
                    }
                    break;
                    
                default:
                    response = new Message(
                        Header.NET, 
                        Header.ERR, 
                        "Unknown inventory action", 
                        "server", 
                        "client", 
                        "client", 
                        "server");
            }
            
            return response;
        }

        private Message handleAccountActions(Message msg) {
            switch (msg.getSecondaryHeader()) {
                case Header.LOGIN:
                    String[] creds = msg.getData().toString().split(",");
                    if (creds.length == 2) {
                        Member result = memberList.authenticate(creds[0], creds[1]);
                        return result != null
                            ? new Message(Header.NET, Header.ACK, result, "server", "client", "client", "server")
                            : new Message(Header.NET, Header.ERR, "Invalid credentials", "server", "client", "client", "server");
                    } else {
                        return new Message(Header.NET, Header.ERR, "Malformed login", "server", "client", "client", "server");
                    }

                case Header.GET:
                	System.out.println(msg.getData().toString());
                    Member found = memberList.getMemberByID(msg.getData().toString());
                    return found != null
                        ? new Message(Header.ACCT, Header.DATA, found, "server", "client", "client", "server")
                        : new Message(Header.NET, Header.ERR, "User not found", "server", "client", "client", "server");

                case Header.CREATE:
                    String name = msg.getData().toString().trim();
                    String newId = memberList.addMember(name);
                    return new Message(
                        Header.NET, 
                        Header.ACK, 
                        "New member added with ID: " + newId,
                        "server",
                        "client",
                        "client",
                        "server"
                    );
                    
                case Header.EDIT:
                    Member edited = (Member) msg.getData();
                    if (edited instanceof StaffMember) {
                        StaffMember editedStaff = (StaffMember) edited;
                        memberList.removeStaffMember(editedStaff.getMemberID());
                        memberList.getAllStaffMembers().add(editedStaff);
                    } else {
                        memberList.removeMember(edited.getMemberID());
                        memberList.getAllMembers().add(edited);
                    }
                    memberList.saveAll();
                    return new Message(Header.NET, Header.ACK, "Account updated", "server", "client", "client", "server");
                
                case Header.MAKESTAFF:
                    Member member = memberList.getMemberByID(msg.getData().toString());
                    if (member != null) {
                        StaffMember newStaff = new StaffMember(
                            member.getMemberID(),
                            member.getName(),
                            member.getStrikes(),
                            member.isAccountHold(),
                            member.isAccountBanned(),
                            "Main Library",
                            StaffMember.generatePassword()
                        );
                        
                        memberList.removeMember(member.getMemberID());
                        memberList.getAllStaffMembers().add(newStaff);
                        memberList.saveAll();
                        
                        return new Message(
                            Header.NET, 
                            Header.ACK, 
                            member.getMemberID() + " is now staff.",
                            "server",
                            "client",
                            "client",
                            "server"
                        );
                    } else {
                        return new Message(
                            Header.NET, 
                            Header.ERR, 
                            "Member not found",
                            "server",
                            "client",
                            "client",
                            "server"
                        );
                    }
                
                default:
                    return new Message(Header.NET, Header.ERR, "Unknown account action", "server", "client", "client", "server");
            }
        }

        private Message handleLocationActions(Message msg) {
            switch (msg.getSecondaryHeader()) {
                case Header.CREATE:
                    boolean added = locationList.addLocation(msg.getData().toString());
                    locationList.save();
                    return new Message(Header.NET, added ? Header.ACK : Header.ERR,
                        added ? "Location added" : "Location exists", "server", "client", "client", "server");

                case Header.GET:
                    String search = msg.getData().toString().trim();
                    return locationList.contains(search)
                        ? new Message(Header.LOC, Header.DATA, search, "server", "client", "client", "server")
                        : new Message(Header.LOC, Header.DATA, null, "server", "client", "client", "server");
                        
                case Header.ADD:
                    String[] addParams = msg.getData().toString().split(",");
                    if (addParams.length == 2) {
                        String staffId = addParams[0];
                        String locationName = addParams[1];
                        
                        StaffMember staff = memberList.getStaffMember(staffId);
                        if (staff != null && locationList.contains(locationName)) {
                            staff.setLocation(locationName);
                            memberList.saveAll();
                            return new Message(
                                Header.NET, 
                                Header.ACK, 
                                "Staff assigned to location", 
                                "server", 
                                "client", 
                                "client", 
                                "server");
                        } else {
                            return new Message(
                                Header.NET, 
                                Header.ERR, 
                                "Staff or location not found", 
                                "server", 
                                "client", 
                                "client", 
                                "server");
                        }
                    } else {
                        return new Message(
                            Header.NET, 
                            Header.ERR, 
                            "Invalid parameters for ADD location", 
                            "server", 
                            "client", 
                            "client", 
                            "server");
                    }

                default:
                    return new Message(Header.NET, Header.ERR, "Unknown location action", "server", "client", "client", "server");
            }
        }

        private Message handleItemActions(Message msg) {
            try {
                String[] parts = msg.getData().toString().split(",");
                switch (msg.getSecondaryHeader()) {
                    case Header.CHECKOUT:
                        String itemId = parts[0].trim();
                        String memberId = parts[1].trim();
                        
                        if (memberList.getMemberByID(memberId) != null) {
                            boolean success = itemList.checkoutItem(itemId, memberId);
                            return new Message(
                                Header.ITEM,
                                success ? Header.DATA : Header.ERR,
                                success ? itemList : "Checkout failed",
                                "server", "client", "client", "server");
                        } else {
                            return new Message(
                                Header.NET,
                                Header.ERR,
                                memberId + " is not an existing Member.",
                                "server", "client", "client", "server");
                        }

                    case Header.CHECKIN:
                        boolean returnSuccess = itemList.returnItem(parts[0].trim());
                        return new Message(
                            Header.NET,
                            returnSuccess ? Header.ACK : Header.ERR,
                            returnSuccess ? parts[1].trim() + " checked in " + parts[0].trim() : "Checkin failed",
                            "server", "client", "client", "server");

                    case Header.RESERVE:
                        boolean reserveSuccess = itemList.reserveItem(parts[0].trim(), parts[1].trim());
                        return new Message(
                            Header.NET,
                            reserveSuccess ? Header.ACK : Header.ERR,
                            reserveSuccess ? parts[1].trim() + " reserved " + parts[0].trim() : "Reservation failed",
                            "server", "client", "client", "server");

                    default:
                        return new Message(
                            Header.NET, 
                            Header.ERR, 
                            "Unknown item action", 
                            "server", 
                            "client", 
                            "client", 
                            "server");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new Message(
                    Header.NET, 
                    Header.ERR, 
                    "Server error in item handler", 
                    "server", 
                    "client", 
                    "client", 
                    "server");
            }
        }
    }
}