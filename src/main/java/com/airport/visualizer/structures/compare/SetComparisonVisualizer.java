package com.airport.visualizer.structures.compare;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.StructureDemo;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/** The same values (with repeats) added to a HashSet, a TreeSet and a LinkedHashSet, side by side. */
public class SetComparisonVisualizer extends StructureDemo<CompareState> {

    public static final String DESTINATIONS = "Destinations";
    public static final String AIRLINES = "Airlines";
    public static final String GATES = "Gates";

    public SetComparisonVisualizer() {
        this(FlightData.first(12));
    }

    public SetComparisonVisualizer(List<Flight> flights) {
        super("Compare: HashSet vs TreeSet vs LinkedHashSet", Category.MAPS_SETS,
                "Three sets get the same values, repeats included. All three refuse twins - but they line things up differently!",
                CompareState.class, flights);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(DESTINATIONS, AIRLINES, GATES);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"TreeSet comparisons", "Duplicates rejected"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) vs O(log n) vs O(1)")
                .row("add", "add(item)", "Hash O(1) | Tree O(log n) | Linked O(1)", "All three check for a twin first and answer false if one exists.")
                .row("iterate", "iterate", "O(n) for all", "Jumbled / ABC order / first-seen order.")
                .whenToUse("HashSet: fastest 'is it there?'. TreeSet: unique AND sorted. LinkedHashSet: unique AND first-seen order.")
                .avoidWhen("Need repeats? Then a set is the wrong tool - use a List.")
                .build();
    }

    private Function<Flight, String> keyFn(String scenario) {
        if (AIRLINES.equals(scenario)) {
            return Flight::getAirline;
        }
        if (GATES.equals(scenario)) {
            return Flight::getGate;
        }
        return Flight::getDestination;
    }

    @Override
    protected void runPlain(String scenario) {
        Function<Flight, String> fn = keyFn(scenario);
        Set<String> a = new HashSet<>();
        Set<String> b = new TreeSet<>();
        Set<String> c = new LinkedHashSet<>();
        for (Flight f : flights()) {
            a.add(fn.apply(f));
            b.add(fn.apply(f));
            c.add(fn.apply(f));
        }
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        Function<Flight, String> fn = keyFn(scenario);
        String word = (scenario == null ? DESTINATIONS : scenario).toLowerCase();
        Set<String> hash = new HashSet<>();
        int[] cmp = {0};
        Set<String> tree = new TreeSet<>((x, y) -> {
            cmp[0]++;
            return x.compareTo(y);
        });
        Set<String> linked = new LinkedHashSet<>();
        r.setOperation(null);
        r.record("Three sets, same " + word,
                "We read the " + word + " of " + flights().size() + " flights. Some repeat! Each set will keep every value only ONCE.",
                snap(hash, tree, linked, null, false, "Same values for everyone", null));
        for (Flight f : flights()) {
            String v = fn.apply(f);
            r.setOperation("add");
            boolean a = hash.add(v);
            boolean c = linked.add(v);
            int before = cmp[0];
            boolean b = tree.add(v);
            r.compare(cmp[0] - before);
            boolean added = a && b && c;
            if (!added) {
                r.swap();
            }
            r.record("add(\"" + v + "\") -> " + added,
                    added
                            ? "\"" + v + "\" is new. HashSet puts it where its hash says, TreeSet in ABC order, LinkedHashSet at the end."
                            : "\"" + v + "\" is already in all three sets - every one of them answers false and nothing changes. No twins allowed!",
                    snap(hash, tree, linked, v, !added, "add(\"" + v + "\") from flight " + f.getFlightNumber(), f));
        }
        r.setOperation("iterate");
        r.record("Same values, three orders",
                "All three sets hold the same " + hash.size() + " unique " + word + ". HashSet: jumbled. TreeSet: ABC order. LinkedHashSet: first-seen order.",
                snap(hash, tree, linked, null, false, "for (String s : set)", null));
        r.setOperation(null);
        String[][] table = {
            {"", "HashSet", "TreeSet", "LinkedHashSet"},
            {"Duplicates?", "Rejected", "Rejected", "Rejected"},
            {"Order", "No order (jumbled)", "Sorted (ABC)", "First seen"},
            {"add / contains", "O(1) average", "O(log n)", "O(1) average"},
            {"Extra tricks", "-", "first, last, ranges", "remembers order"},
            {"Use it for", "Fast 'is it there?'", "Sorted unique lists", "Ordered unique lists"}
        };
        r.record("Which one should I use?",
                "All sets say no to twins. Choose by the ORDER you want: none (fastest), sorted, or first-seen.",
                new CompareState("The takeaway", "Same values, different orders", Collections.<CompareState.Column>emptyList(),
                        true, table, flights(), Collections.<String>emptyList()));
    }

    private CompareState snap(Set<String> hash, Set<String> tree, Set<String> linked, String active, boolean rejected,
            String caption, Flight focus) {
        List<CompareState.Column> cols = new ArrayList<>();
        cols.add(new CompareState.Column("HashSet", "jumbled", "O(1)", new ArrayList<>(hash), active, rejected));
        cols.add(new CompareState.Column("TreeSet", "sorted", "O(log n)", new ArrayList<>(tree), active, rejected));
        cols.add(new CompareState.Column("LinkedHashSet", "first seen", "O(1)", new ArrayList<>(linked), active, rejected));
        return new CompareState("Same values in three sets", caption, cols, false, null, flights(),
                focus == null ? Collections.<String>emptyList() : Collections.singletonList(focus.getFlightNumber()));
    }

    @Override
    protected void paintState(Graphics2D g, CompareState s, int w, int h) {
        ComparePainter.paint(g, s, w, h);
    }
}
