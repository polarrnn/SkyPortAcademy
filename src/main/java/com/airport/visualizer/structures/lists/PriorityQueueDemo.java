package com.airport.visualizer.structures.lists;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.BaseState;
import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructureDemo;
import com.airport.visualizer.structures.StructurePaint;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * PriorityQueue&lt;Flight&gt; ordered by departure time: the next departure is always on top.
 * The steps follow java.util.PriorityQueue's own siftUp / siftDown, and every snapshot is checked
 * against the real queue's internal order.
 */
public class PriorityQueueDemo extends StructureDemo<PriorityQueueDemo.HeapState> {

    private static final Comparator<Flight> BY_TIME = Comparator.comparing(Flight::getDepartureTime);

    /** Snapshot: the heap array plus marks. */
    public static final class HeapState extends BaseState {

        private final String caption;
        private final List<Flight> heap;
        private final Map<Integer, Mark> marks;
        private final List<String> departed;

        HeapState(String caption, List<Flight> heap, Map<Integer, Mark> marks, List<String> departed, Flight focus) {
            super(heap, focus == null ? Collections.<String>emptyList() : Collections.singletonList(focus.getFlightNumber()));
            this.caption = caption;
            this.heap = Collections.unmodifiableList(new ArrayList<>(heap));
            this.marks = Collections.unmodifiableMap(new HashMap<>(marks));
            this.departed = Collections.unmodifiableList(new ArrayList<>(departed));
        }
    }

    private PriorityQueue<Flight> real;
    private final List<Flight> heap = new ArrayList<>();
    private final List<String> departed = new ArrayList<>();

    public PriorityQueueDemo() {
        this(FlightData.first(10));
    }

    public PriorityQueueDemo(List<Flight> flights) {
        super("PriorityQueue (next departure)", Category.COLLECTIONS,
                "Not first-come-first-served: the EARLIEST flight always waits on top, like a magic line where the most urgent plane floats to the front.",
                HeapState.class, flights);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(ALL, "offer (add)", "peek / contains", "poll (remove)", ITERATE);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Swaps"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(log n)")
                .row("offer", "offer(flight)", "O(log n)", "Put it at the bottom, then let it bubble UP while it is earlier than its parent.")
                .row("peek", "peek()", "O(1)", "The earliest flight is always at the top (index 0).")
                .row("contains", "contains(flight)", "O(n)", "The heap is only 'top is smallest', so searching means checking every box.")
                .row("poll", "poll()", "O(log n)", "Take the top, move the last flight up there, let it sink DOWN to its place.")
                .row("iterate", "for-each", "O(n)", "Goes through the hidden array - NOT in sorted order!")
                .whenToUse("You always want the most urgent / smallest item next: the next departure, the closest airport, tasks by priority.")
                .avoidWhen("You need everything in full sorted order at once (sort a list) or need to search by key (use a Map).")
                .build();
    }

    @Override
    protected void runPlain(String scenario) {
        PriorityQueue<Flight> pq = new PriorityQueue<>(BY_TIME);
        for (Flight f : flights()) {
            pq.offer(f);
        }
        pq.peek();
        pq.contains(pick(3));
        pq.poll();
        pq.poll();
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        real = new PriorityQueue<>(BY_TIME);
        heap.clear();
        departed.clear();
        r.record("Meet the PriorityQueue",
                "A PriorityQueue is a 'heap': a tree where every parent leaves EARLIER than its children. So the very top is always "
                + "the next flight to depart. Java stores this tree in a plain array: box 0 is the top, the children of box i are boxes 2i+1 and 2i+2.",
                snap("Empty heap", null, null));
        boolean add = wants(scenario, ADD) || "offer (add)".equals(scenario);
        if (add) {
            for (Flight f : flights()) {
                offer(r, f, true);
            }
        } else {
            for (Flight f : flights()) {
                offer(r, f, false);
            }
            r.setOperation(null);
            r.record("Heap already built", "All flights are already in the heap. The top is " + heap.get(0).getFlightNumber()
                    + " at " + heap.get(0).getTimeText() + " - the earliest one!",
                    snap("Earliest on top", marks(0, Mark.FOUND), heap.get(0)));
        }
        if (wants(scenario, GET) || "peek / contains".equals(scenario)) {
            r.setOperation("peek");
            Flight top = real.peek();
            r.record("peek()", "Which flight leaves next? Just look at the top: " + top.getFlightNumber() + " at " + top.getTimeText()
                    + ". One look, O(1)!", snap("peek() looks at box 0", marks(0, Mark.FOUND), top));
            Flight target = pick(3);
            r.setOperation("contains");
            for (int i = 0; i < heap.size(); i++) {
                r.compare();
                boolean same = heap.get(i).equals(target);
                r.record("contains(" + target.getFlightNumber() + "): box " + i,
                        "The heap only knows the TOP is earliest, so to search we must check box by box. Box " + i + " is "
                        + heap.get(i).getFlightNumber() + (same ? " - found!" : " - not it."),
                        snap("Searching the array", marks(i, same ? Mark.FOUND : Mark.COMPARE), target));
                if (same) {
                    break;
                }
            }
        }
        if (wants(scenario, REMOVE) || "poll (remove)".equals(scenario)) {
            for (int k = 0; k < 2 && !heap.isEmpty(); k++) {
                poll(r);
            }
        }
        if (wants(scenario, ITERATE)) {
            r.setOperation("iterate");
            for (int i = 0; i < heap.size(); i++) {
                Map<Integer, Mark> m = new HashMap<>();
                for (int j = 0; j <= i; j++) {
                    m.put(j, Mark.VISIT);
                }
                r.record("Visit box " + i, "A for-each loop walks the hidden array: box " + i + " is " + heap.get(i).getFlightNumber()
                        + " at " + heap.get(i).getTimeText() + ".", snap("for (Flight f : pq)", m, heap.get(i)));
            }
            r.record("Surprise: not sorted!",
                    "Looking at the times we visited, they are NOT in order! Only the top is guaranteed to be the earliest. "
                    + "To get flights in time order, keep calling poll().",
                    snap("Iteration order is the array order", null, null));
        }
        r.setOperation(null);
        r.record("All done!",
                "PriorityQueue: always gives you the most urgent item next, and each add or remove only takes about log2(n) steps.",
                snap("Still waiting: " + heap.size(), null, null));
    }

    private void offer(StepRecorder r, Flight f, boolean show) {
        r.setOperation("offer");
        real.offer(f);
        heap.add(f);
        int k = heap.size() - 1;
        if (show) {
            r.record("offer(" + f.getFlightNumber() + ")",
                    f.getFlightNumber() + " (" + f.getTimeText() + ") starts at the bottom, in box " + k + ".",
                    snap("New flight at the bottom", marks(k, Mark.ADDED), f));
        }
        while (k > 0) {
            int p = (k - 1) >>> 1;
            r.compare();
            boolean earlier = BY_TIME.compare(heap.get(k), heap.get(p)) < 0;
            if (show) {
                Map<Integer, Mark> m = marks(k, Mark.COMPARE);
                m.put(p, Mark.COMPARE);
                r.record("Compare with parent",
                        "Does " + heap.get(k).getTimeText() + " leave earlier than its parent " + heap.get(p).getTimeText() + "? "
                        + (earlier ? "Yes - swap them, it bubbles UP!" : "No - it stays here. Done!"),
                        snap("Bubble up?", m, f));
            }
            if (!earlier) {
                break;
            }
            Collections.swap(heap, k, p);
            r.swap();
            if (show) {
                Map<Integer, Mark> m = marks(p, Mark.MOVED);
                m.put(k, Mark.MOVED);
                r.record("Swap up", heap.get(p).getFlightNumber() + " moves up to box " + p + ".", snap("Swapped", m, f));
            }
            k = p;
        }
        syncWithReal();
    }

    private void poll(StepRecorder r) {
        r.setOperation("poll");
        Flight top = heap.get(0);
        r.record("poll()", "Time to depart! The top flight " + top.getFlightNumber() + " (" + top.getTimeText() + ") leaves the queue.",
                snap("Taking the top", marks(0, Mark.REMOVED), top));
        real.poll();
        departed.add(top.getFlightNumber() + " " + top.getTimeText());
        Flight last = heap.remove(heap.size() - 1);
        if (heap.isEmpty()) {
            r.record("Queue empty", "That was the last flight!", snap("Empty", null, top));
            syncWithReal();
            return;
        }
        heap.set(0, last);
        r.swap();
        r.record("Last flight jumps to the top",
                "To fill the hole, the LAST flight " + last.getFlightNumber() + " (" + last.getTimeText() + ") moves to the top. "
                + "It's probably too late for the top spot, so it will sink down.",
                snap("Fill the top", marks(0, Mark.MOVED), last));
        int n = heap.size();
        int k = 0;
        while (k < (n >>> 1)) {
            int child = 2 * k + 1;
            int right = child + 1;
            if (right < n) {
                r.compare();
                if (BY_TIME.compare(heap.get(child), heap.get(right)) > 0) {
                    child = right;
                }
            }
            r.compare();
            boolean later = BY_TIME.compare(heap.get(k), heap.get(child)) > 0;
            Map<Integer, Mark> m = marks(k, Mark.COMPARE);
            m.put(child, Mark.COMPARE);
            r.record("Sink down?",
                    "The earlier child is " + heap.get(child).getFlightNumber() + " (" + heap.get(child).getTimeText() + "). Does "
                    + heap.get(k).getTimeText() + " leave later than it? " + (later ? "Yes - swap, sink DOWN." : "No - it stays. Heap fixed!"),
                    snap("Compare with the earlier child", m, last));
            if (!later) {
                break;
            }
            Collections.swap(heap, k, child);
            r.swap();
            Map<Integer, Mark> s = marks(k, Mark.MOVED);
            s.put(child, Mark.MOVED);
            r.record("Swap down", heap.get(k).getFlightNumber() + " moves up, " + last.getFlightNumber() + " sinks to box " + child + ".",
                    snap("Swapped", s, last));
            k = child;
        }
        syncWithReal();
        r.record("New top: " + heap.get(0).getFlightNumber(), "Now " + heap.get(0).getFlightNumber() + " at " + heap.get(0).getTimeText()
                + " is the next departure.", snap("Heap is happy again", marks(0, Mark.FOUND), heap.get(0)));
    }

    /** Our steps copy PriorityQueue's algorithm; if anything ever differs, trust the real queue. */
    private void syncWithReal() {
        List<Flight> actual = new ArrayList<>();
        for (Object o : real.toArray()) {
            actual.add((Flight) o);
        }
        if (!actual.equals(heap)) {
            heap.clear();
            heap.addAll(actual);
        }
    }

    private static Map<Integer, Mark> marks(int index, Mark m) {
        Map<Integer, Mark> map = new HashMap<>();
        map.put(index, m);
        return map;
    }

    private HeapState snap(String caption, Map<Integer, Mark> marks, Flight focus) {
        return new HeapState(caption, heap, marks == null ? new HashMap<Integer, Mark>() : marks, departed, focus);
    }

    @Override
    protected void paintState(Graphics2D g, HeapState s, int w, int h) {
        StructurePaint.header(g, "PriorityQueue (min-heap by time)  -  " + s.heap.size() + " waiting", s.caption, w);
        int n = s.heap.size();
        double arrayTop = h - 34 - 92;
        double treeTop = 84;
        int levels = n == 0 ? 1 : (int) (Math.floor(Math.log(n) / Math.log(2)) + 1);
        double nodeW = Math.min(92, (w - 40.0) / Math.max(1, 1 << (levels - 1)) - 8);
        nodeW = Math.max(46, nodeW);
        double nodeH = 38;
        double levelH = Math.max(nodeH + 8, Math.min(70, (arrayTop - treeTop - nodeH - 16) / Math.max(1, levels - 1)));
        double[] cx = new double[n];
        double[] cy = new double[n];
        for (int i = 0; i < n; i++) {
            int level = (int) Math.floor(Math.log(i + 1) / Math.log(2));
            int pos = i - ((1 << level) - 1);
            cx[i] = 20 + (w - 40.0) * (pos + 0.5) / (1 << level);
            cy[i] = treeTop + nodeH / 2 + level * levelH;
        }
        for (int i = 1; i < n; i++) {
            int p = (i - 1) >>> 1;
            Draw.line(g, cx[p], cy[p] + nodeH / 2, cx[i], cy[i] - nodeH / 2, Theme.BORDER_STRONG, 1.6f);
        }
        for (int i = 0; i < n; i++) {
            Mark m = s.marks.get(i);
            Color fill = i == 0 ? Theme.ACCENT_SOFT : Theme.PRIMARY_SOFT;
            Color border = i == 0 ? Theme.ACCENT : Theme.tint(Theme.PRIMARY, 0.4);
            Color fg = Theme.TEXT;
            if (m != null && m != Mark.NONE) {
                fill = m.color();
                border = Theme.shade(fill, 0.25);
                fg = Color.WHITE;
            }
            Flight f = s.heap.get(i);
            StructurePaint.flightCard(g, cx[i] - nodeW / 2, cy[i] - nodeH / 2, nodeW, nodeH, f.getTimeText(), f.getFlightNumber(), fill, border, fg);
            if (i == 0) {
                Draw.centeredText(g, "TOP = next departure", cx[i], cy[i] - nodeH / 2 - 9, Theme.bold(11f), Theme.shade(Theme.ACCENT, 0.3));
            }
        }
        if (n == 0) {
            Draw.centeredText(g, "(empty)", w / 2.0, (treeTop + arrayTop) / 2, Theme.regular(15f), Theme.TEXT_MUTED);
        }
        // array view
        Draw.leftText(g, "Hidden array inside the PriorityQueue", 20, arrayTop, Theme.bold(12.5f), Theme.TEXT_MUTED);
        double cellW = n == 0 ? 60 : Math.max(44, Math.min(80, (w - 40.0) / n - 4));
        for (int i = 0; i < n; i++) {
            double x = 20 + i * (cellW + 4);
            Mark m = s.marks.get(i);
            Color fill = m != null && m != Mark.NONE ? m.color() : Theme.SURFACE_ALT;
            Color fg = m != null && m != Mark.NONE ? Color.WHITE : Theme.TEXT;
            Draw.centeredText(g, "[" + i + "]", x + cellW / 2, arrayTop + 16, Theme.bold(10.5f), Theme.TEXT_MUTED);
            Draw.roundRect(g, x, arrayTop + 26, cellW, 32, 8, fill, Theme.BORDER_STRONG, 1.2f);
            Draw.textInBox(g, s.heap.get(i).getTimeText(), x, arrayTop + 26, cellW, 32, Theme.bold(12f), fg);
        }
        if (!s.departed.isEmpty()) {
            g.setFont(Theme.bold(12.5f));
            String text = "Departed: " + String.join(", ", s.departed);
            Draw.leftText(g, Draw.ellipsize(g, text, w / 2 - 30), w / 2.0, arrayTop, Theme.bold(12.5f), Theme.DANGER);
        }
        StructurePaint.legendBottom(g, h, Theme.ACCENT_SOFT, "top (earliest)", Theme.V_SORTED, "added",
                Theme.V_COMPARE, "comparing", Theme.V_PIVOT, "swapping", Theme.V_FOUND, "found", Theme.V_SWAP, "leaving",
                Theme.V_VISITED, "visited");
    }
}
