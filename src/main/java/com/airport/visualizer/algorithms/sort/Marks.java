package com.airport.visualizer.algorithms.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable description of what to highlight in one sorting picture: which bars are being
 * compared or swapped, the pivot, the held "key", the active range, pointer labels and,
 * for merge sort, the two halves waiting in the tray. Every "with" method returns a new copy.
 */
public final class Marks {

    private static final int[] NONE = new int[0];
    private static final Marks EMPTY = new Marks();

    private int[] comparing = NONE;
    private int[] swapping = NONE;
    private int pivot = -1;
    private int key = -1;
    private int lo = -1;
    private int hi = -1;
    private int split = -1;
    private int emptyFrom = -1;
    private int emptyTo = -1;
    private int writing = -1;
    private List<SortItem> leftTray = Collections.emptyList();
    private List<SortItem> rightTray = Collections.emptyList();
    private Map<Integer, String> pointers = Collections.emptyMap();

    private Marks() {
    }

    private Marks copy() {
        Marks m = new Marks();
        m.comparing = comparing;
        m.swapping = swapping;
        m.pivot = pivot;
        m.key = key;
        m.lo = lo;
        m.hi = hi;
        m.split = split;
        m.emptyFrom = emptyFrom;
        m.emptyTo = emptyTo;
        m.writing = writing;
        m.leftTray = leftTray;
        m.rightTray = rightTray;
        m.pointers = pointers;
        return m;
    }

    public static Marks none() {
        return EMPTY;
    }

    public Marks compare(int... indices) {
        Marks m = copy();
        m.comparing = indices.clone();
        return m;
    }

    public Marks swap(int... indices) {
        Marks m = copy();
        m.swapping = indices.clone();
        return m;
    }

    public Marks pivot(int index) {
        Marks m = copy();
        m.pivot = index;
        return m;
    }

    public Marks key(int index) {
        Marks m = copy();
        m.key = index;
        return m;
    }

    /** The part of the line the algorithm is currently working on (inclusive). */
    public Marks range(int from, int to) {
        Marks m = copy();
        m.lo = from;
        m.hi = to;
        return m;
    }

    /** Draws a dividing line just after this index (merge sort halves). */
    public Marks split(int afterIndex) {
        Marks m = copy();
        m.split = afterIndex;
        return m;
    }

    /** Slots shown as empty outlines (merge sort is refilling them). */
    public Marks empty(int from, int to) {
        Marks m = copy();
        m.emptyFrom = from;
        m.emptyTo = to;
        return m;
    }

    public Marks writing(int index) {
        Marks m = copy();
        m.writing = index;
        return m;
    }

    public Marks trays(List<SortItem> left, List<SortItem> right) {
        Marks m = copy();
        m.leftTray = Collections.unmodifiableList(new ArrayList<>(left));
        m.rightTray = Collections.unmodifiableList(new ArrayList<>(right));
        return m;
    }

    /** Adds a small label (like "i", "min" or "pivot") under a bar. */
    public Marks pointer(int index, String label) {
        if (index < 0) {
            return this;
        }
        Marks m = copy();
        Map<Integer, String> p = new LinkedHashMap<>(pointers);
        String existing = p.get(index);
        p.put(index, existing == null ? label : existing + "," + label);
        m.pointers = Collections.unmodifiableMap(p);
        return m;
    }

    public boolean isComparing(int i) {
        return contains(comparing, i);
    }

    public boolean isSwapping(int i) {
        return contains(swapping, i);
    }

    public int[] getComparing() {
        return comparing.clone();
    }

    public int[] getSwapping() {
        return swapping.clone();
    }

    public int getPivot() {
        return pivot;
    }

    public int getKey() {
        return key;
    }

    public int getLo() {
        return lo;
    }

    public int getHi() {
        return hi;
    }

    public boolean hasRange() {
        return lo >= 0 && hi >= lo;
    }

    public int getSplit() {
        return split;
    }

    public boolean isEmptySlot(int i) {
        return emptyFrom >= 0 && i >= emptyFrom && i <= emptyTo;
    }

    public int getWriting() {
        return writing;
    }

    public List<SortItem> getLeftTray() {
        return leftTray;
    }

    public List<SortItem> getRightTray() {
        return rightTray;
    }

    public boolean hasTrays() {
        return !leftTray.isEmpty() || !rightTray.isEmpty() || emptyFrom >= 0;
    }

    public String getPointer(int i) {
        return pointers.get(i);
    }

    private static boolean contains(int[] arr, int v) {
        for (int a : arr) {
            if (a == v) {
                return true;
            }
        }
        return false;
    }
}
