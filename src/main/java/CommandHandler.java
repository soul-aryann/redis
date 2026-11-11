import java.util.List;
import java.util.Locale;

public class CommandHandler {
    private CommandHandler() {
    }

    public static byte[] handle(List<String> command) {
        if (command.isEmpty()) {
            return ResponseHandler.error("empty command");
        }
        String name = command.get(0).toUpperCase(Locale.ROOT);
        return switch (name) {
            case "PING" -> ResponseHandler.simpleString("PONG");
            case "ECHO" -> echo(command);
            case "SET" -> set(command);
            case "GET" -> get(command);
            default -> ResponseHandler.error("unknown command '" + command.get(0) + "'");
        };
    }

    private static byte[] echo(List<String> command) {
        if (command.size() < 2) {
            return ResponseHandler.error("wrong number of arguments for 'echo' command");
        }
        return ResponseHandler.bulkString(command.get(1));
    }

    private static byte[] set(List<String> command) {
        if (command.size() < 3) {
            return ResponseHandler.error("wrong number of arguments for 'set' command");
        }
        KeyValueStore.set(command.get(1), command.get(2));
        return ResponseHandler.simpleString("OK");
    }

    private static byte[] get(List<String> command) {
        if (command.size() < 2) {
            return ResponseHandler.error("wrong number of arguments for 'get' command");
        }
        String value = KeyValueStore.get(command.get(1));
        if (value == null) {
            return ResponseHandler.nullBulkString();
        }
        return ResponseHandler.bulkString(value);
    }
}