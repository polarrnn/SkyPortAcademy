package com.airport.visualizer.structures.tree;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructureDemo;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared recording engine for TreeMap and TreeSet demos. Every comparison along the tree path
 * is shown, so learners see why lookups cost O(log n).
 *
 * @param <K> the sorted key type (LocalTime for TreeMap, String for TreeSet)
 */
public abstract class SortedTreeDemoBase<K extends Comparable<K>> extends StructureDemo<SortedTreeState> {

    private SortedStore<K> store;
    private final List<String> rejected = new ArrayList<>();
    /** Value label for each key (TreeMap: flight number of the current value). */
    private final java.util.Map<K, String> valueLabel = new java.util.HashMap<>();

    protected SortedTreeDemoBase(String name, Category category, String summary, List<Flight> flights) {
        super(name, category, summary, SortedTreeState.class, flights);
    }

    protected abstract SortedStore<K> newStore();

    protected abstract K keyOf(Flight f);

    protected abstract String keyText(K key);

    protected abstract boolean isMap();

    protected abstract String structureName();

    protected abstract String keyWord();

    protected abstract K missingKey();

    /** For maps: a second flight with the SAME key as an existing one (to show replacement). */
    protected Flight duplicateKeyFlight() {
        return null;
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Items stored / removed"};
    }

    @Override
    protected void runPlain(String scenario) {
        SortedStore<K> s = newStore();
        for (Flight f : flights()) {
            s.add(keyOf(f), f);
        }
        s.contains(keyOf(pick(2)));
        s.contains(missingKey());
        s.first();
        s.remove(keyOf(pick(1)));
        s.keys();
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        store = newStore();
        rejected.clear();
        valueLabel.clear();
        String sn = structureName();
        r.setOperation(null);
        r.record("Meet the " + sn,
                "A " + sn + " keeps everything SORTED by " + keyWord() + ". Inside it is a balancing tree: smaller "
                + keyWord() + "s go to the left, bigger ones to the right, and it keeps itself short and bushy.",
                snap(Collections.<Integer>emptyList(), -1, Mark.NONE, 0, "An empty tree.", null));
        if (wants(scenario, ADD)) {
            for (Flight f : flights()) {
                addOne(r, f, true);
            }
            Flight dup = isMap() ? duplicateKeyFlight() : null;
            if (dup != null) {
                r.setOperation("put");
                r.record("Two flights, same time?",
                        "Flight " + dup.getFlightNumber() + " also leaves at " + keyText(keyOf(dup)) + ". In this map the KEY is the time, "
                        + "and a map keeps each key only once... let's see what happens!",
                        snap(Collections.<Integer>emptyList(), -1, Mark.NONE, 0, "put(" + keyText(keyOf(dup)) + ", " + dup.getFlightNumber() + ")", dup));
                addOne(r, dup, true);
            }
        } else {
            for (Flight f : flights()) {
                addOne(r, f, false);
            }
            r.setOperation(null);
            r.record("Tree already filled",
                    "We already added all the " + keyWord() + "s. Look: the strip at the bottom is already in sorted order!",
                    snap(Collections.<Integer>emptyList(), -1, Mark.NONE, 0, store.keys().size() + " items, always sorted.", null));
        }
        if (wants(scenario, GET)) {
            lookup(r, keyOf(pick(2)));
            lookup(r, missingKey());
            firstItem(r);
        }
        if (wants(scenario, REMOVE)) {
            removeKey(r, keyOf(pick(1)));
            removeKey(r, missingKey());
        }
        if (wants(scenario, ITERATE)) {
            iterate(r);
        }
        r.setOperation(null);
        r.record("All done!",
                "A " + sn + " is a bit slower than a hash structure (O(log n) instead of O(1)), but it ALWAYS keeps things sorted - "
                + "perfect when you need 'smallest', 'biggest' or 'in order'.",
                snap(Collections.<Integer>emptyList(), -1, Mark.NONE, 0, "Final size: " + store.keys().size(), null));
    }

    private void walk(StepRecorder r, K key, String call, Flight focus) {
        List<K> keys = store.keys();
        List<Integer> path = BalancedShape.searchPath(keys, key);
        List<Integer> so = new ArrayList<>();
        for (int idx : path) {
            so.add(idx);
            r.compare();
            int c = key.compareTo(keys.get(idx));
            String story = c == 0
                    ? keyText(key) + " is EQUAL to " + keyText(keys.get(idx)) + ". We found the spot!"
                    : keyText(key) + " is " + (c < 0 ? "SMALLER" : "BIGGER") + " than " + keyText(keys.get(idx))
                    + ", so we go " + (c < 0 ? "LEFT" : "RIGHT") + ".";
            r.record(call + ": compare with " + keyText(keys.get(idx)), story,
                    snap(so, idx, Mark.COMPARE, 0, story, focus));
        }
    }

    private void addOne(StepRecorder r, Flight f, boolean show) {
        K key = keyOf(f);
        String call = isMap() ? "put(" + keyText(key) + ", " + f.getFlightNumber() + ")" : "add(\"" + keyText(key) + "\")";
        r.setOperation(isMap() ? "put" : "add");
        if (show) {
            if (store.keys().isEmpty()) {
                r.compare(0);
            } else {
                walk(r, key, call, f);
            }
        } else {
            r.compare(BalancedShape.searchPath(store.keys(), key).size());
        }
        boolean isNew = store.add(key, f);
        if (isNew || isMap()) {
            valueLabel.put(key, f.getFlightNumber());
        }
        int idx = store.keys().indexOf(key);
        if (isNew) {
            r.swap();
            if (show) {
                r.record(call + ": added",
                        keyText(key) + " is now in the tree. The tree may wiggle to stay balanced, but the bottom strip is still sorted!",
                        snap(Collections.<Integer>emptyList(), idx, Mark.ADDED, 0, "Size is now " + store.keys().size(), f));
            }
        } else if (isMap()) {
            if (show) {
                r.record(call + ": value replaced",
                        "The key " + keyText(key) + " was already there, so the OLD flight is replaced by " + f.getFlightNumber()
                        + ". Only one flight per time can live in this map - pick your key carefully!",
                        snap(Collections.<Integer>emptyList(), idx, Mark.REPLACED, 0, "Size stays " + store.keys().size(), f));
            }
        } else {
            rejected.add(keyText(key));
            if (show) {
                r.record(call + " says false",
                        "\"" + keyText(key) + "\" is already in the set, so it is NOT added again. Sets never keep twins.",
                        snap(Collections.<Integer>emptyList(), idx, Mark.REJECTED, 0, "Duplicate rejected", f));
            }
        }
    }

    private void lookup(StepRecorder r, K key) {
        boolean present = store.contains(key);
        String op = isMap() ? (present ? "get" : "containsKey") : "contains";
        String call = (isMap() ? (present ? "get(" : "containsKey(") : "contains(\"") + keyText(key) + (isMap() ? ")" : "\")");
        r.setOperation(op);
        Flight focus = flightFor(key);
        r.record(call + ": start at the root",
                "We start at the top of the tree and only go down ONE path. Each step throws away about half of the tree!",
                snap(Collections.<Integer>emptyList(), -1, Mark.NONE, 0, "Looking for " + keyText(key), focus));
        walk(r, key, call, focus);
        int idx = store.keys().indexOf(key);
        if (present) {
            Flight v = store.get(key);
            r.record(call + ": found",
                    v != null ? "Found! The value is flight " + v.getFlightNumber() + " to " + v.getRouteText() + "." : "Found! contains answers true.",
                    snap(Collections.<Integer>emptyList(), idx, Mark.FOUND, 0, "Found " + keyText(key), focus));
        } else {
            r.record(call + ": not found",
                    "We reached the bottom of the path and there was nothing more to check, so " + keyText(key)
                    + " is not stored. Only a few comparisons were needed to be sure!",
                    snap(Collections.<Integer>emptyList(), -1, Mark.MISSING, 0, "Not in the tree", null));
        }
    }

    private void firstItem(StepRecorder r) {
        r.setOperation("first");
        List<K> keys = store.keys();
        if (keys.isEmpty()) {
            return;
        }
        // follow left children from the root down the left edge
        List<Integer> path = new ArrayList<>();
        BalancedShape shape = new BalancedShape(keys.size());
        int node = shape.getRoot();
        while (node >= 0) {
            path.add(node);
            int leftChild = -1;
            for (int i = 0; i < node; i++) {
                if (shape.parent(i) == node) {
                    leftChild = i;
                    break;
                }
            }
            node = leftChild;
        }
        String call = isMap() ? "firstKey()" : "first()";
        K first = store.first();
        r.record(call + ": keep going left",
                "Want the smallest " + keyWord() + "? Just keep walking LEFT until you can't anymore.",
                snap(path, -1, Mark.NONE, 0, "Walking down the left edge", flightFor(first)));
        r.record(call + " = " + keyText(first),
                isMap() ? "The earliest time is " + keyText(first) + ": that's the NEXT departure on the board!"
                        : "\"" + keyText(first) + "\" comes first in alphabetical order.",
                snap(Collections.<Integer>emptyList(), 0, Mark.FOUND, 0, call + " gives " + keyText(first), flightFor(first)));
    }

    private void removeKey(StepRecorder r, K key) {
        r.setOperation("remove");
        String call = "remove(" + keyText(key) + ")";
        Flight focus = flightFor(key);
        if (!store.keys().isEmpty()) {
            walk(r, key, call, focus);
        }
        int idx = store.keys().indexOf(key);
        if (idx >= 0) {
            r.record(call + ": found it",
                    keyText(key) + " is found. Now it is taken out and the tree re-balances itself.",
                    snap(Collections.<Integer>emptyList(), idx, Mark.REMOVED, 0, "Removing " + keyText(key), focus));
        }
        boolean removed = store.remove(key);
        if (removed) {
            r.swap();
            valueLabel.remove(key);
            r.record(call + ": removed",
                    "Gone! The tree reshaped itself, and the bottom strip is still perfectly sorted. Size is now " + store.keys().size() + ".",
                    snap(Collections.<Integer>emptyList(), -1, Mark.NONE, 0, "Removed " + keyText(key), focus));
        } else {
            r.record(call + ": nothing to remove",
                    keyText(key) + " is not in the tree, so nothing changes. No crash, just " + (isMap() ? "null" : "false") + ".",
                    snap(Collections.<Integer>emptyList(), -1, Mark.MISSING, 0, "Nothing removed", null));
        }
    }

    private void iterate(StepRecorder r) {
        r.setOperation("iterate");
        List<K> keys = store.keys();
        r.record("Walk the tree in order",
                "An in-order walk visits LEFT side, then the node, then the RIGHT side. That gives sorted order for free!",
                snap(Collections.<Integer>emptyList(), -1, Mark.NONE, 0, "for (... : " + (isMap() ? "map.keySet()" : "set") + ")", null));
        for (int i = 0; i < keys.size(); i++) {
            r.record("Visit " + (i + 1) + ": " + keyText(keys.get(i)),
                    "Next smallest is " + keyText(keys.get(i)) + ".",
                    snap(Collections.<Integer>emptyList(), i, Mark.VISIT, i, "Visited " + (i + 1) + " of " + keys.size(), flightFor(keys.get(i))));
        }
        r.record("Sorted, every time",
                "We got every " + keyWord() + " from smallest to biggest, without calling any sort method!",
                snap(Collections.<Integer>emptyList(), -1, Mark.NONE, keys.size(), "Done", null));
    }

    private Flight flightFor(K key) {
        if (key == null) {
            return null;
        }
        Flight v = store.get(key);
        if (v != null) {
            return v;
        }
        for (Flight f : flights()) {
            if (keyOf(f).equals(key)) {
                return f;
            }
        }
        return null;
    }

    private SortedTreeState snap(List<Integer> path, int active, Mark mark, int visited, String note, Flight focus) {
        List<K> keys = store.keys();
        List<String> labels = new ArrayList<>();
        List<String> subs = new ArrayList<>();
        for (K k : keys) {
            labels.add(keyText(k));
            if (isMap()) {
                subs.add(valueLabel.getOrDefault(k, ""));
            }
        }
        List<String> hl = new ArrayList<>();
        if (focus != null) {
            for (Flight f : flights()) {
                if (isMap() ? f.equals(focus) : keyOf(f).equals(keyOf(focus))) {
                    hl.add(f.getFlightNumber());
                }
            }
            if (isMap() && !hl.contains(focus.getFlightNumber())) {
                hl.add(focus.getFlightNumber());
            }
        }
        List<Flight> table = new ArrayList<>(flights());
        if (focus != null && !table.contains(focus)) {
            table.add(focus);
        }
        return new SortedTreeState(structureName() + "  -  " + keys.size() + " item(s)", note, labels, subs, path,
                active, mark, visited, rejected, table, hl);
    }

    @Override
    protected void paintState(Graphics2D g, SortedTreeState s, int w, int h) {
        SortedTreePainter.paint(g, s, w, h);
    }
}
