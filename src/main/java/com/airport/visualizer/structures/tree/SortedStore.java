package com.airport.visualizer.structures.tree;

import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/** Adapter over a REAL TreeMap&lt;K, Flight&gt; or TreeSet&lt;K&gt; so one engine can drive both. */
public interface SortedStore<K extends Comparable<K>> {

    /** @return true if new; false if the key already existed (map: value replaced, set: rejected) */
    boolean add(K key, Flight value);

    Flight get(K key);

    boolean contains(K key);

    boolean remove(K key);

    /** Keys in sorted order. */
    List<K> keys();

    K first();

    K last();

    static <K extends Comparable<K>> SortedStore<K> treeMap() {
        return new MapStore<>();
    }

    static <K extends Comparable<K>> SortedStore<K> treeSet() {
        return new SetStore<>();
    }

    final class MapStore<K extends Comparable<K>> implements SortedStore<K> {

        private final TreeMap<K, Flight> map = new TreeMap<>();

        @Override
        public boolean add(K key, Flight value) {
            return map.put(key, value) == null;
        }

        @Override
        public Flight get(K key) {
            return map.get(key);
        }

        @Override
        public boolean contains(K key) {
            return map.containsKey(key);
        }

        @Override
        public boolean remove(K key) {
            return map.remove(key) != null;
        }

        @Override
        public List<K> keys() {
            return new ArrayList<>(map.keySet());
        }

        @Override
        public K first() {
            return map.isEmpty() ? null : map.firstKey();
        }

        @Override
        public K last() {
            return map.isEmpty() ? null : map.lastKey();
        }
    }

    final class SetStore<K extends Comparable<K>> implements SortedStore<K> {

        private final TreeSet<K> set = new TreeSet<>();

        @Override
        public boolean add(K key, Flight value) {
            return set.add(key);
        }

        @Override
        public Flight get(K key) {
            return null;
        }

        @Override
        public boolean contains(K key) {
            return set.contains(key);
        }

        @Override
        public boolean remove(K key) {
            return set.remove(key);
        }

        @Override
        public List<K> keys() {
            return new ArrayList<>(set);
        }

        @Override
        public K first() {
            return set.isEmpty() ? null : set.first();
        }

        @Override
        public K last() {
            return set.isEmpty() ? null : set.last();
        }
    }
}
