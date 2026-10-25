package com.airport.visualizer.hashing;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Hashing lesson with flight numbers: 9 flights go into an 11-bucket table using the
 * "add up the character codes, keep the remainder" hash. Shows chaining and linear probing,
 * plus search and delete in both.
 */
public class FlightHashTableVisualizer extends AbstractVisualizer<FlightHashTableVisualizer.State> {

    public static final String CHAINING = "Separate chaining";
    public static final String PROBING = "Linear probing";
    public static final String CHAIN_OPS = "Search & delete (chaining)";
    public static final String PROBE_OPS = "Search & delete (linear probing)";

    private static final int SIZE = 11;
    /** Chosen so buckets 6 and 7 collide and linear probing has to wrap from slot 10 to slot 0. */
    private static final String[] KEYS = {"PR102", "SQ917", "CX906", "Z2440", "NH820", "5J560", "Z2612", "EK333", "KE624"};

    private final List<Flight> flights;

    /** Snapshot + the flight rows to highlight in the Flight Data table. */
    public static final class State implements FlightTableState {

        private final HashSnapshot snapshot;
        private final List<Flight> flights;

        State(HashSnapshot snapshot, List<Flight> flights) {
            this.snapshot = snapshot;
            this.flights = flights;
        }

        public HashSnapshot getSnapshot() {
            return snapshot;
        }

        @Override
        public List<Flight> getTableFlights() {
            return flights;
        }

        @Override
        public Set<String> getHighlightedFlightNumbers() {
            String n = snapshot.getCurrentName();
            return n == null ? Collections.<String>emptySet() : Collections.singleton(n);
        }
    }

    public FlightHashTableVisualizer() {
        super("Hashing & Hash Tables", Category.MAPS_SETS,
                "Each flight number gets a locker number by adding up its characters' codes. "
                + "Watch what happens when two flights want the same locker!", State.class);
        List<Flight> list = new ArrayList<>();
        for (String k : KEYS) {
            Flight f = FlightData.byNumber(k);
            if (f != null) {
                list.add(f);
            }
        }
        flights = Collections.unmodifiableList(list);
    }

    @Override
    public ComplexityInfo getComplexity() {
        return HashComplexity.info();
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(CHAINING, PROBING, CHAIN_OPS, PROBE_OPS);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Probes / hops"};
    }

    @Override
    public List<Flight> getFlights() {
        return flights;
    }

    private HashTableModel newTable(boolean chaining) {
        return chaining ? new ChainedHashTable(SIZE) : new LinearProbingHashTable(SIZE);
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        boolean chaining = CHAINING.equals(scenario) || CHAIN_OPS.equals(scenario);
        boolean ops = CHAIN_OPS.equals(scenario) || PROBE_OPS.equals(scenario);
        StepRecorder rec = new StepRecorder();
        HashStepSink sink = HashStepSink.recording(rec, snap -> new State(snap, flights));
        HashTableModel table = newTable(chaining);
        String strategy = chaining ? "separate chaining" : "linear probing";
        String unit = chaining ? "buckets" : "slots";

        long start = System.nanoTime();
        HashTableModel quiet = newTable(chaining);
        for (Flight f : flights) {
            quiet.insert(f.getFlightNumber(), HashStepSink.QUIET);
        }
        if (ops) {
            runOps(quiet, chaining, HashStepSink.QUIET, op -> { });
        }
        long nanos = System.nanoTime() - start;

        if (!ops) {
            rec.setOperation("hash");
            sink.step("Meet the hash table",
                    "Our table has " + SIZE + " " + unit + " numbered 0 to " + (SIZE - 1) + ", like lockers at the airport. "
                    + "To pick a locker for a flight we add up the character codes of its flight number and keep the remainder "
                    + "after dividing by " + SIZE + ". We'll use " + strategy + " when two flights want the same locker.",
                    table.snapshot().build());
            rec.setOperation("insert");
            for (Flight f : flights) {
                table.insert(f.getFlightNumber(), sink);
            }
            rec.setOperation("load");
            sink.step("All " + flights.size() + " flights are stored",
                    "Load factor = " + flights.size() + " flights / " + SIZE + " " + unit + " = "
                    + String.format("%.2f", table.getLoadFactor()) + ". "
                    + (chaining ? "Buckets 6 and 7 got 3 flights each - collisions made chains, but nobody was lost."
                            : "Collisions pushed flights into the next free slots, and Z2612 even wrapped around from slot 10 to slot 0."),
                    table.snapshot().build());
            return Timeline.of(rec, nanos, "Using " + strategy + " with " + SIZE + " " + unit + ". Press Play!");
        }

        for (Flight f : flights) {
            table.insert(f.getFlightNumber(), HashStepSink.QUIET);
        }
        rec.setOperation("load");
        sink.step("Here is our full table",
                "The " + flights.size() + " flights are already stored using " + strategy + ". Now let's find some flights "
                + "and remove some - and see why " + (chaining ? "changing arrows" : "DELETED markers") + " matter.",
                table.snapshot().build());
        runOps(table, chaining, sink, rec::setOperation);
        rec.setOperation("load");
        sink.step("Done!",
                "Searching and deleting only looked at one bucket or a short probe trail - not all " + flights.size()
                + " flights. That's why hash tables are so fast on average: O(1).",
                table.snapshot().build());
        return Timeline.of(rec, nanos, chaining
                ? "Search CX906, search a missing flight, delete from the middle and head of chains."
                : "Search across the wrap-around, delete with a DELETED marker, then reuse it.");
    }

    /**
     * The search / delete story. {@code setOp} tells the recorder which Big-O row is active
     * (it does nothing for the quiet timing run).
     */
    private void runOps(HashTableModel table, boolean chaining, HashStepSink sink, Consumer<String> setOp) {
        if (chaining) {
            setOp.accept("search");
            table.search("CX906", sink);   // second node of bucket 6
            table.search("QR933", sink);   // not stored
            setOp.accept("delete");
            table.delete("CX906", sink);   // middle of a chain: SQ917.next = Z2440
            table.delete("NH820", sink);   // head of a chain: head = 5J560
            setOp.accept("search");
            table.search("Z2440", sink);   // chain still connected
        } else {
            setOp.accept("search");
            table.search("Z2612", sink);   // probes 7, 8, 9, 10, wraps to 0
            table.search("QR933", sink);   // stops at an EMPTY slot
            setOp.accept("delete");
            table.delete("NH820", sink);   // leaves a DELETED marker in slot 9
            setOp.accept("search");
            table.search("Z2612", sink);   // must walk past the marker
            setOp.accept("insert");
            table.insert("NH820", sink);   // reuses the DELETED slot
        }
    }

    @Override
    protected void paintState(Graphics2D g, State state, int width, int height) {
        HashTablePainter.paint(g, state.getSnapshot(), width, height);
    }
}
