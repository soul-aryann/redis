import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ResponseHandler {
    private ResponseHandler() {
    }

    public static byte[] simpleString(String value) {
        return ("+" + value + "\r\n").getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] bulkString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.writeBytes(("$" + bytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.writeBytes(bytes);
        output.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    public static byte[] nullBulkString() {
        return "$-1\r\n".getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] error(String message) {
        return ("-ERR " + message + "\r\n").getBytes(StandardCharsets.UTF_8);
    }
}