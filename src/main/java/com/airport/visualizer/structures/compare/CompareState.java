package com.airport.visualizer.structures.compare;

import com.airport.visualizer.model.Flight;
import com.airport.visualizer.structures.BaseState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Snapshot of three structures side by side, fed with exactly the same data. */
public final class CompareState extends BaseState {

    /** One column: structure name, its current iteration order, and the item just touched. */
    public static final class Column {

        final String name;
        final String rule;
        final String speed;
        final List<String> items;
        final String active;
        final boolean rejected;

        public Column(String name, String rule, String speed, List<String> items, String active, boolean rejected) {
            this.name = name;
            this.rule = rule;
            this.speed = speed;
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
            this.active = active;
            this.rejected = rejected;
        }
    }

    final String title;
    final String caption;
    final List<Column> columns;
    final boolean summary;
    final String[][] summaryRows;

    public CompareState(String title, String caption, List<Column> columns, boolean summary, String[][] summaryRows,
            List<Flight> table, Collection<String> highlights) {
        super(table, highlights);
        this.title = title;
        this.caption = caption;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
        this.summary = summary;
        this.summaryRows = summaryRows;
    }
}
