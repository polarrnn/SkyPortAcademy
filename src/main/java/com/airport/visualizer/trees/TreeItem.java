package com.airport.visualizer.trees;

import com.airport.visualizer.model.Flight;

/**
 * One value stored in a tree or heap: a sortable key plus the text drawn on the node.
 * Works for flights (key = minutes after midnight) and for plain numbers typed in the Tree Lab.
 */
public final class TreeItem {

    private final int key;
    private final String label;
    private final String sublabel;
    private final Flight flight;

    private TreeItem(int key, String label, String sublabel, Flight flight) {
        this.key = key;
        this.label = label;
        this.sublabel = sublabel;
        this.flight = flight;
    }

    public static TreeItem of(Flight f) {
        return new TreeItem(f.getMinutes(), f.getFlightNumber(), f.getTimeText(), f);
    }

    public static TreeItem of(int number) {
        return new TreeItem(number, String.valueOf(number), "", null);
    }

    /** A pretend flight time that is not on the board (used for "search: missing"). */
    public static TreeItem time(int hour, int minute) {
        String t = String.format("%02d:%02d", hour, minute);
        return new TreeItem(hour * 60 + minute, t, t, null);
    }

    public int getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getSublabel() {
        return sublabel;
    }

    /** The flight behind this item, or null for plain numbers. */
    public Flight getFlight() {
        return flight;
    }

    /** "PR102 (13:45)" for flights, "42" for numbers. */
    public String describe() {
        return flight != null ? label + " (" + sublabel + ")" : label;
    }

    /** The value that is compared: "13:45" for flights, "42" for numbers. */
    public String keyText() {
        return sublabel.isEmpty() ? label : sublabel;
    }
}
