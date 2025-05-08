package libraryMember;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Main client-side GUI application for the Library Management System.
 * This class handles all user interactions, server communications,
 * and UI rendering for library staff and members.
 */
public class ClientGUI extends JFrame {

    // Network connection variables
    private Socket socket;               // Connection to server
    private ObjectOutputStream out;      // For sending data to server
    private ObjectInputStream in;        // For receiving data from server
    private boolean isConnected = false; // Tracks connection status

    // User session state
    private Member currentUser;          // Currently logged-in user

    // GUI components
    private CardLayout cardLayout;       // For switching between different screens
    private JPanel mainPanel;            // Main container for all UI panels
    private JTable bookTable;            // Table to display book search results
    private DefaultListModel<Item> bookListModel = new DefaultListModel<>();     // Model for book list
    private List<Item> currentSearchResults = new java.util.ArrayList<>();       // Cached search results

    
    //Constructor 
    public ClientGUI() {
        setTitle("Library Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);     // Center window on screen

        // Create our layout for switching between screens
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize all UI panels
        initPanels();

        // Add main panel to frame and show it
        add(mainPanel);
        setVisible(true);

        // Connect to the library server
        connectToServer();
    }

    /**
     * Creates and adds all panels to the main UI container
     */
    private void initPanels() {
        mainPanel.add(loginPanel(), "login");
        mainPanel.add(dashboardPanel(), "dashboard");
        // Add more panels as needed
        mainPanel.add(bookSearchPanel(), "booksearch");
        mainPanel.add(memberSearchPanel(), "membersearch");
        mainPanel.add(addMemberPanel(), "addmember");
        mainPanel.add(addBookPanel(), "addbook");
        mainPanel.add(locationPanel(), "location");
    }


    //Establishes connection to the library server and starts listening for server messages in a separate thread

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 7777);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            isConnected = true;

            // Start a background thread to listen for server messages
            new Thread(() -> listenToServer()).start();
        } catch (IOException e) {
            showError("Failed to connect to server: " + e.getMessage());
        }
    }


    //Background thread method that continuously waits for and processes incoming messages from the server

    private void listenToServer() {
        while (isConnected) {
            try {
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    processServerMessage((Message) obj);
                }
            } catch (IOException | ClassNotFoundException e) {
                isConnected = false;
                showError("Connection lost: " + e.getMessage());
            }
        }
    }


    //Handles messages received from the server based on their headers and routes them to appropriate handlers

    private void processServerMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getPrimaryHeader()) {
                case Header.NET:
                    // Network-related responses (login results, errors)
                    if (msg.getSecondaryHeader() == Header.ACK) {
                        if (msg.getData() instanceof Member) {
                            currentUser = (Member) msg.getData();
                            JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + currentUser.getName());
                            cardLayout.show(mainPanel, "dashboard");
                        }
                    } else if (msg.getSecondaryHeader() == Header.ERR) {
                        showError("Server error: " + msg.getData());
                    }
                    break;
                case Header.INV:
                    // Inventory-related responses (book search results)
                    if (msg.getSecondaryHeader() == Header.DATA && msg.getData() instanceof ItemList) {
                        ItemList list = (ItemList) msg.getData();
                        currentSearchResults = list.getAllItems();
                        SwingUtilities.invokeLater(() -> updateBookTable());
                    }
                    break;
                case Header.ACCT:
                    // Account-related responses (member search results)
                    if (msg.getSecondaryHeader() == Header.DATA && msg.getData() instanceof Member) {
                        Member found = (Member) msg.getData();
                        SwingUtilities.invokeLater(() -> showMemberInfo(found));
                    }
                    break;
                case Header.LOC:
                    // Location-related responses
                    if (msg.getSecondaryHeader() == Header.DATA && msg.getData() instanceof String) {
                        String foundLoc = (String) msg.getData();
                        SwingUtilities.invokeLater(() -> promptAddStaffToLocation(foundLoc));
                    } else if (msg.getData() == null) {
                        showMessage("Location not found.");
                    }
                    break;
                default:
                    System.out.println("Unhandled message: " + msg);
            }
        });
    }

    
    //Creates the login panel with username/password fields and login button

    private JPanel loginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Load and add the login image
        ImageIcon originalIcon = new ImageIcon("src/guiImages/login1.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imgLabel = new JLabel(scaledIcon);

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(imgLabel, gbc);

        // Reset grid for other components
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username/ID:"), gbc);
        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        panel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        panel.add(passField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        JButton loginBtn = new JButton("Login");
        panel.add(loginBtn, gbc);

        // Handle login button click - send credentials to server
        loginBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            if (!user.isEmpty() && !pass.isEmpty()) {
                sendMessage(new Message(
                    Header.ACCT, Header.LOGIN,
                    user + "," + pass,
                    "client", "server", "server", "client"
                ));
            }
        });

        return panel;
    }


    //Creates the main dashboard with buttons for all major system functions

    private JPanel dashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome to the Library System");
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create all navigation buttons
        JButton bookSearchBtn = new JButton("Search Books");
        JButton memberSearchBtn = new JButton("Search Members");
        JButton addBookBtn = new JButton("Add Book");
        JButton addMemberBtn = new JButton("Add Member");
        JButton locationBtn = new JButton("Manage Locations");
        JButton accountBtn = new JButton("View My Account");
        JButton logoutBtn = new JButton("Logout");

        // Add action listeners to all buttons
        bookSearchBtn.addActionListener(e -> cardLayout.show(mainPanel, "booksearch"));
        memberSearchBtn.addActionListener(e -> cardLayout.show(mainPanel, "membersearch"));
        addBookBtn.addActionListener(e -> cardLayout.show(mainPanel, "addbook"));
        addMemberBtn.addActionListener(e -> cardLayout.show(mainPanel, "addmember"));
        locationBtn.addActionListener(e -> cardLayout.show(mainPanel, "location"));
        accountBtn.addActionListener(e -> showAccountInfo());
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(mainPanel, "login");
        });

        // Add spacing and components to panel
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(welcome);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(bookSearchBtn);
        panel.add(memberSearchBtn);
        panel.add(addBookBtn);
        panel.add(addMemberBtn);
        panel.add(locationBtn);
        panel.add(accountBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(logoutBtn);

        return panel;
    }


     //Displays a dialog showing the current user's account information
     //Like name, ID, account status, and location (for staff)

    private void showAccountInfo() {
        if (currentUser == null) return;

        // Build account information string
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(currentUser.getName()).append("\n")
          .append("ID: ").append(currentUser.getMemberID()).append("\n")
          .append("Status: ").append(
              currentUser.isAccountBanned() ? "BANNED" :
              currentUser.isAccountHold() ? "ON HOLD" : "ACTIVE").append("\n")
          .append("Strikes: ").append(currentUser.getStrikes());

        // Add location info for staff members
        if (currentUser instanceof StaffMember) {
            sb.append("\nLocation: ").append(((StaffMember) currentUser).getLocation());
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "My Account", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Sends a message to the server
     */
    private void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            showError("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Shows an error message dialog
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a regular information message dialog
     */
    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    /**
     * Application entry point - starts the GUI on the EDT
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
    

    //Creates the book search panel with search field, results table, and action buttons
    private JPanel bookSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();

        // Search controls
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Title", "ID"});
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");

        top.add(new JLabel("Search by:"));
        top.add(searchType);
        top.add(searchField);
        top.add(searchBtn);

        // Results table
        String[] columns = {"Title", "Author", "Year", "Item ID", "Owned By"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        bookTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);

        // Bottom buttons
        JPanel bottom = new JPanel();
        JButton actionBtn = new JButton("Action");
        JButton backBtn = new JButton("Back");
        bottom.add(actionBtn);
        bottom.add(backBtn);

        // Search button handler - sends search request to server
        searchBtn.addActionListener(e -> {
            String value = searchField.getText().trim();
            if (value.isEmpty()) return;

            String type = searchType.getSelectedItem().equals("Title") ? "title" : "id";
            Message msg = new Message(
                Header.INV, Header.GET, type + "," + value,
                "client", "server", "server", "client"
            );
            sendMessage(msg);
        });

        // Action button handler - show options for selected book
        actionBtn.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row == -1 || row >= currentSearchResults.size()) return;

            Item selected = currentSearchResults.get(row);
            showBookActionDialog(selected);
        });

        // Back button handler
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));

        // Arrange components in the panel
        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }
    

    //Updates the book table with current search results
    private void updateBookTable() {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.setRowCount(0);  // Clear existing rows

        // Add each book to the table
        for (Item item : currentSearchResults) {
            model.addRow(new Object[]{
                item.getTitle(),
                item.getAuthor(),
                item.getYear(),
                item.getItemID(),
                item.getOwnedBy() == null ? "Available" : item.getOwnedBy()
            });
        }
    }
    
    //Shows a dialog with actions for a selected book (checkout, return, reserve, etc.)

    private void showBookActionDialog(Item item) {
        // Create options based on book status
        String[] options = new String[]{
            item.getOwnedBy() == null ? "Check Out" : "Return",
            item.getReservedBy().contains(currentUser.getMemberID()) ? "Cancel Reservation" : "Reserve",
            "Change Location", "Cancel"
        };
        int choice = JOptionPane.showOptionDialog(this, item.toString(), "Book Actions",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        Message actionMessage = null;
        switch (choice) {
            case 0: // Checkout / Return
                actionMessage = new Message(
                    Header.ITEM,
                    item.getOwnedBy() == null ? Header.CHECKOUT : Header.CHECKIN,
                    item.getItemID() + "," + currentUser.getMemberID(),
                    "client", "server", "server", "client"
                );
                break;
            case 1: // Reserve / Cancel
                actionMessage = new Message(
                    Header.ITEM, Header.RESERVE,
                    item.getItemID() + "," + currentUser.getMemberID(),
                    "client", "server", "server", "client"
                );
                break;
            case 2: // Change location
                String loc = JOptionPane.showInputDialog(this, "Enter new location:");
                if (loc != null && !loc.trim().isEmpty()) {
                    actionMessage = new Message(
                        Header.INV, Header.TRANSFER,
                        item.getItemID() + "," + loc,
                        "client", "server", "server", "client"
                    );
                }
                break;
            default:
                return;
        }

        // Send the action to the server if one was selected
        if (actionMessage != null) {
            sendMessage(actionMessage);
        }
    }
    

    //Creates the member search panel - allows searching for members by ID and viewing their details

    private JPanel memberSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel();

        // Search controls
        JTextField memberField = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        JButton backBtn = new JButton("Back");

        top.add(new JLabel("Enter Member ID:"));
        top.add(memberField);
        top.add(searchBtn);
        top.add(backBtn);

        // Results area
        JTextArea resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Search button handler - request member info from server
        searchBtn.addActionListener(e -> {
            String id = memberField.getText().trim();
            if (!id.isEmpty()) {
                Message msg = new Message(
                    Header.ACCT, Header.GET,
                    id,
                    "client", id, "server", "client"
                );
                sendMessage(msg);
            }
        });

        // Back button handler
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));

        return panel;
    }
    

    //Displays member information and provides options to modify their account (place/remove hold, change location, etc.)
    private void showMemberInfo(Member member) {
        // Build member info display
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(member.getName()).append("\n")
          .append("ID: ").append(member.getMemberID()).append("\n")
          .append("Status: ").append(
              member.isAccountBanned() ? "BANNED" :
              member.isAccountHold() ? "ON HOLD" : "ACTIVE").append("\n")
          .append("Strikes: ").append(member.getStrikes()).append("\n");

        // Add staff-specific info if applicable
        if (member instanceof StaffMember) {
            sb.append("Staff at: ").append(((StaffMember) member).getLocation()).append("\n");
        }

        // Create action options based on current status
        String[] actions = new String[]{
            member.isAccountHold() ? "Remove Hold" : "Place Hold",
            (member instanceof StaffMember) ? "Change Location" : "Promote to Staff",
            "Cancel"
        };

        int choice = JOptionPane.showOptionDialog(
            this, sb.toString(), "Member Info",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, actions, actions[0]
        );

        Message actionMessage = null;
        switch (choice) {
            case 0: // Toggle account hold status
                member.setAccountHold(!member.isAccountHold());
                actionMessage = new Message(
                    Header.ACCT, Header.EDIT,
                    member,
                    "client", "server", "server", "client"
                );
                break;

            case 1: // Change location or promote
                if (member instanceof StaffMember) {
                    // Update staff location
                    String newLoc = JOptionPane.showInputDialog(this, "New Location:");
                    if (newLoc != null && !newLoc.trim().isEmpty()) {
                        ((StaffMember) member).setLocation(newLoc);
                        actionMessage = new Message(
                            Header.ACCT, Header.EDIT,
                            member,
                            "client", "server", "server", "client"
                        );
                    }
                } else {
                    // Promote regular member to staff
                    actionMessage = new Message(
                        Header.ACCT, Header.MAKESTAFF,
                        member.getMemberID(),
                        "client", "server", "server", "client"
                    );
                }
                break;

            default:
                return;
        }

        // Send the action to the server if one was selected
        if (actionMessage != null) {
            sendMessage(actionMessage);
        }
    }
    
    //Creates the panel for adding new members to the system

    private JPanel addMemberPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel nameLabel = new JLabel("Member Name:");
        JTextField nameField = new JTextField(20);
        JButton submitBtn = new JButton("Add Member");
        JButton backBtn = new JButton("Back");

        // Layout components
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(nameLabel, gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(submitBtn, gbc);
        gbc.gridx = 1; panel.add(backBtn, gbc);

        // Submit button handler - create new member
        submitBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Name cannot be empty.");
                return;
            }

            Message msg = new Message(
                Header.ACCT, Header.CREATE,
                name,
                "client", "server", "server", "client"
            );
            sendMessage(msg);
            nameField.setText("");  // Clear the field after submission
        });

        // Back button handler
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));

        return panel;
    }
    

    //Creates the panel for adding new books to the library inventory
    private JPanel addBookPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Form labels
        JLabel titleLabel = new JLabel("Title:");
        JLabel authorLabel = new JLabel("Author:");
        JLabel yearLabel = new JLabel("Year:");
        JLabel quantityLabel = new JLabel("Quantity:");

        // Form fields
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField yearField = new JTextField(10);
        JTextField quantityField = new JTextField(5);

        // Buttons
        JButton addBtn = new JButton("Add Book");
        JButton backBtn = new JButton("Back");

        // Layout components
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(titleLabel, gbc);
        gbc.gridx = 1; panel.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(authorLabel, gbc);
        gbc.gridx = 1; panel.add(authorField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(yearLabel, gbc);
        gbc.gridx = 1; panel.add(yearField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(quantityLabel, gbc);
        gbc.gridx = 1; panel.add(quantityField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(addBtn, gbc);
        gbc.gridx = 1; panel.add(backBtn, gbc);

        // Add button handler - validate and send new book info
        addBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String year = yearField.getText().trim();
            String quantityStr = quantityField.getText().trim();

            // Input validation
            if (title.isEmpty() || author.isEmpty() || year.isEmpty() || quantityStr.isEmpty()) {
                showError("All fields must be filled.");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError("Quantity must be a non-negative number.");
                return;
            }

            // Send book creation request
            Message msg = new Message(
                Header.INV, Header.CREATE,
                title + "," + year + "," + author + "," + quantity,
                "client", "server", "server", "client"
            );

            sendMessage(msg);

            // Clear fields after submission
            titleField.setText("");
            authorField.setText("");
            yearField.setText("");
            quantityField.setText("");
        });

        // Back button handler
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));

        return panel;
    }
    
    
    //Creates the panel for managing library locations adding new locations and assigning staff

    private JPanel locationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Controls for adding a location
        JLabel addLabel = new JLabel("Add Location:");
        JTextField addField = new JTextField(15);
        JButton addBtn = new JButton("Add");

        // Controls for searching a location
        JLabel searchLabel = new JLabel("Search Location:");
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");

        JButton backBtn = new JButton("Back");

        // Layout components
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(addLabel, gbc);
        gbc.gridx = 1; panel.add(addField, gbc);
        gbc.gridx = 2; panel.add(addBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(searchLabel, gbc);
        gbc.gridx = 1; panel.add(searchField, gbc);
        gbc.gridx = 2; panel.add(searchBtn, gbc);

        gbc.gridx = 1; gbc.gridy = 2; panel.add(backBtn, gbc);

        // Add location button handler
        addBtn.addActionListener(e -> {
            String loc = addField.getText().trim();
            if (!loc.isEmpty()) {
                sendMessage(new Message(
                    Header.LOC, Header.CREATE,
                    loc, "client", "server", "server", "client"
                ));
                addField.setText("");  // Clear field after submission
            } else {
                showError("Enter a location name.");
            }
        });

        // Search location button handler
        searchBtn.addActionListener(e -> {
            String loc = searchField.getText().trim();
            if (!loc.isEmpty()) {
                sendMessage(new Message(
                    Header.LOC, Header.GET,
                    loc, "client", "server", "server", "client"
                ));
            } else {
                showError("Enter a location to search.");
            }
        });

        // Back button handler
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));

        return panel;
    }
    
    
     //Shows a dialog to assign a staff member to a location after a location search result is received
    private void promptAddStaffToLocation(String locationName) {
        int result = JOptionPane.showConfirmDialog(this,
            "Location '" + locationName + "' found. Add staff?",
            "Add Staff", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            String staffID = JOptionPane.showInputDialog(this, "Enter Staff Member ID:");
            if (staffID != null && !staffID.trim().isEmpty()) {
                sendMessage(new Message(
                    Header.LOC, Header.ADD,
                    staffID.trim() + "," + locationName,
                    "client", "server", "server", "client"
                ));
            }
        }
    }
}