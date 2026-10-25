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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * LinkedList&lt;Flight&gt;: a chain of nodes holding hands (next and prev links). Adding at either end
 * is instant, but reaching the middle means walking node by node.
 */
public class LinkedListDemo extends StructureDemo<LinkedListDemo.ChainState> {

    /** Snapshot of the chain. */
    public static final class ChainState extends BaseState {

        private final String caption;
        private final List<String> labels;
        private final List<String> subs;
        private final Map<Integer, Mark> marks;
        private final int walker;
        private final String walkerLabel;
        private final int relinkAfter;

        ChainState(String caption, List<Flight> nodes, Map<Integer, Mark> marks, int walker, String walkerLabel,
                int relinkAfter, Flight focus) {
            super(nodes, focus == null ? Collections.<String>emptyList() : Collections.singletonList(focus.getFlightNumber()));
            this.caption = caption;
            List<String> l = new ArrayList<>();
            List<String> s = new ArrayList<>();
            for (Flight f : nodes) {
                l.add(f.getFlightNumber());
                s.add(f.getTimeText());
            }
            this.labels = Collections.unmodifiableList(l);
            this.subs = Collections.unmodifiableList(s);
            this.marks = Collections.unmodifiableMap(new HashMap<>(marks));
            this.walker = walker;
            this.walkerLabel = walkerLabel;
            this.relinkAfter = relinkAfter;
        }
    }

    private final LinkedList<Flight> real = new LinkedList<>();

    public LinkedListDemo() {
        this(FlightData.first(8));
    }

    public LinkedListDemo(List<Flight> flights) {
        super("LinkedList", Category.COLLECTIONS,
                "A chain of nodes holding hands: each node knows the one before (prev) and after (next). Great at the ends, slow in the middle.",
                ChainState.class, flights);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Hops / link changes"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) ends, O(n) middle")
                .row("addLast", "addLast(flight) / add(flight)", "O(1)", "Hook a new node onto the TAIL. Nobody moves!")
                .row("addFirst", "addFirst(flight)", "O(1)", "Hook a new node in front of the HEAD.")
                .row("addAt", "add(index, flight)", "O(n)", "Walk to the spot first, then just change 4 links.")
                .row("getFirst", "getFirst() / getLast()", "O(1)", "HEAD and TAIL are always known.")
                .row("get", "get(index)", "O(n)", "No box numbers - walk hop by hop (Java starts from whichever end is closer).")
                .row("contains", "contains(flight)", "O(n)", "Walk and compare each node.")
                .row("removeFirst", "removeFirst()", "O(1)", "Move HEAD to the second node.")
                .row("remove", "remove(flight)", "O(n)", "Walk to it, then let its neighbours hold hands directly.")
                .row("iterate", "for-each", "O(n)", "Follow the next links from HEAD to TAIL.")
                .whenToUse("You add or remove at the front and back a lot (like a queue), or you insert while walking with an iterator.")
                .avoidWhen("You need get(index) often - ArrayList jumps there instantly, LinkedList has to walk.")
                .build();
    }

    @Override
    protected void runPlain(String scenario) {
        LinkedList<Flight> list = new LinkedList<>(flights());
        list.getFirst();
        list.get(Math.max(0, list.size() - 2));
        list.contains(pick(3));
        list.removeFirst();
        for (Flight f : list) {
            f.getMinutes();
        }
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        real.clear();
        r.record("Meet the LinkedList",
                "A LinkedList is a chain of nodes. Each node holds a flight and two arrows: NEXT (to the right) and PREV (to the left). "
                + "HEAD is the first node, TAIL is the last one.",
                snap("Empty chain: HEAD and TAIL point to nothing (null)", null, -1, null, -1));
        int n = flights().size();
        if (wants(scenario, ADD)) {
            int lastCount = n >= 3 ? n - 2 : n;
            for (int i = 0; i < lastCount; i++) {
                addLast(r, flights().get(i));
            }
            if (n >= 3) {
                addFirst(r, flights().get(n - 2));
                addAt(r, Math.min(2, real.size()), flights().get(n - 1));
            }
        } else {
            real.addAll(flights());
            r.swap(n);
            r.setOperation(null);
            r.record("Chain already built", "We already linked all " + n + " flights. Let's try the next operation!",
                    snap("size = " + real.size(), null, -1, null, -1));
        }
        if (wants(scenario, GET)) {
            r.setOperation("getFirst");
            r.record("getFirst()",
                    "The list always remembers its HEAD, so getFirst() is instant: " + real.getFirst().getFlightNumber() + ".",
                    snap("HEAD is known", marks(0, Mark.FOUND), 0, "HEAD", -1, real.getFirst()));
            getAt(r, Math.max(0, real.size() - 2));
            contains(r, pick(3));
        }
        if (wants(scenario, REMOVE)) {
            removeFirst(r);
            removeObject(r, pick(3));
        }
        if (wants(scenario, ITERATE)) {
            r.setOperation("iterate");
            for (int i = 0; i < real.size(); i++) {
                Map<Integer, Mark> m = new HashMap<>();
                for (int k = 0; k <= i; k++) {
                    m.put(k, Mark.VISIT);
                }
                r.swap();
                r.record("Visit node " + i,
                        i == 0 ? "Start at HEAD: " + real.get(0).getFlightNumber() + "." : "Follow the NEXT arrow to " + real.get(i).getFlightNumber() + ".",
                        snap("for (Flight f : list)", m, i, "f", -1, real.get(i)));
            }
        }
        r.setOperation(null);
        r.record("All done!",
                "LinkedList: super quick at the HEAD and TAIL, but finding something in the middle means walking the chain.",
                snap("Final size = " + real.size(), null, -1, null, -1));
    }

    private void addLast(StepRecorder r, Flight f) {
        r.setOperation("addLast");
        real.addLast(f);
        r.swap(real.size() == 1 ? 1 : 2);
        int i = real.size() - 1;
        r.record("addLast(" + f.getFlightNumber() + ")",
                real.size() == 1
                        ? f.getFlightNumber() + " is the only node, so it is BOTH the HEAD and the TAIL."
                        : "The old TAIL's NEXT arrow now points to " + f.getFlightNumber() + ", and " + f.getFlightNumber()
                        + "'s PREV points back. It becomes the new TAIL. Nobody else moved!",
                snap("size = " + real.size(), marks(i, Mark.ADDED), -1, null, i - 1, f));
    }

    private void addFirst(StepRecorder r, Flight f) {
        r.setOperation("addFirst");
        real.addFirst(f);
        r.swap(2);
        r.record("addFirst(" + f.getFlightNumber() + ")",
                f.getFlightNumber() + " cuts in at the front! Its NEXT points to the old HEAD, the old HEAD's PREV points back, and HEAD moves to "
                + f.getFlightNumber() + ". Just 2 links changed.",
                snap("New HEAD", marks(0, Mark.ADDED), 0, "HEAD", 0, f));
    }

    private void addAt(StepRecorder r, int index, Flight f) {
        r.setOperation("addAt");
        for (int i = 0; i < index; i++) {
            r.swap();
            r.record("add(" + index + ", " + f.getFlightNumber() + "): walk",
                    "There are no box numbers, so we walk: hop " + (i + 1) + " reaches node " + i + " (" + real.get(i).getFlightNumber() + ").",
                    snap("Walking to position " + index, marks(i, Mark.LOOK), i, "walker", -1, f));
        }
        real.add(index, f);
        r.swap(4);
        r.record("add(" + index + ", " + f.getFlightNumber() + "): relink",
                "Found the spot! The new node holds hands with both neighbours: 4 arrows change, and NOBODY moves boxes.",
                snap("Links updated around position " + index, marks(index, Mark.ADDED), index, "new", index - 1, f));
    }

    private void getAt(StepRecorder r, int index) {
        r.setOperation("get");
        int size = real.size();
        boolean fromTail = index >= (size >> 1);
        r.record("get(" + index + ")",
                fromTail
                        ? "Index " + index + " is closer to the TAIL, so Java cleverly starts walking from the TAIL backwards."
                        : "Index " + index + " is closer to the HEAD, so Java walks forward from the HEAD.",
                snap("Pick the closer end", null, fromTail ? size - 1 : 0, fromTail ? "start (TAIL)" : "start (HEAD)", -1, real.get(index)));
        if (fromTail) {
            for (int i = size - 1; i >= index; i--) {
                r.swap();
                r.record("Hop to node " + i, "Now at node " + i + " (" + real.get(i).getFlightNumber() + ")" + (i == index ? " - that's the one!" : "."),
                        snap("Walking PREV arrows", marks(i, i == index ? Mark.FOUND : Mark.LOOK), i, "walker", -1, real.get(index)));
            }
        } else {
            for (int i = 0; i <= index; i++) {
                r.swap();
                r.record("Hop to node " + i, "Now at node " + i + " (" + real.get(i).getFlightNumber() + ")" + (i == index ? " - that's the one!" : "."),
                        snap("Walking NEXT arrows", marks(i, i == index ? Mark.FOUND : Mark.LOOK), i, "walker", -1, real.get(index)));
            }
        }
    }

    private void contains(StepRecorder r, Flight target) {
        r.setOperation("contains");
        for (int i = 0; i < real.size(); i++) {
            r.compare();
            boolean same = real.get(i).equals(target);
            r.record("contains(" + target.getFlightNumber() + "): node " + i,
                    "Is " + real.get(i).getFlightNumber() + " the flight we want? " + (same ? "Yes! contains answers true." : "No, follow NEXT."),
                    snap("Checking node " + i, marks(i, same ? Mark.FOUND : Mark.COMPARE), i, "check", -1, target));
            if (same) {
                return;
            }
        }
        r.record("contains(" + target.getFlightNumber() + ") = false", "We walked the whole chain and it isn't here.",
                snap("Not found", null, -1, null, -1, target));
    }

    private void removeFirst(StepRecorder r) {
        if (real.isEmpty()) {
            return;
        }
        r.setOperation("removeFirst");
        Flight first = real.getFirst();
        r.record("removeFirst()", "Remove the HEAD node " + first.getFlightNumber() + "...",
                snap("Removing HEAD", marks(0, Mark.REMOVED), 0, "HEAD", -1, first));
        real.removeFirst();
        r.swap(2);
        r.record("HEAD moved", "HEAD now points to the second node, and its PREV becomes null. Instant - no walking at all!",
                snap("size = " + real.size(), real.isEmpty() ? null : marks(0, Mark.LOOK), real.isEmpty() ? -1 : 0, "HEAD", -1, first));
    }

    private void removeObject(StepRecorder r, Flight target) {
        r.setOperation("remove");
        int idx = real.indexOf(target);
        int limit = idx < 0 ? real.size() : idx + 1;
        for (int i = 0; i < limit; i++) {
            r.compare();
            r.record("remove(" + target.getFlightNumber() + "): node " + i,
                    "Is " + real.get(i).getFlightNumber() + " the one to remove? " + (i == idx ? "Yes!" : "No, keep walking."),
                    snap("Searching", marks(i, i == idx ? Mark.REMOVED : Mark.COMPARE), i, "check", -1, target));
        }
        if (idx < 0) {
            r.record("Nothing removed", target.getFlightNumber() + " is not in the chain, so remove answers false.",
                    snap("Not found", null, -1, null, -1, target));
            return;
        }
        String before = idx > 0 ? real.get(idx - 1).getFlightNumber() : "HEAD";
        String after = idx < real.size() - 1 ? real.get(idx + 1).getFlightNumber() : "TAIL";
        real.remove(target);
        r.swap(2);
        r.record("Neighbours hold hands",
                before + " now points NEXT straight to " + after + ", and " + after + " points PREV back to " + before
                + ". " + target.getFlightNumber() + " is skipped and gone - only 2 links changed!",
                snap("Links updated", null, -1, null, Math.max(-1, idx - 1), target));
    }

    private static Map<Integer, Mark> marks(int index, Mark m) {
        Map<Integer, Mark> map = new HashMap<>();
        if (index >= 0) {
            map.put(index, m);
        }
        return map;
    }

    private ChainState snap(String caption, Map<Integer, Mark> marks, int walker, String walkerLabel, int relinkAfter) {
        return snap(caption, marks, walker, walkerLabel, relinkAfter, null);
    }

    private ChainState snap(String caption, Map<Integer, Mark> marks, int walker, String walkerLabel, int relinkAfter, Flight focus) {
        return new ChainState(caption, new ArrayList<>(real), marks == null ? new HashMap<Integer, Mark>() : marks,
                walker, walkerLabel, relinkAfter, focus);
    }

    @Override
    protected void paintState(Graphics2D g, ChainState s, int w, int h) {
        StructurePaint.header(g, "LinkedList  -  size " + s.labels.size(), s.caption, w);
        int n = s.labels.size();
        double nodeW = 96;
        double nodeH = 50;
        double gap = 50;
        double left = 60;
        double avail = w - left - 60;
        int perRow = Math.max(1, (int) ((avail + gap) / (nodeW + gap)));
        double rowH = nodeH + 74;
        double top = 104;
        if (n == 0) {
            Draw.leftText(g, "HEAD -> null      TAIL -> null", 40, top + 20, Theme.bold(14f), Theme.TEXT_MUTED);
        }
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = left + (i % perRow) * (nodeW + gap);
            y[i] = top + (i / perRow) * rowH;
        }
        for (int i = 0; i + 1 < n; i++) {
            boolean relink = i == s.relinkAfter;
            Color c = relink ? Theme.V_PIVOT : Theme.TEXT_MUTED;
            float wd = relink ? 2.6f : 1.6f;
            if (y[i] == y[i + 1]) {
                Draw.arrow(g, x[i] + nodeW, y[i] + 16, x[i + 1] - 2, y[i + 1] + 16, c, wd);
                Draw.arrow(g, x[i + 1], y[i + 1] + 34, x[i] + nodeW + 2, y[i] + 34, Theme.tint(c, 0.35), 1.3f);
            } else {
                Draw.curvedArrow(g, x[i] + nodeW / 2, y[i] + nodeH, x[i + 1] + nodeW / 2, y[i + 1] - 4, 40, c, wd);
            }
        }
        for (int i = 0; i < n; i++) {
            Mark m = s.marks.get(i);
            Color fill = Theme.PRIMARY_SOFT;
            Color border = Theme.tint(Theme.PRIMARY, 0.4);
            Color fg = Theme.TEXT;
            if (m != null && m != Mark.NONE) {
                fill = m.color();
                border = Theme.shade(fill, 0.25);
                fg = Color.WHITE;
            }
            StructurePaint.flightCard(g, x[i], y[i], nodeW, nodeH, s.labels.get(i), s.subs.get(i), fill, border, fg);
            if (i == 0) {
                Draw.chip(g, "HEAD", x[i] + 4, y[i] - 30, Theme.SUCCESS, Color.WHITE);
                Draw.centeredText(g, "null", x[i] - 26, y[i] + 34, Theme.regular(11f), Theme.TEXT_MUTED);
            }
            if (i == n - 1) {
                Draw.chip(g, "TAIL", x[i] + nodeW - 50, y[i] - 30, Theme.DANGER, Color.WHITE);
                Draw.centeredText(g, "null", x[i] + nodeW + 24, y[i] + 16, Theme.regular(11f), Theme.TEXT_MUTED);
            }
            if (i == s.walker && s.walkerLabel != null) {
                Draw.arrow(g, x[i] + nodeW / 2, y[i] + nodeH + 26, x[i] + nodeW / 2, y[i] + nodeH + 4, Theme.PURPLE, 2f);
                Draw.centeredText(g, s.walkerLabel, x[i] + nodeW / 2, y[i] + nodeH + 36, Theme.bold(11.5f), Theme.PURPLE);
            }
        }
        StructurePaint.legendBottom(g, h, Theme.TEXT_MUTED, "next arrow", Theme.tint(Theme.TEXT_MUTED, 0.35), "prev arrow",
                Theme.V_PIVOT, "link just changed", Theme.V_ACTIVE, "walking", Theme.V_COMPARE, "comparing",
                Theme.V_SORTED, "added", Theme.V_FOUND, "found", Theme.V_SWAP, "removed", Theme.V_VISITED, "visited");
    }
}
