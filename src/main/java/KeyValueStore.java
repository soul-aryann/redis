import java.util.concurrent.ConcurrentHashMap;

public class KeyValueStore {
    private static final ConcurrentHashMap<String, String> VALUES = new ConcurrentHashMap<>();

    private KeyValueStore() {
    }

    public static void set(String key, String value) {
        VALUES.put(key, value);
    }

    public static String get(String key) {
        return VALUES.get(key);
    }
}