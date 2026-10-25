package com.airport.visualizer.structures.hash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The exact bucket math java.util.HashMap / HashSet use, so the picture matches the real thing:
 * index = (h ^ (h >>> 16)) & (capacity - 1), capacity doubling when size > 0.75 * capacity.
 */
public final class HashBuckets {

    public static final int DEFAULT_CAPACITY = 16;

    private HashBuckets() {
    }

    public static int spread(Object key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    public static int index(Object key, int capacity) {
        return spread(key) & (capacity - 1);
    }

    /** Capacity HashMap ends up with after holding {@code maxSizeEver} entries (it never shrinks). */
    public static int capacityFor(int maxSizeEver) {
        int cap = DEFAULT_CAPACITY;
        while (maxSizeEver > cap * 0.75) {
            cap *= 2;
        }
        return cap;
    }

    /**
     * Groups keys into buckets. Keys must be given in the real structure's own order
     * (HashMap iteration order, or LinkedHashMap insertion order) so chains appear in their real order.
     */
    public static List<List<String>> layout(Collection<?> keysInOrder, int capacity) {
        List<List<String>> buckets = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            buckets.add(new ArrayList<String>());
        }
        for (Object k : keysInOrder) {
            buckets.get(index(k, capacity)).add(String.valueOf(k));
        }
        return buckets;
    }

    /** "\"PR102\".hashCode() = 76447331, mixed and & 15 = bucket 3" style explanation. */
    public static String calcText(Object key, int capacity) {
        int h = key.hashCode();
        return "\"" + key + "\".hashCode() = " + h + "  ->  mix bits: " + spread(key)
                + "  ->  & " + (capacity - 1) + "  =  bucket " + index(key, capacity);
    }
}
