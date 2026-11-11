import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler {
    private ClientHandler() {
    }

    public static void handleClient(Socket clientSocket) {
        try (clientSocket) {
            RespParser parser = new RespParser(clientSocket.getInputStream());
            OutputStream output = clientSocket.getOutputStream();
            List<String> command;
            while ((command = parser.readCommand()) != null) {
                output.write(ResponseHandler.response(command));
                output.flush();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}