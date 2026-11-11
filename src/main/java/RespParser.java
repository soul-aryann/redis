
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespParser {
    private final InputStream input;

    public RespParser(InputStream input){
        this.input = input;
    }
    public List<String> readCommand() throws IOException{
        int prefix = input.read();
        if (prefix == -1) {
            return null;
        }
        if (prefix != '*') {
            throw new IOException("Expected RESP array");
        }
        int count = readInteger();
        List<String> parts = new ArrayList<>();
        for(int i = 0; i < count; i++){
            parts.add(readBulkString());
        }
        return parts;
    }
    private String readBulkString() throws IOException{
        int prefix = input.read();
        if(prefix != '$'){
            throw new IOException("Expected RESP bulk string");
        }
        int length = readInteger();
        byte[] value = input.readNBytes(length);
        if(value.length != length){
            throw new IOException("Unexpected end of Bulk Stream");
        }
        expectCRLF();
        return new String(value,StandardCharsets.UTF_8);
    }
    private int readInteger() throws IOException {
        String line = readLine();
        return Integer.parseInt(line);
    }
    private String readLine() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int current;
        while ((current = input.read()) != -1) {
            if (current == '\n') {
                break;
            }
            if (current != '\r') {
                buffer.write(current);
            }
        }
        if (current == -1) {
            throw new IOException("Unexpected end of line");
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
    private void expectCrLf() throws IOException {
        int carriageReturn = input.read();
        int lineFeed = input.read();
        if (carriageReturn != '\r' || lineFeed != '\n') {
            throw new IOException("Expected CRLF");
        }
    }

}
