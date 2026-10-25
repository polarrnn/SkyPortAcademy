package com.airport.visualizer.trees;

import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.trees.TreeSnapshot.Mark;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Snapshot of a heap step: the array, the same data drawn as a tree, marks and the index formula. */
public final class HeapState implements FlightTableState {

    private final List<TreeItem> array;
    private final Map<Integer, Mark> marks;
    private final TreeSnapshot tree;
    private final String caption;
    private final String formula;
    private final List<String> taken;
    private final List<Flight> flights;
    private final Set<String> highlighted;

    public HeapState(List<TreeItem> array, Map<Integer, Mark> marks, String caption, String formula,
            List<String> taken, List<Flight> flights) {
        this.array = Collections.unmodifiableList(new ArrayList<>(array));
        this.marks = Collections.unmodifiableMap(new HashMap<>(marks));
        TreeSnapshot.Builder b = TreeSnapshot.fromArray(this.array);
        for (Map.Entry<Integer, Mark> e : marks.entrySet()) {
            b.mark(e.getKey(), e.getValue());
        }
        this.tree = b.build();
        this.caption = caption;
        this.formula = formula == null ? "" : formula;
        this.taken = Collections.unmodifiableList(new ArrayList<>(taken));
        this.flights = flights == null ? Collections.<Flight>emptyList() : flights;
        Set<String> hi = new LinkedHashSet<>();
        for (Map.Entry<Integer, Mark> e : marks.entrySet()) {
            int i = e.getKey();
            if (i >= 0 && i < array.size() && e.getValue() == Mark.CURRENT && array.get(i).getFlight() != null) {
                hi.add(array.get(i).getFlight().getFlightNumber());
            }
        }
        this.highlighted = Collections.unmodifiableSet(hi);
    }

    public List<TreeItem> getArray() {
        return array;
    }

    public Mark markOf(int index) {
        Mark m = marks.get(index);
        return m == null ? Mark.NONE : m;
    }

    public TreeSnapshot getTree() {
        return tree;
    }

    public String getCaption() {
        return caption;
    }

    public String getFormula() {
        return formula;
    }

    /** Items removed so far (earliest first). */
    public List<String> getTaken() {
        return taken;
    }

    @Override
    public List<Flight> getTableFlights() {
        return flights;
    }

    @Override
    public Set<String> getHighlightedFlightNumbers() {
        return highlighted;
    }
}
