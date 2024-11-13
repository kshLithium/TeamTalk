import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345; // 채팅 서버 포트 번호
    private Map<String, Set<ClientHandler>> roomClients = new HashMap<>(); // 채팅방별 클라이언트 목록 저장

    public static void main(String[] args) {
        new ChatServer().start(); // 서버 시작
    }

    // 서버 시작 메서드
    public void start() {
        System.out.println("Chat server started on port " + PORT); // 서버 시작 메시지 출력
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // 지정된 포트로 서버 소켓 생성
            while (true) {
                new ClientHandler(serverSocket.accept()).start(); // 클라이언트 연결을 수락하고 새 핸들러 스레드 시작
            }
        } catch (IOException e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
        }
    }

    // 클라이언트 핸들러 내부 클래스
    private class ClientHandler extends Thread {
        private Socket socket; // 클라이언트 소켓
        private PrintWriter out; // 클라이언트에 메시지를 보내기 위한 PrintWriter
        private String roomName; // 클라이언트가 속한 채팅방 이름
        private String userName; // 클라이언트 사용자 이름

        public ClientHandler(Socket socket) {
            this.socket = socket; // 클라이언트 소켓 할당
        }

        public void run() {
            try {
                // 클라이언트의 입력 스트림 설정
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true); // 클라이언트로 출력할 PrintWriter 설정

                roomName = in.readLine(); // 클라이언트로부터 방 이름 수신
                userName = in.readLine(); // 클라이언트로부터 사용자 이름 수신

                // 방 이름과 사용자 이름이 유효하지 않으면 종료
                if (roomName == null || userName == null) {
                    return;
                }

                // roomClients에 클라이언트를 추가하고 방에 클라이언트 추가
                synchronized (roomClients) {
                    roomClients.putIfAbsent(roomName, new HashSet<>()); // 방이 없으면 새로 생성
                    roomClients.get(roomName).add(this); // 현재 클라이언트를 방 목록에 추가
                }

                String message;
                while ((message = in.readLine()) != null) { // 클라이언트로부터 메시지를 반복적으로 수신
                    broadcastToRoom(roomName, userName + ": " + message); // 채팅방에 메시지 전송
                }
            } catch (IOException e) {
                e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
            } finally {
                // 클라이언트 연결 종료 및 방에서 제거
                if (roomName != null && out != null) {
                    synchronized (roomClients) {
                        roomClients.get(roomName).remove(this); // 방에서 클라이언트 제거
                        if (roomClients.get(roomName).isEmpty()) { // 방이 비어 있으면 방 삭제
                            roomClients.remove(roomName);
                        }
                    }
                }
                try {
                    socket.close(); // 소켓 닫기
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 특정 방에 메시지를 전송하는 메서드
        private void broadcastToRoom(String roomName, String message) {
            synchronized (roomClients) {
                for (ClientHandler client : roomClients.get(roomName)) { // 방에 있는 모든 클라이언트에게 메시지 전송
                    client.out.println(message); // 메시지 전송
                }
            }
        }
    }
}