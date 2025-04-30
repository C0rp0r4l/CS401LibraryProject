package libraryMember;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
	//port number goes here
	private static int port = 7777;
	
	//Lists of various objects the server must keep track of
	//all registered library members
	private static List<Member> members = new ArrayList<>();
	//all library locations
	//List<Location> Locations;
	
		
	public static void main(String[] args) {
			try (ServerSocket ss = new ServerSocket(port)){
				//loop to create new threads for each new client that connects
				while (true) {
					//accept incoming client connections and print their addresses
					Socket clientSocket = ss.accept();
					System.out.println("New client connected " + clientSocket.getInetAddress());
					
					//create client handler for the client socket
					ClientHandler clientHandler = new ClientHandler(clientSocket);
					
					//create a new thread to 'run' the client handler
					new Thread(clientHandler).start();
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//clientHandler class
		private static class ClientHandler implements Runnable {
		        private Socket clientSocket;
		        private boolean loggedIn = false;
		        
		        //ClientHandler Constructor is fed a socket argument
		        public ClientHandler(Socket socket) {
		            //assign the argument socket to the clientSocket inside the CklientHandler
		        	this.clientSocket = socket;
		        }

		        @Override
		        public void run() {
		            try (ObjectOutputStream outSocket = new ObjectOutputStream(clientSocket.getOutputStream());
		                 ObjectInputStream inSocket = new ObjectInputStream(clientSocket.getInputStream())) {
		                
		                while (true) {
		                    Message inboundMessage = (Message) inSocket.readObject();
		                    
		                    switch (inboundMessage.getHeader()) {
		                        case login:
		                            handleLogin(inboundMessage, outSocket);
		                            break;
		                            
		                        case member:
		                            if (!loggedIn) {
		                                sendError(outSocket, "Please login first");
		                                break;
		                            }
		                            handleMemberOperations(inboundMessage, outSocket);
		                            break;
		                            
		                        case logout:
		                            handleLogout(outSocket);
		                            return;
		                            
		                        default:
		                            sendError(outSocket, "Invalid message type");
		                    }
		                }
		            } catch (IOException | ClassNotFoundException e) {
		                System.out.println("Client disconnected: " + e.getMessage());
		            } finally {
		                try {
		                    clientSocket.close();
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		            }
		        }
		        
		        private void handleLogin(Message msg, ObjectOutputStream outSocket) throws IOException {
		            loggedIn = true;
		            Message response = new Message(HeaderEnum.login, CommandEnum.login, StatusEnum.success, "Login successful");
		            outSocket.writeObject(response);
		            outSocket.flush();
		        }
		        
		        private void handleMemberOperations(Message msg, ObjectOutputStream outSocket) throws IOException {
		            CommandEnum command = msg.getcommands();
		            String text = msg.getText();
		            Message response;
		            
		            try {
		                switch (command) {
		                    case add:
		                        Member newMember = new Member(text);
		                        members.add(newMember);
		                        response = new Message(HeaderEnum.member, CommandEnum.add, StatusEnum.success, 
		                                            "Member created successfully. ID: " + newMember.getUserID());
		                        break;
		                        
		                    case remove:
		                        Member toRemove = findMember(text);
		                        if (toRemove != null) {
		                            members.remove(toRemove);
		                            response = new Message(HeaderEnum.member, CommandEnum.remove, StatusEnum.success, 
		                                                "Member " + text + " removed successfully");
		                        } else {
		                            response = new Message(HeaderEnum.member, CommandEnum.remove, StatusEnum.failure, 
		                                                "Member " + text + " not found");
		                        }
		                        break;
		                        
		                    case getStanding:
		                        Member member = findMember(text);
		                        if (member != null) {
		                            response = new Message(HeaderEnum.member, CommandEnum.getStanding, StatusEnum.success, 
		                                                  member.getStanding() + "\n" + member.getAccountHold());
		                        } else {
		                            response = new Message(HeaderEnum.member, CommandEnum.getStanding, StatusEnum.failure, 
		                                                "Member " + text + " not found");
		                        }
		                        break;
		                        
		                    case viewItems:
		                        member = findMember(text);
		                        if (member != null) {
		                            String items = member.getCheckedItems();
		                            response = new Message(HeaderEnum.member, CommandEnum.viewItems, StatusEnum.success, 
		                                                items.isEmpty() ? "No items checked out" : items);
		                        } else {
		                            response = new Message(HeaderEnum.member, CommandEnum.viewItems, StatusEnum.failure, 
		                                                "Member " + text + " not found");
		                        }
		                        break;
		                        
		                    default:
		                        response = new Message(HeaderEnum.member, command, StatusEnum.failure, 
		                                            "Unsupported operation");
		                }
		                
		                outSocket.writeObject(response);
		                outSocket.flush();
		            } catch (Exception e) {
		                sendError(outSocket, "Error processing request: " + e.getMessage());
		            }
		        }
		        
		        private void handleLogout(ObjectOutputStream outSocket) throws IOException {
		            loggedIn = false;
		            Message response = new Message(HeaderEnum.login, CommandEnum.logout, StatusEnum.success, "Logout successful");
		            outSocket.writeObject(response);
		            outSocket.flush();
		        }
		        
		        private void sendError(ObjectOutputStream outSocket, String errorMsg) throws IOException {
		            Message error = new Message(HeaderEnum.login, CommandEnum.login, StatusEnum.failure, errorMsg);
		            outSocket.writeObject(error);
		            outSocket.flush();
		        }
		        
		        private Member findMember(String userID) {
		            for (Member member : members) {
		                if (member.getUserID().equals(userID)) {
		                    return member;
		                }
		            }
		            return null;
		        }
		    }
		}