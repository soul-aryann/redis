
import java.util.List;
import java.util.Locale;

public class CommandHandler {

    private CommandHandler() {}
    private static volatile String role = "master";
    public static void setRole(String serverRole) {
    role = serverRole;
}
    public static byte[] handle(List<String> command){
        if (command.isEmpty()) {
            return ResponseHandler.error("empty command");
        }
        String name = command.get(0).toUpperCase(Locale.ROOT);
        return switch (name) {
            case "PING" -> ResponseHandler.simpleString("PONG");
            case "ECHO" -> echo(command);
            case "SET" -> set(command);
            case "GET" -> get(command);
            case "LRANGE" -> lrange(command);
            case "RPUSH" -> {
                if (command.size() < 3) {
                    yield ResponseHandler.error(
                        "wrong number of arguments for 'rpush' command"
                    );
                }
                yield ResponseHandler.integer(rpush(command));
            }
            case "LPUSH" -> {
                if (command.size() < 3) {
                    yield ResponseHandler.error(
                        "wrong number of arguments for 'lpush' command"
                    );
                }

                yield ResponseHandler.integer(lpush(command));
            }
            case "LLEN" -> {
                if (command.size() != 2) {
                    yield ResponseHandler.error(
                        "wrong number of arguments for 'llen' command"
                    );
                }

                yield ResponseHandler.integer(
                    KeyValueStore.llen(command.get(1))
                );
            }
            case "LPOP" -> {
                if (command.size() < 2 || command.size() > 3) {
                    yield ResponseHandler.error(
                        "wrong number of arguments for 'lpop' command"
                    );
                }

                yield lpop(command);
            }
            case "BLPOP" -> blpop(command);
            
            case "INFO" -> {
                if (command.size() != 2) {
                    yield ResponseHandler.error(
                        "wrong number of arguments for 'info' command"
                    );
                }
                if (!command.get(1).equalsIgnoreCase("replication")) {
                    yield ResponseHandler.error("unsupported info section");
                }

                yield ResponseHandler.bulkString("role:" + role);
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
    private static byte[] lrange(List<String> command) {
        if (command.size() != 4) {
            return ResponseHandler.error(
                "wrong number of arguments for 'lrange' command"
            );
        }
        int start;
        int stop;
        try {
            start = Integer.parseInt(command.get(2));
            stop = Integer.parseInt(command.get(3));
        } catch (NumberFormatException exception) {
            return ResponseHandler.error(
                "value is not an integer or out of range"
            );
        }
        List<String> result = KeyValueStore.lrange(
            command.get(1), start, stop
        );
        return ResponseHandler.array(result);
    }
    private static int lpush(List<String> command) {
        String key = command.get(1);
        List<String> elements = command.subList(2, command.size());

        return KeyValueStore.lpush(key, elements);
    }

    private static byte[] lpop(List<String> command) {
        String key = command.get(1);
        if (command.size() == 2) {
            List<String> values = KeyValueStore.lpop(key, 1);
            if (values.isEmpty()) {
                return ResponseHandler.nullBulkString();
            }
            return ResponseHandler.bulkString(values.get(0));
        }
        try {
            int count = Integer.parseInt(command.get(2));
            if (count < 0) {
                return ResponseHandler.error("value is out of range");
            }
            List<String> values = KeyValueStore.lpop(key, count);
            return ResponseHandler.array(values);
        } catch (NumberFormatException exception) {
            return ResponseHandler.error(
                "value is not an integer or out of range"
            );
        }
    }
    
    private static byte[] blpop(List<String> command) {
        if (command.size() != 3) {
            return ResponseHandler.error(
                "wrong number of arguments for 'blpop' command"
            );
        }
        String key = command.get(1);
        try {
            double timeout = Double.parseDouble(command.get(2));

            if (!Double.isFinite(timeout) || timeout < 0) {
                return ResponseHandler.error("timeout is out of range");
            }
            List<String> result = KeyValueStore.blpop(key, timeout);
            if (result == null) {
                return ResponseHandler.nullArray();
            }
            return ResponseHandler.array(result);
        } catch (NumberFormatException exception) {
            return ResponseHandler.error(
                "timeout is not a float or out of range"
            );
        }
    }

}
