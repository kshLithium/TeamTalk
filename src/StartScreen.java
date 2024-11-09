import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartScreen extends JFrame {
    private JTextField nameField;

    public StartScreen() {
        setTitle("Enter Your Name");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        nameField = new JTextField(20);
        JButton startButton = new JButton("Start Chatting");

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userName = nameField.getText().trim();
                if (!userName.isEmpty()) {
                    dispose();
                    new ChatAppMain(userName).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(StartScreen.this, "Please enter a name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setLayout(new BorderLayout());
        add(new JLabel("Enter your name:"), BorderLayout.NORTH);
        add(nameField, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartScreen().setVisible(true);
        });
    }
}