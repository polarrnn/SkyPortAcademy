package com.airport.visualizer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The sample departure / arrival board (24 flights). Deliberately NOT in time order
 * so the sorting and searching demos have real work to do.
 */
public final class FlightData {

    private static final List<Flight> SAMPLE;

    static {
        List<Flight> f = new ArrayList<>();
        f.add(new Flight("PR102", "Philippine Airlines", "Tokyo", "A3", "13:45", FlightType.DEPARTURE));
        f.add(new Flight("5J560", "Cebu Pacific", "Cebu", "B2", "06:10", FlightType.DEPARTURE));
        f.add(new Flight("SQ917", "Singapore Airlines", "Singapore", "C1", "09:30", FlightType.ARRIVAL));
        f.add(new Flight("CX906", "Cathay Pacific", "Hong Kong", "A1", "17:20", FlightType.DEPARTURE));
        f.add(new Flight("KE624", "Korean Air", "Seoul", "B4", "08:05", FlightType.DEPARTURE));
        f.add(new Flight("EK333", "Emirates", "Dubai", "C3", "21:55", FlightType.DEPARTURE));
        f.add(new Flight("Z2440", "AirAsia", "Bangkok", "A5", "11:15", FlightType.ARRIVAL));
        f.add(new Flight("JL742", "Japan Airlines", "Osaka", "B1", "15:40", FlightType.DEPARTURE));
        f.add(new Flight("QR933", "Qatar Airways", "Doha", "C2", "19:05", FlightType.DEPARTURE));
        f.add(new Flight("PR212", "Philippine Airlines", "Sydney", "A2", "22:30", FlightType.DEPARTURE));
        f.add(new Flight("5J241", "Cebu Pacific", "Davao", "B3", "07:25", FlightType.ARRIVAL));
        f.add(new Flight("NH820", "ANA", "Tokyo", "C4", "10:50", FlightType.DEPARTURE));
        f.add(new Flight("DL180", "Delta Air Lines", "Los Angeles", "A4", "23:10", FlightType.DEPARTURE));
        f.add(new Flight("QF020", "Qantas", "Sydney", "B5", "12:35", FlightType.ARRIVAL));
        f.add(new Flight("SQ915", "Singapore Airlines", "Singapore", "C1", "16:25", FlightType.DEPARTURE));
        f.add(new Flight("CX902", "Cathay Pacific", "Hong Kong", "A1", "05:45", FlightType.ARRIVAL));
        f.add(new Flight("Z2612", "AirAsia", "Kuala Lumpur", "A6", "14:20", FlightType.DEPARTURE));
        f.add(new Flight("KE622", "Korean Air", "Seoul", "B4", "20:15", FlightType.ARRIVAL));
        f.add(new Flight("5J389", "Cebu Pacific", "Cebu", "B2", "18:30", FlightType.DEPARTURE));
        f.add(new Flight("PR428", "Philippine Airlines", "Osaka", "A3", "09:05", FlightType.DEPARTURE));
        f.add(new Flight("EK335", "Emirates", "Dubai", "C3", "06:55", FlightType.ARRIVAL));
        f.add(new Flight("JL746", "Japan Airlines", "Tokyo", "B1", "13:05", FlightType.ARRIVAL));
        f.add(new Flight("QR931", "Qatar Airways", "Doha", "C2", "11:50", FlightType.ARRIVAL));
        f.add(new Flight("PR104", "Philippine Airlines", "Bangkok", "A2", "16:55", FlightType.DEPARTURE));
        SAMPLE = Collections.unmodifiableList(f);
    }

    private FlightData() {
    }

    /** All 24 sample flights (read-only). */
    public static List<Flight> sample() {
        return SAMPLE;
    }

    /** The first {@code count} sample flights, for demos that need a smaller picture. */
    public static List<Flight> first(int count) {
        return SAMPLE.subList(0, Math.max(0, Math.min(count, SAMPLE.size())));
    }

    public static Flight byNumber(String flightNumber) {
        for (Flight f : SAMPLE) {
            if (f.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                return f;
            }
        }
        return null;
    }
}
