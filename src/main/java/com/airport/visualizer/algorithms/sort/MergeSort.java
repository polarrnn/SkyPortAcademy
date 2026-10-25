package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.ComplexityInfo;
import java.util.ArrayList;
import java.util.List;

/** Merge Sort: split the line into halves until each piece is tiny, then zip pieces back together. */
public final class MergeSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Merge Sort";
    }

    @Override
    public String getSummary() {
        return "Split the line into halves again and again, then zip the sorted halves back together.";
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Writes"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(n log n)")
                .row("best", "Best case (already sorted)", "O(n log n)",
                        "It still splits and zips everything - it never takes a shortcut.")
                .row("average", "Average case (mixed up)", "O(n log n)",
                        "About log n levels of splitting, and each level zips all n flights.")
                .row("worst", "Worst case (reverse order)", "O(n log n)",
                        "Always the same speed - no bad surprises.")
                .row("space", "Extra space", "O(n)",
                        "Needs a waiting tray as big as the line while zipping.")
                .row("stable", "Stable?", "Yes",
                        "When two fronts tie, the left half goes first, keeping the original order.")
                .whenToUse("Big lists when you need guaranteed speed and stability, e.g. sorting thousands of "
                        + "flights by time while keeping airline order. Java uses a merge-style sort for objects.")
                .avoidWhen("Very small memory: it needs an extra tray as long as the list.")
                .build();
    }

    @Override
    public void sort(SortArray a) {
        int n = a.size();
        if (a.isRecording()) {
            a.snapshot("Meet Merge Sort",
                    "Sorting " + n + " " + a.nouns() + " at once is hard. Merge Sort's trick: cut the line in half, "
                    + "and keep cutting until every piece has just one " + a.noun() + ". A single " + a.noun()
                    + " is already sorted! Then we zip pairs of sorted pieces back together.",
                    Marks.none().range(0, n - 1));
        }
        if (n > 1) {
            mergeSort(a, 0, n - 1);
        }
        a.markAllSorted();
        if (a.isRecording()) {
            a.snapshot("All sorted!",
                    "All the little pieces were zipped back into one sorted line. We used " + a.getComparisons()
                    + " comparisons and " + a.getSwaps() + " writes.",
                    Marks.none());
        }
    }

    private void mergeSort(SortArray a, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        if (a.isRecording()) {
            a.snapshot("Split positions " + (lo + 1) + "-" + (hi + 1),
                    "This piece has " + (hi - lo + 1) + " " + a.nouns() + ". Cut it into a left half (positions "
                    + (lo + 1) + "-" + (mid + 1) + ") and a right half (positions " + (mid + 2) + "-" + (hi + 1)
                    + "). We sort each half first.",
                    Marks.none().range(lo, hi).split(mid));
        }
        mergeSort(a, lo, mid);
        mergeSort(a, mid + 1, hi);
        merge(a, lo, mid, hi);
    }

    private void merge(SortArray a, int lo, int mid, int hi) {
        boolean rec = a.isRecording();
        List<SortItem> left = new ArrayList<>();
        List<SortItem> right = new ArrayList<>();
        for (int i = lo; i <= mid; i++) {
            left.add(a.get(i));
        }
        for (int i = mid + 1; i <= hi; i++) {
            right.add(a.get(i));
        }
        if (rec) {
            a.snapshot("Zip time!",
                    "Both halves are sorted. We lift them into the waiting tray (bottom) and refill the empty "
                    + "slots, always taking the " + a.earliest() + " of the two front " + a.nouns() + ".",
                    Marks.none().range(lo, hi).empty(lo, hi).trays(left, right).writing(lo));
        }
        int i = 0;
        int j = 0;
        int k = lo;
        while (i < left.size() && j < right.size()) {
            SortItem l = left.get(i);
            SortItem r = right.get(j);
            int c = a.compareItems(l, r);
            SortItem chosen = c <= 0 ? l : r;
            if (rec) {
                a.snapshot("Compare the two fronts",
                        "Left front: " + l.describe() + ". Right front: " + r.describe() + ". "
                        + chosen.describe() + " is " + (c == 0 ? "tied, and the left one goes first" : a.earlier())
                        + ", so it walks into slot " + (k + 1) + ".",
                        Marks.none().range(lo, hi).empty(k, hi).trays(left.subList(i, left.size()), right.subList(j, right.size()))
                                .writing(k));
            }
            a.set(k, chosen);
            if (c <= 0) {
                i++;
            } else {
                j++;
            }
            k++;
        }
        while (i < left.size()) {
            a.set(k, left.get(i));
            if (rec) {
                a.snapshot("Leftover from the left half",
                        "The right half is empty, so " + left.get(i).describe() + " simply walks into slot " + (k + 1) + ".",
                        Marks.none().range(lo, hi).empty(k + 1, hi).trays(left.subList(i + 1, left.size()), right.subList(j, right.size()))
                                .writing(k));
            }
            i++;
            k++;
        }
        while (j < right.size()) {
            a.set(k, right.get(j));
            if (rec) {
                a.snapshot("Leftover from the right half",
                        "The left half is empty, so " + right.get(j).describe() + " simply walks into slot " + (k + 1) + ".",
                        Marks.none().range(lo, hi).empty(k + 1, hi).trays(left.subList(i, left.size()), right.subList(j + 1, right.size()))
                                .writing(k));
            }
            j++;
            k++;
        }
        if (rec) {
            a.snapshot("Merged positions " + (lo + 1) + "-" + (hi + 1),
                    "These " + (hi - lo + 1) + " " + a.nouns() + " are now one sorted piece.",
                    Marks.none().range(lo, hi));
        }
    }
}
