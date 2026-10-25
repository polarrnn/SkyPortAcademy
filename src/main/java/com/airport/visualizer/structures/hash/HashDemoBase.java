package com.airport.visualizer.structures.hash;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructureDemo;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared recording engine for the four hash-based demos (HashMap, LinkedHashMap, HashSet,
 * LinkedHashSet). Subclasses only say which real structure to use and what the key is.
 */
public abstract class HashDemoBase extends StructureDemo<BucketState> {

    private KeyStore store;
    private int maxSize;
    private final List<String> rejected = new ArrayList<>();

    protected HashDemoBase(String name, Category category, String summary, List<Flight> flights) {
        super(name, category, summary, BucketState.class, flights);
    }

    /** A fresh, empty REAL java.util structure wrapped in a {@link KeyStore}. */
    protected abstract KeyStore newStore();

    /** The key this demo stores for a flight (flight number, destination, gate...). */
    protected abstract String keyOf(Flight f);

    protected abstract boolean isMap();

    protected abstract boolean isLinked();

    /** "HashMap", "LinkedHashSet"... */
    protected abstract String structureName();

    /** Words for the key, e.g. "flight number" or "destination". */
    protected abstract String keyWord();

    /** A key that is surely not stored, for the "not found" example. */
    protected abstract String missingKey();

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Key comparisons", "Items stored / removed"};
    }

    @Override
    protected void runPlain(String scenario) {
        KeyStore s = newStore();
        for (Flight f : flights()) {
            s.add(keyOf(f), f);
        }
        s.get(keyOf(pick(2)));
        s.contains(missingKey());
        s.remove(keyOf(pick(1)));
        for (String k : s.keysInOrder()) {
            k.length();
        }
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        store = newStore();
        maxSize = 0;
        rejected.clear();
        String sn = structureName();
        r.setOperation(null);
        r.record("Meet the " + sn,
                "A " + sn + " is like a wall of " + capacity() + " numbered lockers (we call them buckets). "
                + "A magic number machine called a HASH turns each " + keyWord() + " into a locker number, "
                + "so the computer can jump straight to the right locker instead of searching everywhere.",
                snap(-1, null, Mark.NONE, -1, "", "Empty lockers, waiting for " + keyWord() + "s.", -1, -1));

        if (wants(scenario, ADD)) {
            for (Flight f : flights()) {
                addOne(r, f, true);
            }
            if (isMap()) {
                duplicatePut(r);
            }
        } else {
            for (Flight f : flights()) {
                addOne(r, f, false);
            }
            r.setOperation(null);
            r.record("Lockers already filled",
                    "To save time we already put all " + flights().size() + " flights in. Now let's try the next operation!",
                    snap(-1, null, Mark.NONE, -1, "", store.size() + " " + keyWord() + "s are stored.", -1, -1));
        }
        if (wants(scenario, GET)) {
            lookup(r, keyOf(pick(2)));
            lookup(r, missingKey());
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
                isLinked()
                        ? "A " + sn + " gives you fast hash lockers AND remembers the order things came in. "
                        + "That costs a little extra memory for the links."
                        : "A " + sn + " is super fast for finding by " + keyWord() + ", but it does not keep any order. "
                        + "If you need order, use a Linked or Tree version.",
                snap(-1, null, Mark.NONE, -1, "", "Final size: " + store.size(), -1, -1));
    }

    // ------------------------------------------------------------------ operations

    private void addOne(StepRecorder r, Flight f, boolean show) {
        String key = keyOf(f);
        String op = isMap() ? "put" : "add";
        r.setOperation(op);
        int cap = capacity();
        int b = HashBuckets.index(key, cap);
        List<String> chain = new ArrayList<>(HashBuckets.layout(store.keysInOrder(), cap).get(b));
        String call = isMap() ? "put(\"" + key + "\", " + f.getFlightNumber() + ")" : "add(\"" + key + "\")";
        if (show) {
            r.record(call + ": find the locker",
                    "The hash machine reads \"" + key + "\" and gives the number " + b + ". So " + key
                    + " belongs in locker " + b + ". The same " + keyWord() + " ALWAYS gives the same locker.",
                    snap(b, key, Mark.LOOK, -1, HashBuckets.calcText(key, cap), "Locker " + b + " please!", -1, -1, f));
        }
        for (int j = 0; j < chain.size(); j++) {
            r.compare();
            if (show) {
                r.record("Is it already here?",
                        "Locker " + b + " is not empty, so we check the item inside: is \"" + chain.get(j)
                        + "\" the same as \"" + key + "\"? " + (chain.get(j).equals(key) ? "Yes, it is!" : "No, it is different."),
                        snap(b, key, Mark.COMPARE, j, HashBuckets.calcText(key, cap), "Comparing " + chain.get(j) + " with " + key, -1, -1, f));
            }
            if (chain.get(j).equals(key)) {
                break;
            }
        }
        boolean isNew = store.add(key, f);
        maxSize = Math.max(maxSize, store.size());
        cap = capacity();
        b = HashBuckets.index(key, cap);
        if (isNew) {
            r.swap();
            if (show) {
                String story = chain.isEmpty()
                        ? key + " goes into empty locker " + b + ". Done in one jump - that is why it's O(1)!"
                        : "Locker " + b + " already had " + String.join(", ", chain) + ". That's a COLLISION (two keys, same locker). "
                        + "No problem: " + key + " joins a little chain in that locker.";
                r.record(call + ": stored!", story,
                        snap(b, key, Mark.ADDED, -1, HashBuckets.calcText(key, cap), "Size is now " + store.size(), lastIndex(), lastIndex(), f));
            }
        } else if (isMap()) {
            if (show) {
                r.record(call + ": key already there",
                        "A map keeps each key only once. The old value for " + key + " is swapped for the new one, so the size stays " + store.size() + ".",
                        snap(b, key, Mark.REPLACED, -1, HashBuckets.calcText(key, cap), "Value replaced, size stays " + store.size(), -1, -1, f));
            }
        } else {
            rejected.add(key);
            if (show) {
                r.record(call + " says false",
                        "\"" + key + "\" is already in the set! A set is like a sticker album: you can only stick each sticker once. "
                        + "So add(...) answers false and nothing changes.",
                        snap(b, key, Mark.REJECTED, -1, HashBuckets.calcText(key, cap), "Duplicate rejected: " + key, -1, -1, f));
            }
        }
    }

    private void duplicatePut(StepRecorder r) {
        Flight old = pick(0);
        Flight changed = new Flight(old.getFlightNumber(), old.getAirline(), old.getDestination(),
                old.getGate().startsWith("D") ? "E1" : "D1", old.getDepartureTime(), old.getType());
        r.setOperation("put");
        r.record("Same key again?",
                "What if we put " + old.getFlightNumber() + " again, but now it leaves from gate " + changed.getGate() + "? Watch the size!",
                snap(-1, null, Mark.NONE, -1, "", "put(\"" + old.getFlightNumber() + "\", new gate " + changed.getGate() + ")", -1, -1, old));
        addOne(r, changed, true);
    }

    private void lookup(StepRecorder r, String key) {
        int cap = capacity();
        int b = HashBuckets.index(key, cap);
        boolean present = store.contains(key);
        String op = isMap() ? (present ? "get" : "containsKey") : "contains";
        String call = isMap() ? (present ? "get(\"" + key + "\")" : "containsKey(\"" + key + "\")") : "contains(\"" + key + "\")";
        r.setOperation(op);
        Flight f = flightFor(key);
        r.record(call + ": which locker?",
                "To find \"" + key + "\" we do NOT open every locker. The hash machine says: look in locker " + b + ".",
                snap(b, key, Mark.LOOK, -1, HashBuckets.calcText(key, cap), "Go straight to locker " + b, -1, -1, f));
        List<String> chain = HashBuckets.layout(store.keysInOrder(), cap).get(b);
        for (int j = 0; j < chain.size(); j++) {
            r.compare();
            boolean same = chain.get(j).equals(key);
            r.record("Check item " + (j + 1) + " in locker " + b,
                    "Is \"" + chain.get(j) + "\" equal to \"" + key + "\"? " + (same ? "Yes!" : "No, keep looking in this locker only."),
                    snap(b, same ? key : null, Mark.COMPARE, j, HashBuckets.calcText(key, cap), "Comparing", -1, -1, f));
            if (same) {
                break;
            }
        }
        if (present) {
            Flight value = store.get(key);
            String story = isMap() && value != null
                    ? "Found it! get gives back flight " + value.getFlightNumber() + " to " + value.getRouteText()
                    + " at gate " + value.getGate() + ", " + value.getTimeText() + "."
                    : "Found it! contains answers true.";
            r.record(call + ": found", story,
                    snap(b, key, Mark.FOUND, -1, HashBuckets.calcText(key, cap), "Found in locker " + b, -1, -1, f));
        } else {
            r.record(call + ": not here",
                    chain.isEmpty()
                            ? "Locker " + b + " is empty, so \"" + key + "\" is not stored. We knew that after just ONE look!"
                            : "We checked every item in locker " + b + " and none matched, so \"" + key + "\" is not stored.",
                    snap(b, null, Mark.MISSING, -1, HashBuckets.calcText(key, cap), (isMap() ? (op + " gives null/false") : "contains gives false"), -1, -1, f));
        }
    }

    private void removeKey(StepRecorder r, String key) {
        int cap = capacity();
        int b = HashBuckets.index(key, cap);
        r.setOperation("remove");
        Flight f = flightFor(key);
        String call = "remove(\"" + key + "\")";
        r.record(call + ": which locker?",
                "To remove \"" + key + "\" we first find its locker the same quick way: locker " + b + ".",
                snap(b, key, Mark.LOOK, -1, HashBuckets.calcText(key, cap), "Look in locker " + b, -1, -1, f));
        List<String> chain = HashBuckets.layout(store.keysInOrder(), cap).get(b);
        for (int j = 0; j < chain.size(); j++) {
            r.compare();
            if (chain.get(j).equals(key)) {
                r.record("There it is",
                        "\"" + key + "\" is item " + (j + 1) + " in locker " + b + ". Time to take it out.",
                        snap(b, key, Mark.REMOVED, -1, HashBuckets.calcText(key, cap), "Removing " + key, -1, -1, f));
                break;
            }
        }
        boolean removed = store.remove(key);
        if (removed) {
            r.swap();
            r.record(call + ": removed",
                    "Gone! The chain in locker " + b + " closes the gap" + (isLinked() ? " and the order links skip over it too." : ".")
                    + " Size is now " + store.size() + ".",
                    snap(b, null, Mark.REMOVED, -1, HashBuckets.calcText(key, cap), "Removed " + key, -1, -1, f));
        } else {
            r.record(call + ": nothing to remove",
                    "\"" + key + "\" was never stored, so remove does nothing (it just answers " + (isMap() ? "null" : "false") + "). No crash!",
                    snap(b, null, Mark.MISSING, -1, HashBuckets.calcText(key, cap), "Nothing removed", -1, -1, f));
        }
    }

    private void iterate(StepRecorder r) {
        r.setOperation("iterate");
        List<String> keys = store.keysInOrder();
        r.record("Let's visit everything",
                isLinked()
                        ? "Iterating a " + structureName() + " follows the hidden links in the order things were added."
                        : "Iterating a " + structureName() + " walks locker 0, locker 1, locker 2... so the order depends on the hash numbers, not on when things were added.",
                snap(-1, null, Mark.NONE, -1, "", "for (" + (isMap() ? "String key : map.keySet()" : "String item : set") + ")", 0, -1));
        int cap = capacity();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            r.swap(0);
            r.record("Visit " + (i + 1) + " of " + keys.size() + ": " + k,
                    "Next we visit \"" + k + "\" (in locker " + HashBuckets.index(k, cap) + ").",
                    snap(HashBuckets.index(k, cap), k, Mark.VISIT, -1, "", "Visited " + (i + 1) + " so far", i, i, flightFor(k)));
        }
        r.record("Visiting finished",
                isLinked()
                        ? "The order was exactly the order we added them. Handy for 'first come, first shown' lists!"
                        : "See how the order looks shuffled? That's normal for a " + structureName() + ". Never count on its order.",
                snap(-1, null, Mark.NONE, -1, "", "Order: " + String.join(", ", keys), keys.size(), -1));
    }

    // ------------------------------------------------------------------ snapshot helpers

    private int capacity() {
        return HashBuckets.capacityFor(Math.max(maxSize, 1));
    }

    private int lastIndex() {
        if (!isLinked()) {
            return -1;
        }
        return store.size() - 1;
    }

    private Flight flightFor(String key) {
        for (Flight f : flights()) {
            if (keyOf(f).equals(key)) {
                return f;
            }
        }
        return null;
    }

    private BucketState snap(int bucket, String key, Mark mark, int compareIndex, String calc, String note,
            int orderDone, int orderActive) {
        return snap(bucket, key, mark, compareIndex, calc, note, orderDone, orderActive, null);
    }

    private BucketState snap(int bucket, String key, Mark mark, int compareIndex, String calc, String note,
            int orderDone, int orderActive, Flight focus) {
        int cap = capacity();
        List<String> keys = store.keysInOrder();
        int activeInOrder = orderActive;
        if (activeInOrder < 0 && key != null && mark != Mark.NONE) {
            activeInOrder = keys.indexOf(key);
        }
        List<String> hl = new ArrayList<>();
        if (key != null || focus != null) {
            for (Flight f : flights()) {
                if ((key != null && keyOf(f).equals(key)) || (focus != null && f.equals(focus) && isMap())) {
                    hl.add(f.getFlightNumber());
                }
            }
        }
        String label = isLinked()
                ? "Insertion order (hidden linked list - iteration follows these arrows)"
                : "Iteration order (locker 0, 1, 2 ... so it looks shuffled)";
        return BucketState.builder(structureName() + "  -  " + store.size() + " item(s), " + cap + " lockers", cap,
                HashBuckets.layout(keys, cap))
                .calc(calc)
                .note(note)
                .active(bucket, key, mark)
                .compareIndex(compareIndex)
                .order(label, keys, Math.max(0, orderDone), activeInOrder, isLinked())
                .rejected(new ArrayList<>(rejected))
                .table(flights(), hl)
                .build();
    }

    @Override
    protected void paintState(Graphics2D g, BucketState s, int w, int h) {
        BucketPainter.paint(g, s, w, h);
    }
}
