package dk.magenta.datafordeler.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection mapping keys to lists.
 * Extends HashMap, so the separate lists under each key can be accessed by calling methods on the superclass
 */
public class DoubleListHashMap<K, S, V> extends DoubleHashMap<K, S, ArrayList<V>> {

    public DoubleListHashMap() {
        super();
    }

    public DoubleListHashMap(DoubleHashMap<K, S, List<V>> initial) {
        for (K key : initial.keySet()) {
            for (S subkey : initial.get(key).keySet()) {
                this.put(key, subkey, new ArrayList<V>(initial.get(key, subkey)));
            }
        }
    }

    /**
     * Inserts a value in the list identified by the specified key. Creates a list if none exists.
     *
     * @param key   Key to insert by
     * @param value Value to insert
     */
    public void add(K key, S subkey, V value) {
        ArrayList<V> list = this.get(key, subkey);
        if (list == null) {
            list = new ArrayList<>();
            this.put(key, subkey, list);
        }
        list.add(value);
    }

    /**
     * Obtains an item from the collection, by key and index
     *
     * @param key   Key to look up by
     * @param index Index to look up by
     * @return
     */
    public V get(K key, S subkey, int index) {
        ArrayList<V> list = super.get(key, subkey);
        if (list != null && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    public V getFirst(K key, S subkey) {
        return this.get(key, subkey, 0);
    }

    public V getFirstOf(K[] keys, S[] subkeys) {
        for (K key : keys) {
            for (S subkey : subkeys) {
                if (this.containsKey(key) && this.get(key).containsKey(subkey)) {
                    return this.getFirst(key, subkey);
                }
            }
        }
        return null;
    }

    public Map<K, Map<S, List<V>>> readonly() {
        HashMap<K, Map<S, List<V>>> map = new HashMap<K, Map<S, List<V>>>();
        for (K key : this.keySet()) {
            Map<S, List<V>> submap = new HashMap<>();
            for (S subkey : this.get(key).keySet()) {
                submap.put(subkey, this.get(key, subkey));
            }
            map.put(key, submap);
        }
        return map;
    }
}
