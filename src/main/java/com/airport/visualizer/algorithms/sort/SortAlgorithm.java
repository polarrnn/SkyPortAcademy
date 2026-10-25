package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.ComplexityInfo;

/**
 * Strategy interface for a sorting algorithm. Each algorithm only knows how to sort a
 * {@link SortArray} and describe itself; drawing and playback live elsewhere.
 */
public interface SortAlgorithm {

    String getName();

    String getSummary();

    ComplexityInfo getComplexity();

    default String[] getCounterLabels() {
        return new String[]{"Comparisons", "Swaps"};
    }

    /** Sorts in place, smallest / earliest first. */
    void sort(SortArray a);

    /** Shape of the starting line, used to tell which Big-O case a run shows. */
    enum InputShape {
        MIXED, NEARLY_SORTED, REVERSED
    }

    /** Which Big-O row ("best", "average" or "worst") this starting shape is for this algorithm. */
    default String caseFor(InputShape shape) {
        switch (shape) {
            case NEARLY_SORTED:
                return "best";
            case REVERSED:
                return "worst";
            default:
                return "average";
        }
    }
}
