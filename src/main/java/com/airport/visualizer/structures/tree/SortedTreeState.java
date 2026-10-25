package com.airport.visualizer.structures.tree;

import com.airport.visualizer.model.Flight;
import com.airport.visualizer.structures.BaseState;
import com.airport.visualizer.structures.Mark;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Snapshot of a sorted (tree-based) structure. The keys are stored in sorted order; the painter
 * draws them as a balanced binary search tree, which is what TreeMap's red-black tree keeps close to.
 */
public final class SortedTreeState extends BaseState {

    private final String title;
    private final String note;
    private final List<String> labels;
    private final List<String> subLabels;
    private final Set<Integer> path;
    private final int active;
    private final Mark mark;
    private final int visited;
    private final List<String> rejected;

    public SortedTreeState(String title, String note, List<String> labels, List<String> subLabels,
            Collection<Integer> path, int active, Mark mark, int visited, List<String> rejected,
            List<Flight> table, Collection<String> highlights) {
        super(table, highlights);
        this.title = title;
        this.note = note == null ? "" : note;
        this.labels = Collections.unmodifiableList(new ArrayList<>(labels));
        this.subLabels = Collections.unmodifiableList(new ArrayList<>(subLabels));
        this.path = Collections.unmodifiableSet(new LinkedHashSet<>(path));
        this.active = active;
        this.mark = mark;
        this.visited = visited;
        this.rejected = Collections.unmodifiableList(new ArrayList<>(rejected));
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<String> getSubLabels() {
        return subLabels;
    }

    public Set<Integer> getPath() {
        return path;
    }

    public int getActive() {
        return active;
    }

    public Mark getMark() {
        return mark;
    }

    public int getVisited() {
        return visited;
    }

    public List<String> getRejected() {
        return rejected;
    }
}
