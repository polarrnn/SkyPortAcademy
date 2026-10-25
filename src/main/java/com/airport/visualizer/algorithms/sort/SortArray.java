package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.StepRecorder;
import java.util.Arrays;
import java.util.List;

/**
 * The line of items an algorithm sorts. Every comparison and swap goes through here, so the
 * counters are always correct. When recording is on, {@link #snapshot} saves a picture for the
 * animation; when it is off the algorithm runs at full speed (used to time the "real" run).
 */
public final class SortArray {

    private final SortItem[] items;
    private final boolean[] sorted;
    private final StepRecorder recorder;
    private final boolean flights;
    private int comparisons;
    private int swaps;

    /** @param recorder null for a fast, silent run */
    public SortArray(List<SortItem> data, StepRecorder recorder) {
        this.items = data.toArray(new SortItem[0]);
        this.sorted = new boolean[items.length];
        this.recorder = recorder;
        this.flights = items.length > 0 && items[0].isFlight();
    }

    public int size() {
        return items.length;
    }

    public SortItem get(int i) {
        return items[i];
    }

    public boolean isRecording() {
        return recorder != null;
    }

    /** Compares the items at two positions (counts as one comparison). */
    public int compare(int i, int j) {
        return compareItems(items[i], items[j]);
    }

    /** Compares two items that may be outside the line, e.g. in merge sort's tray. */
    public int compareItems(SortItem a, SortItem b) {
        comparisons++;
        if (recorder != null) {
            recorder.compare();
        }
        return Integer.compare(a.getValue(), b.getValue());
    }

    public void swap(int i, int j) {
        swaps++;
        if (recorder != null) {
            recorder.swap();
        }
        SortItem t = items[i];
        items[i] = items[j];
        items[j] = t;
    }

    /** Writes an item into a slot (merge sort). Counted on the secondary counter. */
    public void set(int i, SortItem item) {
        swaps++;
        if (recorder != null) {
            recorder.swap();
        }
        items[i] = item;
    }

    public void markSorted(int i) {
        if (i >= 0 && i < sorted.length) {
            sorted[i] = true;
        }
    }

    public void markAllSorted() {
        Arrays.fill(sorted, true);
    }

    public void snapshot(String title, String narration, Marks marks) {
        if (recorder != null) {
            recorder.record(title, narration, new SortState(items, sorted, marks));
        }
    }

    public SortItem[] toArray() {
        return items.clone();
    }

    public int getComparisons() {
        return comparisons;
    }

    public int getSwaps() {
        return swaps;
    }

    // ---- words for the narration, so the same algorithm reads well for flights and numbers ----

    /** "flight" or "number". */
    public String noun() {
        return flights ? "flight" : "number";
    }

    /** "flights" or "numbers". */
    public String nouns() {
        return flights ? "flights" : "numbers";
    }

    /** "leaves later than" or "is bigger than". */
    public String laterThan() {
        return flights ? "leaves later than" : "is bigger than";
    }

    /** "leaves earlier than" or "is smaller than". */
    public String earlierThan() {
        return flights ? "leaves earlier than" : "is smaller than";
    }

    /** "earliest" or "smallest". */
    public String earliest() {
        return flights ? "earliest" : "smallest";
    }

    /** "latest" or "biggest". */
    public String latest() {
        return flights ? "latest" : "biggest";
    }

    /** "earlier" or "smaller". */
    public String earlier() {
        return flights ? "earlier" : "smaller";
    }

    /** "later" or "bigger". */
    public String later() {
        return flights ? "later" : "bigger";
    }

    /** "the same time as" or "equal to". */
    public String sameAs() {
        return flights ? "leaves at the same time as" : "is equal to";
    }
}
