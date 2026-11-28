import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main(String[] args) {
        int port = 6379;
        String masterHost = null;
        int masterPort = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException(
                        "Missing port number"
                    );
                }
                port = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--replicaof")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException(
                        "Missing master address"
                    );
                }
                String[] masterAddress = args[++i].trim().split("\\s+");
                if (masterAddress.length != 2) {
                    throw new IllegalArgumentException(
                        "Invalid master address. Expected: host port"
                    );
                }
                masterHost = masterAddress[0];
                masterPort = Integer.parseInt(masterAddress[1]);
            }
        }
        CommandHandler.setRole(
            masterHost == null ? "master" : "slave"
        );
        try {
            if (masterHost != null) {
                ReplicationManager.connectToMaster(
                    masterHost,
                    masterPort,
                    port
                );
            }
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            ClientHandler.handleClient(clientSocket);
                        } catch (TimeoutException ex) {
                            System.getLogger(Main.class.getName())
                                .log(
                                    System.Logger.Level.ERROR,
                                    (String) null,
                                    ex
                                );
                        }
                    }).start();
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}