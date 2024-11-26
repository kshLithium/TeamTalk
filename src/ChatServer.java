import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12349; // 서버가 실행되는 포트 번호
    private Map<String, Set<ClientHandler>> roomClients = new HashMap<>(); // 채팅방과 해당 사용자 목록을 매핑하는 데이터 구조

    public static void main(String[] args) {
        new ChatServer().start(); // 서버 시작
    }

    public void start() {
        System.out.println("Chat server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // 지정된 포트로 서버 소켓 생성
            while (true) {
                // 새 클라이언트 연결 시 ClientHandler 생성 및 실행
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket; // 클라이언트와의 연결 소켓
        private PrintWriter out; // 클라이언트로 메시지를 보내기 위한 출력 스트림
        private String roomName; // 클라이언트가 접속한 채팅방 이름
        private String userName; // 클라이언트의 사용자 이름

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 입력 스트림
                out = new PrintWriter(socket.getOutputStream(), true); // 출력 스트림

                // 클라이언트로부터 채팅방 이름과 사용자 이름 수신
                roomName = in.readLine();
                userName = in.readLine();

                if (roomName == null || userName == null)
                    return;

                // 사용자를 채팅방에 추가
                synchronized (roomClients) {
                    roomClients.putIfAbsent(roomName, new HashSet<>()); // 채팅방이 없으면 생성
                    roomClients.get(roomName).add(this); // 사용자 추가

                    // 입장 알림 메시지
                    broadcastToRoom(userName + "님이 채팅방에 들어왔습니다.", true);
                }

                // 클라이언트 메시지 처리 루프
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/file ")) {
                        handleFileMessage(message.substring(6), in); // 파일 메시지 처리
                    } else {
                        handleChatMessage(message); // 일반 메시지 처리
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 클라이언트 종료 처리
                leaveRoom();
                try {
                    socket.close(); // 소켓 닫기
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleChatMessage(String message) {
            synchronized (roomClients) {
                Set<ClientHandler> clients = roomClients.get(roomName); // 채팅방 사용자 목록 가져오기

                // 읽음 상태 메시지 구성
                StringBuilder readStatus = new StringBuilder("(");
                for (ClientHandler client : clients) {
                    if (!client.userName.equals(this.userName)) { // 자기 자신은 제외
                        readStatus.append(client.userName).append(" 읽음, ");
                    }
                }

                if (readStatus.length() > 1) {
                    readStatus.setLength(readStatus.length() - 2); // 마지막 ", " 제거
                    readStatus.append(")");
                } else {
                    readStatus.setLength(0); // 읽음 상태 없으면 제거
                }

                // 최종 메시지 작성
                String formattedMessage = userName + ": " + message;
                if (readStatus.length() > 0) {
                    formattedMessage += " " + readStatus;
                }

                // 채팅방 사용자들에게 메시지 전송
                broadcastToRoom(formattedMessage, false);
            }
        }

        private void handleFileMessage(String fileName, BufferedReader in) throws IOException {
            String fileContent = in.readLine(); // 파일 내용 (Base64 인코딩) 수신
            String message = "/file " + fileName + " " + fileContent;

            synchronized (roomClients) {
                // 파일 전송 알림 메시지
                broadcastToRoom(userName + "님이 파일을 보냈습니다: " + fileName, false);
                // 파일 데이터 메시지 전송
                broadcastToRoom(message, false);
            }
        }

        private void broadcastToRoom(String message, boolean isSystemMessage) {
            synchronized (roomClients) {
                Set<ClientHandler> clients = roomClients.get(roomName);
                for (ClientHandler client : clients) {
                    client.out.println(message); // 각 사용자에게 메시지 전송
                }
            }
        }

        private void leaveRoom() {
            synchronized (roomClients) {
                Set<ClientHandler> clients = roomClients.get(roomName);
                if (clients != null) {
                    clients.remove(this); // 사용자 제거
                    if (clients.isEmpty()) {
                        roomClients.remove(roomName); // 채팅방이 비었으면 삭제
                    } else {
                        broadcastToRoom(userName + "님이 채팅방에서 나갔습니다.", true); // 퇴장 알림
                    }
                }
            }
        }
    }
}
