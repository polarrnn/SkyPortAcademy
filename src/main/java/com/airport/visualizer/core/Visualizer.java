package com.airport.visualizer.core;

import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;

/**
 * The common interface behind EVERY demo (sorts, searches, maps, sets, collections, trees,
 * graphs, hashing). The UI only talks to this interface, so new demos can be added without
 * changing any screen (Open/Closed + Dependency Inversion).
 */
public interface Visualizer {

    String getName();

    Category getCategory();

    /** One kid-friendly sentence shown under the title. */
    String getSummary();

    ComplexityInfo getComplexity();

    /** Variations the learner can pick (e.g. "Sort by time", "Search: found"). */
    default List<String> getScenarios() {
        return Collections.singletonList("Default");
    }

    /** Names of the two counters, e.g. {"Comparisons", "Swaps"}. */
    default String[] getCounterLabels() {
        return new String[]{"Comparisons", "Swaps"};
    }

    /** Flights used by this demo (shown in the Flight Data table). */
    default List<Flight> getFlights() {
        return FlightData.sample();
    }

    /** Runs the algorithm and records every step for the chosen scenario. */
    Timeline buildTimeline(String scenario);

    /** Paints one step onto the canvas. */
    void paint(Graphics2D g, Step step, int width, int height);
}
