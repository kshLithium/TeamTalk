import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StartScreen extends JFrame {
    private JTextField nameField; // 사용자 이름 입력 필드
    private JTextField ipField; // IP 주소 입력 필드
    private JTextField portField; // 포트 번호 입력 필드

    private String userName; // 유저 id
    private String serverIp; // ip 주소
    private int serverPort; // 포트 번호

    public StartScreen() {
        setTitle("로그인 화면");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 레이아웃 구성
        setLayout(new GridLayout(5, 1));

        JLabel titleLabel = new JLabel("TALK", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel);

        nameField = new JTextField();
        add(createLabeledField("ID", nameField));

        ipField = new JTextField("127.0.0.1"); // 기본값 설정
        add(createLabeledField("IP Address", ipField));

        portField = new JTextField("12345"); // 기본값 설정
        add(createLabeledField("Port Number", portField));

        JButton loginButton = new JButton("로그인");
        loginButton.addActionListener(this::onLogin);
        add(loginButton);
    }

    private JPanel createLabeledField(String label, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private void onLogin(ActionEvent e) {
        userName = nameField.getText().trim();
        serverIp = ipField.getText().trim();
        try {
            serverPort = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "포트 번호는 숫자로 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID를 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose(); // 현재 창 닫기
        new ChatAppMain(userName, serverIp, serverPort).setVisible(true); // 메인 애플리케이션 실행
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartScreen().setVisible(true));
    }
}