import javax.swing.*; // GUI 컴포넌트 라이브러리 임포트
import java.awt.*; // 그래픽 관련 라이브러리 임포트
import java.io.*; // 입출력 처리 라이브러리 임포트
import java.net.*; // 네트워크 소켓 관련 라이브러리 임포트

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost"; // 서버 주소 설정
    private static final int SERVER_PORT = 12345; // 서버 포트 설정

    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 BufferedReader
    private PrintWriter out; // 서버로 메시지를 전송하기 위한 PrintWriter
    private JFrame frame; // 클라이언트의 메인 GUI 창
    private JTextArea chatArea; // 채팅 메시지를 표시할 텍스트 영역
    private JTextField inputField; // 사용자 입력 필드
    private String userName; // 사용자 이름

    // 생성자: 사용자 이름을 입력받고, UI를 설정한 뒤 서버에 연결
    public ChatClient(String roomName) {
        this.userName = promptForUserName(); // 사용자 이름을 입력받음
        setupUI(roomName); // UI 설정
        connectToServer(); // 서버에 연결
    }

    // 사용자 이름을 입력받기 위한 메서드 (팝업창으로 입력)
    private String promptForUserName() {
        return JOptionPane.showInputDialog(
                null,
                "Enter your name:", // 입력창 메시지
                "Username", // 입력창 제목
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // GUI 구성 요소를 설정하는 메서드
    private void setupUI(String roomName) {
        frame = new JFrame(roomName); // 채팅방 이름을 창 제목으로 설정
        frame.setSize(400, 300); // 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 창 닫기 시 창만 닫힘

        chatArea = new JTextArea(); // 채팅 메시지를 표시할 텍스트 영역 생성
        chatArea.setEditable(false); // 텍스트 영역은 읽기 전용으로 설정
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER); // 스크롤이 가능한 영역으로 추가

        inputField = new JTextField(); // 메시지 입력 필드 생성
        inputField.addActionListener(e -> sendMessage()); // Enter 키 입력 시 메시지 전송
        frame.add(inputField, BorderLayout.SOUTH); // 입력 필드를 창의 하단에 배치

        frame.setVisible(true); // 창을 화면에 표시
    }

    // 서버에 연결하는 메서드
    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 서버에 연결
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 서버로부터 입력 스트림 설정
            out = new PrintWriter(socket.getOutputStream(), true); // 서버로 출력 스트림 설정

            // 서버로부터 메시지를 비동기적으로 수신하는 스레드
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) { // 메시지를 반복적으로 읽음
                        chatArea.append(message + "\n"); // 읽은 메시지를 화면에 추가
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메시지를 전송하는 메서드
    private void sendMessage() {
        String message = inputField.getText(); // 입력 필드의 텍스트 가져오기
        if (!message.isEmpty()) { // 빈 메시지가 아닐 경우
            out.println(userName + ": " + message); // 사용자 이름과 함께 메시지 전송
            inputField.setText(""); // 입력 필드 비우기
        }
    }
}