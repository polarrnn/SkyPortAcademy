package com.airport.visualizer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Big-O facts for one algorithm or data structure, written so a kid can follow,
 * plus "when should I use this?" advice.
 */
public final class ComplexityInfo {

    /** One operation, e.g. key "put", label "put(key, value)", bigO "O(1) average". */
    public static final class Row {

        private final String key;
        private final String label;
        private final String bigO;
        private final String note;

        public Row(String key, String label, String bigO, String note) {
            this.key = key;
            this.label = label;
            this.bigO = bigO;
            this.note = note;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }

        public String getBigO() {
            return bigO;
        }

        public String getNote() {
            return note;
        }
    }

    private final String headline;
    private final List<Row> rows;
    private final String whenToUse;
    private final String avoidWhen;

    private ComplexityInfo(Builder b) {
        this.headline = b.headline;
        this.rows = Collections.unmodifiableList(new ArrayList<>(b.rows));
        this.whenToUse = b.whenToUse;
        this.avoidWhen = b.avoidWhen;
    }

    public static Builder builder(String headline) {
        return new Builder(headline);
    }

    /** The one Big-O to show in the metrics tile, e.g. "O(n²)". */
    public String getHeadline() {
        return headline;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Row findRow(String key) {
        if (key == null) {
            return null;
        }
        for (Row r : rows) {
            if (key.equals(r.getKey())) {
                return r;
            }
        }
        return null;
    }

    public String getWhenToUse() {
        return whenToUse;
    }

    public String getAvoidWhen() {
        return avoidWhen;
    }

    public static final class Builder {

        private final String headline;
        private final List<Row> rows = new ArrayList<>();
        private String whenToUse = "";
        private String avoidWhen = "";

        private Builder(String headline) {
            this.headline = headline;
        }

        public Builder row(String key, String label, String bigO, String note) {
            rows.add(new Row(key, label, bigO, note));
            return this;
        }

        public Builder whenToUse(String text) {
            this.whenToUse = text;
            return this;
        }

        public Builder avoidWhen(String text) {
            this.avoidWhen = text;
            return this;
        }

        public ComplexityInfo build() {
            return new ComplexityInfo(this);
        }
    }
}
