package com.airport.visualizer.trees;

import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.trees.TreeSnapshot.Mark;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A real min-heap stored in an array: the smallest (earliest) item is always at index 0.
 * Parent of i = (i - 1) / 2, children of i = 2i + 1 and 2i + 2.
 */
public final class HeapEngine {

    private final Words words;
    private final List<Flight> tableFlights;
    private final List<TreeItem> a = new ArrayList<>();
    private final List<String> taken = new ArrayList<>();
    private StepRecorder rec;
    private String caption = "";

    public HeapEngine(Words words, List<Flight> tableFlights) {
        this.words = words;
        this.tableFlights = tableFlights == null ? Collections.<Flight>emptyList() : tableFlights;
    }

    public int size() {
        return a.size();
    }

    public List<TreeItem> items() {
        return Collections.unmodifiableList(a);
    }

    public void clear() {
        a.clear();
        taken.clear();
    }

    /** True when every parent is <= its children (used by sanity checks). */
    public boolean isValidHeap() {
        for (int i = 1; i < a.size(); i++) {
            if (a.get((i - 1) / 2).getKey() > a.get(i).getKey()) {
                return false;
            }
        }
        return true;
    }

    public HeapState plainState(String caption) {
        return new HeapState(a, new HashMap<Integer, Mark>(), caption, "", taken, tableFlights);
    }

    // ------------------------------------------------------------------ quiet versions (timing / setup)

    public void insertQuietly(TreeItem item) {
        a.add(item);
        int i = a.size() - 1;
        while (i > 0 && a.get((i - 1) / 2).getKey() > a.get(i).getKey()) {
            Collections.swap(a, i, (i - 1) / 2);
            i = (i - 1) / 2;
        }
    }

    public TreeItem removeMinQuietly() {
        if (a.isEmpty()) {
            return null;
        }
        TreeItem min = a.get(0);
        TreeItem last = a.remove(a.size() - 1);
        if (!a.isEmpty()) {
            a.set(0, last);
            int i = 0;
            while (true) {
                int l = 2 * i + 1;
                int r = l + 1;
                int s = i;
                if (l < a.size() && a.get(l).getKey() < a.get(s).getKey()) {
                    s = l;
                }
                if (r < a.size() && a.get(r).getKey() < a.get(s).getKey()) {
                    s = r;
                }
                if (s == i) {
                    break;
                }
                Collections.swap(a, i, s);
                i = s;
            }
        }
        return min;
    }

    public void buildQuietly(List<TreeItem> items) {
        a.clear();
        for (TreeItem t : items) {
            insertQuietly(t);
        }
    }

    // ------------------------------------------------------------------ narrated operations

    public void insert(TreeItem item, StepRecorder recorder) {
        rec = recorder;
        rec.setOperation("insert");
        caption = "Insert " + item.describe();
        a.add(item);
        int i = a.size() - 1;
        snap(item.getLabel() + " joins at the end", item.describe() + " is placed in the next free spot: index " + i
                + ". In the tree that is the bottom row, as far left as possible.", marks(i, Mark.NEW), i == 0 ? "" : parentFormula(i));
        while (i > 0) {
            int p = (i - 1) / 2;
            rec.compare();
            boolean up = a.get(i).getKey() < a.get(p).getKey();
            snap("Compare with its parent", "The parent of index " + i + " is index " + p + ". Is " + a.get(i).keyText() + " "
                    + words.less() + " than " + a.get(p).keyText() + "? " + (up ? "Yes! A parent must be the " + words.less()
                    + " one, so they swap." : "No - the parent is already " + words.less() + ", so it can stay."),
                    marks(i, Mark.CURRENT, p, Mark.PATH), parentFormula(i));
            if (!up) {
                break;
            }
            Collections.swap(a, i, p);
            rec.swap();
            snap("Bubble up!", a.get(p).getLabel() + " climbs up to index " + p + ".", marks(p, Mark.CURRENT, i, Mark.PATH),
                    p == 0 ? "" : parentFormula(p));
            i = p;
        }
        snap("Heap is happy again", "Every parent is " + words.less() + " than (or equal to) its children. The "
                + words.least() + " " + words.thing() + " on top is " + a.get(0).describe() + ".", marks(i, Mark.FOUND, 0, Mark.FOUND), "");
    }

    public TreeItem removeMin(StepRecorder recorder) {
        rec = recorder;
        rec.setOperation("remove");
        caption = "Remove the " + words.least();
        if (a.isEmpty()) {
            snap("The heap is empty", "There is nothing to remove.", marks(), "");
            return null;
        }
        TreeItem min = a.get(0);
        snap(min.getLabel() + " is on top", "The top (index 0) always holds the " + words.least() + " " + words.thing()
                + ": " + min.describe() + ". We take it out" + (min.getFlight() != null ? " - time to take off!" : "."),
                marks(0, Mark.REMOVED), "");
        taken.add(min.getLabel());
        TreeItem last = a.remove(a.size() - 1);
        if (a.isEmpty()) {
            snap("Heap is now empty", "That was the only " + words.thing() + ".", marks(), "");
            return min;
        }
        a.set(0, last);
        rec.swap();
        snap(last.getLabel() + " moves to the top", "To keep the tree complete, the LAST " + words.thing() + " ("
                + last.getLabel() + ") jumps into the empty top spot. It is probably too " + words.more()
                + " for the top, so it must sink down.", marks(0, Mark.CURRENT), childFormula(0));
        int i = 0;
        while (true) {
            int l = 2 * i + 1;
            int r = l + 1;
            if (l >= a.size()) {
                snap("Reached the bottom", a.get(i).getLabel() + " has no children, so it stops here.", marks(i, Mark.FOUND), "");
                break;
            }
            int s = l;
            if (r < a.size()) {
                rec.compare();
                s = a.get(r).getKey() < a.get(l).getKey() ? r : l;
                snap("Which child is " + words.less() + "?", "Children of index " + i + " are " + l + " (" + a.get(l).keyText()
                        + ") and " + r + " (" + a.get(r).keyText() + "). The " + words.less() + " one is " + a.get(s).keyText() + ".",
                        marks(i, Mark.CURRENT, l, Mark.PATH, r, Mark.PATH), childFormula(i));
            }
            rec.compare();
            boolean down = a.get(s).getKey() < a.get(i).getKey();
            snap("Compare with child " + a.get(s).getLabel(), "Is " + a.get(s).keyText() + " " + words.less() + " than "
                    + a.get(i).keyText() + "? " + (down ? "Yes - they swap and " + a.get(i).getLabel() + " sinks down."
                    : "No - " + a.get(i).getLabel() + " is in the right place."), marks(i, Mark.CURRENT, s, Mark.PATH), childFormula(i));
            if (!down) {
                snap("Heap is happy again", "The new top is " + a.get(0).describe() + ".", marks(i, Mark.FOUND, 0, Mark.FOUND), "");
                break;
            }
            Collections.swap(a, i, s);
            rec.swap();
            i = s;
            snap("Sink down", a.get(i).getLabel() + " moved down to index " + i + ".", marks(i, Mark.CURRENT), childFormula(i));
        }
        return min;
    }

    /** Turns any list into a heap by fixing every parent from the bottom up (heapify). */
    public void buildHeap(List<TreeItem> items, StepRecorder recorder) {
        rec = recorder;
        rec.setOperation("build");
        caption = "Build a heap from a messy list";
        a.clear();
        a.addAll(items);
        snap("A messy list", "These " + a.size() + " " + words.things() + " are in board order, NOT heap order. "
                + "Instead of adding them one by one, we fix each parent from the bottom up.", marks(), "");
        for (int start = a.size() / 2 - 1; start >= 0; start--) {
            snap("Fix the little tree at index " + start, "Index " + start + " (" + a.get(start).getLabel()
                    + ") is a parent. We let it sink until it is " + words.less() + " than its children.",
                    marks(start, Mark.CURRENT), childFormula(start));
            int i = start;
            while (true) {
                int l = 2 * i + 1;
                int r = l + 1;
                int s = i;
                if (l < a.size()) {
                    rec.compare();
                    if (a.get(l).getKey() < a.get(s).getKey()) {
                        s = l;
                    }
                }
                if (r < a.size()) {
                    rec.compare();
                    if (a.get(r).getKey() < a.get(s).getKey()) {
                        s = r;
                    }
                }
                if (s == i) {
                    snap(a.get(i).getLabel() + " can stay", "None of its children is " + words.less() + ", so this part is a heap now.",
                            marks(i, Mark.FOUND), "");
                    break;
                }
                Collections.swap(a, i, s);
                rec.swap();
                snap("Swap " + a.get(i).getLabel() + " up", a.get(i).getLabel() + " is " + words.less() + ", so it moves up to index "
                        + i + " and " + a.get(s).getLabel() + " sinks to index " + s + ".", marks(s, Mark.CURRENT, i, Mark.PATH), childFormula(s));
                i = s;
            }
        }
        snap("It's a heap!", "Every parent is " + words.less() + " than its children. The " + words.least() + " is on top: "
                + a.get(0).describe() + ". Building this way takes only O(n) work!", marks(0, Mark.FOUND), "");
    }

    // ------------------------------------------------------------------ helpers

    private static String parentFormula(int i) {
        return "parent(" + i + ") = (" + i + " - 1) / 2 = " + (i - 1) / 2;
    }

    private String childFormula(int i) {
        int l = 2 * i + 1;
        int r = l + 1;
        return "left(" + i + ") = 2*" + i + "+1 = " + l + (l < a.size() ? "" : " (none)")
                + "    right(" + i + ") = 2*" + i + "+2 = " + r + (r < a.size() ? "" : " (none)");
    }

    private static Map<Integer, Mark> marks(Object... pairs) {
        Map<Integer, Mark> m = new HashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            m.put((Integer) pairs[i], (Mark) pairs[i + 1]);
        }
        return m;
    }

    private void snap(String title, String narration, Map<Integer, Mark> marks, String formula) {
        rec.record(title, narration, new HeapState(a, marks, caption, formula, taken, tableFlights));
    }
}
