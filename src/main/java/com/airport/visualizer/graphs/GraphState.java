package com.airport.visualizer.graphs;

import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.model.Flight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Snapshot of one graph-walk step: airport colors, the edge being checked, the queue/stack and the visit order. */
public final class GraphState implements FlightTableState {

    /** How an airport is colored. */
    public enum NodeMark {
        NONE, WAITING, CURRENT, VISITED, PATH
    }

    private final String caption;
    private final Map<String, NodeMark> marks;
    private final String edgeFrom;
    private final String edgeTo;
    private final Set<String> treeEdges;
    private final Set<String> pathEdges;
    private final boolean queue;
    private final List<String> frontier;
    private final List<String> order;
    private final String start;
    private final String target;
    private final List<Flight> flights;
    private final Set<String> highlighted;

    public GraphState(String caption, Map<String, NodeMark> marks, String edgeFrom, String edgeTo,
            Set<String> treeEdges, Set<String> pathEdges, boolean queue, List<String> frontier, List<String> order,
            String start, String target, List<Flight> flights, Set<String> highlighted) {
        this.caption = caption;
        this.marks = Collections.unmodifiableMap(new HashMap<>(marks));
        this.edgeFrom = edgeFrom;
        this.edgeTo = edgeTo;
        this.treeEdges = Collections.unmodifiableSet(new LinkedHashSet<>(treeEdges));
        this.pathEdges = Collections.unmodifiableSet(new LinkedHashSet<>(pathEdges));
        this.queue = queue;
        this.frontier = Collections.unmodifiableList(new ArrayList<>(frontier));
        this.order = Collections.unmodifiableList(new ArrayList<>(order));
        this.start = start;
        this.target = target;
        this.flights = flights;
        this.highlighted = Collections.unmodifiableSet(new LinkedHashSet<>(highlighted));
    }

    /** Key for an undirected edge, the same for A-B and B-A. */
    public static String edgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "-" + b : b + "-" + a;
    }

    public String getCaption() {
        return caption;
    }

    public NodeMark markOf(String code) {
        NodeMark m = marks.get(code);
        return m == null ? NodeMark.NONE : m;
    }

    public String getEdgeFrom() {
        return edgeFrom;
    }

    public String getEdgeTo() {
        return edgeTo;
    }

    public Set<String> getTreeEdges() {
        return treeEdges;
    }

    public Set<String> getPathEdges() {
        return pathEdges;
    }

    public boolean isQueue() {
        return queue;
    }

    /** Queue: front first. Stack: bottom first. */
    public List<String> getFrontier() {
        return frontier;
    }

    public List<String> getOrder() {
        return order;
    }

    public String getStart() {
        return start;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public List<Flight> getTableFlights() {
        return flights;
    }

    @Override
    public Set<String> getHighlightedFlightNumbers() {
        return highlighted;
    }
}
