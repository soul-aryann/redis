import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResponseHandler {
    private ResponseHandler() {
    }

    public static byte[] response(List<String> command) {
        if (command.isEmpty()) {
            return error("empty command");
        }
        String name = command.get(0);
        if (name.equalsIgnoreCase("PING")) {
            return "+PONG\r\n".getBytes(StandardCharsets.UTF_8);
        }
        if (name.equalsIgnoreCase("ECHO")) {
            if (command.size() < 2) {
                return error("wrong number of arguments for 'echo' command");
            }
            return bulkString(command.get(1));
        }
        return error("unknown command '" + name + "'");
    }

    private static byte[] bulkString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        String header = "$" + bytes.length + "\r\n";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] response = new byte[headerBytes.length + bytes.length + 2];
        System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
        System.arraycopy(bytes, 0, response, headerBytes.length, bytes.length);
        response[response.length - 2] = '\r';
        response[response.length - 1] = '\n';
        return response;
    }

    private static byte[] error(String message) {
        return ("-ERR " + message + "\r\n").getBytes(StandardCharsets.UTF_8);
    }
}