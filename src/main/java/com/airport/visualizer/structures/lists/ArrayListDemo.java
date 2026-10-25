package com.airport.visualizer.structures.lists;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructureDemo;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ArrayList&lt;Flight&gt;: a row of numbered boxes. The real ArrayList does the work; the picture also
 * shows its hidden array (capacity 10, growing by half when full) and every shift it performs.
 */
public class ArrayListDemo extends StructureDemo<RowState> {

    private final List<Flight> real = new ArrayList<>();
    private Flight[] boxes = new Flight[0];

    public ArrayListDemo() {
        this(FlightData.first(11));
    }

    public ArrayListDemo(List<Flight> flights) {
        super("ArrayList", Category.COLLECTIONS,
                "A row of numbered boxes. Jumping to box #5 is instant, but squeezing a flight into the middle means everyone after it must shuffle over.",
                RowState.class, flights);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Box moves"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) get, O(n) insert")
                .row("add", "add(flight)  (at the end)", "O(1) amortized", "Drop it in the next free box. Sometimes the row is full and must be copied into a bigger one (rare).")
                .row("addAt", "add(index, flight)", "O(n)", "Everyone after the index must move one box to the right first.")
                .row("get", "get(index)", "O(1)", "Box number -> straight there, no searching.")
                .row("contains", "contains(flight)", "O(n)", "Has to check boxes one by one.")
                .row("remove", "remove(index)", "O(n)", "Everyone after the hole moves one box to the left.")
                .row("iterate", "for-each", "O(n)", "Box 0, box 1, box 2... in order.")
                .whenToUse("Your default list! Great when you mostly add at the end and read by position, like a flight board you scroll through.")
                .avoidWhen("You keep inserting or removing at the FRONT or middle of a huge list - lots of shuffling.")
                .build();
    }

    @Override
    protected void runPlain(String scenario) {
        List<Flight> list = new ArrayList<>();
        for (Flight f : flights()) {
            list.add(f);
        }
        list.get(list.size() / 2);
        list.contains(pick(3));
        list.remove(Math.min(1, list.size() - 1));
        for (Flight f : list) {
            f.getMinutes();
        }
    }

    @Override
    protected void record(StepRecorder r, String scenario) {
        real.clear();
        boxes = new Flight[0];
        r.record("Meet the ArrayList",
                "An ArrayList is like a row of numbered boxes. Box numbers (indexes) start at 0! "
                + "Inside, Java keeps a hidden array that starts with room for 10 flights.",
                snap("No boxes yet. size = 0", Mark.NONE, null));
        int n = flights().size();
        if (wants(scenario, ADD)) {
            int atEnd = n > 1 ? n - 1 : n;
            for (int i = 0; i < atEnd; i++) {
                addLast(r, flights().get(i), true);
            }
            if (n > 1) {
                addAt(r, Math.min(2, real.size()), flights().get(n - 1));
            }
        } else {
            for (Flight f : flights()) {
                addLast(r, f, false);
            }
            r.setOperation(null);
            r.record("Boxes already filled", "We already put all " + n + " flights into the boxes. Let's try the next thing!",
                    snap(real.size() + " flights stored", Mark.NONE, null));
        }
        if (wants(scenario, GET)) {
            get(r, real.size() / 2);
            contains(r, pick(Math.max(0, n - 2)));
            contains(r, notInList());
        }
        if (wants(scenario, REMOVE)) {
            removeAt(r, Math.min(1, real.size() - 1));
        }
        if (wants(scenario, ITERATE)) {
            iterate(r);
        }
        r.setOperation(null);
        r.record("All done!",
                "ArrayList: instant jumps by box number, cheap adds at the end, but costly squeezes and removals in the middle.",
                snap("Final size = " + real.size() + ", capacity = " + boxes.length, Mark.NONE, null));
    }

    private Flight notInList() {
        for (Flight f : FlightData.sample()) {
            if (!flights().contains(f)) {
                return f;
            }
        }
        return new Flight("ZZ999", "Ghost Air", "Nowhere", "Z9", "04:00", com.airport.visualizer.model.FlightType.DEPARTURE);
    }

    private void grow(StepRecorder r, boolean show) {
        int oldCap = boxes.length;
        int newCap = oldCap == 0 ? 10 : oldCap + (oldCap >> 1);
        Flight[] bigger = new Flight[newCap];
        System.arraycopy(boxes, 0, bigger, 0, real.size());
        boxes = bigger;
        r.swap(oldCap == 0 ? 0 : real.size());
        if (show) {
            r.record(oldCap == 0 ? "Get the first 10 boxes" : "Row is full - make it bigger!",
                    oldCap == 0
                            ? "The first time we add, the ArrayList gets a row of 10 empty boxes."
                            : "All " + oldCap + " boxes are used, so Java makes a NEW row of " + newCap
                            + " boxes (half as big again) and copies every flight across. That's slow, but it happens rarely.",
                    snap("capacity " + oldCap + " -> " + newCap, Mark.MOVED, null));
        }
    }

    private void addLast(StepRecorder r, Flight f, boolean show) {
        r.setOperation("add");
        if (real.size() == boxes.length) {
            grow(r, show);
        }
        real.add(f);
        boxes[real.size() - 1] = f;
        r.swap();
        if (show) {
            int i = real.size() - 1;
            r.record("add(" + f.getFlightNumber() + ")",
                    f.getFlightNumber() + " goes into the next free box: box " + i + ". No one else had to move!",
                    snap("size = " + real.size(), Mark.ADDED, f, i, Mark.ADDED));
        }
    }

    private void addAt(StepRecorder r, int index, Flight f) {
        r.setOperation("addAt");
        r.record("add(" + index + ", " + f.getFlightNumber() + ")",
                "Now let's squeeze " + f.getFlightNumber() + " into box " + index + ". That box is taken, so everyone from box "
                + index + " onward must slide one box to the right first.",
                snap("Squeeze into box " + index, Mark.LOOK, f, index, Mark.LOOK));
        if (real.size() == boxes.length) {
            grow(r, true);
        }
        for (int j = real.size() - 1; j >= index; j--) {
            boxes[j + 1] = boxes[j];
            boxes[j] = null;
            r.swap();
            RowState.Builder b = base("Slide box " + j + " -> " + (j + 1), Mark.MOVED, boxes[j + 1]);
            b.mark(j + 1, Mark.MOVED).arrow(j, j + 1);
            r.record("Shift right: box " + j + " -> " + (j + 1),
                    boxes[j + 1].getFlightNumber() + " moves from box " + j + " to box " + (j + 1) + ".",
                    b.build());
        }
        real.add(index, f);
        boxes[index] = f;
        r.swap();
        r.record("Placed in box " + index,
                f.getFlightNumber() + " sits in box " + index + ". It took " + (real.size() - 1 - index)
                + " moves - that's why inserting in the middle is O(n).",
                snap("size = " + real.size(), Mark.ADDED, f, index, Mark.ADDED));
    }

    private void get(StepRecorder r, int index) {
        if (real.isEmpty()) {
            return;
        }
        r.setOperation("get");
        Flight f = real.get(index);
        r.record("get(" + index + ")",
                "Want box " + index + "? The computer does a tiny math trick (start + " + index + " x box size) and jumps straight there. "
                + "It's " + f.getFlightNumber() + " to " + f.getRouteText() + ". One step, no matter how long the list is!",
                snap("Jump straight to box " + index, Mark.FOUND, f, index, Mark.FOUND));
    }

    private void contains(StepRecorder r, Flight target) {
        r.setOperation("contains");
        for (int i = 0; i < real.size(); i++) {
            r.compare();
            boolean same = real.get(i).equals(target);
            RowState.Builder b = base("contains(" + target.getFlightNumber() + ")", Mark.COMPARE, target);
            b.mark(i, same ? Mark.FOUND : Mark.COMPARE).pointer(i, "check");
            for (int k = 0; k < i; k++) {
                b.mark(k, Mark.VISIT);
            }
            r.record("contains(" + target.getFlightNumber() + "): box " + i,
                    "Is box " + i + " (" + real.get(i).getFlightNumber() + ") the flight " + target.getFlightNumber() + "? "
                    + (same ? "YES! contains answers true." : "No - check the next box."),
                    b.build());
            if (same) {
                return;
            }
        }
        r.record("contains(" + target.getFlightNumber() + ") = false",
                "We checked every single box and " + target.getFlightNumber() + " was not there. Without box numbers, an ArrayList has to look one by one.",
                snap("Not found after " + real.size() + " checks", Mark.MISSING, target));
    }

    private void removeAt(StepRecorder r, int index) {
        if (real.isEmpty()) {
            return;
        }
        r.setOperation("remove");
        Flight f = real.get(index);
        r.record("remove(" + index + ")",
                "Take " + f.getFlightNumber() + " out of box " + index + ". That leaves a hole...",
                snap("Removing box " + index, Mark.REMOVED, f, index, Mark.REMOVED));
        real.remove(index);
        int size = real.size();
        for (int j = index; j < size; j++) {
            boxes[j] = boxes[j + 1];
            boxes[j + 1] = null;
            r.swap();
            RowState.Builder b = base("Slide box " + (j + 1) + " -> " + j, Mark.MOVED, boxes[j]);
            b.mark(j, Mark.MOVED).arrow(j + 1, j);
            r.record("Shift left: box " + (j + 1) + " -> " + j,
                    "To close the hole, " + boxes[j].getFlightNumber() + " slides from box " + (j + 1) + " to box " + j + ".",
                    b.build());
        }
        if (size == index) {
            boxes[index] = null;
        }
        r.record("Hole closed",
                "Done! All boxes are packed again and size = " + size + ". Removing near the front costs many moves.",
                snap("size = " + size, Mark.NONE, f));
    }

    private void iterate(StepRecorder r) {
        r.setOperation("iterate");
        for (int i = 0; i < real.size(); i++) {
            RowState.Builder b = base("for (Flight f : list)", Mark.VISIT, real.get(i));
            for (int k = 0; k <= i; k++) {
                b.mark(k, Mark.VISIT);
            }
            b.pointer(i, "f");
            r.record("Visit box " + i,
                    "The for-each loop visits box " + i + ": " + real.get(i).getFlightNumber() + " at " + real.get(i).getTimeText() + ".",
                    b.build());
        }
    }

    // ------------------------------------------------------------------ snapshots

    private RowState.Builder base(String caption, Mark noteMark, Flight focus) {
        List<String> cells = new ArrayList<>();
        List<String> subs = new ArrayList<>();
        for (Flight f : boxes) {
            cells.add(f == null ? null : f.getFlightNumber());
            subs.add(f == null ? "" : f.getTimeText());
        }
        return RowState.builder("ArrayList  -  size " + real.size() + ", capacity " + boxes.length, cells, subs)
                .caption(caption)
                .table(new ArrayList<>(real), focus == null ? Collections.<String>emptyList()
                        : Collections.singletonList(focus.getFlightNumber()));
    }

    private RowState snap(String caption, Mark noteMark, Flight focus) {
        return base(caption, noteMark, focus).build();
    }

    private RowState snap(String caption, Mark noteMark, Flight focus, int index, Mark m) {
        return base(caption, noteMark, focus).mark(index, m).build();
    }

    @Override
    protected void paintState(Graphics2D g, RowState s, int w, int h) {
        RowPainter.paint(g, s, w, h);
    }
}
