package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.ComplexityInfo;

/** Selection Sort: hunt for the earliest item in the unsorted part and put it next in line. */
public final class SelectionSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Selection Sort";
    }

    @Override
    public String getSummary() {
        return "Hunt through the line for the earliest flight, move it to the front, then hunt again for the next one.";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(n²)")
                .row("best", "Best case (already sorted)", "O(n²)",
                        "Even a tidy line is searched completely every round - Selection Sort never stops early.")
                .row("average", "Average case (mixed up)", "O(n²)",
                        "Round 1 looks at n flights, round 2 at n-1, ... that adds up to about n x n / 2.")
                .row("worst", "Worst case (reverse order)", "O(n²)",
                        "Same number of comparisons as always, but at most one swap per round.")
                .row("space", "Extra space", "O(1)",
                        "Only remembers where the current champion is.")
                .row("stable", "Stable?", "No",
                        "A long-distance swap can make two equal flights trade places.")
                .whenToUse("When swaps are expensive (like moving heavy luggage carts): it makes at most n-1 swaps.")
                .avoidWhen("Large lists, or lists that are almost sorted - it does the same big amount of work anyway.")
                .build();
    }

    @Override
    public void sort(SortArray a) {
        int n = a.size();
        boolean rec = a.isRecording();
        if (rec) {
            a.snapshot("Meet Selection Sort",
                    "Imagine picking players for a team: you look at everyone and choose the best first. "
                    + "Here we look at every " + a.noun() + " that is not finished yet, find the " + a.earliest()
                    + " one, and put it at the front of the unfinished part. Then we repeat.",
                    Marks.none());
        }
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            if (rec) {
                a.snapshot("Round " + (i + 1) + ": the hunt begins",
                        "Everything to the left of position " + (i + 1) + " is finished. We start by guessing that "
                        + a.get(i).describe() + " is the " + a.earliest() + " one. It is our champion for now.",
                        Marks.none().key(min).range(i, n - 1).pointer(i, "i").pointer(min, "min"));
            }
            for (int j = i + 1; j < n; j++) {
                int c = a.compare(j, min);
                if (rec) {
                    String text;
                    if (c < 0) {
                        text = a.get(j).describe() + " " + a.earlierThan() + " our champion " + a.get(min).describe()
                                + ". We have a new champion!";
                    } else {
                        text = a.get(j).describe() + " does not beat our champion " + a.get(min).describe()
                                + ", so the champion stays.";
                    }
                    a.snapshot("Is " + a.get(j).getLabel() + " " + a.earliest() + "?", text,
                            Marks.none().compare(j, min).key(min).range(i, n - 1)
                                    .pointer(i, "i").pointer(min, "min").pointer(j, "j"));
                }
                if (c < 0) {
                    min = j;
                }
            }
            if (min != i) {
                if (rec) {
                    a.snapshot("Champion found: " + a.get(min).getLabel(),
                            a.get(min).describe() + " is the " + a.earliest() + " of the unfinished " + a.nouns()
                            + ". It swaps places with " + a.get(i).describe() + " at position " + (i + 1) + ".",
                            Marks.none().swap(i, min).range(i, n - 1).pointer(i, "i").pointer(min, "min"));
                }
                a.swap(i, min);
            } else if (rec) {
                a.snapshot("Champion is already in place",
                        a.get(i).describe() + " was the " + a.earliest() + " all along and it is already at position "
                        + (i + 1) + ". No swap needed!",
                        Marks.none().key(i).range(i, n - 1).pointer(i, "i,min"));
            }
            a.markSorted(i);
            if (rec) {
                a.snapshot(a.get(i).getLabel() + " is finished",
                        "Position " + (i + 1) + " now holds " + a.get(i).describe() + " and turns green. "
                        + (n - 1 - i) + " " + a.nouns() + " are still waiting.",
                        Marks.none());
            }
        }
        a.markAllSorted();
        if (rec) {
            a.snapshot("All sorted!",
                    "The last " + a.noun() + " is automatically the " + a.latest() + ", so we are done. We needed "
                    + a.getComparisons() + " comparisons but only " + a.getSwaps() + " swaps.",
                    Marks.none());
        }
    }
}
