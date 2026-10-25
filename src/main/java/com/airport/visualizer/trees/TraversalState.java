package com.airport.visualizer.trees;

import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Snapshot of a traversal step: tree, the to-do structure (stack or queue) and the output so far. */
public final class TraversalState implements FlightTableState {

    private final TreeSnapshot tree;
    private final String caption;
    private final boolean queue;
    private final List<String> structure;
    private final List<String> output;
    private final List<Flight> flights;
    private final Set<String> highlighted;

    public TraversalState(TreeSnapshot tree, String caption, boolean queue, List<String> structure,
            List<String> output, List<Flight> flights, Set<String> highlighted) {
        this.tree = tree;
        this.caption = caption;
        this.queue = queue;
        this.structure = Collections.unmodifiableList(new ArrayList<>(structure));
        this.output = Collections.unmodifiableList(new ArrayList<>(output));
        this.flights = flights == null ? Collections.<Flight>emptyList() : flights;
        this.highlighted = Collections.unmodifiableSet(new LinkedHashSet<>(highlighted));
    }

    public TreeSnapshot getTree() {
        return tree;
    }

    public String getCaption() {
        return caption;
    }

    /** True for level-order (queue), false for the recursive orders (call stack). */
    public boolean isQueue() {
        return queue;
    }

    /** Stack: bottom first. Queue: front first. */
    public List<String> getStructure() {
        return structure;
    }

    public List<String> getOutput() {
        return output;
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
