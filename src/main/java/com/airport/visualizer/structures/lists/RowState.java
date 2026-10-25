package com.airport.visualizer.structures.lists;

import com.airport.visualizer.model.Flight;
import com.airport.visualizer.structures.BaseState;
import com.airport.visualizer.structures.Mark;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Snapshot of an array-like row of boxes (ArrayList, Collections utility demos). */
public final class RowState extends BaseState {

    private final String title;
    private final String caption;
    private final String note;
    private final Mark noteMark;
    private final List<String> cells;
    private final List<String> subs;
    private final Map<Integer, Mark> marks;
    private final Map<Integer, String> pointers;
    private final int arrowFrom;
    private final int arrowTo;
    private final String extraLabel;
    private final List<String> extra;

    private RowState(Builder b) {
        super(b.table, b.highlights);
        this.title = b.title;
        this.caption = b.caption;
        this.note = b.note;
        this.noteMark = b.noteMark;
        this.cells = Collections.unmodifiableList(new ArrayList<>(b.cells));
        this.subs = Collections.unmodifiableList(new ArrayList<>(b.subs));
        this.marks = Collections.unmodifiableMap(new HashMap<>(b.marks));
        this.pointers = Collections.unmodifiableMap(new HashMap<>(b.pointers));
        this.arrowFrom = b.arrowFrom;
        this.arrowTo = b.arrowTo;
        this.extraLabel = b.extraLabel;
        this.extra = Collections.unmodifiableList(new ArrayList<>(b.extra));
    }

    public static Builder builder(String title, List<String> cells, List<String> subs) {
        return new Builder(title, cells, subs);
    }

    public String getTitle() {
        return title;
    }

    public String getCaption() {
        return caption;
    }

    public String getNote() {
        return note;
    }

    public Mark getNoteMark() {
        return noteMark;
    }

    /** Box labels; a null entry is an empty (unused) slot. */
    public List<String> getCells() {
        return cells;
    }

    public List<String> getSubs() {
        return subs;
    }

    public Map<Integer, Mark> getMarks() {
        return marks;
    }

    /** Little name tags drawn under boxes, e.g. "low", "mid", "high". */
    public Map<Integer, String> getPointers() {
        return pointers;
    }

    public int getArrowFrom() {
        return arrowFrom;
    }

    public int getArrowTo() {
        return arrowTo;
    }

    public String getExtraLabel() {
        return extraLabel;
    }

    public List<String> getExtra() {
        return extra;
    }

    public static final class Builder {

        private final String title;
        private final List<String> cells;
        private final List<String> subs;
        private String caption = "";
        private String note = "";
        private Mark noteMark = Mark.NONE;
        private final Map<Integer, Mark> marks = new HashMap<>();
        private final Map<Integer, String> pointers = new HashMap<>();
        private int arrowFrom = -1;
        private int arrowTo = -1;
        private String extraLabel = "";
        private List<String> extra = new ArrayList<>();
        private List<Flight> table = new ArrayList<>();
        private Collection<String> highlights = new ArrayList<>();

        private Builder(String title, List<String> cells, List<String> subs) {
            this.title = title;
            this.cells = cells;
            this.subs = subs;
        }

        public Builder caption(String c) {
            caption = c == null ? "" : c;
            return this;
        }

        public Builder note(String n, Mark m) {
            note = n == null ? "" : n;
            noteMark = m == null ? Mark.NONE : m;
            return this;
        }

        public Builder mark(int index, Mark m) {
            if (index >= 0) {
                marks.put(index, m);
            }
            return this;
        }

        public Builder pointer(int index, String label) {
            if (index >= 0) {
                String old = pointers.get(index);
                pointers.put(index, old == null ? label : old + "/" + label);
            }
            return this;
        }

        public Builder arrow(int from, int to) {
            arrowFrom = from;
            arrowTo = to;
            return this;
        }

        public Builder extra(String label, List<String> items) {
            extraLabel = label;
            extra = items;
            return this;
        }

        public Builder table(List<Flight> flights, Collection<String> hl) {
            table = flights;
            highlights = hl;
            return this;
        }

        public RowState build() {
            return new RowState(this);
        }
    }
}
