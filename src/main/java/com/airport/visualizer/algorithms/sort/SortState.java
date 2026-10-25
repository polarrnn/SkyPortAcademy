package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Immutable picture of the line while it is being sorted. */
public final class SortState implements FlightTableState {

    private final SortItem[] items;
    private final boolean[] sorted;
    private final Marks marks;

    public SortState(SortItem[] items, boolean[] sorted, Marks marks) {
        this.items = items.clone();
        this.sorted = sorted.clone();
        this.marks = marks == null ? Marks.none() : marks;
    }

    public int size() {
        return items.length;
    }

    public SortItem get(int i) {
        return items[i];
    }

    public boolean isSorted(int i) {
        return sorted[i];
    }

    public int sortedCount() {
        int c = 0;
        for (boolean b : sorted) {
            if (b) {
                c++;
            }
        }
        return c;
    }

    public Marks getMarks() {
        return marks;
    }

    public boolean hasFlights() {
        return items.length > 0 && items[0].isFlight();
    }

    @Override
    public List<Flight> getTableFlights() {
        List<Flight> out = new ArrayList<>();
        for (SortItem it : items) {
            if (it.isFlight()) {
                out.add(it.getFlight());
            }
        }
        return out;
    }

    @Override
    public Set<String> getHighlightedFlightNumbers() {
        if (!hasFlights()) {
            return Collections.emptySet();
        }
        Set<String> out = new LinkedHashSet<>();
        for (int i : marks.getComparing()) {
            add(out, i);
        }
        for (int i : marks.getSwapping()) {
            add(out, i);
        }
        add(out, marks.getPivot());
        add(out, marks.getKey());
        add(out, marks.getWriting());
        return out;
    }

    private void add(Set<String> out, int i) {
        if (i >= 0 && i < items.length && items[i].isFlight()) {
            out.add(items[i].getFlight().getFlightNumber());
        }
    }
}
