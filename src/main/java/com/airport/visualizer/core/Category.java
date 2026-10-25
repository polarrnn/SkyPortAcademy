package com.airport.visualizer.core;

import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;

/** The groups shown in the sidebar. */
public enum Category {
    SORT("Sort", "Put flights in order", Theme.PRIMARY),
    SEARCH("Search", "Find a flight fast", Theme.TEAL),
    TREE("Trees & Graphs", "Family trees and route maps", Theme.SUCCESS),
    MAPS_SETS("Maps & Sets", "Labels, lockers and hashing", Theme.PURPLE),
    COLLECTIONS("Collections", "Lists, lines and queues", Theme.WARNING),
    LABS("Practice Labs", "Type your own data", Theme.DANGER);

    private final String title;
    private final String tagline;
    private final Color color;

    Category(String title, String tagline, Color color) {
        this.title = title;
        this.tagline = tagline;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public String getTagline() {
        return tagline;
    }

    public Color getColor() {
        return color;
    }
}
