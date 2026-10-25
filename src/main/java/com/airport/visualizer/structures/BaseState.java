package com.airport.visualizer.structures;

import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Immutable base for structure snapshots: which flights the table shows and which are highlighted. */
public abstract class BaseState implements FlightTableState {

    private final List<Flight> table;
    private final Set<String> highlights;

    protected BaseState(List<Flight> table, Collection<String> highlights) {
        this.table = Collections.unmodifiableList(new ArrayList<>(table == null ? Collections.<Flight>emptyList() : table));
        this.highlights = Collections.unmodifiableSet(new LinkedHashSet<>(
                highlights == null ? Collections.<String>emptySet() : highlights));
    }

    @Override
    public List<Flight> getTableFlights() {
        return table;
    }

    @Override
    public Set<String> getHighlightedFlightNumbers() {
        return highlights;
    }

    protected static Set<String> one(String flightNumber) {
        return flightNumber == null ? Collections.<String>emptySet() : Collections.singleton(flightNumber);
    }
}
