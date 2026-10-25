package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.ComplexityInfo;

/**
 * Quick Sort (Lomuto partition): pick the last item as the pivot, move everything earlier to its
 * left, then sort each side the same way.
 */
public final class QuickSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public String getSummary() {
        return "Pick a pivot flight; earlier flights go to its left, later ones to its right. Repeat on each side.";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(n log n)")
                .row("best", "Best case (pivot splits evenly)", "O(n log n)",
                        "Each pivot cuts the line roughly in half, like Merge Sort.")
                .row("average", "Average case (mixed up)", "O(n log n)",
                        "Usually the fastest sort in practice - no extra tray and very little moving.")
                .row("worst", "Worst case (already sorted / reverse)", "O(n²)",
                        "With the last flight as pivot, a sorted line splits into 'everything' and 'nothing' each time.")
                .row("space", "Extra space", "O(log n)",
                        "Only remembers which pieces are still waiting to be sorted.")
                .row("stable", "Stable?", "No",
                        "Long jumps across the pivot can reorder equal flights.")
                .whenToUse("Large, randomly mixed lists of numbers where speed matters most. "
                        + "Java's Arrays.sort for numbers uses a clever Quick Sort.")
                .avoidWhen("Lists that are already sorted when you always pick the last element as pivot, "
                        + "or when you need equal items to keep their order.")
                .build();
    }

    /** With the last item as pivot, sorted-ish and reversed lines are the bad cases. */
    @Override
    public String caseFor(InputShape shape) {
        return shape == InputShape.MIXED ? "average" : "worst";
    }

    @Override
    public void sort(SortArray a) {
        int n = a.size();
        if (a.isRecording()) {
            a.snapshot("Meet Quick Sort",
                    "Quick Sort picks one " + a.noun() + " to be the pivot - think of it as the referee. Everyone who "
                    + a.earlierThan() + " the pivot goes to the "
                    + "left team, everyone else to the right team. Then the pivot stands between them in its final "
                    + "place, and we do the same thing to each team.",
                    Marks.none().range(0, n - 1));
        }
        quick(a, 0, n - 1);
        a.markAllSorted();
        if (a.isRecording()) {
            a.snapshot("All sorted!",
                    "Every pivot found its forever spot and every team got sorted. We used " + a.getComparisons()
                    + " comparisons and " + a.getSwaps() + " swaps.",
                    Marks.none());
        }
    }

    private void quick(SortArray a, int lo, int hi) {
        boolean rec = a.isRecording();
        if (lo > hi) {
            return;
        }
        if (lo == hi) {
            a.markSorted(lo);
            if (rec) {
                a.snapshot(a.get(lo).getLabel() + " is alone",
                        "A team with only one " + a.noun() + " is already sorted, so " + a.get(lo).describe()
                        + " is in its final place.",
                        Marks.none());
            }
            return;
        }
        int pivot = hi;
        if (rec) {
            a.snapshot("Pick a pivot: " + a.get(pivot).getLabel(),
                    "We work on positions " + (lo + 1) + "-" + (hi + 1) + ". The last one, " + a.get(pivot).describe()
                    + ", is the pivot (purple). The wall starts at the left: " + a.nouns() + " that are "
                    + a.earlier() + " than the pivot will be put behind the wall.",
                    Marks.none().pivot(pivot).range(lo, hi).pointer(lo, "wall").pointer(pivot, "pivot"));
        }
        int wall = lo;
        for (int j = lo; j < hi; j++) {
            int c = a.compare(j, pivot);
            if (c < 0) {
                if (rec) {
                    a.snapshot(a.get(j).getLabel() + " joins the left team",
                            a.get(j).describe() + " " + a.earlierThan() + " the pivot " + a.get(pivot).describe()
                            + ". It belongs on the left, so it swaps to the wall and the wall moves one step right.",
                            Marks.none().compare(j, pivot).pivot(pivot).range(lo, hi)
                                    .pointer(wall, "wall").pointer(j, "j"));
                }
                if (wall != j) {
                    a.swap(wall, j);
                    if (rec) {
                        a.snapshot("Swap behind the wall",
                                a.get(wall).describe() + " is now on the left team.",
                                Marks.none().swap(wall, j).pivot(pivot).range(lo, hi)
                                        .pointer(wall, "wall").pointer(j, "j"));
                    }
                }
                wall++;
            } else if (rec) {
                a.snapshot(a.get(j).getLabel() + " stays on the right",
                        a.get(j).describe() + " is not " + a.earlier() + " than the pivot "
                        + a.get(pivot).describe() + ", so it stays where it is, on the right team.",
                        Marks.none().compare(j, pivot).pivot(pivot).range(lo, hi)
                                .pointer(wall, "wall").pointer(j, "j"));
            }
        }
        if (wall != hi) {
            a.swap(wall, hi);
        }
        a.markSorted(wall);
        if (rec) {
            a.snapshot("Pivot lands in its forever spot",
                    "The pivot " + a.get(wall).describe() + " swaps to the wall at position " + (wall + 1)
                    + ". Everything left of it is " + a.earlier() + ", everything right of it is "
                    + a.later() + ". It is green: finished!",
                    Marks.none().swap(wall, hi).range(lo, hi).pointer(wall, "pivot"));
        }
        quick(a, lo, wall - 1);
        quick(a, wall + 1, hi);
    }
}
