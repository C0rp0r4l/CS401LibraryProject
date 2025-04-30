package libraryMember;

import java.io.*;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ClientGUI {

    private static final String serverAddress = "localhost"; // this is where the server is supposed to be running
    private static final int Port = 7777; // the port number the server is listening on
    private boolean isConnected = false; // flag to keep track of whether we’re currently connected

    private Socket socket; // the connection itself
    private ObjectOutputStream outSocket; // this is how we’ll send messages to the server
    private ObjectInputStream inSocket; // and this is how we’ll receive messages back

    private JFrame frame; // the main window for our GUI
    private JTextArea outputArea; // this is the box that shows output like server responses

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new); // this makes sure our GUI launches on the correct thread
    }

    public ClientGUI() {
        setupGUI(); // set up all the GUI components
        connectToServer(); // try to establish a connection to the backend server
        performLogin(); // immediately try to log in once connected
    }

    private void setupGUI() {
        frame = new JFrame("Library Member Client"); // make the main window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close everything on exit
        frame.setSize(500, 400); // reasonable default window size

        outputArea = new JTextArea(); // this is where feedback gets printed
        outputArea.setEditable(false); // we don’t want users typing in this area
        JScrollPane scrollPane = new JScrollPane(outputArea); // makes the output scrollable if it gets long

        JPanel buttonPanel = new JPanel(); // this panel holds all the action buttons
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // stack buttons vertically

        buttonPanel.add(makeButton("1. Create Member", () -> promptAndSend("Enter new member name:", CommandEnum.add)));
        buttonPanel.add(makeButton("2. Delete Member", () -> promptAndSend("Enter member ID to delete:", CommandEnum.remove)));
        buttonPanel.add(makeButton("3. Check Standing", () -> promptAndSend("Enter member ID:", CommandEnum.getStanding)));
        buttonPanel.add(makeButton("4. View Rented Items", () -> promptAndSend("Enter member ID:", CommandEnum.viewItems)));
        buttonPanel.add(makeButton("5. View Waitlisted Items", () -> promptAndSend("Enter member ID:", CommandEnum.getStanding))); // placeholder reused
        buttonPanel.add(makeButton("6. Logout", this::logout)); // exits the session

        JPanel mainPanel = new JPanel(); // the top level panel that holds everything
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // vertical layout for scroll area and buttons
        mainPanel.add(scrollPane); // add the output box first
        mainPanel.add(buttonPanel); // then the action buttons

        frame.setContentPane(mainPanel); // tell the frame to use our main panel
        frame.setVisible(true); // show the window
    }

    private JButton makeButton(String label, Runnable action) {
        JButton button = new JButton(label); // make a button with text
        button.addActionListener(e -> action.run()); // run the given action when clicked
        button.setAlignmentX(JComponent.CENTER_ALIGNMENT); // center it nicely in layout
        return button;
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, Port); // try to connect to the server
            outSocket = new ObjectOutputStream(socket.getOutputStream()); // prepare to send
            inSocket = new ObjectInputStream(socket.getInputStream()); // prepare to receive
            isConnected = true; // we’re good
            log("Connected to server at " + serverAddress + ":" + Port); // feedback for user
        } catch (IOException e) {
            log("Connection failed: " + e.getMessage()); // couldn’t connect
            showErrorAndExit("Could not connect to server"); // show a popup and shut down
        }
    }

    private void performLogin() {
        try {
            Message loginMsg = new Message(HeaderEnum.login, CommandEnum.login, StatusEnum.toServer, "login request"); // build the login message
            sendMessage(loginMsg); // send it
            Message response = (Message) inSocket.readObject(); // wait for reply

            if (response.getStatus() == StatusEnum.success) {
                log("Login successful: " + response.getText()); // great we’re in
            } else {
                log("Login failed: " + response.getText()); // didn’t go through
                showErrorAndExit("Login failed. Client will exit"); // end session immediately
            }
        } catch (IOException | ClassNotFoundException e) {
            showErrorAndExit("Login error: " + e.getMessage()); // something broke during login
        }
    }

    private void promptAndSend(String prompt, CommandEnum command) {
        String input = JOptionPane.showInputDialog(frame, prompt); // ask the user for input
        if (input != null && !input.trim().isEmpty()) { // make sure they actually typed something
            HeaderEnum header = HeaderEnum.member;
            if (command == CommandEnum.logout || command == CommandEnum.login) {
                header = HeaderEnum.login; // certain commands need the login header instead
            }
            sendAndReceive(new Message(header, command, StatusEnum.toServer, input.trim())); // wrap it in a Message and go
        }
    }

    private void logout() {
        sendAndReceive(new Message(HeaderEnum.login, CommandEnum.logout, StatusEnum.toServer, "logout request")); // tell server we’re logging out
        isConnected = false; // no longer connected
        frame.dispose(); // close window
        System.exit(0); // full exit
    }

    private void sendAndReceive(Message message) {
        if (!isConnected) {
            log("Not connected to server"); // we can’t do anything if there’s no connection
            return;
        }
        try {
            sendMessage(message); // send to server
            Message response = (Message) inSocket.readObject(); // get reply
            log("Server: " + response.getText()); // print response
        } catch (IOException | ClassNotFoundException e) {
            log("Communication error: " + e.getMessage()); // connection went bad
        }
    }

    private void sendMessage(Message message) throws IOException {
        outSocket.writeObject(message); // send object over the stream
        outSocket.flush(); // make sure it’s actually sent
    }

    private void log(String text) {
        outputArea.append(text + "\n"); // this prints messages into the scrolling output area
    }

    private void showErrorAndExit(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE); // popup with message
        System.exit(1); // shut down
    }
}
