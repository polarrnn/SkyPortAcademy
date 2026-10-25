package com.airport.visualizer.hashing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable picture of a hash table at one moment, plus what to highlight
 * (the bucket/slot being checked, the probe trail, a link being changed...).
 */
public final class HashSnapshot {

    public enum Mode {
        CHAINING, PROBING
    }

    /** What is happening at the highlighted spot. */
    public enum Mark {
        NONE, CHECKING, COLLISION, FOUND, INSERTED, DELETED, MISSING, FULL
    }

    /** Link drawing for chaining: none, a brand-new link, or an arrow that jumps over a removed node. */
    public enum Link {
        NONE, NEW_LINK, BYPASS
    }

    /** Use in linkFrom to mean "the bucket's head pointer"; in linkTo to mean "null". */
    public static final int HEAD = -1;
    public static final int NULL = -1;

    private final Mode mode;
    private final int size;
    private final List<List<String>> chains;
    private final String[] slots;
    private final boolean[] tombstones;
    private final String title;
    private final String currentName;
    private final HashFunction.Calculation calc;
    private final String calcSuffix;
    private final int activeIndex;
    private final int activeNode;
    private final List<Integer> probePath;
    private final Mark mark;
    private final Link link;
    private final int linkFrom;
    private final int linkTo;

    private HashSnapshot(Builder b) {
        this.mode = b.mode;
        this.size = b.size;
        List<List<String>> copy = new ArrayList<>();
        if (b.chains != null) {
            for (List<String> c : b.chains) {
                copy.add(Collections.unmodifiableList(new ArrayList<>(c)));
            }
        }
        this.chains = Collections.unmodifiableList(copy);
        this.slots = b.slots == null ? new String[0] : b.slots.clone();
        this.tombstones = b.tombstones == null ? new boolean[0] : b.tombstones.clone();
        this.title = b.title;
        this.currentName = b.currentName;
        this.calc = b.calc;
        this.calcSuffix = b.calcSuffix;
        this.activeIndex = b.activeIndex;
        this.activeNode = b.activeNode;
        this.probePath = Collections.unmodifiableList(new ArrayList<>(b.probePath));
        this.mark = b.mark;
        this.link = b.link;
        this.linkFrom = b.linkFrom;
        this.linkTo = b.linkTo;
    }

    public static Builder chaining(int size, List<List<String>> chains) {
        Builder b = new Builder(Mode.CHAINING, size);
        b.chains = chains;
        return b;
    }

    public static Builder probing(int size, String[] slots, boolean[] tombstones) {
        Builder b = new Builder(Mode.PROBING, size);
        b.slots = slots;
        b.tombstones = tombstones;
        return b;
    }

    public Mode getMode() {
        return mode;
    }

    public int getSize() {
        return size;
    }

    /** Chaining: the names in bucket i, head first. */
    public List<String> getChain(int i) {
        return i >= 0 && i < chains.size() ? chains.get(i) : Collections.<String>emptyList();
    }

    /** Probing: the name in slot i, or null for EMPTY / DELETED. */
    public String getSlot(int i) {
        return i >= 0 && i < slots.length ? slots[i] : null;
    }

    public boolean isTombstone(int i) {
        return i >= 0 && i < tombstones.length && tombstones[i];
    }

    public int getItemCount() {
        int n = 0;
        if (mode == Mode.CHAINING) {
            for (List<String> c : chains) {
                n += c.size();
            }
        } else {
            for (String s : slots) {
                if (s != null) {
                    n++;
                }
            }
        }
        return n;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrentName() {
        return currentName;
    }

    public HashFunction.Calculation getCalc() {
        return calc;
    }

    /** Full calculation line, e.g. "A(65)+... = 231 → 231 % 7 = 0 → home index 0 → stored at slot 2". */
    public String getCalcLine() {
        if (calc == null) {
            return null;
        }
        String base = currentName + ":  " + calc.text();
        return calcSuffix == null || calcSuffix.isEmpty() ? base : base + HashFunction.ARROW + calcSuffix;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public int getActiveNode() {
        return activeNode;
    }

    public List<Integer> getProbePath() {
        return probePath;
    }

    public Mark getMark() {
        return mark;
    }

    public Link getLink() {
        return link;
    }

    public int getLinkFrom() {
        return linkFrom;
    }

    public int getLinkTo() {
        return linkTo;
    }

    /** Fluent builder; tables pre-fill the structure, operations add the highlights. */
    public static final class Builder {

        private final Mode mode;
        private final int size;
        private List<List<String>> chains;
        private String[] slots;
        private boolean[] tombstones;
        private String title = "";
        private String currentName;
        private HashFunction.Calculation calc;
        private String calcSuffix;
        private int activeIndex = -1;
        private int activeNode = -1;
        private List<Integer> probePath = new ArrayList<>();
        private Mark mark = Mark.NONE;
        private Link link = Link.NONE;
        private int linkFrom = HEAD;
        private int linkTo = NULL;

        private Builder(Mode mode, int size) {
            this.mode = mode;
            this.size = size;
        }

        public Builder title(String t) {
            this.title = t;
            return this;
        }

        public Builder name(String n) {
            this.currentName = n;
            return this;
        }

        public Builder calc(HashFunction.Calculation c) {
            this.calc = c;
            return this;
        }

        public Builder calcSuffix(String s) {
            this.calcSuffix = s;
            return this;
        }

        public Builder active(int index) {
            this.activeIndex = index;
            return this;
        }

        public Builder node(int nodePosition) {
            this.activeNode = nodePosition;
            return this;
        }

        public Builder path(List<Integer> path) {
            this.probePath = path == null ? new ArrayList<Integer>() : new ArrayList<>(path);
            return this;
        }

        public Builder mark(Mark m) {
            this.mark = m;
            return this;
        }

        public Builder link(Link kind, int from, int to) {
            this.link = kind;
            this.linkFrom = from;
            this.linkTo = to;
            return this;
        }

        public HashSnapshot build() {
            return new HashSnapshot(this);
        }
    }
}
