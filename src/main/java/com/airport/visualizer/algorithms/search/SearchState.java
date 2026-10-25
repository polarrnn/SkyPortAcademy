package com.airport.visualizer.algorithms.search;

import com.airport.visualizer.algorithms.sort.SortItem;
import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Immutable picture of a search in progress (shared by linear and binary search). */
public final class SearchState implements FlightTableState {

    private final SortItem[] items;
    private final boolean[] ruledOut;
    private final String targetText;
    private final boolean sortedBoard;
    private final int checking;
    private final int found;
    private final int lo;
    private final int mid;
    private final int hi;

    public SearchState(SortItem[] items, boolean[] ruledOut, String targetText, boolean sortedBoard,
            int checking, int found, int lo, int mid, int hi) {
        this.items = items.clone();
        this.ruledOut = ruledOut.clone();
        this.targetText = targetText;
        this.sortedBoard = sortedBoard;
        this.checking = checking;
        this.found = found;
        this.lo = lo;
        this.mid = mid;
        this.hi = hi;
    }

    public int size() {
        return items.length;
    }

    public SortItem get(int i) {
        return items[i];
    }

    public boolean isRuledOut(int i) {
        return ruledOut[i];
    }

    public int ruledOutCount() {
        int c = 0;
        for (boolean b : ruledOut) {
            if (b) {
                c++;
            }
        }
        return c;
    }

    public String getTargetText() {
        return targetText;
    }

    public boolean isSortedBoard() {
        return sortedBoard;
    }

    public int getChecking() {
        return checking;
    }

    public int getFound() {
        return found;
    }

    /** Search window start, or -1 when there is no window (linear search). */
    public int getLo() {
        return lo;
    }

    public int getMid() {
        return mid;
    }

    public int getHi() {
        return hi;
    }

    public boolean hasFlights() {
        return items.length > 0 && items[0].isFlight();
    }

    @Override
    public List<Flight> getTableFlights() {
        List<Flight> out = new ArrayList<>();
        for (SortItem it : items) {
            if (it.isFlight()) {
                out.add(it.getFlight());
            }
        }
        return out;
    }

    @Override
    public Set<String> getHighlightedFlightNumbers() {
        if (!hasFlights()) {
            return Collections.emptySet();
        }
        Set<String> out = new LinkedHashSet<>();
        if (checking >= 0) {
            out.add(items[checking].getLabel());
        }
        if (found >= 0) {
            out.add(items[found].getLabel());
        }
        return out;
    }
}
