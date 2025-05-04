package libraryMember;

import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.CardLayout; 
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;

    private JTextField userIdField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JTextArea outputArea;

    private ObjectOutputStream outSocket;
    private ObjectInputStream inSocket;

    private Member loggedInMember = null;

    public ClientGUI() {
        setTitle("Library Client System");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initNetwork();
        buildGUI();
        setVisible(true);
    }

    private void initNetwork() {
        try {
            Socket socket = new Socket("localhost", 7777);
            outSocket = new ObjectOutputStream(socket.getOutputStream());
            inSocket = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to server.");
            Message initial = (Message) inSocket.readObject();
            if (!initial.getPrimaryHeader().equals(Header.ACCT)) {
                throw new IOException("Unexpected initial message from server.");
            }
        } catch (Exception e) {
            showError("Failed to connect or handshake with server: " + e.getMessage());
            System.exit(1);
        }
    }

    private void buildGUI() {
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        buildLoginScreen();
        buildMainScreen();
        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
    }

    private void buildLoginScreen() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel titleLabel = new JLabel("Library Login");
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        userIdField = new JTextField();
        userIdField.setColumns(1);
        passwordField = new JPasswordField();
        passwordField.setColumns(1);
        
        statusLabel = new JLabel(" ");
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");

        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        signupBtn.setAlignmentX(CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> loginUser());
        signupBtn.addActionListener(e -> signupUser());

        loginPanel.add(titleLabel);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(new JLabel("User ID:"));
        loginPanel.add(userIdField);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(loginBtn);
        loginPanel.add(signupBtn);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(statusLabel);

        mainPanel.add(loginPanel, "Login");
    }

    private void buildMainScreen() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JLabel label = new JLabel("Welcome to Library System");
        label.setAlignmentX(CENTER_ALIGNMENT);
        main.add(label);

        String[] actions = {
            "Add Member", "Search Member",
            "Add Item", "Search Item",
            "Add Location", "Search Location"
        };

        for (String action : actions) {
            JButton button = new JButton(action);
            button.setAlignmentX(CENTER_ALIGNMENT);
            button.addActionListener(e -> handleAction(action));
            main.add(Box.createVerticalStrut(10));
            main.add(button);
        }

        outputArea = new JTextArea(8, 40);
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);
        main.add(Box.createVerticalStrut(15));
        main.add(scroll);

        mainPanel.add(main, "Main");
    }

    private void loginUser() {
        String id = userIdField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        try {
            Message msg = new Message(Header.ACCT, Header.LOGIN, id + "," + pass,
                    "client", "login", "server", "client");
            outSocket.writeObject(msg);
            Message response = (Message) inSocket.readObject();

            if (response.getPrimaryHeader() == Header.NET &&
                response.getSecondaryHeader() == Header.ACK &&
                response.getData() instanceof Member) {
                loggedInMember = (Member) response.getData();
                statusLabel.setText("Welcome, " + loggedInMember.getName());
                cardLayout.show(mainPanel, "Main");
            } else {
                statusLabel.setText("Login failed: " + response.getData());
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
        }
    }

    private void signupUser() {
        String name = JOptionPane.showInputDialog(this, "Enter your name:");
        if (name == null || name.isBlank()) return;

        try {
            Message msg = new Message(Header.ACCT, Header.CREATE, name,
                    "client", "create", "server", "client");
            outSocket.writeObject(msg);
            Message response = (Message) inSocket.readObject();

            if (response.getPrimaryHeader() == Header.NET &&
                response.getSecondaryHeader() == Header.ACK) {
                JOptionPane.showMessageDialog(this, "Account created. Your info:\n" + response.getData());
            } else {
                showError("Signup failed: " + response.getData());
            }
        } catch (Exception e) {
            showError("Signup error: " + e.getMessage());
        }
    }

    private void handleAction(String label) {
        String prompt = switch (label) {
            case "Add Member" -> "Enter new member's name:";
            case "Search Member" -> "Enter member ID:";
            case "Add Item" -> "Enter item as Title,Year,Author,Quantity:";
            case "Search Item" -> "Enter item title:";
            case "Add Location" -> "Enter location name:";
            case "Search Location" -> "Enter location name:";
            default -> "Enter data:";
        };

        String input = JOptionPane.showInputDialog(this, prompt);
        if (input == null || input.isBlank()) return;

        Header header = label.contains("Item") ? Header.INV : Header.ACCT;
        Header sub = label.contains("Search") ? Header.GET : Header.CREATE;

        try {
            Message msg = new Message(header, sub, input, "client", "menu", "server", "client");
            outSocket.writeObject(msg);
            Message response = (Message) inSocket.readObject();
            outputArea.append("➤ " + label + ": " + response.getData() + "\n");
        } catch (Exception e) {
            showError("Action failed: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}