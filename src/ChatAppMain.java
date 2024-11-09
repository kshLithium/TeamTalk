import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class ChatAppMain extends JFrame {
    private JList<String> chatRoomList;
    private DefaultListModel<String> chatRoomModel;
    private HashMap<String, ChatRoomWindow> openChatRooms;
    private String userName;

    public ChatAppMain(String userName) {
        this.userName = userName; // StartScreen에서 전달받은 사용자 이름
        setTitle("Chat App - User: " + userName);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatRoomModel = new DefaultListModel<>();
        chatRoomModel.addElement("Chat Room 1");
        chatRoomModel.addElement("Chat Room 2");
        chatRoomModel.addElement("Chat Room 3");

        chatRoomList = new JList<>(chatRoomModel);
        chatRoomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        openChatRooms = new HashMap<>();

        chatRoomList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedRoom = chatRoomList.getSelectedValue();
                    openChatRoom(selectedRoom);
                }
            }
        });

        add(new JScrollPane(chatRoomList), BorderLayout.CENTER);
    }

    // main 메서드 추가
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartScreen().setVisible(true); // StartScreen을 통해 애플리케이션 시작
        });
    }

    private void openChatRoom(String roomName) {
        if (!openChatRooms.containsKey(roomName)) {
            ChatRoomWindow chatRoomWindow = new ChatRoomWindow(roomName, userName); // 전달받은 userName 사용
            openChatRooms.put(roomName, chatRoomWindow);
        } else {
            openChatRooms.get(roomName).setVisible(true);
        }
    }
}