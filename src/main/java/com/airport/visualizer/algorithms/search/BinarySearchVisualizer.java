package com.airport.visualizer.algorithms.search;

import com.airport.visualizer.algorithms.sort.SortItem;
import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Binary Search: on a sorted board, look in the middle and throw away the half that can't contain it. */
public final class BinarySearchVisualizer extends AbstractVisualizer<SearchState> {

    public static final String EXISTS = "Find a flight that exists (16:25)";
    public static final String MISSING = "Find a flight that is missing (12:00)";
    public static final String FIRST = "Find the first flight (05:45)";
    public static final String LAB = "Your numbers";

    private final List<Integer> labValues;
    private final int labTarget;

    /** Lesson mode: searches the sample board, sorted by departure time, for a time. */
    public BinarySearchVisualizer() {
        this(null, 0);
    }

    /** Lab mode: sorts the learner's numbers first, then searches them. */
    public BinarySearchVisualizer(List<Integer> values, int target) {
        super("Binary Search", Category.SEARCH,
                "On a board sorted by time, peek at the middle flight and throw away the half that can't hold the answer.",
                SearchState.class);
        this.labValues = values == null ? null : Collections.unmodifiableList(new ArrayList<>(values));
        this.labTarget = target;
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(log n)")
                .row("best", "Best case (it's in the middle)", "O(1)",
                        "The very first middle flight is the one we want.")
                .row("average", "Average case", "O(log n)",
                        "Each look throws away half. 24 flights need at most 5 looks; 1,000,000 need only 20!")
                .row("worst", "Worst case (missing)", "O(log n)",
                        "Even 'not here' is fast: we halve until nothing is left.")
                .row("space", "Extra space", "O(1)",
                        "Only three bookmarks: lo, mid and hi.")
                .row("sorted", "Needs a sorted board?", "Yes!",
                        "If the board is messy, throwing away half could throw away the answer.")
                .whenToUse("Big sorted lists you search many times - departure boards, dictionaries, phone books. "
                        + "Java gives you Collections.binarySearch and Arrays.binarySearch.")
                .avoidWhen("Unsorted data you search only once - sorting first costs O(n log n), more than one linear search.")
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
    public List<Flight> getFlights() {
        List<Flight> sorted = new ArrayList<>(FlightData.sample());
        Collections.sort(sorted);
        return sorted;
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        boolean numbers = labValues != null;
        List<SortItem> items;
        int target;
        String targetText;
        if (numbers) {
            List<Integer> sorted = new ArrayList<>(labValues);
            Collections.sort(sorted);
            items = SortItem.fromNumbers(sorted);
            target = labTarget;
            targetText = String.valueOf(target);
        } else {
            items = SortItem.fromFlights(getFlights());
            String hhmm = MISSING.equals(scenario) ? "12:00" : FIRST.equals(scenario) ? "05:45" : "16:25";
            target = Integer.parseInt(hhmm.substring(0, 2)) * 60 + Integer.parseInt(hhmm.substring(3));
            targetText = "a flight at " + hhmm;
        }
        SortItem[] arr = items.toArray(new SortItem[0]);
        int n = arr.length;
        boolean[] out = new boolean[n];
        String things = numbers ? "numbers" : "flights";
        StepRecorder rec = new StepRecorder();
        int answer = plainSearch(arr, target);
        rec.setOperation(answer >= 0 && answer == (n - 1) / 2 ? "best" : answer < 0 ? "worst" : "average");

        String intro = numbers
                ? "Binary Search only works on a SORTED list, so first we sorted your numbers from smallest to biggest: "
                + sortedText(arr) + ". Now we can use the halving trick: look at the middle number. If it is too "
                + "small, the answer can only be on the right, so the whole left half is thrown away at once!"
                : "The board is sorted by departure time, earliest first. That lets us use a trick: look at the middle "
                + "flight. If it leaves too early, the flight we want must be on the right, so the whole left half "
                + "is thrown away at once - like guessing a number with 'higher' or 'lower' hints!";
        rec.record("Meet Binary Search", intro, new SearchState(arr, out, targetText, true, -1, -1, 0, -1, n - 1));

        int lo = 0;
        int hi = n - 1;
        int found = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            rec.compare();
            int c = Integer.compare(arr[mid].getValue(), target);
            rec.record("Peek at the middle: #" + (mid + 1),
                    "The " + things + " still possible are #" + (lo + 1) + " to #" + (hi + 1) + ". The middle one is "
                    + arr[mid].describe() + ". " + (c == 0 ? "That's exactly what we want!"
                    : c < 0 ? (numbers ? "It is smaller than " + target + "." : "It leaves too EARLY.")
                    : (numbers ? "It is bigger than " + target + "." : "It leaves too LATE.")),
                    new SearchState(arr, out, targetText, true, mid, -1, lo, mid, hi));
            if (c == 0) {
                found = mid;
                break;
            }
            int gone;
            String text;
            if (c < 0) {
                gone = mid - lo + 1;
                for (int i = lo; i <= mid; i++) {
                    out[i] = true;
                }
                lo = mid + 1;
                text = "So the answer can only be to the RIGHT of #" + (mid + 1) + ". We cross out #" + (mid + 1 - gone + 1)
                        + " to #" + (mid + 1) + ": " + gone + " " + things + " gone with one comparison!";
            } else {
                gone = hi - mid + 1;
                for (int i = mid; i <= hi; i++) {
                    out[i] = true;
                }
                hi = mid - 1;
                text = "So the answer can only be to the LEFT of #" + (mid + 1) + ". We cross out #" + (mid + 1)
                        + " to #" + (mid + gone) + ": " + gone + " " + things + " gone with one comparison!";
            }
            rec.swap(gone);
            rec.record(c < 0 ? "Throw away the left part" : "Throw away the right part", text,
                    new SearchState(arr, out, targetText, true, -1, -1, lo <= hi ? lo : -1, -1, lo <= hi ? hi : -1));
        }
        String summary;
        if (found >= 0) {
            rec.record("Found it!",
                    arr[found].describe() + " is at position " + (found + 1) + ". Binary Search needed only "
                    + rec.getPrimary() + " comparison" + (rec.getPrimary() == 1 ? "" : "s") + ". Linear Search "
                    + "would have needed " + (found + 1) + ".",
                    new SearchState(arr, out, targetText, true, -1, found, -1, -1, -1));
            summary = "Found " + targetText + " at position " + (found + 1) + " with " + rec.getPrimary() + " comparison"
                    + (rec.getPrimary() == 1 ? "" : "s") + " (out of " + n + ").";
        } else {
            for (int i = 0; i < n; i++) {
                out[i] = true;
            }
            rec.record("Not on the board",
                    "Nothing is left to check, so " + targetText + " is not here. We proved it with only "
                    + rec.getPrimary() + " comparisons - Linear Search would have needed all " + n + ".",
                    new SearchState(arr, out, targetText, true, -1, -1, -1, -1, -1));
            summary = "Not on the board: " + targetText + " - proven with only " + rec.getPrimary() + " comparison"
                    + (rec.getPrimary() == 1 ? "." : "s.");
        }
        return Timeline.of(rec, measure(arr, target), summary);
    }

    private static String sortedText(SortItem[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arr[i].getValue());
        }
        return sb.toString();
    }

    private static int plainSearch(SortItem[] arr, int target) {
        int lo = 0;
        int hi = arr.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int c = Integer.compare(arr[mid].getValue(), target);
            if (c == 0) {
                return mid;
            }
            if (c < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return -1;
    }

    private static long measure(SortItem[] arr, int target) {
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
