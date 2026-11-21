import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.StructuredTaskScope.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class KeyValueStore {
    private static final ConcurrentHashMap<String, Entry> VALUES = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, List<String>> LISTS =
        new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Object> LOCKS =
    new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Deque<CompletableFuture<String>>> WAITERS =
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
        Object lock = LOCKS.computeIfAbsent(key, k -> new Object());

        synchronized (lock) {
            List<String> list = LISTS.computeIfAbsent(
                key, k -> new ArrayList<>()
            );

            for (String element : elements) {
                Deque<CompletableFuture<String>> queue = WAITERS.get(key);

                if (queue != null && !queue.isEmpty()) {
                    CompletableFuture<String> waiter = queue.pollFirst();

                    waiter.complete(element);
                } else {
                    list.add(element);
                }

                if (queue != null && queue.isEmpty()) {
                    WAITERS.remove(key, queue);
                }
            }

            if (list.isEmpty()) {
                LISTS.remove(key);
                return 0;
            }

            return list.size();
        }
    }
    public static int lpush(String key, List<String> elements) {
        return LISTS.compute(key, (k, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
             for (String element : elements) {
                    list.add(0, element);
                }
                return list;
        }).size();
    }

    private record Entry(String value, long expiresAtMillis) {
        private boolean expired() {
            return expiresAtMillis >= 0 &&
                System.currentTimeMillis() >= expiresAtMillis;
        }
    }

    public static List<String> lrange(String key, int start, int stop) {
        List<String> list = LISTS.get(key);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        int size = list.size();
        if (start < 0) {
            start = Math.max(0, size + start);
        }
        if (stop < 0) {
            stop = Math.max(0, size + stop);
        }
        if (start >= size || start > stop) {
            return List.of();
        }
        stop = Math.min(stop, size - 1);
        return new ArrayList<>(list.subList(start, stop + 1));
    }
    public static int llen(String key) {
        List<String> list = LISTS.get(key);

        if (list == null) {
            return 0;
        }
        return list.size();
    }
    public static List<String> lpop(String key, int count) {
        AtomicReference<List<String>> popped = new AtomicReference<>(List.of());
        LISTS.computeIfPresent(key, (k, list) -> {
            int elementsToRemove = Math.min(count, list.size());
            List<String> removed = new ArrayList<>(
                list.subList(0, elementsToRemove)
            );

            popped.set(removed);

            list.subList(0, elementsToRemove).clear();

            return list.isEmpty() ? null : list;
        });

        return popped.get();
    }
    public static List<String> blpop(String key, double timeoutSeconds) throws java.util.concurrent.TimeoutException {
        Object lock = LOCKS.computeIfAbsent(key, k -> new Object());
        CompletableFuture<String> waiter = new CompletableFuture<>();

        synchronized (lock) {
            List<String> list = LISTS.get(key);

            if (list != null && !list.isEmpty()) {
                String value = list.remove(0);

                if (list.isEmpty()) {
                    LISTS.remove(key);
                }

                return List.of(key, value);
            }

            WAITERS.computeIfAbsent(key, k -> new ArrayDeque<>())
                .addLast(waiter);
        }

        try {
            String value;

            if (timeoutSeconds == 0) {
                value = waiter.get();
            } else {
                value = waiter.get(
                    (long)(timeoutSeconds * 1000),
                    TimeUnit.MILLISECONDS
                );
            }

            return List.of(key, value);

        } catch (TimeoutException exception) {
            return null;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;

        } catch (java.util.concurrent.ExecutionException exception) {
            throw new RuntimeException(exception);

        } finally {
            synchronized (lock) {
                Deque<CompletableFuture<String>> queue = WAITERS.get(key);

                if (queue != null) {
                    queue.remove(waiter);

                    if (queue.isEmpty()) {
                        WAITERS.remove(key, queue);
                    }
                }
            }
        }
    }
}
