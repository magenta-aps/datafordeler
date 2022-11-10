package dk.magenta.datafordeler.cpr.records;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;

public class Mapping extends HashMap<String, Pair<Integer, Integer>> {
    public void add(String key, int start, int length) {
        this.put(key, new ImmutablePair<>(start, length));
    }
}
