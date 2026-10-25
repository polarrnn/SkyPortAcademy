package com.airport.visualizer.trees;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.trees.TreeSnapshot.Mark;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

/** Binary search tree of flights keyed by departure time: insert, search and the three delete cases. */
public class BstVisualizer extends AbstractVisualizer<BstState> {

    static final String INSERT = "Insert flights";
    static final String FOUND = "Search (found)";
    static final String MISSING = "Search (missing)";
    static final String DEL_LEAF = "Delete a leaf";
    static final String DEL_ONE = "Delete a node with one child";
    static final String DEL_TWO = "Delete a node with two children (successor)";

    /** The 11 flights used to grow the tree (board order, so the shape is interesting). */
    private final List<Flight> flights = FlightData.first(11);

    public BstVisualizer() {
        super("Binary Search Tree", Category.TREE,
                "A tree where earlier flights live on the left and later flights on the right, "
                + "so every question cuts the search in half.", BstState.class);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(INSERT, FOUND, MISSING, DEL_LEAF, DEL_ONE, DEL_TWO);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Links changed"};
    }

    @Override
    public List<Flight> getFlights() {
        return flights;
    }

    @Override
    public ComplexityInfo getComplexity() {
        return complexity();
    }

    /** Shared with the Tree Lab. */
    public static ComplexityInfo complexity() {
        return ComplexityInfo.builder("O(log n) average")
                .row("insert", "insert(value)", "O(log n) avg / O(n) worst",
                        "Walk down one path from the root. A bushy tree is short, so the walk is short.")
                .row("search", "search(value)", "O(log n) avg / O(n) worst",
                        "Each question throws away half of what is left - like a guessing game of higher / lower.")
                .row("delete", "delete(value)", "O(log n) avg / O(n) worst",
                        "Find it, then fix at most a couple of links (leaf, one child, or successor swap).")
                .row("inorder", "in-order walk", "O(n)",
                        "Visiting every node left-root-right prints everything in sorted order.")
                .whenToUse("Use a BST (in Java: TreeMap / TreeSet) when you need things kept in order AND want fast add, "
                        + "find and remove - for example flights sorted by departure time, or 'what is the next flight after 14:00?'.")
                .avoidWhen("If values arrive already sorted, a plain BST becomes a long stick (O(n)). Java's TreeMap avoids this "
                        + "by balancing itself (red-black tree). If you never need order, a HashMap is even faster.")
                .build();
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        StepRecorder rec = new StepRecorder();
        BstEngine engine = new BstEngine(Words.FLIGHTS, flights);
        String summary;
        long nanos;
        if (INSERT.equals(scenario)) {
            rec.record("An empty tree", "We will plant a tree of " + flights.size() + " flights. The rule is simple: "
                    + "EARLIER flights go to the LEFT, LATER flights go to the RIGHT.", engine.plainState("Insert flights one by one"));
            for (Flight f : flights) {
                engine.insert(TreeItem.of(f), rec);
            }
            rec.setOperation("inorder");
            rec.record("Our tree is ready!", "Read the tree from left to right and the departure times are in order: "
                    + "that is the magic of a binary search tree.", engine.plainState("The finished tree"));
            nanos = time(() -> buildQuiet());
            summary = "Built a tree of " + flights.size() + " flights with " + rec.getPrimary() + " comparisons.";
        } else {
            buildInto(engine);
            rec.record("Here is our tree", "The tree already holds " + engine.size() + " flights. Earlier = left, later = right.",
                    engine.plainState("Ready"));
            TreeItem target;
            boolean ok;
            switch (scenario == null ? FOUND : scenario) {
                case MISSING:
                    target = TreeItem.time(12, 0);
                    ok = engine.search(target, rec);
                    nanos = time(() -> buildQuiet().contains(target.getKey()));
                    summary = "Is there a flight at 12:00? " + (ok ? "Yes." : "No - found out after " + rec.getPrimary() + " comparisons.");
                    break;
                case DEL_LEAF:
                    target = TreeItem.of(FlightData.byNumber("5J241"));
                    ok = engine.delete(target, rec);
                    nanos = time(() -> buildQuiet().deleteQuietly(target.getKey()));
                    summary = "Deleted leaf " + target.describe() + (ok ? "." : " (not found).");
                    break;
                case DEL_ONE:
                    target = TreeItem.of(FlightData.byNumber("KE624"));
                    ok = engine.delete(target, rec);
                    nanos = time(() -> buildQuiet().deleteQuietly(target.getKey()));
                    summary = "Deleted " + target.describe() + ", whose only child moved up.";
                    break;
                case DEL_TWO:
                    target = TreeItem.of(FlightData.byNumber("PR102"));
                    ok = engine.delete(target, rec);
                    nanos = time(() -> buildQuiet().deleteQuietly(target.getKey()));
                    summary = "Deleted the root " + target.describe() + " and replaced it with its successor.";
                    break;
                default:
                    target = TreeItem.of(FlightData.byNumber("JL742"));
                    ok = engine.search(target, rec);
                    nanos = time(() -> buildQuiet().contains(target.getKey()));
                    summary = "Found " + target.describe() + " with only " + rec.getPrimary() + " comparisons out of "
                            + flights.size() + " flights.";
                    break;
            }
        }
        return Timeline.of(rec, nanos, summary);
    }

    private void buildInto(BstEngine engine) {
        for (Flight f : flights) {
            engine.insertQuietly(TreeItem.of(f));
        }
    }

    private BstEngine buildQuiet() {
        BstEngine e = new BstEngine(Words.FLIGHTS, flights);
        buildInto(e);
        return e;
    }

    static long time(Runnable r) {
        r.run(); // warm-up
        long t0 = System.nanoTime();
        r.run();
        return System.nanoTime() - t0;
    }

    @Override
    protected void paintState(Graphics2D g, BstState s, int w, int h) {
        render(g, s, w, h);
    }

    /** Draws a BST step: caption, tree, comparison trail and legend. Shared with the Tree Lab. */
    public static void render(Graphics2D g, BstState s, int w, int h) {
        Draw.heading(g, s.getCaption(), 18, 22);
        Draw.caption(g, "Rule: smaller / earlier -> LEFT      bigger / later -> RIGHT", 18, 44);
        double side = Math.max(210, Math.min(320, w * 0.28));
        double treeW = w - side - 44;
        TreePainter.paint(g, s.getTree(), 14, 62, treeW, h - 62 - 40);
        double px = w - side - 16;
        double top = SidePanels.card(g, px, 14, side, h - 58, "Comparison trail", Theme.V_COMPARE);
        SidePanels.lines(g, s.getTrail(), px + 14, top, side - 20, h - 54);
        TreePainter.legend(g, 18, h - 28, Mark.CURRENT, Mark.PATH, Mark.FOUND, Mark.NEW, Mark.SPECIAL, Mark.REMOVED);
    }
}
