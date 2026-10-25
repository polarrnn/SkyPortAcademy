package com.airport.visualizer.structures;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Common base for every Map, Set and Collection demo. It fixes the shared recipe
 * (pick scenario, measure the real java.util work, record kid-friendly steps) while each
 * subclass only describes its own structure (Template Method pattern).
 *
 * @param <S> snapshot type painted by the subclass
 */
public abstract class StructureDemo<S> extends AbstractVisualizer<S> {

    public static final String ALL = "All operations";
    public static final String ADD = "add / put";
    public static final String GET = "get / contains";
    public static final String REMOVE = "remove";
    public static final String ITERATE = "iterate";

    /** Largest number of flights a demo will draw, so every picture fits on screen. */
    public static final int MAX_FLIGHTS = 12;

    private final List<Flight> flights;

    protected StructureDemo(String name, Category category, String summary, Class<S> stateType,
            List<Flight> flights) {
        super(name, category, summary, stateType);
        List<Flight> copy = flights == null ? new ArrayList<Flight>() : new ArrayList<>(flights);
        if (copy.size() > MAX_FLIGHTS) {
            copy = new ArrayList<>(copy.subList(0, MAX_FLIGHTS));
        }
        this.flights = Collections.unmodifiableList(copy);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(ALL, ADD, GET, REMOVE, ITERATE);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Moves"};
    }

    @Override
    public List<Flight> getFlights() {
        return flights;
    }

    /** Flights in this demo (already capped to {@link #MAX_FLIGHTS}). */
    protected List<Flight> flights() {
        return flights;
    }

    /** Safe way to pick "the i-th flight" even when the learner typed only a few flights. */
    protected Flight pick(int index) {
        return flights.get(Math.max(0, Math.min(index, flights.size() - 1)));
    }

    protected static boolean wants(String scenario, String part) {
        return scenario == null || ALL.equals(scenario) || part.equals(scenario);
    }

    @Override
    public final Timeline buildTimeline(String scenario) {
        StepRecorder r = new StepRecorder();
        if (flights.isEmpty()) {
            return Timeline.empty("There are no flights yet. Add at least one flight to see this structure work.");
        }
        runPlain(scenario); // warm-up run so the timing below isn't just Java starting up
        long start = System.nanoTime();
        runPlain(scenario);
        long nanos = System.nanoTime() - start;
        record(r, scenario == null ? ALL : scenario);
        return Timeline.of(r, nanos, resultSummary(scenario));
    }

    /** Performs the same real java.util operations as {@link #record} but without drawing (for timing). */
    protected abstract void runPlain(String scenario);

    /** Performs the operations and records a snapshot after every small step. */
    protected abstract void record(StepRecorder r, String scenario);

    /** Optional one-line message shown above the player. */
    protected String resultSummary(String scenario) {
        return "";
    }
}
