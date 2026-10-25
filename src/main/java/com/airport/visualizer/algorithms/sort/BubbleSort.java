package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.ComplexityInfo;

/** Bubble Sort: neighbours swap until the latest item "bubbles" to the end of the line. */
public final class BubbleSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Bubble Sort";
    }

    @Override
    public String getSummary() {
        return "Neighbours compare and swap, so the latest flight floats to the end like a bubble in a soda.";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(n²)")
                .row("best", "Best case (already sorted)", "O(n)",
                        "One walk down the line with no swaps, then we stop early.")
                .row("average", "Average case (mixed up)", "O(n²)",
                        "About n x n / 2 comparisons - double the flights, four times the work.")
                .row("worst", "Worst case (reverse order)", "O(n²)",
                        "Every neighbour pair is in the wrong order, so almost every comparison is a swap.")
                .row("space", "Extra space", "O(1)",
                        "Sorts right inside the line - no extra shelf needed.")
                .row("stable", "Stable?", "Yes",
                        "Two flights with the same time never jump over each other.")
                .whenToUse("Learning how sorting works, or tiny lists that are almost sorted already "
                        + "(it notices quickly that nothing needs to move).")
                .avoidWhen("Big airports! With 1,000 flights it could need about 500,000 comparisons.")
                .build();
    }

    @Override
    public void sort(SortArray a) {
        int n = a.size();
        boolean rec = a.isRecording();
        if (rec) {
            a.snapshot("Meet Bubble Sort",
                    "We have " + n + " " + a.nouns() + " in a line. We will walk from left to right and look at two "
                    + "neighbours at a time. If the left one " + a.laterThan() + " the right one, they swap places. "
                    + "After each walk the " + a.latest() + " one has floated to the end, like a bubble rising up!",
                    Marks.none());
        }
        for (int pass = 0; pass < n - 1; pass++) {
            int end = n - 1 - pass;
            boolean swapped = false;
            if (rec) {
                a.snapshot("Walk number " + (pass + 1) + " begins",
                        "We check neighbours from position 1 up to position " + (end + 1) + ". "
                        + "The green ones at the end are already finished, so we skip them.",
                        Marks.none().range(0, end));
            }
            for (int j = 0; j < end; j++) {
                int c = a.compare(j, j + 1);
                if (rec) {
                    SortItem l = a.get(j);
                    SortItem r = a.get(j + 1);
                    String text = c > 0
                            ? l.describe() + " " + a.laterThan() + " " + r.describe() + ". The later one should be "
                            + "on the right, so these two must swap!"
                            : l.describe() + " " + (c == 0 ? a.sameAs() : a.earlierThan()) + " " + r.describe()
                            + ". They are already in a good order - no swap needed.";
                    a.snapshot("Compare " + l.getLabel() + " and " + r.getLabel(), text,
                            Marks.none().compare(j, j + 1).range(0, end).pointer(j, "j").pointer(j + 1, "j+1"));
                }
                if (c > 0) {
                    a.swap(j, j + 1);
                    swapped = true;
                    if (rec) {
                        a.snapshot("Swap!", a.get(j + 1).describe() + " hops one place to the right and "
                                + a.get(j).describe() + " hops to the left. The bubble keeps rising.",
                                Marks.none().swap(j, j + 1).range(0, end));
                    }
                }
            }
            a.markSorted(end);
            if (rec) {
                a.snapshot(a.get(end).getLabel() + " is in its final spot",
                        "The " + a.latest() + " " + a.noun() + " left in the line, " + a.get(end).describe()
                        + ", bubbled all the way to position " + (end + 1) + ". It turns green: it will never move again.",
                        Marks.none());
            }
            if (!swapped) {
                a.markAllSorted();
                if (rec) {
                    a.snapshot("No swaps - we can stop early!",
                            "We walked the whole line and nobody had to swap. That means everything is already in "
                            + "order, so Bubble Sort finishes early. This is why its best case is fast: O(n).",
                            Marks.none());
                }
                return;
            }
        }
        a.markAllSorted();
        if (rec) {
            a.snapshot("All sorted!",
                    "Every " + a.noun() + " is green, from " + a.earliest() + " on the left to " + a.latest()
                    + " on the right. We used " + a.getComparisons() + " comparisons and " + a.getSwaps() + " swaps.",
                    Marks.none());
        }
    }
}
