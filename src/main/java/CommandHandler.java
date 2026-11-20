
import java.util.List;
import java.util.Locale;

public class CommandHandler {

    private CommandHandler() {}

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
            case "RPUSH" -> {
                if (command.size() < 3) {
                    yield ResponseHandler.error(
                        "wrong number of arguments for 'rpush' command"
                    );
                }
                yield ResponseHandler.integer(rpush(command));
            }
            default -> ResponseHandler.error(
                "unknown command '" + command.get(0) + "'"
            );
        };
    }

    private static byte[] echo(List<String> command) {
        if (command.size() < 2) {
            return ResponseHandler.error(
                "wrong number of arguments for 'echo' command"
            );
        }
        return ResponseHandler.bulkString(command.get(1));
    }

    private static byte[] set(List<String> command) {
        if (command.size() < 3) {
            return ResponseHandler.error(
                "wrong number of arguments for 'set' command"
            );
        }

        Long expiryMillis = null;

        for (int index = 3; index < command.size(); index++) {
            String option = command.get(index).toUpperCase(Locale.ROOT);
            if (!"PX".equals(option) || index + 1 >= command.size()) {
                return ResponseHandler.error("syntax error");
            }
            try {
                expiryMillis = Long.parseLong(command.get(++index));
            } catch (NumberFormatException exception) {
                return ResponseHandler.error(
                    "value is not an integer or out of range"
                );
            }
        }
        KeyValueStore.set(
            command.get(1),
            command.get(2),
            expiryMillis
        );

        return ResponseHandler.simpleString("OK");
    }

    private static byte[] get(List<String> command) {
        if (command.size() < 2) {
            return ResponseHandler.error(
                "wrong number of arguments for 'get' command"
            );
        }
        String value = KeyValueStore.get(command.get(1));
        if (value == null) {
            return ResponseHandler.nullBulkString();
        }
        return ResponseHandler.bulkString(value);
    }

    private static int rpush(List<String> command) {
        String key = command.get(1);
        List<String> elements = command.subList(2, command.size());
        return KeyValueStore.rpush(key, elements);
    }
}
