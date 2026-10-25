package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.List;

/**
 * One thing being sorted or searched: either a flight (value = departure minutes)
 * or a plain number typed by the learner. Immutable.
 */
public final class SortItem {

    private final String label;
    private final int value;
    private final Flight flight;

    public SortItem(String label, int value, Flight flight) {
        this.label = label;
        this.value = value;
        this.flight = flight;
    }

    public static SortItem of(Flight f) {
        return new SortItem(f.getFlightNumber(), f.getMinutes(), f);
    }

    public static SortItem of(int number) {
        return new SortItem(String.valueOf(number), number, null);
    }

    public static List<SortItem> fromFlights(List<Flight> flights) {
        List<SortItem> out = new ArrayList<>();
        for (Flight f : flights) {
            out.add(of(f));
        }
        return out;
    }

    public static List<SortItem> fromNumbers(List<Integer> numbers) {
        List<SortItem> out = new ArrayList<>();
        for (Integer n : numbers) {
            out.add(of(n));
        }
        return out;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    /** The flight behind this item, or null for a plain number. */
    public Flight getFlight() {
        return flight;
    }

    public boolean isFlight() {
        return flight != null;
    }

    /** Text drawn on the bar / card: the time for flights, the number otherwise. */
    public String getValueText() {
        return flight != null ? flight.getTimeText() : String.valueOf(value);
    }

    /** How the narration names this item, e.g. "PR102 (13:45)" or "42". */
    public String describe() {
        return flight != null ? flight.getFlightNumber() + " (" + flight.getTimeText() + ")" : String.valueOf(value);
    }

    @Override
    public String toString() {
        return describe();
    }
}
