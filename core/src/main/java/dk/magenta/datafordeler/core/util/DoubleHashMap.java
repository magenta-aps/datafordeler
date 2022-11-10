package dk.magenta.datafordeler.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for storing values by two keys (like a two-dimensional table)
 */
public class DoubleHashMap<K, S, V> extends HashMap<K, HashMap<S, V>> {

    /**
     * Sees if there is a value at the specified keyset
     *
     * @param key    Primary key
     * @param subKey Secondary key
     * @return true if a value is found, false otherwise
     */
    public boolean containsKey(K key, S subKey) {
        if (super.containsKey(key)) {
            return this.get(key).containsKey(subKey);
        }
        return false;
    }

    /**
     * Inserts a value in the table, by the specified keys
     *
     * @param key    Primary key
     * @param subKey Secondary key
     * @param value  Value to insert
     */
    public void put(K key, S subKey, V value) {
        HashMap<S, V> subMap = super.get(key);
        if (subMap == null) {
            subMap = new HashMap<S, V>();
            super.put(key, subMap);
        }
        subMap.put(subKey, value);
    }

    /**
     * Inserts a value in the table, by the specified keys
     *
     * @param key Primary key
     * @param map map of values to insert
     */
    public void putAll(K key, Map<S, V> map) {
        HashMap<S, V> subMap = super.get(key);
        if (subMap == null) {
            subMap = new HashMap<S, V>();
            super.put(key, subMap);
        }
        subMap.putAll(map);
    }

    /**
     * Obtains a value by the specified keys
     *
     * @param key    Primary key
     * @param subKey Secondary key
     * @return The stored value, or null if none was found
     */
    public V get(K key, S subKey) {
        HashMap<S, V> subMap = super.get(key);
        if (subMap != null) {
            return subMap.get(subKey);
        }
        return null;
    }
}
