import javax.swing.*; // GUI 컴포넌트를 위한 스윙 라이브러리 임포트
import java.awt.*; // AWT 그래픽 구성 요소 라이브러리 임포트
import java.awt.event.*; // 이벤트 처리를 위한 라이브러리 임포트
import java.util.HashMap; // HashMap 자료구조 임포트 (채팅방 관리를 위해 사용)

public class ChatAppMain extends JFrame {
    private JList<String> chatRoomList; // 채팅방 목록을 표시하는 JList 컴포넌트
    private DefaultListModel<String> chatRoomModel; // 채팅방 목록의 데이터 모델
    private HashMap<String, ChatRoomWindow> openChatRooms; // 열린 채팅방을 관리하는 HashMap (방 이름: 창 객체)
    private String userName; // 사용자 이름
    private String serverIp;
    private int serverPort;

    // 생성자: 사용자 이름을 인자로 받아 UI 설정 및 초기화
    public ChatAppMain(String userName, String serverIp, int serverPort) {
        this.userName = userName; // StartScreen에서 전달받은 사용자 이름
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        setTitle("채팅 앱 - 유저 " + userName); // 창 제목 설정
        setSize(400, 300); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 종료 시 애플리케이션 종료 설정

        chatRoomModel = new DefaultListModel<>(); // 채팅방 모델 생성
        // 채팅방 목록에 예시 방 추가

        // 테스트용 3개 채팅룸
        chatRoomModel.addElement("채팅방 1");
        chatRoomModel.addElement("채팅방 2");
        chatRoomModel.addElement("채팅방 3");

        chatRoomList = new JList<>(chatRoomModel); // 채팅방 목록을 JList에 추가
        chatRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 단일 선택 모드로 설정
        openChatRooms = new HashMap<>(); // 열린 채팅방 관리를 위한 HashMap 초기화

        // 채팅방을 더블 클릭하여 열 수 있도록 마우스 리스너 추가
        chatRoomList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 더블 클릭 시
                    String selectedRoom = chatRoomList.getSelectedValue(); // 선택한 채팅방 이름 가져오기
                    openChatRoom(selectedRoom); // 채팅방 열기
                }
            }
        });

        // 채팅방 목록을 스크롤 가능하도록 추가
        add(new JScrollPane(chatRoomList), BorderLayout.CENTER);
    }

    // main 메서드: 애플리케이션 시작 지점
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartScreen().setVisible(true); // StartScreen을 통해 애플리케이션 시작
        });
    }

    // 채팅방 열기 메서드
    private void openChatRoom(String roomName) {
        if (!openChatRooms.containsKey(roomName)) { // 해당 채팅방이 열려 있지 않은 경우
            ChatRoomWindow chatRoomWindow = new ChatRoomWindow(roomName, userName, serverIp, serverPort);
            openChatRooms.put(roomName, chatRoomWindow);
        } else {
            ChatRoomWindow existingWindow = openChatRooms.get(roomName);
            if (existingWindow.isVisible()) {
                // 이미 채팅방이 열려있고 보이는 상태라면 경고 메시지 표시
                JOptionPane.showMessageDialog(
                        this,
                        "이미 열려있는 채팅방입니다.",
                        "알림",
                        JOptionPane.INFORMATION_MESSAGE);
                existingWindow.toFront(); // 기존 창을 앞으로 가져오기
            } else {
                existingWindow.setVisible(true); // 숨겨져 있던 창을 다시 보이게 함
            }
        }
    }
}