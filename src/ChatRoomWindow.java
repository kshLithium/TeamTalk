import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Base64;

public class ChatRoomWindow extends JFrame {
    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 스트림
    private PrintWriter out; // 서버로 메시지를 보내기 위한 스트림
    private JTextPane chatArea; // 채팅 메시지를 표시하는 창
    private JTextField inputField; // 사용자 입력 필드
    private String userName; // 사용자 이름
    private String roomName; // 채팅방 이름
    private Socket socket; // 서버와 연결된 소켓

    public ChatRoomWindow(String roomName, String userName, String serverIp, int serverPort) {
        this.roomName = roomName;
        this.userName = userName;
        setupUI(); // 사용자 인터페이스 초기화
        connectToServer(serverIp, serverPort); // 서버 연결
    }

    private void setupUI() {
        setTitle(roomName); // 윈도우 제목 설정
        setSize(600, 400); // 윈도우 크기 설정
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 닫기 동작 설정

        // 채팅 메시지 표시 창 설정
        chatArea = new JTextPane();
        chatArea.setEditable(false); // 메시지 창은 편집 불가
        add(new JScrollPane(chatArea), BorderLayout.CENTER); // 스크롤 추가

        // 입력 패널 설정
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage()); // Enter 키로 메시지 전송

        // 파일 전송 버튼 추가
        JButton fileButton = new JButton("Send File");
        fileButton.addActionListener(e -> sendFile());

        // 파일 저장 버튼 추가
        JButton saveButton = new JButton("Save File");
        saveButton.addActionListener(e -> saveFile());

        // 버튼 패널에 버튼 추가
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(fileButton);
        buttonPanel.add(saveButton);

        // 입력 필드와 버튼 패널을 입력 패널에 추가
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // 입력 패널을 메인 윈도우에 추가
        add(inputPanel, BorderLayout.SOUTH);
        setVisible(true); // 윈도우 표시
    }

    private void connectToServer(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort); // 서버와 소켓 연결
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 입력 스트림 초기화
            out = new PrintWriter(socket.getOutputStream(), true); // 출력 스트림 초기화

            // 서버에 채팅방 이름과 사용자 이름 전송
            out.println(roomName);
            out.println(userName);

            // 메시지 수신 스레드 시작
            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server.", "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/file ")) {
                    handleIncomingFile(message.substring(6)); // 파일 메시지 처리
                } else {
                    appendMessage(message); // 일반 메시지 처리
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim(); // 입력 필드 내용 가져오기
        if (!message.isEmpty() && out != null) {
            out.println(message); // 서버로 메시지 전송
            inputField.setText(""); // 입력 필드 초기화
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser(); // 파일 선택 창
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile(); // 선택된 파일
            try {
                // 파일 내용 인코딩 및 서버로 전송
                String encodedContent = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                out.println("/file " + file.getName());
                out.println(encodedContent);
                appendMessage("파일 전송: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "파일 전송 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleIncomingFile(String fileMessage) {
        String[] parts = fileMessage.split(" ", 2); // 파일 이름과 내용 분리
        if (parts.length < 2) {
            return; // 메시지 형식 오류
        }
        String fileName = parts[0];
        String fileContent = parts[1];

        // 사용자에게 파일 저장 여부 확인
        int option = JOptionPane.showConfirmDialog(
                this,
                "파일을 저장하시겠습니까? (" + fileName + ")",
                "파일 수신",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser(); // 파일 저장 창
            fileChooser.setSelectedFile(new File(fileName));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    // 파일 디코딩 및 저장
                    File saveFile = fileChooser.getSelectedFile();
                    byte[] decodedContent = Base64.getDecoder().decode(fileContent);
                    Files.write(saveFile.toPath(), decodedContent);
                    JOptionPane.showMessageDialog(this, "파일이 성공적으로 저장되었습니다: " + saveFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "파일 저장 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void saveFile() {
        JOptionPane.showMessageDialog(this, "현재는 수신된 파일만 저장할 수 있습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
    }

    private void appendMessage(String message) {
        try {
            StyledDocument doc = chatArea.getStyledDocument(); // 채팅 영역의 문서 가져오기
            doc.insertString(doc.getLength(), message + "\n", null); // 메시지 추가
            chatArea.setCaretPosition(doc.getLength()); // 스크롤을 최신 메시지로 이동
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
