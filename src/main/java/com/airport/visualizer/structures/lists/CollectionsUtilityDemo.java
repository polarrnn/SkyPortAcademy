package com.airport.visualizer.structures.lists;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.model.FlightType;
import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructureDemo;
import java.awt.Graphics2D;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * The java.util.Collections helper class: sort, reverse, shuffle, min, max and binarySearch on a
 * List&lt;Flight&gt;. Each step copies the real JDK algorithm (TimSort's small-list path, the swap loops,
 * the index binary search) and the final result is checked against the real method.
 */
public class CollectionsUtilityDemo extends StructureDemo<RowState> {

    public static final String SORT = "Collections.sort";
    public static final String REVERSE = "Collections.reverse";
    public static final String SHUFFLE = "Collections.shuffle";
    public static final String MINMAX = "Collections.min / max";
    public static final String BSEARCH = "Collections.binarySearch";

    private static final Comparator<Flight> BY_TIME = Comparator.comparing(Flight::getDepartureTime);
    private static final long SEED = 42L;

    private List<Flight> list = new ArrayList<>();

    public CollectionsUtilityDemo() {
        this(FlightData.first(8));
    }

    public CollectionsUtilityDemo(List<Flight> flights) {
        super("Collections utility class", Category.COLLECTIONS,
                "Ready-made tools for any list: sort it, flip it, mix it, find the earliest or latest flight, and search it super fast.",
                RowState.class, flights);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(ALL, SORT, REVERSE, SHUFFLE, MINMAX, BSEARCH);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Moves / swaps"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("varies by method")
                .row("sort", "Collections.sort(list, byTime)", "O(n log n)", "Java's TimSort. For small lists it slides each flight into place like sorting playing cards.")
                .row("reverse", "Collections.reverse(list)", "O(n)", "Swap the two ends, then the next two, meeting in the middle.")
                .row("shuffle", "Collections.shuffle(list, random)", "O(n)", "From the back, swap each box with a random earlier box - like mixing cards.")
                .row("min", "Collections.min(list, byTime)", "O(n)", "Walk once, remember the earliest so far.")
                .row("max", "Collections.max(list, byTime)", "O(n)", "Walk once, remember the latest so far.")
                .row("binarySearch", "Collections.binarySearch(list, key, byTime)", "O(log n)", "Only on a SORTED list: check the middle, throw away half, repeat.")
                .whenToUse("Whenever you have a List and need a common job done - these methods are tested, fast and save you writing loops.")
                .avoidWhen("binarySearch on an unsorted list gives nonsense answers - always sort first!")
                .build();
    }

    @Override
    protected void runPlain(String scenario) {
        List<Flight> l = new ArrayList<>(flights());
        Collections.sort(l, BY_TIME);
        Collections.reverse(l);
        Collections.shuffle(l, new Random(SEED));
        Collections.min(l, BY_TIME);
        Collections.max(l, BY_TIME);
        Collections.sort(l, BY_TIME);
        Collections.binarySearch(l, pick(2), BY_TIME);
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        list = new ArrayList<>(flights());
        r.setOperation(null);
        r.record("Our list of flights",
                "Here is a List of " + list.size() + " flights in the order they were written on the board. "
                + "The Collections class has handy tools that work on ANY list.",
                snap("List<Flight> flights", null).build());
        boolean all = ALL.equals(scenario);
        if (all || SORT.equals(scenario)) {
            sort(r);
        }
        if (all || REVERSE.equals(scenario)) {
            reverse(r);
        }
        if (all || SHUFFLE.equals(scenario)) {
            shuffle(r);
        }
        if (all || MINMAX.equals(scenario)) {
            minMax(r, true);
            minMax(r, false);
        }
        if (all || BSEARCH.equals(scenario)) {
            binarySearch(r);
        }
        r.setOperation(null);
        r.record("All done!", "The Collections class is a toolbox: one line of code does each job for you.",
                snap("Final list", null).build());
    }

    // ------------------------------------------------------------------ sort (TimSort small-list path)

    private void sort(StepRecorder r) {
        r.setOperation("sort");
        List<Flight> expected = new ArrayList<>(list);
        int[] realCount = {0};
        Collections.sort(expected, (a, b) -> {
            realCount[0]++;
            return BY_TIME.compare(a, b);
        });
        r.record("Collections.sort(flights, byTime)",
                "Let's put the flights in time order. For a short list, Java's TimSort first looks for a piece at the start that is "
                + "already in order, then slides every other flight into its correct place - just like sorting cards in your hand.",
                snap("Sort by departure time", Mark.LOOK).build());
        int n = list.size();
        if (n < 2) {
            return;
        }
        // countRunAndMakeAscending
        int runHi = 1;
        r.compare();
        if (BY_TIME.compare(list.get(1), list.get(0)) < 0) {
            while (runHi + 1 < n) {
                r.compare();
                if (BY_TIME.compare(list.get(runHi + 1), list.get(runHi)) < 0) {
                    runHi++;
                } else {
                    break;
                }
            }
            runHi++;
            for (int lo = 0, hi = runHi - 1; lo < hi; lo++, hi--) {
                Collections.swap(list, lo, hi);
                r.swap();
            }
            r.record("Flip the backwards start", "The first " + runHi + " flights were in backwards order, so TimSort flips them around.",
                    marksRange(snap("Starting run of " + runHi, Mark.MOVED), 0, runHi, Mark.SORTED).build());
        } else {
            while (runHi + 1 < n) {
                r.compare();
                if (BY_TIME.compare(list.get(runHi + 1), list.get(runHi)) >= 0) {
                    runHi++;
                } else {
                    break;
                }
            }
            runHi++;
            r.record("Already-sorted start", "The first " + runHi + " flight(s) are already in time order. We keep them as our sorted hand.",
                    marksRange(snap("Starting run of " + runHi, Mark.NONE), 0, runHi, Mark.SORTED).build());
        }
        // binarySort
        for (int start = runHi; start < n; start++) {
            Flight pivot = list.get(start);
            int left = 0;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                r.compare();
                boolean before = BY_TIME.compare(pivot, list.get(mid)) < 0;
                RowState.Builder b = marksRange(snap("Where does " + pivot.getFlightNumber() + " go?", Mark.COMPARE), 0, start, Mark.SORTED);
                b.mark(start, Mark.LOOK).mark(mid, Mark.COMPARE).pointer(left, "left").pointer(mid, "mid").pointer(right - 1 >= left ? right - 1 : left, "right");
                r.record("Find the spot for " + pivot.getFlightNumber(),
                        "Is " + pivot.getTimeText() + " earlier than " + list.get(mid).getTimeText() + " (box " + mid + ")? "
                        + (before ? "Yes - look in the LEFT half." : "No - look in the RIGHT half."),
                        b.build());
                if (before) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            if (left < start) {
                list.remove(start);
                list.add(left, pivot);
                r.swap(start - left);
            }
            RowState.Builder b = marksRange(snap("Inserted at box " + left, Mark.MOVED), 0, start + 1, Mark.SORTED);
            b.mark(left, Mark.MOVED).arrow(start, left);
            r.record(pivot.getFlightNumber() + " slides into box " + left,
                    left == start ? pivot.getFlightNumber() + " is already in the right place." :
                            pivot.getFlightNumber() + " slides left into box " + left + "; the " + (start - left) + " later flight(s) shift right by one.",
                    b.build());
        }
        boolean same = expected.equals(list);
        if (!same) {
            list = expected;
        }
        r.record("Sorted!",
                "All flights are in time order. Java's real Collections.sort made " + realCount[0] + " comparisons on this list"
                + (same ? " - exactly what we just watched." : "."),
                marksRange(snap("Sorted by time", Mark.FOUND), 0, list.size(), Mark.FOUND).build());
    }

    // ------------------------------------------------------------------ reverse / shuffle

    private void reverse(StepRecorder r) {
        r.setOperation("reverse");
        List<Flight> expected = new ArrayList<>(list);
        Collections.reverse(expected);
        r.record("Collections.reverse(flights)", "Flip the list! The first becomes the last, the last becomes the first.",
                snap("Reverse", Mark.LOOK).build());
        int n = list.size();
        for (int i = 0, j = n - 1; i < j; i++, j--) {
            Collections.swap(list, i, j);
            r.swap();
            RowState.Builder b = snap("Swap the ends", Mark.MOVED).mark(i, Mark.MOVED).mark(j, Mark.MOVED).arrow(j, i)
                    .pointer(i, "i").pointer(j, "j");
            r.record("Swap boxes " + i + " and " + j, list.get(i).getFlightNumber() + " and " + list.get(j).getFlightNumber()
                    + " trade places. Then i moves right and j moves left until they meet in the middle.", b.build());
        }
        if (!expected.equals(list)) {
            list = expected;
        }
        r.record("Reversed!", "Only n/2 swaps were needed.", snap("Reversed list", Mark.FOUND).build());
    }

    private void shuffle(StepRecorder r) {
        r.setOperation("shuffle");
        List<Flight> expected = new ArrayList<>(list);
        Collections.shuffle(expected, new Random(SEED));
        r.record("Collections.shuffle(flights, new Random(42))",
                "Mix the flights like a deck of cards. Starting from the last box, each box swaps with a random box at or before it. "
                + "We use a fixed seed (42) so you get the same 'random' result every time.",
                snap("Shuffle", Mark.LOOK).build());
        Random rnd = new Random(SEED);
        for (int i = list.size(); i > 1; i--) {
            int j = rnd.nextInt(i);
            Collections.swap(list, i - 1, j);
            r.swap();
            RowState.Builder b = snap("random number = " + j, Mark.MOVED).mark(i - 1, Mark.MOVED).mark(j, Mark.MOVED)
                    .arrow(i - 1, j).pointer(i - 1, "i").pointer(j, "random");
            r.record("Swap box " + (i - 1) + " with random box " + j,
                    j == i - 1 ? "The random number picked box " + j + " itself, so it stays put." :
                            "The random number is " + j + ", so boxes " + (i - 1) + " and " + j + " swap.",
                    b.build());
        }
        if (!expected.equals(list)) {
            list = expected;
        }
        r.record("Shuffled!", "Every order was equally likely. Same result as Java's real shuffle with seed 42.",
                snap("Shuffled list", Mark.FOUND).build());
    }

    // ------------------------------------------------------------------ min / max

    private void minMax(StepRecorder r, boolean min) {
        r.setOperation(min ? "min" : "max");
        String name = min ? "min" : "max";
        if (list.isEmpty()) {
            return;
        }
        int best = 0;
        r.record("Collections." + name + "(flights, byTime)",
                "Find the " + (min ? "EARLIEST" : "LATEST") + " flight. Start by guessing the first one is the winner.",
                snap(name + ": best so far = box 0", Mark.LOOK).mark(0, Mark.FOUND).pointer(0, "best").build());
        for (int i = 1; i < list.size(); i++) {
            r.compare();
            int c = BY_TIME.compare(list.get(i), list.get(best));
            boolean better = min ? c < 0 : c > 0;
            int prev = best;
            if (better) {
                best = i;
            }
            RowState.Builder b = snap(name + ": best so far = box " + best, Mark.COMPARE).mark(best, Mark.FOUND).pointer(best, "best");
            if (i != best) {
                b.mark(i, Mark.COMPARE);
            }
            r.record("Compare box " + i + " with the best",
                    "Is " + list.get(i).getTimeText() + " " + (min ? "earlier" : "later") + " than " + list.get(prev).getTimeText() + "? "
                    + (better ? "Yes! New best." : "No, the best stays."), b.build());
        }
        Flight real = min ? Collections.min(list, BY_TIME) : Collections.max(list, BY_TIME);
        r.record(name + " = " + real.getFlightNumber(),
                "The " + (min ? "earliest" : "latest") + " flight is " + real.getFlightNumber() + " at " + real.getTimeText()
                + ". We looked at each flight exactly once: O(n).",
                snap(name + " result", Mark.FOUND).mark(list.indexOf(real), Mark.FOUND).build());
    }

    // ------------------------------------------------------------------ binary search

    private void binarySearch(StepRecorder r) {
        r.setOperation("binarySearch");
        Collections.sort(list, BY_TIME);
        r.record("binarySearch needs a SORTED list",
                "Binary search only works when the list is in order, so first we sort the flights by time. "
                + "(Searching an unsorted list this way would give wrong answers!)",
                marksRange(snap("Sorted by time first", Mark.LOOK), 0, list.size(), Mark.SORTED).build());
        Flight target = list.get(Math.min(list.size() - 1, Math.max(0, list.size() * 2 / 3)));
        search(r, target, target.getFlightNumber() + " (" + target.getTimeText() + ")");
        Flight ghost = new Flight("ZZ999", "Ghost Air", "Nowhere", "Z9", freeTime(), FlightType.DEPARTURE);
        search(r, ghost, "a flight at " + ghost.getTimeText());
    }

    private LocalTime freeTime() {
        LocalTime t = LocalTime.of(12, 0);
        boolean taken = true;
        while (taken) {
            taken = false;
            for (Flight f : list) {
                if (f.getDepartureTime().equals(t)) {
                    taken = true;
                    t = t.plusMinutes(1);
                    break;
                }
            }
        }
        return t;
    }

    private void search(StepRecorder r, Flight key, String what) {
        int low = 0;
        int high = list.size() - 1;
        int result = -1;
        boolean found = false;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            r.compare();
            int c = BY_TIME.compare(list.get(mid), key);
            RowState.Builder b = snap("Looking for " + what, Mark.COMPARE).mark(mid, c == 0 ? Mark.FOUND : Mark.COMPARE)
                    .pointer(low, "low").pointer(mid, "mid").pointer(high, "high");
            for (int i = 0; i < list.size(); i++) {
                if (i < low || i > high) {
                    b.mark(i, Mark.DIM);
                }
            }
            String story = "The middle box is " + mid + " (" + list.get(mid).getTimeText() + "). "
                    + (c == 0 ? "That's it - found!" : c < 0 ? "It's earlier than " + key.getTimeText() + ", so throw away the LEFT half."
                            : "It's later than " + key.getTimeText() + ", so throw away the RIGHT half.");
            r.record("binarySearch: check the middle", story, b.build());
            if (c < 0) {
                low = mid + 1;
            } else if (c > 0) {
                high = mid - 1;
            } else {
                result = mid;
                found = true;
                break;
            }
        }
        int real = Collections.binarySearch(list, key, BY_TIME);
        if (!found) {
            result = -(low + 1);
        }
        if (found) {
            r.record("Found at box " + result,
                    "binarySearch answers " + real + ". Halving the list each time means even 1,000 flights need only about 10 checks!",
                    snap("Result = " + real, Mark.FOUND).mark(result, Mark.FOUND).build());
        } else {
            r.record("Not found: answer " + real,
                    "Nothing matches, so binarySearch answers a NEGATIVE number: " + real + ". It secretly tells you where the flight WOULD go: "
                    + "box " + (-real - 1) + " (the formula is -(spot) - 1).",
                    snap("Result = " + real + " (would be inserted at box " + (-real - 1) + ")", Mark.MISSING)
                            .pointer(Math.min(list.size() - 1, -real - 1), "would go here").build());
        }
    }

    // ------------------------------------------------------------------ snapshots

    private RowState.Builder snap(String caption, Mark noteMark) {
        List<String> cells = new ArrayList<>();
        List<String> subs = new ArrayList<>();
        for (Flight f : list) {
            cells.add(f.getFlightNumber());
            subs.add(f.getTimeText());
        }
        return RowState.builder("List<Flight>  -  " + list.size() + " flights", cells, subs)
                .caption(caption)
                .table(new ArrayList<>(list), Collections.<String>emptyList());
    }

    private static RowState.Builder marksRange(RowState.Builder b, int from, int to, Mark m) {
        for (int i = from; i < to; i++) {
            b.mark(i, m);
        }
        return b;
    }

    @Override
    protected void paintState(Graphics2D g, RowState s, int w, int h) {
        RowPainter.paint(g, s, w, h);
    }
}
