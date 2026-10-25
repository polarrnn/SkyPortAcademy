package com.airport.visualizer.input;

import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Checks a flight typed into a form and builds it only when every field is valid.
 * Every problem is reported in plain words; nothing throws.
 */
public final class FlightValidator {

    private static final Pattern FLIGHT_NO = Pattern.compile("[A-Z0-9]{2}\\d{1,4}");
    private static final Pattern WORDS = Pattern.compile("[A-Za-z][A-Za-z .'\\-]{0,29}");
    private static final Pattern GATE = Pattern.compile("[A-Z]\\d{1,2}");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("H:mm");

    /** Outcome of validation: either a Flight or a list of messages. */
    public static final class Result {

        private final Flight flight;
        private final List<String> errors;

        private Result(Flight flight, List<String> errors) {
            this.flight = flight;
            this.errors = errors;
        }

        public boolean isOk() {
            return flight != null;
        }

        public Flight getFlight() {
            return flight;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String errorText() {
            return String.join(" ", errors);
        }
    }

    private FlightValidator() {
    }

    /**
     * @param existing flights already on the board, used to reject a duplicate flight number
     */
    public static Result validate(String flightNumber, String airline, String destination, String gate,
            String time, FlightType type, Collection<Flight> existing) {
        List<String> errors = new ArrayList<>();
        String no = safe(flightNumber).toUpperCase();
        String al = safe(airline);
        String de = safe(destination);
        String ga = safe(gate).toUpperCase();
        String ti = safe(time);

        if (no.isEmpty()) {
            errors.add("Flight number is empty.");
        } else if (!FLIGHT_NO.matcher(no).matches()) {
            errors.add("Flight number should look like PR102 (2 letters/digits + 1-4 digits).");
        } else if (existing != null) {
            for (Flight f : existing) {
                if (f.getFlightNumber().equalsIgnoreCase(no)) {
                    errors.add("Flight " + no + " already exists - flight numbers must be unique.");
                    break;
                }
            }
        }
        if (al.isEmpty()) {
            errors.add("Airline is empty.");
        } else if (!WORDS.matcher(al).matches()) {
            errors.add("Airline should use letters only (up to 30).");
        }
        if (de.isEmpty()) {
            errors.add("Destination is empty.");
        } else if (!WORDS.matcher(de).matches()) {
            errors.add("Destination should use letters only (up to 30).");
        }
        if (ga.isEmpty()) {
            errors.add("Gate is empty.");
        } else if (!GATE.matcher(ga).matches()) {
            errors.add("Gate should look like A3 or B12.");
        }
        LocalTime parsed = null;
        if (ti.isEmpty()) {
            errors.add("Time is empty.");
        } else {
            try {
                parsed = LocalTime.parse(ti, TIME);
            } catch (DateTimeParseException ex) {
                errors.add("Time should be 24-hour HH:mm, like 08:15 or 21:40.");
            }
        }
        if (type == null) {
            errors.add("Choose Departure or Arrival.");
        }
        if (!errors.isEmpty()) {
            return new Result(null, errors);
        }
        return new Result(new Flight(no, al, de, ga, parsed, type), errors);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
