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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Min-heap of flights by departure time: the earliest flight is always waiting on top. */
public class HeapVisualizer extends AbstractVisualizer<HeapState> {

    static final String INSERT = "Insert (sift up)";
    static final String REMOVE = "Remove earliest (sift down)";
    static final String BUILD = "Build heap from the board";

    private final List<Flight> flights = FlightData.first(10);

    public HeapVisualizer() {
        super("Min-Heap (Next Departure)", Category.TREE,
                "A special tree where every parent leaves earlier than its children, so the very next departure is always on top.",
                HeapState.class);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(INSERT, REMOVE, BUILD);
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
        return ComplexityInfo.builder("O(log n)")
                .row("insert", "insert / offer", "O(log n)",
                        "Add at the end, then bubble up - at most one swap per row of the tree.")
                .row("remove", "remove earliest / poll", "O(log n)",
                        "Take the top, move the last item up, then let it sink down one row at a time.")
                .row("peek", "peek at the earliest", "O(1)",
                        "The earliest is always at index 0 - no searching at all!")
                .row("build", "build heap (heapify)", "O(n)",
                        "Fixing parents from the bottom up is cheaper than adding items one by one.")
                .whenToUse("Use a heap (Java: PriorityQueue) when you keep asking 'what is NEXT?' - the next flight to depart, "
                        + "the most urgent task, the closest airport. Adding and taking the next one are both fast.")
                .avoidWhen("A heap is NOT sorted from left to right and cannot quickly find a flight by number. "
                        + "If you need everything in order or fast lookups, use a TreeMap or HashMap.")
                .build();
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        StepRecorder rec = new StepRecorder();
        HeapEngine heap = new HeapEngine(Words.FLIGHTS, flights);
        List<TreeItem> items = new ArrayList<>();
        for (Flight f : flights) {
            items.add(TreeItem.of(f));
        }
        long nanos;
        String summary;
        if (REMOVE.equals(scenario)) {
            heap.buildQuietly(items);
            rec.record("A ready heap", "These " + heap.size() + " flights already form a heap. The earliest flight is on top.",
                    heap.plainState("Remove the earliest flights"));
            TreeItem first = heap.removeMin(rec);
            TreeItem second = heap.removeMin(rec);
            nanos = BstVisualizer.time(() -> {
                HeapEngine h = new HeapEngine(Words.FLIGHTS, flights);
                h.buildQuietly(items);
                h.removeMinQuietly();
                h.removeMinQuietly();
            });
            summary = "Took off in order: " + first.describe() + ", then " + second.describe() + ".";
        } else if (BUILD.equals(scenario)) {
            heap.buildHeap(items, rec);
            nanos = BstVisualizer.time(() -> new HeapEngine(Words.FLIGHTS, flights).buildQuietly(items));
            summary = "Heapified " + items.size() + " flights; the next departure is " + heap.items().get(0).describe() + ".";
        } else {
            rec.record("An empty heap", "We will add 8 flights one at a time. Each one starts at the bottom and climbs up "
                    + "while it leaves earlier than its parent.", heap.plainState("Insert flights"));
            List<TreeItem> eight = items.subList(0, 8);
            for (TreeItem t : eight) {
                heap.insert(t, rec);
            }
            nanos = BstVisualizer.time(() -> {
                HeapEngine h = new HeapEngine(Words.FLIGHTS, flights);
                for (TreeItem t : eight) {
                    h.insertQuietly(t);
                }
            });
            summary = "Inserted 8 flights with " + rec.getSecondary() + " swaps; the next departure is "
                    + heap.items().get(0).describe() + ".";
        }
        return Timeline.of(rec, nanos, summary);
    }

    @Override
    protected void paintState(Graphics2D g, HeapState s, int w, int h) {
        render(g, s, w, h);
    }

    /** Tree on top, the real array underneath, plus the index formula. Shared with the Tree Lab. */
    public static void render(Graphics2D g, HeapState s, int w, int h) {
        Draw.heading(g, s.getCaption(), 18, 22);
        Draw.caption(g, "Same data, two pictures: the tree (top) is really stored as the array (bottom).", 18, 44);
        double arrayH = 66;
        double bottomBlock = arrayH + 34 + 30 + 52;
        double treeH = Math.max(120, h - 60 - bottomBlock);
        TreePainter.paint(g, s.getTree(), 14, 58, w - 28, treeH);

        // the array
        double ay = 58 + treeH + 14;
        List<TreeItem> arr = s.getArray();
        int n = Math.max(1, arr.size());
        double cellW = Math.min(78, (w - 60.0) / n);
        double ax = 18;
        Draw.leftText(g, "Array:", ax, ay + 32, Theme.bold(12.5f), Theme.TEXT_MUTED);
        ax += 52;
        if (arr.isEmpty()) {
            Draw.cell(g, ax, ay + 12, 120, 40, Theme.SURFACE, Theme.BORDER, "(empty)", Theme.TEXT_MUTED, Theme.regular(12f));
        }
        for (int i = 0; i < arr.size(); i++) {
            Mark m = s.markOf(i);
            double cx = ax + i * cellW;
            Draw.centeredText(g, String.valueOf(i), cx + cellW / 2, ay + 4, Theme.bold(11f), Theme.PURPLE);
            Draw.cell(g, cx + 2, ay + 12, cellW - 4, 40, TreePainter.fill(m), TreePainter.border(m),
                    arr.get(i).getLabel(), TreePainter.text(m), Theme.bold(12f));
            String sub = arr.get(i).getSublabel();
            if (sub != null && !sub.isEmpty() && !sub.equals(arr.get(i).getLabel())) {
                Draw.centeredText(g, sub, cx + cellW / 2, ay + 62, Theme.regular(11f), Theme.TEXT_MUTED);
            }
        }

        // formula + taken off
        double fy = ay + arrayH + 10;
        if (!s.getFormula().isEmpty()) {
            g.setFont(Theme.mono(12.5f));
            int fw = Math.min(w - 36, g.getFontMetrics().stringWidth(s.getFormula()) + 24);
            Draw.roundRect(g, 18, fy, fw, 26, 10, Theme.PURPLE_SOFT, null);
            Draw.leftText(g, Draw.ellipsize(g, s.getFormula(), fw - 20), 30, fy + 13, Theme.mono(12.5f), Theme.shade(Theme.PURPLE, 0.3));
        }
        if (!s.getTaken().isEmpty()) {
            String t = "Taken out (in order): " + String.join(", ", s.getTaken());
            g.setFont(Theme.bold(12.5f));
            Draw.leftText(g, Draw.ellipsize(g, t, w - 40), 18, fy + 42, Theme.bold(12.5f), Theme.shade(Theme.V_SWAP, 0.2));
        }
        TreePainter.legend(g, 18, h - 24, Mark.CURRENT, Mark.PATH, Mark.FOUND, Mark.NEW, Mark.REMOVED);
    }
}
