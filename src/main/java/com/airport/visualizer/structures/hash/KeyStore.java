package com.airport.visualizer.structures.hash;

import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tiny adapter so one recording engine can drive a REAL HashMap, LinkedHashMap, HashSet or
 * LinkedHashSet (Adapter pattern; each implementation wraps the genuine java.util class).
 */
public interface KeyStore {

    /** @return true if the key was new; false if it already existed (map: value replaced, set: rejected) */
    boolean add(String key, Flight value);

    Flight get(String key);

    boolean contains(String key);

    boolean remove(String key);

    /** Keys in the structure's own iteration order. */
    List<String> keysInOrder();

    int size();

    static KeyStore hashMap() {
        return new MapStore(new HashMap<String, Flight>());
    }

    static KeyStore linkedHashMap() {
        return new MapStore(new LinkedHashMap<String, Flight>());
    }

    static KeyStore hashSet() {
        return new SetStore(new HashSet<String>());
    }

    static KeyStore linkedHashSet() {
        return new SetStore(new LinkedHashSet<String>());
    }

    /** Wraps a real Map&lt;String, Flight&gt;. */
    final class MapStore implements KeyStore {

        private final Map<String, Flight> map;

        MapStore(Map<String, Flight> map) {
            this.map = map;
        }

        @Override
        public boolean add(String key, Flight value) {
            return map.put(key, value) == null;
        }

        @Override
        public Flight get(String key) {
            return map.get(key);
        }

        @Override
        public boolean contains(String key) {
            return map.containsKey(key);
        }

        @Override
        public boolean remove(String key) {
            return map.remove(key) != null;
        }

        @Override
        public List<String> keysInOrder() {
            return new ArrayList<>(map.keySet());
        }

        @Override
        public int size() {
            return map.size();
        }
    }

    /** Wraps a real Set&lt;String&gt;. */
    final class SetStore implements KeyStore {

        private final Set<String> set;

        SetStore(Set<String> set) {
            this.set = set;
        }

        @Override
        public boolean add(String key, Flight value) {
            return set.add(key);
        }

        @Override
        public Flight get(String key) {
            return null;
        }

        @Override
        public boolean contains(String key) {
            return set.contains(key);
        }

        @Override
        public boolean remove(String key) {
            return set.remove(key);
        }

        @Override
        public List<String> keysInOrder() {
            return new ArrayList<>(set);
        }

        @Override
        public int size() {
            return set.size();
        }
    }
}
