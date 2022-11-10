package dk.magenta.datafordeler.core.util;

import java.util.ArrayList;
import java.util.HashMap;

public class FixedQueueMap<K, V> extends HashMap<K, V> {
    private final ArrayList<K> order;
    private final int capacity;

    public FixedQueueMap(int capacity) {
        super(capacity);
        this.order = new ArrayList<>(capacity);
        this.capacity = capacity;
    }

    @Override
    public V put(K key, V value) {
        if (!this.containsKey(key)) {
            if (this.size() >= this.capacity) {
                K oldKey = this.order.remove(0);
                this.remove(oldKey);
            }
            this.order.add(key);
        }
        return super.put(key, value);
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getUsedCapacity() {
        return this.order.size();
    }
}
