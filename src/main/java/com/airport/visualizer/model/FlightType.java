package com.airport.visualizer.model;

/** Whether a flight is leaving our airport or landing at it. */
public enum FlightType {
    DEPARTURE("Departure"),
    ARRIVAL("Arrival");

    private final String label;

    FlightType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
