package com.airport.visualizer.structures.maps;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.model.FlightType;
import com.airport.visualizer.structures.tree.SortedStore;
import com.airport.visualizer.structures.tree.SortedTreeDemoBase;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** TreeMap&lt;LocalTime departureTime, Flight&gt;: always sorted by time. */
public class TreeMapDemo extends SortedTreeDemoBase<LocalTime> {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    public TreeMapDemo() {
        this(FlightData.first(10));
    }

    public TreeMapDemo(List<Flight> flights) {
        super("TreeMap", Category.MAPS_SETS,
                "departureTime -> Flight, always sorted by time. It's a self-balancing tree, so the next departure is easy to find.",
                flights);
    }

    @Override
    protected SortedStore<LocalTime> newStore() {
        return SortedStore.treeMap();
    }

    @Override
    protected LocalTime keyOf(Flight f) {
        return f.getDepartureTime();
    }

    @Override
    protected String keyText(LocalTime key) {
        return key.format(HHMM);
    }

    @Override
    protected boolean isMap() {
        return true;
    }

    @Override
    protected String structureName() {
        return "TreeMap";
    }

    @Override
    protected String keyWord() {
        return "departure time";
    }

    @Override
    protected LocalTime missingKey() {
        LocalTime t = LocalTime.of(4, 0);
        for (Flight f : flights()) {
            if (f.getDepartureTime().equals(t)) {
                return LocalTime.of(3, 33);
            }
        }
        return t;
    }

    @Override
    protected Flight duplicateKeyFlight() {
        Flight base = pick(0);
        String number = base.getFlightNumber().equals("XX777") ? "XX778" : "XX777";
        return new Flight(number, "Demo Air", "Manila", "D1", base.getDepartureTime(), FlightType.DEPARTURE);
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(log n)")
                .row("put", "put(key, value)", "O(log n)", "Walk down one path of the tree (about log2 n steps), then add and re-balance.")
                .row("get", "get(key)", "O(log n)", "Bigger? go right. Smaller? go left. 1,000 flights need only about 10 steps!")
                .row("containsKey", "containsKey(key)", "O(log n)", "Same walk as get.")
                .row("first", "firstKey()", "O(log n)", "Keep going left: the earliest departure.")
                .row("remove", "remove(key)", "O(log n)", "Find it, unhook it, re-balance.")
                .row("iterate", "iterate keySet()", "O(n)", "In-order walk = sorted by time.")
                .whenToUse("You need keys kept in order: the next departure, all flights between 09:00 and 12:00, or a timetable printed in time order.")
                .avoidWhen("You only look things up by key and never need order - HashMap is faster. Also beware: two flights at the same time share one key!")
                .build();
    }
}
