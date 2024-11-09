import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatRoomWindow extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int FILE_PORT = 12346;  // 파일 전송을 위한 별도 포트

    private BufferedReader in;
    private PrintWriter out;
    private JTextPane chatArea;
    private JTextField inputField;
    private String userName;
    private String roomName;
    private Socket socket;

    public ChatRoomWindow(String roomName, String userName) {
        this.roomName = roomName;
        this.userName = userName;
        setupUI(roomName);
        connectToServer();
    }

    private void setupUI(String roomName) {
        setTitle(roomName);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());

        JButton fileButton = new JButton("+"); // 파일 전송 버튼
        fileButton.addActionListener(e -> sendFile());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(fileButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 서버에 채팅방 이름과 사용자 이름을 처음에 전송
            out.println(roomName);
            out.println(userName);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("FILE:")) {
                            String[] parts = message.split(":", 3);
                            String sender = parts[1];
                            String fileName = parts[2];
                            displayFileButton(sender, fileName);
                        } else {
                            appendMessage(message);
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("Connection closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to connect to server. Please ensure the server is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty() && out != null) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Socket fileSocket = new Socket(SERVER_ADDRESS, FILE_PORT);
                 DataOutputStream dataOut = new DataOutputStream(fileSocket.getOutputStream());
                 FileInputStream fileIn = new FileInputStream(file)) {

                dataOut.writeUTF(roomName);
                dataOut.writeUTF(userName);
                dataOut.writeUTF(file.getName());
                byte[] fileData = fileIn.readAllBytes();
                dataOut.writeInt(fileData.length);
                dataOut.write(fileData);
                dataOut.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayFileButton(String sender, String fileName) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();

            JButton fileButton = new JButton(sender + ": " + fileName);
            fileButton.addActionListener(e -> saveFile(fileName));

            doc.insertString(doc.getLength(), "\n", attrs);
            chatArea.setCaretPosition(doc.getLength());
            chatArea.insertComponent(fileButton);
            doc.insertString(doc.getLength(), "\n", attrs);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String message) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), message + "\n", null);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Socket fileSocket = new Socket(SERVER_ADDRESS, FILE_PORT);
                 DataInputStream dataIn = new DataInputStream(fileSocket.getInputStream());
                 BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file))) {

                dataIn.readUTF();  // 방 이름 읽기
                dataIn.readUTF();  // 파일 이름 읽기
                int fileLength = dataIn.readInt();
                byte[] fileData = new byte[fileLength];
                dataIn.readFully(fileData);
                fileOut.write(fileData);
                fileOut.flush();
                JOptionPane.showMessageDialog(this, "File downloaded: " + file.getName());

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to download file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}