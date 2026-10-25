package com.airport.visualizer.structures.sets;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.hash.HashDemoBase;
import com.airport.visualizer.structures.hash.KeyStore;
import java.util.List;

/** LinkedHashSet&lt;String&gt; of gates: no duplicates, and the first-seen order is kept. */
public class LinkedHashSetDemo extends HashDemoBase {

    public LinkedHashSetDemo() {
        this(defaultFlights());
    }

    public LinkedHashSetDemo(List<Flight> flights) {
        super("LinkedHashSet", Category.MAPS_SETS,
                "Ordered gates: each gate is kept once, in the order we first saw it - like a guest list that remembers who came first.",
                flights);
    }

    /** Sample flights chosen so some gates repeat (C1 and A1 appear twice). */
    private static List<Flight> defaultFlights() {
        List<Flight> list = new java.util.ArrayList<>(FlightData.first(9));
        list.add(FlightData.byNumber("SQ915"));   // gate C1 again
        list.add(FlightData.byNumber("CX902"));   // gate A1 again
        list.add(FlightData.byNumber("PR212"));
        return list;
    }

    @Override
    protected KeyStore newStore() {
        return KeyStore.linkedHashSet();
    }

    @Override
    protected String keyOf(Flight f) {
        return f.getGate();
    }

    @Override
    protected boolean isMap() {
        return false;
    }

    @Override
    protected boolean isLinked() {
        return true;
    }

    @Override
    protected String structureName() {
        return "LinkedHashSet";
    }

    @Override
    protected String keyWord() {
        return "gate";
    }

    @Override
    protected String missingKey() {
        return "Z9";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) average")
                .row("add", "add(item)", "O(1) average", "Locker jump; new items are linked at the end. Repeats answer false and keep their old spot.")
                .row("contains", "contains(item)", "O(1) average", "Quick locker check.")
                .row("remove", "remove(item)", "O(1) average", "Unhook from the locker and from the order links.")
                .row("iterate", "iterate", "O(n)", "Follows the links: first seen comes first.")
                .whenToUse("You want 'no repeats' AND to remember the order items first appeared, like the gates in the order planes used them.")
                .avoidWhen("You need sorted order (TreeSet) or the smallest memory (HashSet).")
                .build();
    }
}
