import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private String userName;

    public ChatClient(String roomName) {
        this.userName = promptForUserName();
        setupUI(roomName);
        connectToServer();
    }

    private String promptForUserName() {
        return JOptionPane.showInputDialog(
                null,
                "Enter your name:",
                "Username",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void setupUI(String roomName) {
        frame = new JFrame(roomName);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        frame.add(inputField, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        chatArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(userName + ": " + message);
            inputField.setText("");
        }
    }
}