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
import java.awt.geom.Ellipse2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Queue / Deque as a boarding line, using a real {@link ArrayDeque}. People join at the BACK and
 * board from the FRONT (a queue: first in, first out). A Deque can also add or remove at both ends.
 */
public class DequeDemo extends StructureDemo<DequeDemo.LineState> {

    private static final String[] NAMES = {"Ana", "Ben", "Carla", "Dan", "Ema", "Finn", "Gia", "Hugo", "Iris", "Jon", "Kai", "Lea"};

    /** One passenger in line: a name and the flight on their ticket. */
    public static final class Passenger {

        final String name;
        final Flight flight;

        Passenger(String name, Flight flight) {
            this.name = name;
            this.flight = flight;
        }

        @Override
        public String toString() {
            return name + " (" + flight.getFlightNumber() + ")";
        }
    }

    /** Snapshot of the line. */
    public static final class LineState extends BaseState {

        private final String caption;
        private final List<Passenger> line;
        private final List<String> boarded;
        private final int active;
        private final Mark mark;
        private final boolean joinFront;
        private final boolean joinBack;

        LineState(String caption, List<Passenger> line, List<String> boarded, int active, Mark mark,
                boolean joinFront, boolean joinBack, List<Flight> all, Passenger focus) {
            super(all, focus == null ? Collections.<String>emptyList() : Collections.singletonList(focus.flight.getFlightNumber()));
            this.caption = caption;
            this.line = Collections.unmodifiableList(new ArrayList<>(line));
            this.boarded = Collections.unmodifiableList(new ArrayList<>(boarded));
            this.active = active;
            this.mark = mark;
            this.joinFront = joinFront;
            this.joinBack = joinBack;
        }
    }

    private final Deque<Passenger> line = new ArrayDeque<>();
    private final List<String> boarded = new ArrayList<>();
    private final List<Passenger> passengers = new ArrayList<>();

    public DequeDemo() {
        this(FlightData.first(7));
    }

    public DequeDemo(List<Flight> flights) {
        super("Queue / Deque (boarding line)", Category.COLLECTIONS,
                "A boarding line at the gate: join at the BACK, board from the FRONT. A Deque (double-ended queue) can also use both ends.",
                LineState.class, flights);
        for (int i = 0; i < flights().size(); i++) {
            String name = i < NAMES.length ? NAMES[i] : "P" + (i + 1);
            passengers.add(new Passenger(name, flights().get(i)));
        }
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Checks", "People moved"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) at both ends")
                .row("offerLast", "offer / offerLast(x)", "O(1)", "Join the BACK of the line.")
                .row("offerFirst", "offerFirst(x)", "O(1)", "Jump to the FRONT (only a Deque can do this) - like families with babies boarding first.")
                .row("peekFirst", "peek / peekFirst()", "O(1)", "Look at who is at the FRONT without removing them.")
                .row("peekLast", "peekLast()", "O(1)", "Look at the BACK.")
                .row("contains", "contains(x)", "O(n)", "Walk the line from front to back.")
                .row("pollFirst", "poll / pollFirst()", "O(1)", "The FRONT person boards the plane and leaves the line.")
                .row("pollLast", "pollLast()", "O(1)", "The BACK person leaves the line.")
                .row("iterate", "for-each", "O(n)", "From FRONT to BACK.")
                .whenToUse("First come, first served: boarding lines, tasks waiting their turn, or undo history (use it as a stack with push/pop).")
                .avoidWhen("You need the 5th person directly (use a List) or the 'most important' first (use a PriorityQueue).")
                .build();
    }

    @Override
    protected void runPlain(String scenario) {
        Deque<Passenger> d = new ArrayDeque<>();
        for (Passenger p : passengers) {
            d.offerLast(p);
        }
        d.peekFirst();
        d.peekLast();
        d.pollFirst();
        d.pollLast();
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        line.clear();
        boarded.clear();
        r.record("Welcome to Gate A3",
                "This is a boarding line. The FRONT is next to the gate door, the BACK is the end of the line. "
                + "Java's ArrayDeque is the perfect tool for it.",
                snap("ArrayDeque<Passenger> line = new ArrayDeque<>()", -1, Mark.NONE, false, false, null));
        int n = passengers.size();
        if (wants(scenario, ADD)) {
            int normal = n > 1 ? n - 1 : n;
            for (int i = 0; i < normal; i++) {
                Passenger p = passengers.get(i);
                r.setOperation("offerLast");
                line.offerLast(p);
                r.swap();
                r.record("offerLast(" + p.name + ")",
                        p.name + " (flight " + p.flight.getFlightNumber() + ") joins the BACK of the line. Fair and square!",
                        snap("Line length = " + line.size(), line.size() - 1, Mark.ADDED, false, true, p));
            }
            if (n > 1) {
                Passenger vip = passengers.get(n - 1);
                r.setOperation("offerFirst");
                line.offerFirst(vip);
                r.swap();
                r.record("offerFirst(" + vip.name + ")",
                        vip.name + " has a baby, so the staff let them go to the FRONT. Only a Deque (double-ended queue) can add at the front!",
                        snap("Priority boarding at the FRONT", 0, Mark.ADDED, true, false, vip));
            }
        } else {
            for (Passenger p : passengers) {
                line.offerLast(p);
            }
            r.swap(n);
            r.setOperation(null);
            r.record("Line already formed", "Everyone is already standing in line. Now let's see what we can do with it!",
                    snap("Line length = " + line.size(), -1, Mark.NONE, false, false, null));
        }
        if (wants(scenario, GET)) {
            r.setOperation("peekFirst");
            Passenger front = line.peekFirst();
            r.compare();
            r.record("peekFirst()", "Who is next? peekFirst() just LOOKS at the FRONT: it's " + front.name + ". Nobody leaves the line.",
                    snap("peek, don't remove", 0, Mark.FOUND, false, false, front));
            r.setOperation("peekLast");
            Passenger back = line.peekLast();
            r.compare();
            r.record("peekLast()", "And at the BACK is " + back.name + ".",
                    snap("Looking at the BACK", line.size() - 1, Mark.FOUND, false, false, back));
            Passenger target = passengers.get(Math.min(3, n - 1));
            r.setOperation("contains");
            List<Passenger> list = new ArrayList<>(line);
            for (int i = 0; i < list.size(); i++) {
                r.compare();
                boolean same = list.get(i) == target;
                r.record("contains(" + target.name + "): spot " + (i + 1),
                        "Is " + list.get(i).name + " the person we're looking for? " + (same ? "Yes! They're in line." : "No, look further back."),
                        snap("Walking from FRONT to BACK", i, same ? Mark.FOUND : Mark.COMPARE, false, false, target));
                if (same) {
                    break;
                }
            }
        }
        if (wants(scenario, REMOVE)) {
            for (int k = 0; k < 2 && !line.isEmpty(); k++) {
                r.setOperation("pollFirst");
                Passenger p = line.peekFirst();
                r.record("pollFirst()", p.name + " is at the FRONT, shows their ticket for " + p.flight.getFlightNumber() + "...",
                        snap("Boarding...", 0, Mark.REMOVED, false, false, p));
                line.pollFirst();
                boarded.add(p.name);
                r.swap();
                r.record(p.name + " boarded!", p.name + " walks onto the plane. Everyone else is now one step closer to the FRONT - and nobody had to be moved one by one.",
                        snap("On the plane: " + String.join(", ", boarded), -1, Mark.NONE, false, false, p));
            }
            if (!line.isEmpty()) {
                r.setOperation("pollLast");
                Passenger p = line.peekLast();
                r.record("pollLast()", p.name + " at the BACK forgot their passport and leaves the line.",
                        snap("Leaving from the BACK", line.size() - 1, Mark.REMOVED, false, false, p));
                line.pollLast();
                r.swap();
                r.record(p.name + " left", "pollLast() removed the BACK person in one step.",
                        snap("Line length = " + line.size(), -1, Mark.NONE, false, false, p));
            }
        }
        if (wants(scenario, ITERATE)) {
            r.setOperation("iterate");
            List<Passenger> list = new ArrayList<>(line);
            for (int i = 0; i < list.size(); i++) {
                r.record("Visit spot " + (i + 1), "Going from FRONT to BACK: " + list.get(i) + ".",
                        snap("for (Passenger p : line)", i, Mark.VISIT, false, false, list.get(i)));
            }
        }
        r.setOperation(null);
        r.record("All done!",
                "Queues are FIFO: First In, First Out. A Deque can work at both ends, so it can be a queue OR a stack.",
                snap("Still waiting: " + line.size(), -1, Mark.NONE, false, false, null));
    }

    private LineState snap(String caption, int active, Mark mark, boolean joinFront, boolean joinBack, Passenger focus) {
        return new LineState(caption, new ArrayList<>(line), boarded, active, mark, joinFront, joinBack, flights(), focus);
    }

    @Override
    protected void paintState(Graphics2D g, LineState s, int w, int h) {
        StructurePaint.header(g, "Boarding line  -  " + s.line.size() + " waiting", s.caption, w);
        double top = 96;
        // gate door on the left
        Draw.roundRect(g, 20, top, 64, 120, 12, Theme.SIDEBAR, null);
        Draw.plane(g, 52, top + 40, 34, Theme.ACCENT, -Math.PI / 2);
        Draw.centeredText(g, "GATE", 52, top + 82, Theme.bold(12f), Color.WHITE);
        Draw.centeredText(g, "door", 52, top + 100, Theme.regular(11f), Theme.SIDEBAR_MUTED);

        int n = s.line.size();
        double startX = 104;
        double avail = w - startX - 80;
        double slot = n == 0 ? 90 : Math.max(58, Math.min(96, avail / n));
        int perRow = Math.max(1, (int) (avail / slot));
        double personH = 96;
        for (int i = 0; i < n; i++) {
            Passenger p = s.line.get(i);
            int row = i / perRow;
            double x = startX + (i % perRow) * slot;
            double y = top + 12 + row * (personH + 50);
            Color c = Theme.PRIMARY;
            if (i == s.active && s.mark != Mark.NONE) {
                c = s.mark.color();
            }
            double cx = x + slot / 2 - 4;
            g.setColor(Theme.tint(c, 0.15));
            g.fill(new Ellipse2D.Double(cx - 13, y, 26, 26));
            Draw.roundRect(g, cx - 22, y + 28, 44, 34, 14, c, null);
            Draw.textInBox(g, p.name, x, y + 64, slot - 8, 18, Theme.bold(12.5f), Theme.TEXT);
            Draw.textInBox(g, p.flight.getFlightNumber(), x, y + 80, slot - 8, 14, Theme.regular(11f), Theme.TEXT_MUTED);
            if (n == 1) {
                Draw.chip(g, "FRONT = BACK", cx - 50, y - 30, Theme.PURPLE, Color.WHITE);
            } else if (i == 0) {
                Draw.chip(g, "FRONT", cx - 26, y - 30, Theme.SUCCESS, Color.WHITE);
            } else if (i == n - 1) {
                Draw.chip(g, "BACK", cx - 22, y - 30, Theme.DANGER, Color.WHITE);
            }
        }
        if (n == 0) {
            Draw.leftText(g, "(nobody in line)", startX, top + 50, Theme.regular(14f), Theme.TEXT_MUTED);
        }
        if (s.joinBack && n > 0) {
            double x = startX + Math.min(n - 1, perRow - 1) * slot + slot;
            Draw.leftText(g, "joins here", Math.min(x, w - 90), top + 130, Theme.bold(12f), Theme.V_SORTED);
        }
        if (s.joinFront) {
            Draw.arrow(g, startX + slot / 2, top + 150, startX + slot / 2, top + 116, Theme.V_SORTED, 2.4f);
            Draw.leftText(g, "cuts in at the FRONT", startX, top + 162, Theme.bold(12f), Theme.V_SORTED);
        }
        StructurePaint.strip(g, "On the plane (boarded in this order)", s.boarded, 20, h - 34 - 74, w - 40,
                s.boarded.size(), -1, Mark.NONE, true);
        StructurePaint.legendBottom(g, h, Theme.PRIMARY, "waiting", Theme.V_SORTED, "just joined",
                Theme.V_FOUND, "peeked / found", Theme.V_COMPARE, "checking", Theme.V_SWAP, "leaving", Theme.V_VISITED, "visited");
    }
}
