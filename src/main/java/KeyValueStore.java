
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class KeyValueStore {
    private static final ConcurrentHashMap<String, Entry> VALUES = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, List<String>> LISTS =
        new ConcurrentHashMap<>();

    private KeyValueStore() {}

    public static void set(String key, String value, Long expiryMillis) {
        long expiresAt = expiryMillis == null
            ? -1
            : System.currentTimeMillis() + expiryMillis;

        VALUES.put(key, new Entry(value, expiresAt));
    }

    public static String get(String key) {
        Entry entry = VALUES.get(key);

        if (entry == null) return null;

        if (entry.expired()) {
            VALUES.remove(key, entry);
            return null;
        }

        return entry.value();
    }

    public static int rpush(String key, List<String> elements) {
        return LISTS.compute(key, (k, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.addAll(elements);
            return list;
        }).size();
    }

    private record Entry(String value, long expiresAtMillis) {
        private boolean expired() {
            return expiresAtMillis >= 0 &&
                System.currentTimeMillis() >= expiresAtMillis;
        }
    }
}
