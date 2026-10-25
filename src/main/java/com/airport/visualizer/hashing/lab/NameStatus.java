package com.airport.visualizer.hashing.lab;

import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;

/** Where a name in the queue is in its journey. */
public enum NameStatus {
    CURRENT("current", Theme.PRIMARY),
    WAITING("waiting", Theme.TEXT_MUTED),
    STORED("stored", Theme.SUCCESS),
    DELETED("deleted", Theme.DANGER),
    FULL("full", Theme.WARNING);

    private final String label;
    private final Color color;

    NameStatus(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }
}
