package librarySoftwareG5;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Server {
    // Static instances of the facades, shared by all ClientHandlers
    private static MemberList memberList = new MemberList();
    private static ItemList itemList = new ItemList();
    private static Location locationList = new Location();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            System.out.println("Server started on port 7777. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket, memberList, itemList, locationList)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private final MemberList CHandlerMemberList;
        private final ItemList CHandlerItemList;
        private final Location CHandlerLocationList;


        public ClientHandler(Socket socket, MemberList mList, ItemList iList, Location lList) {
            this.clientSocket = socket;
            this.CHandlerMemberList = mList;
            this.CHandlerItemList = iList;
            this.CHandlerLocationList = lList;
        }

        private Item enrichItemDetailsIfNeeded(Item item) {
            if (item == null) return null;
            if (item.getOwnedBy() != null && !item.getOwnedBy().isEmpty() && (item.getOwnedByName() == null || item.getOwnedByName().isEmpty())) {
                Member owner = CHandlerMemberList.getMember(item.getOwnedBy());
                if (owner != null) {
                    item.setOwnedByName(owner.getName());
                } else {
                     item.setOwnedByName(null);
                }
            }
            List<String> reserverIds = item.getReservedBy();
            if (reserverIds != null && !reserverIds.isEmpty() &&
                (item.getReservedByNames() == null || item.getReservedByNames().isEmpty() || item.getReservedByNames().size() != reserverIds.size())) {
                List<String> reserverNames = new ArrayList<>();
                for (String memberId : reserverIds) {
                    Member reserver = CHandlerMemberList.getMember(memberId);
                    if (reserver != null) {
                        reserverNames.add(reserver.getName() + " (ID: " + memberId + ")");
                    } else {
                        reserverNames.add("Unknown Member (ID: " + memberId + ")");
                    }
                }
                item.setReservedByNames(reserverNames);
            } else if (reserverIds.isEmpty() && (item.getReservedByNames() != null && !item.getReservedByNames().isEmpty())) {
                item.setReservedByNames(new ArrayList<>());
            }
            return item;
        }

        private List<Item> enrichItemListDetailsIfNeeded(List<Item> items) {
            if (items == null) return new ArrayList<>();
            return items.stream()
                        .map(this::enrichItemDetailsIfNeeded)
                        .collect(Collectors.toList());
        }


        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                Message loginRequest = (Message) in.readObject();
                Message loginResponse;

                if (loginRequest.getAction() == Message.ActionType.LOGIN) {
                    loginResponse = handleLogin(loginRequest);
                    out.writeObject(loginResponse);
                    out.flush();
                    if (!loginResponse.isSuccess()) {
                        System.out.println("Login failed for " + clientSocket.getInetAddress() + ". Closing connection.");
                        return;
                    }
                    System.out.println("Login successful for " + clientSocket.getInetAddress());
                } else {
                    System.out.println("First action was not LOGIN for " + clientSocket.getInetAddress() + ". Closing connection.");
                    out.writeObject(errorResponse("Login required as first action."));
                    out.flush();
                    return;
                }

                while (true) {
                    Message request = (Message) in.readObject();
                    System.out.println("Received request: " + request.getAction() + " from " + clientSocket.getInetAddress());
                    Message response = processRequest(request);

                    out.reset();
                    out.writeObject(response);
                    out.flush();
                    System.out.println("Sent response for: " + request.getAction() + " to " + clientSocket.getInetAddress());

                    if (request.getAction() == Message.ActionType.LOGOUT) {
                        System.out.println("Client " + clientSocket.getInetAddress() + " logged out. Closing connection.");
                        break;
                    }
                }
            } catch (EOFException e) {
                System.out.println("Client " + clientSocket.getInetAddress() + " disconnected (EOF).");
            } catch (SocketException e) {
                System.out.println("SocketException for " + clientSocket.getInetAddress() + ": " + e.getMessage() + ". Client likely disconnected.");
            } catch (Exception e) {
                System.err.println("Client handler error for " + clientSocket.getInetAddress() + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                    System.out.println("Closed connection for " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    System.err.println("Error closing socket for " + clientSocket.getInetAddress() + ": " + e.getMessage());
                }
            }
        }

        private Message processRequest(Message request) {
            try {
                switch (request.getAction()) {
                    case LOGOUT:
                        return new Message(Message.ActionType.ACK, "Logged out successfully");
                        
                    // Member operations
                    case ADD_MEMBER:
                        return addMember(request);
                    case REMOVE_MEMBER:
                        return removeMember(request);
                    case GET_MEMBER:
                        return getMember(request);
                    case SEARCH_MEMBERS:
                        return searchMembers(request);
                    case GET_ALL_MEMBERS:
                        return getAllMembers(request);
                    case MARK_MEMBER_PAST_DUE: // New handler
                        return handleMarkMemberPastDue(request);
                    case MEMBER_ITEM_RETURNED: // New handler
                        return handleMemberItemReturned(request);
                        
                    // Staff operations
                    case ADD_STAFF:
                        return addStaff(request);
                    case REMOVE_STAFF:
                        return removeStaff(request);
                    case GET_STAFF:
                        return getStaff(request);
                    case GET_ALL_STAFF:
                        return getAllStaff(request);
                        
                    // Item operations
                    case ADD_ITEM:
                        return addItem(request);
                    case REMOVE_ITEM:
                        return removeItem(request);
                    case GET_ITEM:
                        Item item = CHandlerItemList.getItem((String) request.getData());
                        return item != null ?
                            new Message(Message.ActionType.ACK, enrichItemDetailsIfNeeded(item)) :
                            errorResponse("Item not found.");
                    case SEARCH_ITEMS:
                        List<Item> searchResults = CHandlerItemList.searchByTitle((String) request.getData());
                        return new Message(Message.ActionType.ACK, enrichItemListDetailsIfNeeded(searchResults));
                    case GET_ALL_ITEMS:
                        List<Item> allItems = CHandlerItemList.getAllItems();
                        return new Message(Message.ActionType.ACK, enrichItemListDetailsIfNeeded(allItems));
                    case CHECKOUT:
                        return checkoutItem(request);
                    case RETURN:
                        return returnItem(request);
                    case RESERVE:
                        return reserveItem(request);
                    case CANCEL_RESERVATION:
                        return cancelReservation(request);
                    
                    // Location operations
                    case ADD_LOCATION:
                        return addLocation(request);
                    case REMOVE_LOCATION:
                        return removeLocation(request);
                    case GET_LOCATIONS:
                        return getLocations(request);
                    default:
                        return errorResponse("Invalid action specified: " + request.getAction());
                }
            } catch (Exception e) {
                System.err.println("Exception during processRequest for action " + request.getAction() + ": " + e.getMessage());
                e.printStackTrace();
                return errorResponse("Server error while processing request: " + e.getMessage());
            }
        }
        
        
        //Login Operation
        private Message handleLogin(Message request) {
            String[] credentials = (String[]) request.getData();
            if (credentials == null || credentials.length < 2) {
                return errorResponse("Invalid login data format.");
            }
            StaffMember staff = CHandlerMemberList.getStaffMember(credentials[0]);
            if (staff != null && staff.authenticate(credentials[1])) {
                return new Message(Message.ActionType.ACK, staff);
            }
            return errorResponse("Invalid staff ID or password.");
        }

        
        // Member operations
        private Message addMember(Message request) {
            String memberName = (String) request.getData();
            if (memberName == null || memberName.trim().isEmpty()) {
                return errorResponse("Member name cannot be empty.");
            }
            String memberId = CHandlerMemberList.addMember(memberName);
            return new Message(Message.ActionType.ACK, "Member added with ID: " + memberId);
        }

        private Message removeMember(Message request) {
            String memberId = (String) request.getData();
            boolean success = CHandlerMemberList.removeMember(memberId);
            return success ?
                new Message(Message.ActionType.ACK, "Member removed successfully.") :
                errorResponse("Member not found or could not be removed.");
        }

        private Message getMember(Message request) {
            String memberId = (String) request.getData();
            Member member = CHandlerMemberList.getMember(memberId);
            return member != null ?
                new Message(Message.ActionType.ACK, member) :
                errorResponse("Member not found.");
        }

        private Message searchMembers(Message request) {
            String searchTerm = (String) request.getData();
            List<Member> results = CHandlerMemberList.searchByName(searchTerm);
            return new Message(Message.ActionType.ACK, results);
        }

        private Message getAllMembers(Message request) {
            List<Member> members = CHandlerMemberList.getAllMembers();
            return new Message(Message.ActionType.ACK, members);
        }

        private Message handleMarkMemberPastDue(Message request) {
            String memberId = (String) request.getData();
            if (memberId == null || memberId.trim().isEmpty()) {
                return errorResponse("Member ID cannot be empty.");
            }
            Member member = CHandlerMemberList.getMember(memberId);
            if (member == null) {
                return errorResponse("Member not found with ID: " + memberId);
            }
            member.addStrike();
            member.setAccountHold(true);
            CHandlerMemberList.saveAll(); // Persist changes
            return new Message(Message.ActionType.ACK, member); // Return updated member
        }

        private Message handleMemberItemReturned(Message request) {
            String memberId = (String) request.getData();
            if (memberId == null || memberId.trim().isEmpty()) {
                return errorResponse("Member ID cannot be empty.");
            }
            Member member = CHandlerMemberList.getMember(memberId);
            if (member == null) {
                return errorResponse("Member not found with ID: " + memberId);
            }
            member.setAccountHold(false);
            CHandlerMemberList.saveAll(); //save file again just in case
            // Return the updated member object, client can format the message.
            return new Message(Message.ActionType.ACK, member);
        }


        // Staff operations
        private Message addStaff(Message request) {
            String[] data = (String[]) request.getData();
            if (data == null || data.length < 2 || data[0] == null || data[0].trim().isEmpty() || data[1] == null || data[1].trim().isEmpty()) {
                return errorResponse("Staff name and location cannot be empty.");
            }
            String staffId = CHandlerMemberList.addStaffMember(data[0], data[1]);
            return new Message(Message.ActionType.ACK, "Staff member added with ID: " + staffId);
        }

        private Message removeStaff(Message request) {
            String staffId = (String) request.getData();
            boolean success = CHandlerMemberList.removeStaffMember(staffId);
            return success ?
                new Message(Message.ActionType.ACK, "Staff member removed successfully.") :
                errorResponse("Staff member not found or could not be removed.");
        }

        private Message getStaff(Message request) {
            String staffId = (String) request.getData();
            StaffMember staff = CHandlerMemberList.getStaffMember(staffId);
            return staff != null ?
                new Message(Message.ActionType.ACK, staff) :
                errorResponse("Staff member not found.");
        }

        private Message getAllStaff(Message request) {
            List<StaffMember> staffList = CHandlerMemberList.getAllStaffMembers();
            return new Message(Message.ActionType.ACK, staffList);
        }

        
        // Item operations
        private Message addItem(Message request) {
            String[] itemData = (String[]) request.getData();
            if (itemData == null || itemData.length < 4) {
                 return errorResponse("Incomplete item data. Expected: title, year, author, location.");
            }
            for(String s : itemData) {
                if (s == null || s.trim().isEmpty()) return errorResponse("All item fields (title, year, author, location) are required.");
            }
            if (!CHandlerLocationList.contains(itemData[3])) {
                return errorResponse("Location '" + itemData[3] + "' does not exist. Please add the location first.");
            }
            String itemId = CHandlerItemList.addItem(itemData[0], itemData[1], itemData[2], itemData[3]);
            return new Message(Message.ActionType.ACK, "Item added with ID: " + itemId);
        }

        private Message removeItem(Message request) {
            String itemId = (String) request.getData();
            boolean success = CHandlerItemList.removeItem(itemId);
            return success ?
                new Message(Message.ActionType.ACK, "Item removed successfully.") :
                errorResponse("Item not found or could not be removed.");
        }

        private Message checkoutItem(Message request) {
            String[] data = (String[]) request.getData();
             if (data == null || data.length < 2) {
                return errorResponse("Item ID and Member ID are required for checkout.");
            }
            String itemId = data[0];
            String memberId = data[1];

            Member member = CHandlerMemberList.getMember(memberId);
            if (member == null) {
                return errorResponse("Member ID " + memberId + " not found for checkout.");
            }
            if (member.isAccountBanned()) {
                return errorResponse("Checkout failed: Member " + member.getName() + " (ID: " + memberId + ") account is banned.");
            }
            if (member.isAccountHold()) {
                return errorResponse("Checkout failed: Member " + member.getName() + " (ID: " + memberId + ") account is on hold.");
            }
            String memberName = member.getName();

            boolean success = CHandlerItemList.checkoutItem(itemId, memberId, memberName);
            if (success) {
                return new Message(Message.ActionType.ACK, "Item ID: " + itemId + " checked out successfully to " + memberName + " (ID: " + memberId + ")");
            }
            Item currentItemState = CHandlerItemList.getItem(itemId);
            if (currentItemState == null) {
                return errorResponse("Checkout failed: Item ID " + itemId + " not found.");
            }
            if (currentItemState.getOwnedBy() != null) {
                 String currentOwnerName = currentItemState.getOwnedByName();
                 if (currentOwnerName == null || currentOwnerName.isEmpty()) {
                    Member currentOwnerMember = CHandlerMemberList.getMember(currentItemState.getOwnedBy());
                    currentOwnerName = (currentOwnerMember != null) ? currentOwnerMember.getName() : "member ID: " + currentItemState.getOwnedBy();
                 } else {
                    currentOwnerName += " (ID: " + currentItemState.getOwnedBy() + ")";
                 }
                return errorResponse("Checkout failed: Item ID " + itemId + " is already checked out to " + currentOwnerName);
            }
            return errorResponse("Checkout failed for Item ID " + itemId + " (Item available, member valid, unexpected error).");
        }

        private Message returnItem(Message request) {
            String itemId = (String) request.getData();
            boolean success = CHandlerItemList.returnItem(itemId);
            return success ?
                new Message(Message.ActionType.ACK, "Item ID: " + itemId + " returned successfully.") :
                errorResponse("Return failed. Item ID: " + itemId + " not found or was not checked out.");
        }

        private Message reserveItem(Message request) {
            String[] data = (String[]) request.getData();
            if (data == null || data.length < 2) {
                return errorResponse("Item ID and Member ID are required for reservation.");
            }
            String itemId = data[0];
            String memberId = data[1];
            Member member = CHandlerMemberList.getMember(memberId);
            if (member == null) {
                return errorResponse("Member ID " + memberId + " not found for reservation.");
            }
            if (member.isAccountBanned()) {
                return errorResponse("Reservation failed: Member " + member.getName() + " (ID: " + memberId + ") account is banned.");
            }
            String memberName = member.getName();

            boolean success = CHandlerItemList.reserveItem(itemId, memberId, memberName);
            if (success) {
                return new Message(Message.ActionType.ACK, "Item ID: " + itemId + " reserved successfully by " + memberName + " (ID: " + memberId + ")");
            }
            Item currentItemState = CHandlerItemList.getItem(itemId);
            if (currentItemState == null) return errorResponse("Reservation failed: Item ID " + itemId + " not found.");
            if (currentItemState.getReservedBy().contains(memberId)) {
                 return errorResponse("Reservation failed: Item ID " + itemId + " already reserved by " + memberName + " (ID: " + memberId + ").");
            }
            return errorResponse("Reservation failed for Item ID " + itemId + " (e.g., item not reservable or other issue).");
        }

        private Message cancelReservation(Message request) {
            String[] data = (String[]) request.getData();
             if (data == null || data.length < 2) {
                return errorResponse("Item ID and Member ID are required to cancel reservation.");
            }
            String itemId = data[0];
            String memberId = data[1];
            Member member = CHandlerMemberList.getMember(memberId);
            String memberName = (member != null) ? member.getName() + " (ID: " + memberId + ")" : "member ID: " + memberId;

            boolean success = CHandlerItemList.cancelReservation(itemId, memberId);
            return success ?
                new Message(Message.ActionType.ACK, "Reservation for item ID: " + itemId + " by " + memberName + " canceled successfully.") :
                errorResponse("Cancel reservation failed. Item ID: " + itemId + " or reservation by " + memberName + " not found.");
        }

        
        // Location operations
        private Message addLocation(Message request) {
            String locationName = (String) request.getData();
            if (locationName == null || locationName.trim().isEmpty()) {
                return errorResponse("Location name cannot be empty.");
            }
            boolean success = CHandlerLocationList.addLocation(locationName);
            return success ?
                new Message(Message.ActionType.ACK, "Location added: " + locationName) :
                errorResponse("Location '" + locationName + "' already exists or could not be added.");
        }

        private Message removeLocation(Message request) {
            String locationName = (String) request.getData();
            boolean success = CHandlerLocationList.removeLocation(locationName);
            return success ?
                new Message(Message.ActionType.ACK, "Location removed: " + locationName) :
                errorResponse("Location '" + locationName + "' not found or could not be removed.");
        }

        private Message getLocations(Message request) {
            List<String> locations = CHandlerLocationList.getAllLocations();
            return new Message(Message.ActionType.ACK, locations);
        }

        private Message errorResponse(String error) {
            Message response = new Message(Message.ActionType.ERR, null);
            response.setErrorMessage(error);
            return response;
        }
    }
}