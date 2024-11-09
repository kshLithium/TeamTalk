import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final int FILE_PORT = 12346;  // 파일 전송을 위한 별도 포트
    private Map<String, Set<ClientHandler>> roomClients = new HashMap<>();

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {
        System.out.println("Chat server started on port " + PORT);
        new Thread(this::handleFileTransfers).start();  // 파일 전송 핸들링 스레드 시작
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileTransfers() {
        try (ServerSocket fileServerSocket = new ServerSocket(FILE_PORT)) {
            while (true) {
                Socket fileSocket = fileServerSocket.accept();
                new Thread(() -> handleFileTransfer(fileSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileTransfer(Socket fileSocket) {
        try (DataInputStream dataIn = new DataInputStream(fileSocket.getInputStream())) {
            String roomName = dataIn.readUTF();
            String sender = dataIn.readUTF();
            String fileName = dataIn.readUTF();
            byte[] fileData = new byte[dataIn.readInt()];
            dataIn.readFully(fileData);

            // 해당 방의 모든 클라이언트에게 파일을 전송
            synchronized (roomClients) {
                for (ClientHandler client : roomClients.getOrDefault(roomName, new HashSet<>())) {
                    client.sendFileToClient(sender, fileName, fileData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String roomName;
        private String userName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 방 이름과 사용자 이름을 수신
                roomName = in.readLine();
                userName = in.readLine();

                if (roomName == null || userName == null) {
                    return;
                }

                synchronized (roomClients) {
                    roomClients.putIfAbsent(roomName, new HashSet<>());
                    roomClients.get(roomName).add(this);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("FILE:")) {
                        String fileName = message.substring(5);
                        broadcastToRoom(roomName, "FILE:" + userName + ":" + fileName);
                    } else {
                        broadcastToRoom(roomName, userName + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (roomName != null && out != null) {
                    synchronized (roomClients) {
                        roomClients.get(roomName).remove(this);
                        if (roomClients.get(roomName).isEmpty()) {
                            roomClients.remove(roomName);
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastToRoom(String roomName, String message) {
            synchronized (roomClients) {
                for (ClientHandler client : roomClients.get(roomName)) {
                    client.out.println(message);
                }
            }
        }

        public void sendFileToClient(String sender, String fileName, byte[] fileData) {
            try {
                Socket fileSocket = new Socket(socket.getInetAddress(), FILE_PORT);
                DataOutputStream dataOut = new DataOutputStream(fileSocket.getOutputStream());

                dataOut.writeUTF(roomName);
                dataOut.writeUTF(sender);
                dataOut.writeUTF(fileName);
                dataOut.writeInt(fileData.length);
                dataOut.write(fileData);
                dataOut.flush();

                fileSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}