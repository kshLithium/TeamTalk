import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatRoomWindow extends JFrame {
    private static final String SERVER_ADDRESS = "localhost"; // 서버 주소
    private static final int SERVER_PORT = 12345; // 서버 포트

    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 입력 스트림
    private PrintWriter out; // 서버로 메시지를 보내기 위한 출력 스트림
    private JTextPane chatArea; // 채팅 메시지를 표시하는 영역
    private JTextField inputField; // 사용자 메시지 입력 필드
    private String userName; // 사용자 이름
    private String roomName; // 채팅방 이름
    private Socket socket; // 서버와의 소켓 연결

    // 생성자: 채팅방 이름과 사용자 이름을 받아 UI 초기화 및 서버 연결
    public ChatRoomWindow(String roomName, String userName) {
        this.roomName = roomName; // 채팅방 이름 설정
        this.userName = userName; // 사용자 이름 설정
        setupUI(roomName); // UI 설정
        connectToServer(); // 서버에 연결
    }

    // UI 구성 요소 설정 메서드
    private void setupUI(String roomName) {
        setTitle(roomName); // 창 제목을 채팅방 이름으로 설정
        setSize(500, 400); // 창 크기 설정
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 창 닫기 시 자원 해제

        chatArea = new JTextPane(); // 채팅 메시지 영역 초기화
        chatArea.setEditable(false); // 읽기 전용으로 설정
        add(new JScrollPane(chatArea), BorderLayout.CENTER); // 스크롤 가능한 영역으로 추가

        JPanel inputPanel = new JPanel(new BorderLayout()); // 입력 패널 생성
        inputField = new JTextField(); // 메시지 입력 필드 생성
        inputField.addActionListener(e -> sendMessage()); // Enter 키로 메시지 전송 설정

        inputPanel.add(inputField, BorderLayout.CENTER); // 입력 필드를 패널 중앙에 추가
        add(inputPanel, BorderLayout.SOUTH); // 입력 패널을 창 하단에 추가

        setVisible(true); // 창을 화면에 표시
    }

    // 서버에 연결하는 메서드
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 서버에 소켓 연결 생성
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 서버로부터 입력 스트림 설정
            out = new PrintWriter(socket.getOutputStream(), true); // 서버로 출력 스트림 설정

            out.println(roomName); // 방 이름을 서버에 전송
            out.println(userName); // 사용자 이름을 서버에 전송

            new Thread(this::receiveMessages).start(); // 서버로부터 메시지를 비동기적으로 수신하는 스레드 시작

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server.", "Connection Error", JOptionPane.ERROR_MESSAGE); // 연결 오류 시 경고 메시지
            e.printStackTrace();
        }
    }

    // 서버로부터 메시지를 수신하여 채팅창에 표시하는 메서드
    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) { // 서버로부터 메시지를 반복적으로 읽음
                appendMessage(message); // 채팅창에 메시지 추가
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 서버로 메시지를 전송하는 메서드
    private void sendMessage() {
        String message = inputField.getText().trim(); // 입력 필드의 텍스트 가져와서 공백 제거
        if (!message.isEmpty() && out != null) { // 빈 메시지가 아니면 전송
            out.println(message); // 메시지 서버에 전송
            inputField.setText(""); // 입력 필드 비우기
        }
    }

    // 채팅창에 메시지를 추가하는 메서드
    private void appendMessage(String message) {
        try {
            StyledDocument doc = chatArea.getStyledDocument(); // 채팅 메시지 영역의 문서 객체 가져오기
            doc.insertString(doc.getLength(), message + "\n", null); // 메시지 추가
            chatArea.setCaretPosition(doc.getLength()); // 스크롤을 최신 메시지로 이동
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}