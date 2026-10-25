package com.airport.visualizer.algorithms.search;

import com.airport.visualizer.algorithms.sort.SortItem;
import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.model.FlightData;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Linear Search: check every flight, one by one, until the right one turns up. */
public final class LinearSearchVisualizer extends AbstractVisualizer<SearchState> {

    public static final String EXISTS = "Find a flight that exists (QF020)";
    public static final String MISSING = "Find a flight that is missing (AA999)";
    public static final String FIRST = "Find the very first flight (PR102)";
    public static final String LAB = "Your numbers";

    private final List<Integer> labValues;
    private final int labTarget;

    /** Lesson mode: searches the unsorted sample board by flight number. */
    public LinearSearchVisualizer() {
        this(null, 0);
    }

    /** Lab mode: searches the learner's numbers for a target. */
    public LinearSearchVisualizer(List<Integer> values, int target) {
        super("Linear Search", Category.SEARCH,
                "Check every flight on the board, one after another, until you find the one you want.",
                SearchState.class);
        this.labValues = values == null ? null : Collections.unmodifiableList(new ArrayList<>(values));
        this.labTarget = target;
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(n)")
                .row("best", "Best case (it's the first one)", "O(1)",
                        "Lucky! The very first flight we look at is the one.")
                .row("average", "Average case", "O(n)",
                        "On average we check about half the board.")
                .row("worst", "Worst case (last one, or missing)", "O(n)",
                        "We must look at every single flight before we can say 'not here'.")
                .row("space", "Extra space", "O(1)",
                        "We only remember where we are.")
                .row("sorted", "Needs a sorted board?", "No",
                        "Works on any list, even a messy one. That's its superpower.")
                .whenToUse("Small or unsorted lists, or when you search only once (sorting first would cost more).")
                .avoidWhen("Huge lists you search again and again - sort them once and use Binary Search, "
                        + "or put them in a HashMap for O(1) look-ups.")
                .build();
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Ruled out"};
    }

    @Override
    public List<String> getScenarios() {
        return labValues != null ? Collections.singletonList(LAB) : Arrays.asList(EXISTS, MISSING, FIRST);
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        List<SortItem> items;
        String target;
        boolean numbers = labValues != null;
        if (numbers) {
            items = SortItem.fromNumbers(labValues);
            target = String.valueOf(labTarget);
        } else {
            items = SortItem.fromFlights(FlightData.sample());
            target = MISSING.equals(scenario) ? "AA999" : FIRST.equals(scenario) ? "PR102" : "QF020";
        }
        SortItem[] arr = items.toArray(new SortItem[0]);
        int n = arr.length;
        boolean[] out = new boolean[n];
        StepRecorder rec = new StepRecorder();
        String thing = numbers ? "number" : "flight";
        String things = numbers ? "numbers" : "flights";

        int answer = plainSearch(arr, target);
        rec.setOperation(answer == 0 ? "best" : answer < 0 || answer == n - 1 ? "worst" : "average");

        rec.record("Meet Linear Search",
                "We are looking for " + target + ". The board is not in any special order, so there is no shortcut: "
                + "we start at the first " + thing + " and check each one, like looking for your friend in a line "
                + "by checking every face from the front.",
                new SearchState(arr, out, target, false, -1, -1, -1, -1, -1));
        int found = -1;
        for (int i = 0; i < n; i++) {
            rec.compare();
            boolean match = key(arr[i]).equals(target);
            rec.record("Check " + thing + " #" + (i + 1),
                    "Is " + arr[i].describe() + " the one we want? "
                    + (match ? "YES - it matches " + target + "!" : "No, " + key(arr[i]) + " is not " + target + "."),
                    new SearchState(arr, out, target, false, i, -1, -1, -1, -1));
            if (match) {
                found = i;
                break;
            }
            out[i] = true;
            rec.swap();
            rec.record("Cross out #" + (i + 1),
                    key(arr[i]) + " is crossed out. " + (n - i - 1) + " " + things + " are left to check.",
                    new SearchState(arr, out, target, false, -1, -1, -1, -1, -1));
        }
        String summary;
        if (found >= 0) {
            rec.record("Found it!",
                    target + " is at position " + (found + 1) + ". We needed " + (found + 1) + " comparison"
                    + (found == 0 ? "" : "s") + ". " + (found == 0 ? "That's the best case: O(1)!"
                    : "If it had been last, we would have needed " + n + "."),
                    new SearchState(arr, out, target, false, -1, found, -1, -1, -1));
            summary = "Found " + target + " at position " + (found + 1) + " after " + (found + 1) + " comparison"
                    + (found == 0 ? "." : "s.");
        } else {
            rec.record("Not on the board",
                    "We checked all " + n + " " + things + " and none of them is " + target + ". To be sure something "
                    + "is missing, Linear Search must look at everything - that's the worst case, O(n).",
                    new SearchState(arr, out, target, false, -1, -1, -1, -1, -1));
            summary = target + " is not on the board - all " + n + " " + things + " had to be checked.";
        }
        return Timeline.of(rec, measure(arr, target), summary);
    }

    private static String key(SortItem it) {
        return it.isFlight() ? it.getLabel() : String.valueOf(it.getValue());
    }

    private static int plainSearch(SortItem[] arr, String target) {
        for (int i = 0; i < arr.length; i++) {
            if (key(arr[i]).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    private static long measure(SortItem[] arr, String target) {
        long[] t = new long[7];
        for (int r = 0; r < t.length; r++) {
            long t0 = System.nanoTime();
            plainSearch(arr, target);
            t[r] = System.nanoTime() - t0;
        }
        Arrays.sort(t);
        return t[t.length / 2];
    }

    @Override
    protected void paintState(Graphics2D g, SearchState state, int width, int height) {
        SearchPainter.paint(g, state, width, height);
    }
}
