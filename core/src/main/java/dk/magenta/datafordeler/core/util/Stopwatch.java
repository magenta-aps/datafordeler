package dk.magenta.datafordeler.core.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Convenience class for measuring several tasks being run in sequence, possibly
 * repeated, and then reporting in the end.
 * Suppose you execute tasks A, B, C, A, B, C, A, B, C in that sequence
 * and want to know the sum total time for each task A, B, and C.
 */
@Component
public class Stopwatch {

    private final HashMap<String, Long> results = new HashMap<>();
    private final HashMap<String, Long> running = new HashMap<>();

    /**
     * Start a timer for the given key
     *
     * @param key
     */
    public void start(String key) {
        this.running.put(key, Instant.now().toEpochMilli());
    }

    /**
     * Measure and store the time since start() was called for the given key.
     * If you want to call measure() several times in a row for a key, be sure
     * to call start() for each instance as well
     *
     * @param key
     */
    public void measure(String key) {
        if (running.containsKey(key)) {
            long time = Instant.now().toEpochMilli() - this.running.get(key);
            Long old = results.get(key);
            results.put(key, (old == null ? 0L : old) + time);
        }
    }

    /**
     * Get the total accumulated time for the key
     *
     * @param key
     * @return
     */
    public long getTotal(String key) {
        Long total = this.results.get(key);
        return total != null ? total : 0;
    }

    /**
     * Set the acumulated time for the key to 0
     *
     * @param key
     */
    public void reset(String key) {
        this.results.put(key, 0L);
    }

    public void clear() {
        this.results.clear();
        this.running.clear();
    }

    /**
     * Get the key and time in a pretty format: "key: [time]ms"
     *
     * @param key
     * @return
     */
    public String formatTotal(String key) {
        return key + ": " + this.getTotal(key) + "ms";
    }

    public String formatAllTotal() {
        StringJoiner sj = new StringJoiner("\n");
        List<Map.Entry<String, Long>> list = new LinkedList<Map.Entry<String, Long>>(this.results.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        for (Map.Entry<String, Long> entry : list) {
            sj.add(this.formatTotal(entry.getKey()));
        }
        return sj.toString();
    }

}
