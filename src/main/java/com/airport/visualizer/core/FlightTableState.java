package com.airport.visualizer.core;

import com.airport.visualizer.model.Flight;
import java.util.List;
import java.util.Set;

/**
 * Optional interface for a step's state: when implemented, the "Flight Data" JTable
 * shows these rows (e.g. the current order while sorting) and highlights some of them.
 */
public interface FlightTableState {

    List<Flight> getTableFlights();

    /** Flight numbers to highlight in the table (may be empty, never null). */
    Set<String> getHighlightedFlightNumbers();
}
