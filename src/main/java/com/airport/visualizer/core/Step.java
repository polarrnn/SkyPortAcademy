package com.airport.visualizer.core;

/**
 * One frame of an animation: what the picture looks like (state), what a 9-year-old
 * should be told about it (title + narration), and the running counters at that moment.
 * The state object must be an immutable snapshot.
 */
public final class Step {

    private final String title;
    private final String narration;
    private final Object state;
    private final int primaryCount;
    private final int secondaryCount;
    private final String operationKey;

    public Step(String title, String narration, Object state, int primaryCount, int secondaryCount,
            String operationKey) {
        this.title = title == null ? "" : title;
        this.narration = narration == null ? "" : narration;
        this.state = state;
        this.primaryCount = primaryCount;
        this.secondaryCount = secondaryCount;
        this.operationKey = operationKey;
    }

    public String getTitle() {
        return title;
    }

    public String getNarration() {
        return narration;
    }

    public Object getState() {
        return state;
    }

    /** Usually "comparisons". */
    public int getPrimaryCount() {
        return primaryCount;
    }

    /** Usually "swaps" (or moves / visits, see {@link Visualizer#getCounterLabels()}). */
    public int getSecondaryCount() {
        return secondaryCount;
    }

    /** Key of the {@link ComplexityInfo.Row} this step belongs to (e.g. "put"), or null. */
    public String getOperationKey() {
        return operationKey;
    }
}
