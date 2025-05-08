package librarySoftwareG5;

import librarySoftwareG5.Message.ActionType;
import librarySoftwareG5.Member;
import librarySoftwareG5.StaffMember;
import librarySoftwareG5.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ClientGUI {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 7777;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private JFrame frame;
    private JTextArea outputArea;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel; //Main panel that uses CardLayout
    private JPanel selectedMemberActionsPanel; //Panel for actions on a selected member
    private JLabel selectedMemberLabel; //To display info about the selected member

    private StaffMember loggedInStaff;
    private Member currentlySelectedMember; //To store the member being acted upon

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    public ClientGUI() {
        setupGUI();
        connectAndLogin();
    }

    private void connectAndLogin() {
       
        try {
            log("Attempting to connect to server at " + SERVER_ADDRESS + ":" + SERVER_PORT + "...");
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            log("Connected to server: " + socket.getRemoteSocketAddress());

            if (!handleLogin()) {
                log("Login failed. Exiting client.");
                showErrorAndClose("Login failed. Exiting client.", false); 
                return;
            }
            cardLayout.show(cardPanel, "MainMenu");

        } catch (SocketException e) {
            log("Connection error: Could not connect to the server or connection lost. " + e.getMessage());
            showErrorAndClose("Connection error: " + e.getMessage(), true);
        } catch (EOFException e) {
            log("Connection closed by server unexpectedly.");
            showErrorAndClose("Connection closed by server unexpectedly.", true);
        } catch (IOException | ClassNotFoundException e) {
            log("Client communication error: " + e.getMessage());
            showErrorAndClose("Client communication error: " + e.getMessage(), true);
        }
    }

    private boolean handleLogin() throws IOException, ClassNotFoundException {
        // ... (same as your previous working version)
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField staffIdField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);
        panel.add(new JLabel("Staff ID:"));
        panel.add(staffIdField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String staffId = staffIdField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (staffId.isEmpty() || password.isEmpty()) {
                log("Staff ID and Password cannot be empty.");
                return false;
            }

            Message loginMsg = new Message(ActionType.LOGIN, new String[]{staffId, password});
            out.writeObject(loginMsg);
            out.flush();

            Message response = (Message) in.readObject();
            if (response.isSuccess() && response.getData() instanceof StaffMember) {
                loggedInStaff = (StaffMember) response.getData();
                log("Logged in successfully as " + loggedInStaff.getName() + " (ID: " + loggedInStaff.getMemberID() + ")");
                frame.setTitle("Library Management System - User: " + loggedInStaff.getName());
                return true;
            } else {
                String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "Unknown login error.";
                log("Login failed: " + errorMsg);
                JOptionPane.showMessageDialog(frame, "Login failed: " + errorMsg, "Login Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            log("Login cancelled by user.");
            return false; 
        }
    }


    private void setupGUI() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600); // Increased height a bit
        frame.setLocationRelativeTo(null);

        outputArea = new JTextArea(20, 50); // Increased rows
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel loginPanelPlaceholder = new JPanel();
        loginPanelPlaceholder.add(new JLabel("Attempting login..."));

        JPanel mainMenuPanel = createMainMenuPanel();
        JPanel memberOpsPanel = createMemberOperationsPanel(); // General member operations
        selectedMemberActionsPanel = createSelectedMemberActionsPanel(); // Specific actions for a selected member
        JPanel staffOpsPanel = createStaffOperationsPanel();
        JPanel itemOpsPanel = createItemOperationsPanel();
        JPanel locationOpsPanel = createLocationOperationsPanel();

        cardPanel.add(loginPanelPlaceholder, "Login");
        cardPanel.add(mainMenuPanel, "MainMenu");
        cardPanel.add(memberOpsPanel, "MemberOperations");
        cardPanel.add(selectedMemberActionsPanel, "SelectedMemberActions"); // Add new panel
        cardPanel.add(staffOpsPanel, "StaffOperations");
        cardPanel.add(itemOpsPanel, "ItemOperations");
        cardPanel.add(locationOpsPanel, "LocationOperations");

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(cardPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
    //create the main panel
    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(createButton("Member Operations", e -> cardLayout.show(cardPanel, "MemberOperations")));
        panel.add(createButton("Staff Member Operations", e -> cardLayout.show(cardPanel, "StaffOperations")));
        panel.add(createButton("Item Operations", e -> cardLayout.show(cardPanel, "ItemOperations")));
        panel.add(createButton("Location Operations", e -> cardLayout.show(cardPanel, "LocationOperations")));
        panel.add(createButton("Logout", e -> handleLogout()));
        return panel;
    }

    //Member Operations Panel
    private JPanel createMemberOperationsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(createButton("Add Member", e -> handleAddMember()));
        panel.add(createButton("Search Member by ID (to select)", e -> handleSelectMemberById()));
        panel.add(createButton("Search Members by Name (to select)", e -> handleSelectMembersByName()));
        panel.add(createButton("List All Members", e -> sendRequest(new Message(ActionType.GET_ALL_MEMBERS, null), true, false)));
        panel.add(createButton("Back to Main Menu", e -> {
            currentlySelectedMember = null; // Clear selection when going back
            selectedMemberLabel.setText("No member selected.");
            cardLayout.show(cardPanel, "MainMenu");
        }));
        return panel;
    }

    //Selected Member Actions
    private JPanel createSelectedMemberActionsPanel() {
        JPanel panel = new JPanel();
        //vertical panel
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        selectedMemberLabel = new JLabel("No member selected.");
        selectedMemberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(selectedMemberLabel);
        panel.add(Box.createRigidArea(new Dimension(0,10)));


        panel.add(createButton("Mark Account Past Due", e -> handleMarkPastDue()));
        panel.add(createButton("Process Item Return (Release Hold)", e -> handleMemberItemReturned()));
        panel.add(createButton("View Full Details", e -> handleViewSelectedMemberDetails()));
        panel.add(createButton("Remove This Member", e -> handleRemoveSelectedMember()));
        panel.add(Box.createRigidArea(new Dimension(0,10)));
        JButton backButton = createButton("Back to Member Search", e -> {
             currentlySelectedMember = null; // Clear selection
             selectedMemberLabel.setText("No member selected.");
             cardLayout.show(cardPanel, "MemberOperations");
        });
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(backButton);
        
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(panel);
        return wrapper;
    }

    //Staff operations panel made
    private JPanel createStaffOperationsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(createButton("Add Staff", e -> handleAddStaff()));
        panel.add(createButton("Remove Staff", e -> handleRemoveStaff()));
        panel.add(createButton("Search Staff by ID", e -> handleGetStaff()));
        panel.add(createButton("List All Staff", e -> sendRequest(new Message(ActionType.GET_ALL_STAFF, null), true, false)));
        panel.add(createButton("Back to Main Menu", e -> cardLayout.show(cardPanel, "MainMenu")));
        return panel;
    }
    
    //make item operations panel
    private JPanel createItemOperationsPanel() {
        // ... (same as your previous working version)
        JPanel panel = new JPanel(new GridLayout(0,1)); 
        panel.add(createButton("Add Item", e -> handleAddItem()));
        panel.add(createButton("Remove Item", e -> handleRemoveItem()));
        panel.add(createButton("Search Item by ID", e -> handleGetItem()));
        panel.add(createButton("Search Items by Title", e -> handleSearchItems()));
        panel.add(createButton("List All Items", e -> sendRequest(new Message(ActionType.GET_ALL_ITEMS, null), true, false)));
        panel.add(createButton("Checkout Item", e -> handleCheckoutItem()));
        panel.add(createButton("Return Item", e -> handleReturnItem()));
        panel.add(createButton("Reserve Item", e -> handleReserveItem()));
        panel.add(createButton("Cancel Reservation", e -> handleCancelReservation()));
        panel.add(createButton("Back to Main Menu", e -> cardLayout.show(cardPanel, "MainMenu")));

        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapperPanel.add(panel);
        return wrapperPanel;
    }

    //create location operations panel
    private JPanel createLocationOperationsPanel() {
        // ... (same as your previous working version)
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(createButton("Add Location", e -> handleAddLocation()));
        panel.add(createButton("Remove Location", e -> handleRemoveLocation()));
        panel.add(createButton("List All Locations", e -> sendRequest(new Message(ActionType.GET_LOCATIONS, null), true, false)));
        panel.add(createButton("Back to Main Menu", e -> cardLayout.show(cardPanel, "MainMenu")));
        return panel;
    }

    //Actions for selecting member by ID
    private void handleSelectMemberById() {
        String id = JOptionPane.showInputDialog(frame, "Enter member ID to select:");
        if (id != null && !id.trim().isEmpty()) {
            // We expect GET_MEMBER to return a Member object, which will then be handled
            // by a callback or a modified displayGenericResponse to set currentlySelectedMember
            sendRequest(new Message(ActionType.GET_MEMBER, id.trim()), true, true);
        } else if (id != null) {
            log("Member ID cannot be empty.");
        }
    }
    
    //actions for selecting member by name
    private void handleSelectMembersByName() {
        String name = JOptionPane.showInputDialog(frame, "Enter name (or part of name) to search:");
        if (name != null && !name.trim().isEmpty()) {
            // This will list members. User then needs to use "Search Member by ID (to select)"
            log("Search results will be displayed. Please use 'Search Member by ID (to select)' with a specific ID from the list.");
            sendRequest(new Message(ActionType.SEARCH_MEMBERS, name.trim()), true, false);
        } else if (name != null) {
            log("Search term cannot be empty.");
        }
    }

    //actions for marking member past due
    private void handleMarkPastDue() {
        if (currentlySelectedMember == null) {
            log("No member selected. Please search and select a member first.");
            JOptionPane.showMessageDialog(frame, "No member selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendRequest(new Message(ActionType.MARK_MEMBER_PAST_DUE, currentlySelectedMember.getMemberID()), true, true); 
    }
    //actions for member returned item
    private void handleMemberItemReturned() {
        if (currentlySelectedMember == null) {
            log("No member selected. Please search and select a member first.");
            JOptionPane.showMessageDialog(frame, "No member selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendRequest(new Message(ActionType.MEMBER_ITEM_RETURNED, currentlySelectedMember.getMemberID()), true, true); 
    }
    
    private void handleViewSelectedMemberDetails() {
        if (currentlySelectedMember == null) {
            log("No member selected. Please search and select a member first.");
            JOptionPane.showMessageDialog(frame, "No member selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Re-fetch or just display current data. Re-fetching is safer for up-to-date info.
        sendRequest(new Message(ActionType.GET_MEMBER, currentlySelectedMember.getMemberID()), true, true);
    }

    private void handleRemoveSelectedMember() {
        if (currentlySelectedMember == null) {
            log("No member selected. Please search and select a member first.");
             JOptionPane.showMessageDialog(frame, "No member selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to remove member: " + currentlySelectedMember.getName() + " (ID: " + currentlySelectedMember.getMemberID() + ")?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            sendRequest(new Message(ActionType.REMOVE_MEMBER, currentlySelectedMember.getMemberID()), false, false);
            currentlySelectedMember = null; // Clear selection after removal
            selectedMemberLabel.setText("No member selected.");
            cardLayout.show(cardPanel, "MemberOperations"); // Go back to member search
        }
    }


    //add member action 
    private void handleAddMember() {
        String name = JOptionPane.showInputDialog(frame, "Enter member name:");
        if (name != null && !name.trim().isEmpty()) {
            sendRequest(new Message(ActionType.ADD_MEMBER, name.trim()), false, false);
        } else if (name != null) {
            log("Member name cannot be empty.");
        }
    }


    private void handleAddStaff() { 
        JTextField nameField = new JTextField(15);
        JTextField locationField = new JTextField(15);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Staff Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Staff Location:"));
        panel.add(locationField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Staff Member", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            if (!name.isEmpty() && !location.isEmpty()) {
                sendRequest(new Message(ActionType.ADD_STAFF, new String[]{name, location}), false, false);
            } else {
                log("Staff name and location cannot be empty.");
            }
        }
    }
    private void handleRemoveStaff() { 
        String id = JOptionPane.showInputDialog(frame, "Enter staff ID to remove:");
        if (id != null && !id.trim().isEmpty()) {
            sendRequest(new Message(ActionType.REMOVE_STAFF, id.trim()), false, false);
        } else if (id != null) {
            log("Staff ID cannot be empty.");
        }
    }
    private void handleGetStaff() { 
        String id = JOptionPane.showInputDialog(frame, "Enter staff ID to search:");
        if (id != null && !id.trim().isEmpty()) {
            sendRequest(new Message(ActionType.GET_STAFF, id.trim()), true, false);
        } else if (id != null) {
            log("Staff ID cannot be empty.");
        }
    }
    private void handleAddItem() { 
        JTextField titleField = new JTextField(20);
        JTextField yearField = new JTextField(5);
        JTextField authorField = new JTextField(15);
        JTextField locationField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Year:"));
        panel.add(yearField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        panel.add(new JLabel("Location (must exist):"));
        panel.add(locationField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String year = yearField.getText().trim();
            String author = authorField.getText().trim();
            String location = locationField.getText().trim();

            if (!title.isEmpty() && !year.isEmpty() && !author.isEmpty() && !location.isEmpty()) {
                sendRequest(new Message(ActionType.ADD_ITEM, new String[]{title, year, author, location}), false, false);
            } else {
                log("All item fields (title, year, author, location) are required.");
            }
        }
    }
    private void handleRemoveItem() { 
        String id = JOptionPane.showInputDialog(frame, "Enter item ID to remove:");
        if (id != null && !id.trim().isEmpty()) {
            sendRequest(new Message(ActionType.REMOVE_ITEM, id.trim()), false, false);
        } else if (id != null) {
            log("Item ID cannot be empty.");
        }
    }
    private void handleGetItem() { 
        String id = JOptionPane.showInputDialog(frame, "Enter item ID to search:");
        if (id != null && !id.trim().isEmpty()) {
            sendRequest(new Message(ActionType.GET_ITEM, id.trim()), true, false);
        } else if (id != null) {
            log("Item ID cannot be empty.");
        }
    }
    private void handleSearchItems() { 
        String title = JOptionPane.showInputDialog(frame, "Enter title (or part of title) to search:");
        if (title != null && !title.trim().isEmpty()) {
            sendRequest(new Message(ActionType.SEARCH_ITEMS, title.trim()), true, false);
        } else if (title != null) {
            log("Search title cannot be empty.");
        }
    }
    private void handleCheckoutItem() { 
        JTextField itemIdField = new JTextField(10);
        JTextField memberIdField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Item ID to checkout:"));
        panel.add(itemIdField);
        panel.add(new JLabel("Member ID for checkout:"));
        panel.add(memberIdField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Checkout Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String itemId = itemIdField.getText().trim();
            String memberId = memberIdField.getText().trim();
            if (!itemId.isEmpty() && !memberId.isEmpty()) {
                sendRequest(new Message(ActionType.CHECKOUT, new String[]{itemId, memberId}), false, false);
            } else {
                log("Item ID and Member ID cannot be empty for checkout.");
            }
        }
    }
    private void handleReturnItem() { 
        String itemId = JOptionPane.showInputDialog(frame, "Enter item ID to return:");
        if (itemId != null && !itemId.trim().isEmpty()) {
            sendRequest(new Message(ActionType.RETURN, itemId.trim()), false, false);
        } else if (itemId != null) {
            log("Item ID cannot be empty for return.");
        }
    }
    private void handleReserveItem() { 
        JTextField itemIdField = new JTextField(10);
        JTextField memberIdField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Item ID to reserve:"));
        panel.add(itemIdField);
        panel.add(new JLabel("Member ID for reservation:"));
        panel.add(memberIdField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Reserve Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String itemId = itemIdField.getText().trim();
            String memberId = memberIdField.getText().trim();
            if (!itemId.isEmpty() && !memberId.isEmpty()) {
                sendRequest(new Message(ActionType.RESERVE, new String[]{itemId, memberId}), false, false);
            } else {
                log("Item ID and Member ID cannot be empty for reservation.");
            }
        }
    }
    private void handleCancelReservation() { 
        JTextField itemIdField = new JTextField(10);
        JTextField memberIdField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Item ID to cancel reservation for:"));
        panel.add(itemIdField);
        panel.add(new JLabel("Member ID whose reservation to cancel:"));
        panel.add(memberIdField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Cancel Reservation", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String itemId = itemIdField.getText().trim();
            String memberId = memberIdField.getText().trim();
            if (!itemId.isEmpty() && !memberId.isEmpty()) {
                sendRequest(new Message(ActionType.CANCEL_RESERVATION, new String[]{itemId, memberId}), false, false);
            } else {
                log("Item ID and Member ID cannot be empty for cancelling reservation.");
            }
        }
    }
    private void handleAddLocation() { 
        String name = JOptionPane.showInputDialog(frame, "Enter location name to add:");
        if (name != null && !name.trim().isEmpty()) {
            sendRequest(new Message(ActionType.ADD_LOCATION, name.trim()), false, false);
        } else if (name != null) {
            log("Location name cannot be empty.");
        }
    }
    private void handleRemoveLocation() { 
        String name = JOptionPane.showInputDialog(frame, "Enter location name to remove:");
        if (name != null && !name.trim().isEmpty()) {
            sendRequest(new Message(ActionType.REMOVE_LOCATION, name.trim()), false, false);
        } else if (name != null) {
            log("Location name cannot be empty.");
        }
    }
    private void handleLogout() { 
        if (socket == null || socket.isClosed()) {
            log("Not connected to server or already logged out.");
            frame.dispose(); 
            System.exit(0); 
            return;
        }
        try {
            Message logoutMsg = new Message(ActionType.LOGOUT, null);
            out.writeObject(logoutMsg);
            out.flush();
            Message response = (Message) in.readObject(); 
            if (response.isSuccess()) {
                log("Logged out successfully from server: " + response.getData());
            } else {
                log("Logout acknowledgement error from server: " + response.getErrorMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            log("Error during logout: " + e.getMessage());
        } finally {
            closeConnection();
            log("Exiting client application.");
            frame.dispose();
            System.exit(0);
        }
    }

    //sendRequest to take a flag for updating selected member
    private void sendRequest(Message msg, boolean expectComplexData, boolean forMemberSelection) {
        if (socket == null || socket.isClosed() || out == null || in == null) {
            log("Not connected to server. Please try restarting the application.");
            showErrorAndClose("Connection lost. Please restart.", true);
            return;
        }
        new SwingWorker<Message, Void>() {
            @Override
            protected Message doInBackground() throws Exception {
                out.reset();
                out.writeObject(msg);
                out.flush();
                return (Message) in.readObject();
            }

            @Override
            protected void done() {
                try {
                    Message response = get();
                    if (response.isSuccess()) {
                        if (forMemberSelection && response.getData() instanceof Member) {
                            currentlySelectedMember = (Member) response.getData();
                            selectedMemberLabel.setText("Selected: " + currentlySelectedMember.getName() + " (ID: " + currentlySelectedMember.getMemberID() + ")");
                            log("Member selected: " + currentlySelectedMember.getName());
                            displayGenericResponse(response.getData(), msg.getAction()); // Display details
                            cardLayout.show(cardPanel, "SelectedMemberActions"); // Switch to actions panel
                        } else if (msg.getAction() == ActionType.MARK_MEMBER_PAST_DUE && response.getData() instanceof Member) {
                            currentlySelectedMember = (Member) response.getData(); // Update local copy
                            log("Member " + currentlySelectedMember.getName() + " marked past due. Strikes: " + currentlySelectedMember.getStrikes() + ", Hold: " + currentlySelectedMember.isAccountHold());
                            selectedMemberLabel.setText("Selected: " + currentlySelectedMember.getName() + " (ID: " + currentlySelectedMember.getMemberID() + ")"); // Update label
                             displayGenericResponse(response.getData(), msg.getAction());
                        } else if (msg.getAction() == ActionType.MEMBER_ITEM_RETURNED && response.getData() instanceof Member) {
                            currentlySelectedMember = (Member) response.getData(); // Update local copy
                            log("Item returned for member " + currentlySelectedMember.getName() + ". Account hold released. Strikes: " + currentlySelectedMember.getStrikes());
                            JOptionPane.showMessageDialog(frame, "Thank you for returning your item, member " + currentlySelectedMember.getName() + " has " + currentlySelectedMember.getStrikes() + " strikes on their account.", "Item Returned", JOptionPane.INFORMATION_MESSAGE);
                            selectedMemberLabel.setText("Selected: " + currentlySelectedMember.getName() + " (ID: " + currentlySelectedMember.getMemberID() + ")"); // Update label
                             displayGenericResponse(response.getData(), msg.getAction()); // Display full details if needed
                        }
                        else if (expectComplexData) {
                            displayGenericResponse(response.getData(), msg.getAction());
                        } else if (response.getData() instanceof String) {
                            log("Server: " + response.getData());
                        } else if (response.getData() == null && msg.getAction() == ActionType.LOGOUT) {
                             log("Server: Logout Acknowledged");
                        } else {
                             if (msg.getAction() != ActionType.GET_ALL_MEMBERS &&
                                msg.getAction() != ActionType.GET_ALL_STAFF &&
                                msg.getAction() != ActionType.GET_ALL_ITEMS &&
                                msg.getAction() != ActionType.GET_LOCATIONS &&
                                msg.getAction() != ActionType.SEARCH_MEMBERS &&
                                msg.getAction() != ActionType.SEARCH_ITEMS &&
                                !(msg.getAction() == ActionType.GET_MEMBER && forMemberSelection) &&
                                msg.getAction() != ActionType.GET_STAFF &&
                                msg.getAction() != ActionType.GET_ITEM) {
                                log("Operation successful.");
                            }
                        }
                    } else {
                        log("Error from server: " + response.getErrorMessage());
                        JOptionPane.showMessageDialog(frame, "Server Error: " + response.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                         if (forMemberSelection) { // If member selection failed, clear any stale selection
                            currentlySelectedMember = null;
                            selectedMemberLabel.setText("No member selected.");
                        }
                    }
                } catch (InterruptedException e) {
                    log("Request interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                     if (cause instanceof SocketException || cause instanceof EOFException) {
                        log("Connection error with server: " + cause.getMessage());
                        showErrorAndClose("Connection lost. Please restart.", true);
                    } else {
                        log("Error processing server response: " + cause.getMessage());
                    }
                } catch (Exception e) {
                    log("Unexpected error during request processing: " + e.getMessage());
                }
            }
        }.execute();
    }

    // DisplayGenericResponse to also handle specific messages for new actions if needed
    private void displayGenericResponse(Object payload, ActionType actionType) {
        StringBuilder sb = new StringBuilder();
        if (payload == null) {
            if (actionType == ActionType.GET_ALL_MEMBERS || actionType == ActionType.GET_ALL_STAFF ||
                actionType == ActionType.GET_ALL_ITEMS || actionType == ActionType.GET_LOCATIONS ||
                actionType == ActionType.SEARCH_MEMBERS || actionType == ActionType.SEARCH_ITEMS) {
                sb.append("No records found matching your criteria.");
            } else if (actionType == ActionType.GET_MEMBER || actionType == ActionType.GET_STAFF || actionType == ActionType.GET_ITEM) {
                 sb.append("The requested record was not found.");
            }
        } else if (payload instanceof Member && !(payload instanceof StaffMember)) {
            Member member = (Member) payload;
            sb.append("\n--- Member Details ---\n");
            sb.append("ID: ").append(member.getMemberID()).append("\n");
            sb.append("Name: ").append(member.getName()).append("\n");
            sb.append("Strikes: ").append(member.getStrikes()).append("\n");
            sb.append("Account Hold: ").append(member.isAccountHold() ? "Yes" : "No").append("\n");
            sb.append("Account Banned: ").append(member.isAccountBanned() ? "Yes" : "No").append("\n");
            sb.append("Status: ").append(member.isAccountBanned() ? "Banned" :
                                          member.isAccountHold() ? "On Hold" : "Active").append("\n");
            
            // Specific messages for new actions based on the returned Member object
            if (actionType == ActionType.MARK_MEMBER_PAST_DUE) {
                sb.insert(0, "Member account updated. \n"); // Prepend status
            } else if (actionType == ActionType.MEMBER_ITEM_RETURNED) {
                // The specific "Thank you" message is shown in a JOptionPane in sendRequest.
                // This will just log the details.
                 sb.insert(0, "Member account hold status updated. \n");
            }

        } else if (payload instanceof StaffMember) {
            // ... (StaffMember handling - same as before)
            StaffMember staff = (StaffMember) payload;
            sb.append("\n--- Staff Member Details ---\n");
            sb.append("ID: ").append(staff.getMemberID()).append("\n");
            sb.append("Name: ").append(staff.getName()).append("\n");
            sb.append("Location: ").append(staff.getLocation()).append("\n");
            sb.append("Strikes: ").append(staff.getStrikes()).append("\n");
            sb.append("Account Hold: ").append(staff.isAccountHold() ? "Yes" : "No").append("\n");
            sb.append("Account Banned: ").append(staff.isAccountBanned() ? "Yes" : "No").append("\n");
            sb.append("Status: ").append(staff.isAccountBanned() ? "Banned" :
                                           staff.isAccountHold() ? "On Hold" : "Active").append("\n");
        } else if (payload instanceof Item) {
            
        	//ITEM HANDLING
            Item item = (Item) payload;
            sb.append("\n--- Item Details ---\n");
            sb.append(item.toString()).append("\n"); 
        } else if (payload instanceof List) {
            //LIST HANDLING 
            List<?> list = (List<?>) payload;
            if (list.isEmpty()) {
                sb.append("No records found in the list.\n");
            } else {
                sb.append("\n--- List Results (").append(list.size()).append(" record(s)) ---\n");
                for (Object itemInList : list) {
                    if (itemInList instanceof Member && !(itemInList instanceof StaffMember)) {
                        Member m = (Member) itemInList;
                        sb.append(String.format("Member | ID: %-10s | Name: %-20s | Strikes: %d | Status: %s%n",
                            m.getMemberID(), m.getName(), m.getStrikes(),
                            (m.isAccountBanned() ? "Banned" : m.isAccountHold() ? "On Hold" : "Active")));
                    } else if (itemInList instanceof StaffMember) {
                        StaffMember s = (StaffMember) itemInList;
                        sb.append(String.format("Staff  | ID: %-10s | Name: %-20s | Location: %-15s | Status: %s%n",
                            s.getMemberID(), s.getName(), s.getLocation(),
                            (s.isAccountBanned() ? "Banned" : s.isAccountHold() ? "On Hold" : "Active")));
                    } else if (itemInList instanceof Item) {
                        sb.append("Item   | ").append(itemInList.toString()).append("\n");
                    } else if (itemInList instanceof String) { 
                        sb.append("Location: ").append(itemInList).append("\n");
                    } else {
                        sb.append("Unknown list item: ").append(itemInList.toString()).append("\n");
                    }
                }
            }
        } else if (payload instanceof String) { 
            sb.append(payload.toString());
        }
         else {
             sb.append("Received unhandled data type for display: ").append(payload.getClass().getName());
        }
        log(sb.toString());
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        button.setAlignmentX(JComponent.CENTER_ALIGNMENT); // For BoxLayout Y_AXIS
        return button;
    }

    private void log(String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            outputArea.append(message + "\n");
        } else {
            SwingUtilities.invokeLater(() -> outputArea.append(message + "\n"));
        }
    }
   private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            log("Connection closed.");
        } catch (IOException e) {
            log("Error closing connection: " + e.getMessage());
        }
    }
    private void showErrorAndClose(String message, boolean exit) {
        JOptionPane.showMessageDialog(frame, message, "Critical Error", JOptionPane.ERROR_MESSAGE);
        if (exit) {
            closeConnection();
            System.exit(1);
        }
    }
}