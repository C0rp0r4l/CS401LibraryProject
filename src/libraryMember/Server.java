package scmot;

import java.io.*;
import java.net.*;

// Server class
class Server {
	
    private static MemberList memberList = new MemberList();
    private static ItemList itemList = new ItemList(itemListType.Library, "");
    private static StaffMemberList staffList = new StaffMemberList("");
    private static LocationList locationList = new LocationList();
    
    public static void main(String[] args) {
        ServerSocket server = null;

        try {
            // Server is listening on port 7777
            server = new ServerSocket(7777);
            server.setReuseAddress(true);
            
            locationList.load();
            memberList.loadList();
            
            itemList.load();
            staffList.loadList();
            System.out.println(staffList.toString());

            // Running infinite loop for getting client requests
            while (true) {
                // Socket object to receive incoming client requests
                Socket client = server.accept();

                // Displaying that a new client is connected to the server
                System.out.println("New client connected"
                        + client.getInetAddress().getHostAddress());

                // Create a new thread object to handle the client separately
                ClientHandler clientSock = new ClientHandler(client, memberList);

                // Start the client handler thread
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
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
                    "Please provide login credentials",  // data indicating the request for login
                    "server",
                    "client",
                    "client",
                    "server"
                );
                out.writeObject(requestLoginMessage);
                out.flush();  // Ensure the message is sent immediately
                System.out.println("Sent login request to client.");
                
                        while (true) {
                        	if (clientSocket.getInputStream().available() > 0) {
                        	    Object obj = in.readObject();
                        	    if (!(obj instanceof Message)) {
                        	        System.out.println("Received unknown object type");
                        	        return;
                        	    }
                            Message msg = (Message) obj;
                            System.out.println("Recieved Message " + msg.getPrimaryHeader());
                            Message response;

                            // Primary header switch (INV, ACC, LOC, ITEM, NET)
                            switch (msg.getPrimaryHeader()) {
                                case Header.INV:
                                    // Handle Inventory-related actions
                                    response = handleInventoryActions(msg);
                                    out.writeObject(response);
                                    break;

                                case Header.ACCT:
                                    // Account Management-specific actions (Create, Delete, Status, Edit, Get, Data)
                                    response = handleAccountActions(msg);
                                    out.writeObject(response);
                                    break;

                                case Header.LOC:
                                    // Handle Location-related actions
                                    response = handleLocationActions(msg);
                                    out.writeObject(response);
                                    break;

                                case Header.ITEM:
                                    // Handle Item Attention-related actions
                                    response = handleItemActions(msg);
                                    out.writeObject(response);
                                    break;

                                case Header.NET:
                                    // Handle Network-related actions
                                    response = handleNetworkActions(msg);
                                    out.writeObject(response);
                                    break;

                                default:
                                    System.out.println("Unknown primary header received.");
                                    response = new Message(Header.NET, Header.ERR, "Unknown request", "server", "client", "server", "client");
                                    out.writeObject(response);
                                    break;
                            }
                        	} else {
                        	    try {
                        	        Thread.sleep(100); // reduce CPU usage
                        	    } catch (InterruptedException e) {
                        	        Thread.currentThread().interrupt();
                        	    }
                        	}
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                            System.out.println("Connection closed.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                private Message handleInventoryActions(Message msg) {
                    Message response = null;
                    Item item = null;
                    
                    switch(msg.getSecondaryHeader()) {
                    
                    case Header.CREATE:
                    	String[] parts = ((String) msg.getData()).split(",");
                    	System.out.println(parts.length);

                    	String t = parts[0];           // Title
                    	String y = parts[1]; 		   // Year
                    	String a = parts[2];           // Author
                    	int q = Integer.parseInt(parts[3]); // Quantity (assuming 4)
                    	for(int i = 0; i < q; i++) {
                        	itemList.addItem(t, y, a);
                    	}
				        response = new Message(
				        		Header.NET, 
				        		Header.ACK, 
				        		q + " New " + t + " Item" + (q > 1 ? "s" : ""), 
				        		"server", 
				        		"client", 
				        		"client", 
				        		"server");
                    	break;
                    	
                    case Header.GET:
                    	ItemList items = new ItemList(itemListType.Library, "");
                    	String[] sec = ((String) msg.getData()).split(",");
                    	String identifier = sec[0];           // Title
                    	String info = sec[1]; 		   // Year
                    	if(identifier.equals("title")) {
                        	items = itemList.getItemsFromTitle(info);
                    	}
                    	else if(identifier.equals("id")){
                        	items.addItem(itemList.getItemFromID(info));
                    	}
				        response = new Message(
				        		Header.INV, 
				        		Header.DATA, 
				        		items, 
				        		"server", 
				        		"client", 
				        		"client", 
				        		"server");
				        break;
				        
                    case Header.TRANSFER:
                    	sec = ((String) msg.getData()).split(",");
                    	String id = sec[0];           // item id
                    	String name = sec[1]; 		   // location name
                    	item = (itemList.getItemFromID(id));
                    	itemList.addLoc(name, id);
				        response = new Message(
				        		Header.NET, 
				        		Header.ACK, 
				        		"Location of Item successfully changed", 
				        		"server", 
				        		"client", 
				        		"client", 
				        		"server");
				        break;
                    }
                    return response;
                }

                private Message handleAccountActions(Message msg) {
                    Message response = null;

					switch (msg.getSecondaryHeader()) {
					case Header.CREATE:
					    System.out.println("Creating Account for client " + msg.getFrom() + " " + msg.getData());

					    String name = msg.getData().toString().trim();
					    if (name.isEmpty()) {
					        System.out.println("Invalid name provided.");
					        response = new Message(Header.NET, Header.ERR, null, "server", "client", "client", "server");
					        break;
					    }

					    // Generate new member using just the name
					    Member newMember = new Member(name);

					    // Check if userID already exists (assuming userID is generated inside Member(name))
					    boolean exists = (memberList.searchMember(newMember.getUserID()) != null);

					    if (exists) {
					        System.out.println("Account already exists for userID: " + newMember.getUserID());
					        response = new Message(Header.NET, Header.ERR, null, "server", "client", "client", "server");
					        break;
					    }

					    // Add, save, and acknowledge
					    memberList.addMember(newMember);
					    memberList.setModified(true);

					    response = new Message(Header.NET, Header.ACK, newMember.toString(), "server", "client", "client", "server");
					    break;

                        case Header.DELETE:
                            // Handle Account deletion
                            response = new Message(Header.NET, Header.ACK, "Account Deleted", "server", "client", "client", "server");
                            break;

                        case Header.STATUS:
                            // Handle Account status change by first finding account by id and then changing status to status
                            response = new Message(Header.NET, Header.ACK, "Account Status Change", "server", "client", "client", "server");
                            break;
                            
                        case Header.LOGIN:
                            String msgData = msg.getData().toString();  // Get the data from the message
                            String[] credentials = msgData.split(",");  // Split the string at the comma

                            if (credentials.length == 2) {
                                String user = credentials[0];  // First part before the comma
                                String pass = credentials[1];  // Second part after the comma

                                // Attempt login
                                Object goodCred = staffList.attemptLogin(user, pass);
                                
                                if(goodCred == null) {
                                	System.out.println("Wrong Password");
                                    response = new Message(Header.NET, Header.ERR, "Wrong Password", "server", "client", "client", "server");
                                }
                                else if(goodCred instanceof Member) {
                                	System.out.println("Successful client login");
                                    response = new Message(Header.NET, Header.ACK, goodCred, "server", "client", "client", "server");
                                }
                                else if(goodCred.toString().equals("1")) {
                                	System.out.println("That User doesnt exist");
                                    response = new Message(Header.NET, Header.ERR, "That user doesnt exist", "server", "client", "client", "server");
                                }
                            }
                            break;

                        	
                        case Header.EDIT:
                            memberList.editMember((Member) msg.getData());
                            response = new Message(Header.NET, Header.ACK, "Account Edited", "server", "client", "client", "server");
                            break;
                            
                        case Header.MAKESTAFF:
                        	Member member = memberList.searchMember(msg.getData().toString());
                        	staffList.addMember(member);
                        	memberList.removeMember(member.getUserID());
                            response = new Message(Header.NET, Header.ACK, member.getUserID() + " is now staff.", "server", "client", "client", "server");
                            break;
                        	

                        case Header.GET:
                            // Handle Account data retrieval
                            response = new Message(Header.ACCT, Header.DATA, memberList.searchMember(msg.getData().toString()), "server", "client", "client", "server");
                            break;

                        default:
                            //Send Error
                            response = new Message(Header.NET, Header.ERR, "Unknown secondary header for Account Management", "server", "client", "server", "client");

                            break;
                    }
                    return response;
                }

                private Message handleLocationActions(Message msg) {
                    Message response = null;
                    Location loc;

                	switch(msg.getSecondaryHeader()) {
                	case Header.CREATE:
                		locationList.addLocation((String) msg.getData());
                        response = new Message(Header.NET, Header.ACK, "Location action executed", "server", "client", "server", "client");
                        break;
                	
                	case Header.GET:
                		loc = locationList.searchLocation((String) msg.getData());
                		System.out.println(loc.toString());
                        response = new Message(Header.LOC, Header.DATA, loc, "server", "client", "server", "client");
                		break;
                		
                	case Header.ADD:
                		String msgData = msg.getData().toString();  // Get the data from the message
                        String[] request = msgData.split(",");  // Split the string at the comma

                        if (request.length == 2) {
                            String id = request[0];  // First part before the comma
                            String location = request[1];  // Second part after the comma
                            
                    		loc = locationList.searchLocation(location);
                    		
                    		StaffMember staff = staffList.searchMember(id);
                    		
                    		loc.addStaffMember(staff);
                    		
                    		System.out.println("Staff Member successfully added");
                    		response = new Message(Header.NET, Header.ACK, "Staff Member successfully added", "server", "client", "server", "client");
                        }
                	}
                	
                	return response;
                }

                private Message handleItemActions(Message msg) {
                    Message response = null;

                    // Handle Item Attention-related actions here
                	String[] parts;

                    switch (msg.getSecondaryHeader()) {
                    case Header.CHECKIN:
                    	parts = msg.getData().toString().split(",");
                    	itemList.removeOwner(parts[0]);
                    	System.out.println(parts[1] + " checked in " + parts[0]);
                    	response = new Message(
				        		Header.NET, 
				        		Header.ACK, 
				        		parts[1] + " checked in " + parts[0], 
				        		"server", 
				        		"client", 
				        		"client", 
				        		"server");
                    	break;
                    	
                    case Header.CHECKOUT:
                    	parts = msg.getData().toString().split(",");
                    	if(memberList.searchMember(parts[1]) != null) {

                        	itemList.addOwner(parts[1], parts[0]);
                        	System.out.println(parts[1] + " checked out " + parts[0]);
                        	response = new Message(
    				        		Header.NET, 
    				        		Header.ACK, 
    				        		parts[1] + " checked out " + parts[0], 
    				        		"server", 
    				        		"client", 
    				        		"client", 
    				        		"server");
                    	}else {
                    		response = new Message(
    				        		Header.NET, 
    				        		Header.ERR, 
    				        		parts[1] + " is not an existing Member.", 
    				        		"server", 
    				        		"client", 
    				        		"client", 
    				        		"server");
                    	}

                    	break;
                    	
                    case Header.RESERVE:
                    	parts = msg.getData().toString().split(",");
                    	itemList.handleReservation(parts[0], parts[1]);
                    	System.out.println(parts[1] + " reserved " + parts[0]);
                    	response = new Message(
				        		Header.NET, 
				        		Header.ACK, 
				        		parts[1] + " reserved " + parts[0], 
				        		"server", 
				        		"client", 
				        		"client", 
				        		"server");
                    	break;
                    }
                    return response;
                }

                private Message handleNetworkActions(Message msg) {
                    Message response = null;
					switch(msg.getSecondaryHeader()) {
                    case Header.ACK:
                    	response = new Message(
				        		Header.NET, 
				        		Header.ACK, 
				        		"Acknowledgement of Disconnection", 
				        		"server", 
				        		"client", 
				        		"client", 
				        		"server");
                    	break;
                    	
					default:
						break;
                    }
					return response;
                }
            }
        }
        
