package com.airport.visualizer.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * One flight on the airport board. Immutable, so a snapshot of a list of flights
 * can never be changed by accident while an animation is playing.
 */
public final class Flight implements Comparable<Flight> {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final String flightNumber;
    private final String airline;
    private final String destination;
    private final String gate;
    private final LocalTime departureTime;
    private final FlightType type;

    public Flight(String flightNumber, String airline, String destination, String gate,
            LocalTime departureTime, FlightType type) {
        this.flightNumber = Objects.requireNonNull(flightNumber, "flightNumber");
        this.airline = Objects.requireNonNull(airline, "airline");
        this.destination = Objects.requireNonNull(destination, "destination");
        this.gate = Objects.requireNonNull(gate, "gate");
        this.departureTime = Objects.requireNonNull(departureTime, "departureTime");
        this.type = Objects.requireNonNull(type, "type");
    }

    public Flight(String flightNumber, String airline, String destination, String gate, String hhmm,
            FlightType type) {
        this(flightNumber, airline, destination, gate, LocalTime.parse(hhmm, TIME), type);
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public String getDestination() {
        return destination;
    }

    public String getGate() {
        return gate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public FlightType getType() {
        return type;
    }

    /** "08:15" style text. */
    public String getTimeText() {
        return departureTime.format(TIME);
    }

    /** Minutes after midnight, handy as a number to sort or draw bars with. */
    public int getMinutes() {
        return departureTime.getHour() * 60 + departureTime.getMinute();
    }

    /** "Tokyo" for departures, "from Tokyo" for arrivals. */
    public String getRouteText() {
        return type == FlightType.ARRIVAL ? "from " + destination : destination;
    }

    /** Natural order: earlier time first, then flight number. */
    @Override
    public int compareTo(Flight other) {
        int byTime = departureTime.compareTo(other.departureTime);
        return byTime != 0 ? byTime : flightNumber.compareTo(other.flightNumber);
    }

    /** Two flights are "the same flight" when they share a flight number. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Flight && ((Flight) o).flightNumber.equals(flightNumber);
    }

    @Override
    public int hashCode() {
        return flightNumber.hashCode();
    }

    @Override
    public String toString() {
        return flightNumber + " " + getRouteText() + " " + getTimeText();
    }
}
