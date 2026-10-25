package com.airport.visualizer.structures;

import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;

/** What is happening to the highlighted item in a structure snapshot. Each mark has a color AND a word. */
public enum Mark {
    NONE(Theme.V_NEUTRAL, ""),
    LOOK(Theme.V_ACTIVE, "looking"),
    COMPARE(Theme.V_COMPARE, "comparing"),
    ADDED(Theme.V_SORTED, "added"),
    FOUND(Theme.V_FOUND, "found"),
    MISSING(Theme.V_COMPARE, "not found"),
    REJECTED(Theme.V_SWAP, "rejected"),
    REMOVED(Theme.V_SWAP, "removed"),
    REPLACED(Theme.V_PIVOT, "replaced"),
    MOVED(Theme.V_PIVOT, "moving"),
    VISIT(Theme.V_VISITED, "visiting"),
    SORTED(Theme.tint(Theme.V_SORTED, 0.15), "sorted part"),
    DIM(Theme.V_DIM, "ignored");

    private final Color color;
    private final String word;

    Mark(Color color, String word) {
        this.color = color;
        this.word = word;
    }

    public Color color() {
        return color;
    }

    public String word() {
        return word;
    }

    /** Readable text color on top of this mark's fill. */
    public Color textColor() {
        return this == DIM ? Theme.TEXT_MUTED : Color.WHITE;
    }
}
