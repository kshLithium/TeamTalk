import javax.swing.*; // 스윙 GUI 컴포넌트 라이브러리
import java.awt.*; // 그래픽 관련 라이브러리
import java.awt.event.ActionEvent; // 이벤트 관련 클래스
import java.awt.event.ActionListener; // 이벤트 리스너 인터페이스

public class StartScreen extends JFrame {
    private JTextField nameField; // 사용자 이름 입력 필드

    // 생성자: 화면 초기화 및 설정
    public StartScreen() {
        setTitle("Enter Your Name"); // 창 제목 설정
        setSize(300, 150); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 종료 시 애플리케이션 종료
        setLocationRelativeTo(null); // 창을 화면 중앙에 표시

        nameField = new JTextField(20); // 사용자 이름을 입력받을 텍스트 필드 생성
        JButton startButton = new JButton("Start Chatting"); // 채팅 시작 버튼 생성

        // 버튼 클릭 시 이벤트 리스너 설정
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userName = nameField.getText().trim(); // 입력된 이름을 가져와서 공백 제거
                if (!userName.isEmpty()) { // 이름이 입력된 경우
                    dispose(); // 현재 창 닫기
                    new ChatAppMain(userName).setVisible(true); // ChatAppMain 창을 열고 사용자 이름 전달
                } else {
                    // 이름이 비어 있을 경우 에러 메시지 표시
                    JOptionPane.showMessageDialog(StartScreen.this, "Please enter a name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 레이아웃 설정 및 컴포넌트 추가
        setLayout(new BorderLayout());
        add(new JLabel("Enter your name:"), BorderLayout.NORTH); // 상단에 안내 텍스트 추가
        add(nameField, BorderLayout.CENTER); // 중앙에 이름 입력 필드 추가
        add(startButton, BorderLayout.SOUTH); // 하단에 시작 버튼 추가
    }

    // main 메서드: 애플리케이션 시작 지점
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartScreen().setVisible(true); // StartScreen 창을 표시
        });
    }
}