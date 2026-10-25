package com.airport.visualizer.structures.hash;

import com.airport.visualizer.model.Flight;
import com.airport.visualizer.structures.BaseState;
import com.airport.visualizer.structures.Mark;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Immutable picture of a hash-based structure: buckets with chains plus an order strip. */
public final class BucketState extends BaseState {

    private final String title;
    private final String calc;
    private final String note;
    private final int capacity;
    private final List<List<String>> buckets;
    private final int activeBucket;
    private final String activeKey;
    private final Mark mark;
    private final int compareIndex;
    private final String orderLabel;
    private final List<String> order;
    private final int orderDone;
    private final int orderActive;
    private final boolean orderLinked;
    private final List<String> rejected;

    private BucketState(Builder b) {
        super(b.table, b.highlights);
        this.title = b.title;
        this.calc = b.calc;
        this.note = b.note;
        this.capacity = b.capacity;
        List<List<String>> copy = new ArrayList<>();
        for (List<String> chain : b.buckets) {
            copy.add(Collections.unmodifiableList(new ArrayList<>(chain)));
        }
        this.buckets = Collections.unmodifiableList(copy);
        this.activeBucket = b.activeBucket;
        this.activeKey = b.activeKey;
        this.mark = b.mark;
        this.compareIndex = b.compareIndex;
        this.orderLabel = b.orderLabel;
        this.order = Collections.unmodifiableList(new ArrayList<>(b.order));
        this.orderDone = b.orderDone;
        this.orderActive = b.orderActive;
        this.orderLinked = b.orderLinked;
        this.rejected = Collections.unmodifiableList(new ArrayList<>(b.rejected));
    }

    public static Builder builder(String title, int capacity, List<List<String>> buckets) {
        return new Builder(title, capacity, buckets);
    }

    public String getTitle() {
        return title;
    }

    public String getCalc() {
        return calc;
    }

    public String getNote() {
        return note;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<List<String>> getBuckets() {
        return buckets;
    }

    public int getActiveBucket() {
        return activeBucket;
    }

    public String getActiveKey() {
        return activeKey;
    }

    public Mark getMark() {
        return mark;
    }

    public int getCompareIndex() {
        return compareIndex;
    }

    public String getOrderLabel() {
        return orderLabel;
    }

    public List<String> getOrder() {
        return order;
    }

    public int getOrderDone() {
        return orderDone;
    }

    public int getOrderActive() {
        return orderActive;
    }

    public boolean isOrderLinked() {
        return orderLinked;
    }

    public List<String> getRejected() {
        return rejected;
    }

    /** Fluent builder - the snapshot has many optional parts. */
    public static final class Builder {

        private final String title;
        private final int capacity;
        private final List<List<String>> buckets;
        private String calc = "";
        private String note = "";
        private int activeBucket = -1;
        private String activeKey;
        private Mark mark = Mark.NONE;
        private int compareIndex = -1;
        private String orderLabel = "Order";
        private List<String> order = new ArrayList<>();
        private int orderDone;
        private int orderActive = -1;
        private boolean orderLinked;
        private List<String> rejected = new ArrayList<>();
        private List<Flight> table = new ArrayList<>();
        private Collection<String> highlights = new ArrayList<>();

        private Builder(String title, int capacity, List<List<String>> buckets) {
            this.title = title;
            this.capacity = capacity;
            this.buckets = buckets;
        }

        public Builder calc(String text) {
            this.calc = text == null ? "" : text;
            return this;
        }

        public Builder note(String text) {
            this.note = text == null ? "" : text;
            return this;
        }

        public Builder active(int bucket, String key, Mark mark) {
            this.activeBucket = bucket;
            this.activeKey = key;
            this.mark = mark;
            return this;
        }

        public Builder compareIndex(int i) {
            this.compareIndex = i;
            return this;
        }

        public Builder order(String label, List<String> items, int done, int active, boolean linked) {
            this.orderLabel = label;
            this.order = items;
            this.orderDone = done;
            this.orderActive = active;
            this.orderLinked = linked;
            return this;
        }

        public Builder rejected(List<String> items) {
            this.rejected = items;
            return this;
        }

        public Builder table(List<Flight> flights, Collection<String> highlightNumbers) {
            this.table = flights;
            this.highlights = highlightNumbers;
            return this;
        }

        public BucketState build() {
            return new BucketState(this);
        }
    }
}
