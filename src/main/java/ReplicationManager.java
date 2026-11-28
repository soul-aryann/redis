
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ReplicationManager {

    private static Socket masterSocket;

    private ReplicationManager() {}

    public static void connectToMaster(
            String host,
            int masterPort,
            int replicaPort
    ) throws IOException {

        masterSocket = new Socket(host, masterPort);

        InputStream input = masterSocket.getInputStream();
        OutputStream output = masterSocket.getOutputStream();

        sendCommand(output, "*1\r\n$4\r\nPING\r\n");
        String pingResponse = readResponse(input);

        System.out.println("PING response: " + pingResponse);

        String port = String.valueOf(replicaPort);
        String listeningPortCommand =
                "*3\r\n" +
                "$8\r\nREPLCONF\r\n" +
                "$14\r\nlistening-port\r\n" +
                "$" + port.length() + "\r\n" +
                port + "\r\n";

        sendCommand(output, listeningPortCommand);
        String listeningPortResponse = readResponse(input);

        System.out.println("REPLCONF listening-port: " + listeningPortResponse);
        String capaCommand =
                "*3\r\n" +
                "$8\r\nREPLCONF\r\n" +
                "$4\r\ncapa\r\n" +
                "$6\r\npsync2\r\n";

        sendCommand(output, capaCommand);
        String capaResponse = readResponse(input);

        System.out.println("REPLCONF capa: " + capaResponse);

        String psyncCommand =
                "*3\r\n" +
                "$5\r\nPSYNC\r\n" +
                "$1\r\n?\r\n" +
                "$2\r\n-1\r\n";

        sendCommand(output, psyncCommand);
        String psyncResponse = readResponse(input);

        System.out.println("PSYNC response: " + psyncResponse);
    }

    private static void sendCommand(
            OutputStream output,
            String command
    ) throws IOException {

        output.write(command.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    private static String readResponse(
            InputStream input
    ) throws IOException {
        StringBuilder response = new StringBuilder();
        int previous = -1;
        int current;
        while ((current = input.read()) != -1) {
            response.append((char) current);

            if (previous == '\r' && current == '\n') {
                response.setLength(response.length() - 2);
                return response.toString();
            }
            previous = current;
        }
        throw new IOException("Master closed the connection");
    }
}
