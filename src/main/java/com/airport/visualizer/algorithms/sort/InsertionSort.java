package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.ComplexityInfo;

/** Insertion Sort: like sorting playing cards in your hand, one new card at a time. */
public final class InsertionSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Insertion Sort";
    }

    @Override
    public String getSummary() {
        return "Like sorting cards in your hand: pick up the next flight and slide it left until it fits.";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(n²)")
                .row("best", "Best case (already sorted)", "O(n)",
                        "Each new card is already bigger than the last one in your hand - one look and done.")
                .row("average", "Average case (mixed up)", "O(n²)",
                        "Each new card slides past about half of the hand.")
                .row("worst", "Worst case (reverse order)", "O(n²)",
                        "Every new card has to slide all the way to the front.")
                .row("space", "Extra space", "O(1)",
                        "You only hold one card at a time.")
                .row("stable", "Stable?", "Yes",
                        "A card only slides past cards that are strictly bigger.")
                .whenToUse("Small lists, lists that are nearly sorted, or when new flights keep arriving one by one "
                        + "and must be slotted into an already sorted board.")
                .avoidWhen("Large, very mixed-up lists - all that sliding adds up to n² work.")
                .build();
    }

    @Override
    public void sort(SortArray a) {
        int n = a.size();
        boolean rec = a.isRecording();
        if (rec) {
            a.snapshot("Meet Insertion Sort",
                    "Pretend the " + a.nouns() + " are playing cards. The first card alone is already a sorted hand. "
                    + "Then we pick up one new card at a time and slide it to the left until the card before it is "
                    + "not " + a.later() + ".",
                    Marks.none().range(0, 0));
        }
        if (n > 0) {
            a.markSorted(0);
        }
        for (int i = 1; i < n; i++) {
            if (rec) {
                a.snapshot("Pick up " + a.get(i).getLabel(),
                        "Our sorted hand is positions 1 to " + i + ". Now we pick up " + a.get(i).describe()
                        + " (the blue one) and find where it belongs in the hand.",
                        Marks.none().key(i).range(0, i - 1).pointer(i, "key"));
            }
            int j = i;
            while (j > 0) {
                int c = a.compare(j - 1, j);
                if (c > 0) {
                    if (rec) {
                        a.snapshot("Slide past " + a.get(j - 1).getLabel(),
                                a.get(j - 1).describe() + " " + a.laterThan() + " " + a.get(j).describe()
                                + ", so " + a.get(j - 1).getLabel() + " moves one step right to make room.",
                                Marks.none().compare(j - 1, j).key(j).range(0, i).pointer(j, "key"));
                    }
                    a.swap(j - 1, j);
                    j--;
                    if (rec) {
                        a.snapshot("Moved", a.get(j).describe() + " is now at position " + (j + 1) + ".",
                                Marks.none().swap(j, j + 1).key(j).range(0, i).pointer(j, "key"));
                    }
                } else {
                    if (rec) {
                        a.snapshot(a.get(j).getLabel() + " found its spot",
                                a.get(j - 1).describe() + " is not " + a.later() + " than "
                                + a.get(j).describe() + ", so the card stops here at position " + (j + 1) + ".",
                                Marks.none().compare(j - 1, j).key(j).range(0, i).pointer(j, "key"));
                    }
                    break;
                }
            }
            if (j == 0 && rec) {
                a.snapshot(a.get(0).getLabel() + " goes to the very front",
                        a.get(0).describe() + " is the " + a.earliest() + " card so far, so it slid all the way "
                        + "to position 1.",
                        Marks.none().key(0).range(0, i).pointer(0, "key"));
            }
            for (int k = 0; k <= i; k++) {
                a.markSorted(k);
            }
            if (rec) {
                a.snapshot("Hand has " + (i + 1) + " cards",
                        "Positions 1 to " + (i + 1) + " are in order now (green). "
                        + (n - 1 - i) + " " + a.nouns() + " still to pick up.",
                        Marks.none().range(0, i));
            }
        }
        a.markAllSorted();
        if (rec) {
            a.snapshot("All sorted!",
                    "Every card is in the hand and in order. We used " + a.getComparisons() + " comparisons and "
                    + a.getSwaps() + " slides.",
                    Marks.none());
        }
    }
}
