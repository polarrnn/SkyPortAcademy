package com.airport.visualizer.structures.compare;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.StructureDemo;
import java.awt.Graphics2D;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/** The same flights put into a HashMap, a TreeMap and a LinkedHashMap, side by side. */
public class MapComparisonVisualizer extends StructureDemo<CompareState> {

    public static final String BY_NUMBER = "Key = flight number";
    public static final String BY_TIME = "Key = departure time";
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    public MapComparisonVisualizer() {
        this(FlightData.first(10));
    }

    public MapComparisonVisualizer(List<Flight> flights) {
        super("Compare: HashMap vs TreeMap vs LinkedHashMap", Category.MAPS_SETS,
                "Three maps get exactly the same flights. Watch where each new flight lands - same data, three different orders!",
                CompareState.class, flights);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(BY_NUMBER, BY_TIME);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"TreeMap comparisons", "Flights put"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) vs O(log n) vs O(1)")
                .row("put", "put(key, value)", "Hash O(1) | Tree O(log n) | Linked O(1)", "The hash maps jump to a locker; the tree walks down a path.")
                .row("iterate", "iterate keys", "O(n) for all", "Same work, different ORDER: jumbled / sorted / as added.")
                .whenToUse("HashMap: fastest lookups. TreeMap: you need sorted keys or 'next/previous'. LinkedHashMap: fast AND keeps the order you added.")
                .avoidWhen("Don't pick TreeMap just for lookups (slower), and don't rely on HashMap's order - it can change.")
                .build();
    }

    @Override
    protected void runPlain(String scenario) {
        Map<String, Flight> a = new HashMap<>();
        Map<String, Flight> b = new TreeMap<>();
        Map<String, Flight> c = new LinkedHashMap<>();
        for (Flight f : flights()) {
            a.put(f.getFlightNumber(), f);
            b.put(f.getFlightNumber(), f);
            c.put(f.getFlightNumber(), f);
        }
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        if (BY_TIME.equals(scenario)) {
            run(r, Flight::getDepartureTime, k -> k.format(HHMM), "departure time");
        } else {
            run(r, Flight::getFlightNumber, k -> k, "flight number");
        }
    }

    private <K extends Comparable<K>> void run(StepRecorder r, Function<Flight, K> keyFn, Function<K, String> keyText, String keyWord) {
        Map<K, Flight> hash = new HashMap<>();
        int[] treeCompares = {0};
        Map<K, Flight> tree = new TreeMap<>((x, y) -> {
            treeCompares[0]++;
            return x.compareTo(y);
        });
        Map<K, Flight> linked = new LinkedHashMap<>();
        r.setOperation(null);
        r.record("Three maps, one job",
                "We will put the SAME " + flights().size() + " flights into three maps, using the " + keyWord + " as the key. "
                + "They all store the same things... but do they keep them in the same order?",
                snap(hash, tree, linked, keyText, null, "Same data for everyone", null));
        for (Flight f : flights()) {
            K key = keyFn.apply(f);
            r.setOperation("put");
            hash.put(key, f);
            linked.put(key, f);
            int before = treeCompares[0];
            tree.put(key, f);
            r.compare(treeCompares[0] - before);
            r.swap();
            String k = keyText.apply(key);
            int hp = indexOf(hash.keySet(), key) + 1;
            int tp = indexOf(tree.keySet(), key) + 1;
            int lp = indexOf(linked.keySet(), key) + 1;
            r.record("put(" + k + ")",
                    k + " landed at spot " + hp + " in the HashMap (decided by its hash), spot " + tp
                    + " in the TreeMap (sorted place) and spot " + lp + " in the LinkedHashMap (always the end - it was added last).",
                    snap(hash, tree, linked, keyText, key, "put(" + k + ", " + f.getFlightNumber() + ")", f));
        }
        r.setOperation("iterate");
        r.record("Now iterate all three",
                "Same flights, three orders! HashMap looks jumbled, TreeMap is sorted by " + keyWord
                + ", and LinkedHashMap is in the order we added them.",
                snap(hash, tree, linked, keyText, null, "for (key : map.keySet())", null));
        r.setOperation(null);
        String[][] table = {
            {"", "HashMap", "TreeMap", "LinkedHashMap"},
            {"Order", "No order (jumbled)", "Sorted by key", "Order added"},
            {"put / get", "O(1) average", "O(log n)", "O(1) average"},
            {"Extra tricks", "-", "first, last, ranges", "remembers order"},
            {"Memory", "Small", "Medium", "A bit more (links)"},
            {"Use it for", "Fast lookups", "Timetables", "Ordered history"}
        };
        r.record("Which one should I use?",
                "Pick by what you need: speed only? HashMap. Sorted? TreeMap. Speed + the order you added? LinkedHashMap.",
                new CompareState("The takeaway", "Same flights, different super-powers", Collections.<CompareState.Column>emptyList(),
                        true, table, flights(), Collections.<String>emptyList()));
    }

    private static <K> int indexOf(Iterable<K> keys, K key) {
        int i = 0;
        for (K k : keys) {
            if (k.equals(key)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private <K> CompareState snap(Map<K, Flight> hash, Map<K, Flight> tree, Map<K, Flight> linked,
            Function<K, String> keyText, K active, String caption, Flight focus) {
        List<CompareState.Column> cols = new ArrayList<>();
        cols.add(column("HashMap", "jumbled", "O(1)", hash, keyText, active));
        cols.add(column("TreeMap", "sorted", "O(log n)", tree, keyText, active));
        cols.add(column("LinkedHashMap", "as added", "O(1)", linked, keyText, active));
        return new CompareState("Same flights in three maps", caption, cols, false, null, flights(),
                focus == null ? Collections.<String>emptyList() : Collections.singletonList(focus.getFlightNumber()));
    }

    private static <K> CompareState.Column column(String name, String rule, String speed, Map<K, Flight> map,
            Function<K, String> keyText, K active) {
        List<String> items = new ArrayList<>();
        String activeText = null;
        for (Map.Entry<K, Flight> e : map.entrySet()) {
            String k = keyText.apply(e.getKey());
            String text = k.equals(e.getValue().getFlightNumber()) ? k : k + "  (" + e.getValue().getFlightNumber() + ")";
            items.add(text);
            if (e.getKey().equals(active)) {
                activeText = text;
            }
        }
        return new CompareState.Column(name, rule, speed, items, activeText, false);
    }

    @Override
    protected void paintState(Graphics2D g, CompareState s, int w, int h) {
        ComparePainter.paint(g, s, w, h);
    }
}
