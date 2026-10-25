package com.airport.visualizer.trees;

import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Snapshot for BST steps: the tree picture, a caption and the comparison trail so far. */
public final class BstState implements FlightTableState {

    private final TreeSnapshot tree;
    private final String caption;
    private final List<String> trail;
    private final List<Flight> flights;
    private final Set<String> highlighted;

    public BstState(TreeSnapshot tree, String caption, List<String> trail, List<Flight> flights,
            Set<String> highlighted) {
        this.tree = tree;
        this.caption = caption;
        this.trail = Collections.unmodifiableList(new ArrayList<>(trail));
        this.flights = flights == null ? Collections.<Flight>emptyList() : flights;
        this.highlighted = Collections.unmodifiableSet(new LinkedHashSet<>(highlighted));
    }

    public TreeSnapshot getTree() {
        return tree;
    }

    public String getCaption() {
        return caption;
    }

    public List<String> getTrail() {
        return trail;
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
