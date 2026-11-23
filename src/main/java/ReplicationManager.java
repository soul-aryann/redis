import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ReplicationManager {
    private static Socket masterSocket;
    private ReplicationManager() {}
    public static void connectToMaster(String host, int port)
            throws IOException {
        masterSocket = new Socket(host, port);
        OutputStream output = masterSocket.getOutputStream();
        String ping = "*1\r\n$4\r\nPING\r\n";
        output.write(ping.getBytes(StandardCharsets.UTF_8));
        output.flush();

        System.out.println("Connected to master: " + host + ":" + port);
        System.out.println("PING sent to master");
    }
}