package dk.magenta.datafordeler.core.util;

import java.util.*;

/**
 * A collection mapping keys to lists.
 * Extends HashMap, so the separate lists under each key can be accessed by calling methods on the superclass
 */
public class ListHashMap<K, V> extends HashMap<K, ArrayList<V>> {

    public ListHashMap() {
        super();
    }

    public ListHashMap(Map<K, List<V>> initial) {
        for (K key : initial.keySet()) {
            this.put(key, new ArrayList<V>(initial.get(key)));
        }
    }

    /**
     * Inserts a value in the list identified by the specified key. Creates a list if none exists.
     *
     * @param key   Key to insert by
     * @param value Value to insert
     */
    public void add(K key, V value) {
        ArrayList<V> list = this.get(key);
        if (list == null) {
            list = new ArrayList<>();
            this.put(key, list);
        }
        list.add(value);
    }

    public void add(K key, Collection<V> values) {
        ArrayList<V> list = this.get(key);
        if (list == null) {
            list = new ArrayList<>();
            this.put(key, list);
        }
        list.addAll(values);
    }

    /**
     * Obtains an item from the collection, by key and index
     *
     * @param key   Key to look up by
     * @param index Index to look up by
     * @return
     */
    public V get(K key, int index) {
        ArrayList<V> list = super.get(key);
        if (list != null && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    public V getFirst(K key) {
        return this.get(key, 0);
    }

    public V getFirstOf(K[] keys) {
        for (K key : keys) {
            if (this.containsKey(key)) {
                return this.getFirst(key);
            }
        }
        return null;
    }

    public Map<K, List<V>> readonly() {
        HashMap<K, List<V>> map = new HashMap<K, List<V>>();
        for (K key : this.keySet()) {
            map.put(key, this.get(key));
        }
        return map;
    }
}
