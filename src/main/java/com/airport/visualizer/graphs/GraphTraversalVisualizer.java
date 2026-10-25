package com.airport.visualizer.graphs;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.graphs.GraphState.NodeMark;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.trees.SidePanels;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Breadth-first and depth-first search over an airline route map, plus "fewest stops" with BFS. */
public class GraphTraversalVisualizer extends AbstractVisualizer<GraphState> {

    static final String BFS_MNL = "BFS from Manila";
    static final String DFS_MNL = "DFS from Manila";
    static final String BFS_SYD = "BFS from Sydney";
    static final String DFS_DXB = "DFS from Dubai";
    static final String PATH = "Fewest stops Manila -> Los Angeles (BFS path)";

    private final RouteGraph graph = RouteGraph.sample();

    public GraphTraversalVisualizer() {
        super("Graph Traversals (BFS & DFS)", Category.TREE,
                "Airports are dots and routes are lines. Explore the map wave by wave (BFS) or as deep as possible first (DFS).",
                GraphState.class);
    }

    @Override
    public List<String> getScenarios() {
        return Arrays.asList(BFS_MNL, DFS_MNL, BFS_SYD, DFS_DXB, PATH);
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Edges checked", "Airports visited"};
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(V + E)")
                .row("bfs", "Breadth-first search (BFS)", "O(V + E)",
                        "Every airport (V) joins the waiting line once and every route (E) is checked from both ends once.")
                .row("dfs", "Depth-first search (DFS)", "O(V + E)",
                        "Same total work as BFS, but it uses a stack: go as deep as you can, then back up.")
                .row("path", "Fewest stops (BFS + breadcrumbs)", "O(V + E)",
                        "BFS reaches places in order of how many flights away they are, so the first time we reach a city is the shortest trip.")
                .row("neighbours", "List an airport's routes", "O(degree)",
                        "With an adjacency list we only read that airport's own list of routes.")
                .whenToUse("BFS: fewest stops / fewest moves (route planning, friend-of-a-friend). DFS: explore everything, "
                        + "check if two airports are connected at all, or find loops - it uses less memory on wide maps.")
                .avoidWhen("If routes have different lengths or prices, fewest STOPS is not the cheapest trip - use Dijkstra's "
                        + "algorithm instead. BFS can need lots of memory when the map is very wide.")
                .build();
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        StepRecorder rec = new StepRecorder();
        Walker w = new Walker(rec);
        String s = scenario == null ? BFS_MNL : scenario;
        String summary;
        switch (s) {
            case DFS_MNL:
                w.dfsAll("MNL");
                summary = "DFS from MNL visited: " + String.join(" -> ", w.order);
                break;
            case BFS_SYD:
                w.bfs("SYD", null);
                summary = "BFS from SYD visited: " + String.join(" -> ", w.order);
                break;
            case DFS_DXB:
                w.dfsAll("DXB");
                summary = "DFS from DXB visited: " + String.join(" -> ", w.order);
                break;
            case PATH:
                List<String> p = w.bfs("MNL", "LAX");
                summary = "Fewest stops: " + String.join(" -> ", p) + " (" + (p.size() - 1) + " flights).";
                break;
            default:
                w.bfs("MNL", null);
                summary = "BFS from MNL visited: " + String.join(" -> ", w.order);
                break;
        }
        boolean dfs = s.startsWith("DFS");
        String start = s.contains("Sydney") ? "SYD" : s.contains("Dubai") ? "DXB" : "MNL";
        long nanos = time(() -> plain(start, dfs));
        return Timeline.of(rec, nanos, summary);
    }

    /** Plain BFS / DFS without recording, for the "real run" time and for checks. */
    public List<String> plain(String start, boolean dfs) {
        List<String> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (dfs) {
            plainDfs(start, seen, out);
        } else {
            Deque<String> q = new ArrayDeque<>();
            q.add(start);
            seen.add(start);
            while (!q.isEmpty()) {
                String u = q.poll();
                out.add(u);
                for (String v : graph.neighbours(u)) {
                    if (seen.add(v)) {
                        q.add(v);
                    }
                }
            }
        }
        return out;
    }

    private void plainDfs(String u, Set<String> seen, List<String> out) {
        seen.add(u);
        out.add(u);
        for (String v : graph.neighbours(u)) {
            if (!seen.contains(v)) {
                plainDfs(v, seen, out);
            }
        }
    }

    private static long time(Runnable r) {
        r.run();
        long t0 = System.nanoTime();
        r.run();
        return System.nanoTime() - t0;
    }

    /** Records the steps of one walk. */
    private final class Walker {

        final StepRecorder rec;
        final Map<String, NodeMark> marks = new HashMap<>();
        final Set<String> treeEdges = new LinkedHashSet<>();
        final Set<String> pathEdges = new LinkedHashSet<>();
        final List<String> frontier = new ArrayList<>();
        final List<String> order = new ArrayList<>();
        String caption = "";
        boolean queue;
        String start;
        String target;

        Walker(StepRecorder rec) {
            this.rec = rec;
        }

        String name(String code) {
            Airport a = graph.get(code);
            return a == null ? code : a.getCode() + " (" + a.getCity() + ")";
        }

        void snap(String title, String narration, String current, String from, String to) {
            Map<String, NodeMark> m = new HashMap<>(marks);
            if (current != null) {
                m.put(current, NodeMark.CURRENT);
            }
            Set<String> hi = new LinkedHashSet<>();
            String focus = current != null ? current : to;
            if (focus != null && graph.get(focus) != null) {
                for (Flight f : FlightData.sample()) {
                    if (f.getDestination().equals(graph.get(focus).getCity())) {
                        hi.add(f.getFlightNumber());
                    }
                }
            }
            rec.record(title, narration, new GraphState(caption, m, from, to, treeEdges, pathEdges, queue, frontier,
                    order, start, target, FlightData.sample(), hi));
        }

        List<String> bfs(String s, String goal) {
            queue = true;
            start = s;
            target = goal;
            rec.setOperation(goal == null ? "bfs" : "path");
            caption = goal == null ? "Breadth-first search from " + s : "Fewest stops from " + s + " to " + goal;
            Map<String, String> parent = new HashMap<>();
            snap(goal == null ? "Explore in waves" : "Find the trip with the fewest stops",
                    "BFS explores like ripples in a pond: first every airport ONE flight away, then every airport TWO flights away, "
                    + "and so on. It keeps a waiting line (a queue) of airports to visit.", null, null, null);
            frontier.add(s);
            marks.put(s, NodeMark.WAITING);
            snap(s + " joins the line", "We start at " + name(s) + ". We mark it as SEEN and put it in the queue.", null, null, null);
            boolean found = false;
            while (!frontier.isEmpty() && !found) {
                String u = frontier.remove(0);
                order.add(u);
                rec.swap();
                marks.put(u, NodeMark.VISITED);
                snap("Visit " + u, name(u) + " was at the FRONT of the line, so we visit it now. Let's look at all its routes: "
                        + String.join(", ", graph.neighbours(u)) + ".", u, null, null);
                for (String v : graph.neighbours(u)) {
                    rec.compare();
                    if (marks.containsKey(v)) {
                        snap("Route " + u + " - " + v, v + " was already seen, so we skip it. No airport joins the line twice!", u, u, v);
                        continue;
                    }
                    marks.put(v, NodeMark.WAITING);
                    parent.put(v, u);
                    treeEdges.add(GraphState.edgeKey(u, v));
                    frontier.add(v);
                    snap("New airport: " + v, name(v) + " is new! We mark it as seen and it waits at the BACK of the line. "
                            + "We also leave a breadcrumb: we reached " + v + " from " + u + ".", u, u, v);
                    if (v.equals(goal)) {
                        found = true;
                        snap("We reached " + v + "!", "BFS reaches airports in order of how many flights away they are, "
                                + "so this is already the trip with the fewest stops.", v, u, v);
                        break;
                    }
                }
            }
            List<String> path = new ArrayList<>();
            if (goal != null) {
                if (!found) {
                    snap("No route", "There is no way to fly from " + s + " to " + goal + ".", null, null, null);
                    return path;
                }
                String c = goal;
                while (c != null) {
                    path.add(c);
                    c = parent.get(c);
                }
                Collections.reverse(path);
                for (int i = 0; i < path.size(); i++) {
                    marks.put(path.get(i), NodeMark.PATH);
                    if (i > 0) {
                        pathEdges.add(GraphState.edgeKey(path.get(i - 1), path.get(i)));
                    }
                }
                frontier.clear();
                snap("Follow the breadcrumbs", "Going backwards from " + goal + " along the breadcrumbs gives the trip: "
                        + String.join(" -> ", path) + ". That is only " + (path.size() - 1) + " flights!", null, null, null);
            } else {
                snap("Everywhere explored!", "The line is empty, so every airport we can reach has been visited. Order: "
                        + String.join(", ", order) + ".", null, null, null);
            }
            return path;
        }

        void dfsAll(String s) {
            queue = false;
            start = s;
            rec.setOperation("dfs");
            caption = "Depth-first search from " + s;
            snap("Go deep first", "DFS is like exploring a maze: keep walking to a NEW place as long as you can. "
                    + "When you get stuck, walk back one step and try another way. The to-do stack remembers the way back.",
                    null, null, null);
            dfs(s, null);
            snap("Everywhere explored!", "We are back at the start with nothing new to visit. Order: "
                    + String.join(", ", order) + ".", null, null, null);
        }

        void dfs(String u, String from) {
            frontier.add(u);
            order.add(u);
            rec.swap();
            marks.put(u, NodeMark.VISITED);
            if (from != null) {
                treeEdges.add(GraphState.edgeKey(from, u));
            }
            snap("Land at " + u, (from == null ? "We start at " + name(u) : "We flew from " + from + " to " + name(u))
                    + ". It goes on top of the stack and we mark it as visited.", u, from, from == null ? null : u);
            for (String v : graph.neighbours(u)) {
                rec.compare();
                if (marks.containsKey(v)) {
                    snap("Route " + u + " - " + v, v + " is already visited, so we do not go there again.", u, u, v);
                    continue;
                }
                snap("Go deeper: " + v, v + " is new, so we fly there right away - before checking " + u + "'s other routes.", u, u, v);
                dfs(v, u);
                snap("Back at " + u, "We came back to " + u + " and continue with its next route.", u, null, null);
            }
            frontier.remove(frontier.size() - 1);
            snap(u + " is finished", "All of " + u + "'s routes lead to visited airports, so we take it off the stack"
                    + (frontier.isEmpty() ? "." : " and back up to " + frontier.get(frontier.size() - 1) + "."),
                    frontier.isEmpty() ? null : frontier.get(frontier.size() - 1), null, null);
        }
    }

    // ------------------------------------------------------------------ painting

    @Override
    protected void paintState(Graphics2D g, GraphState s, int w, int h) {
        Draw.heading(g, s.getCaption(), 18, 22);
        double side = Math.max(220, Math.min(290, w * 0.27));
        double mapX = 14;
        double mapY = 40;
        double mapW = w - side - 44;
        double orderH = 76;
        double mapH = h - mapY - orderH - 46;

        Map<String, double[]> xy = new HashMap<>();
        for (Airport a : graph.getAirports()) {
            xy.put(a.getCode(), new double[]{mapX + 34 + a.getX() * (mapW - 68), mapY + 26 + a.getY() * (mapH - 60)});
        }
        // routes
        for (String[] r : graph.getRoutes()) {
            double[] a = xy.get(r[0]);
            double[] b = xy.get(r[1]);
            String key = GraphState.edgeKey(r[0], r[1]);
            Color c = Theme.BORDER_STRONG;
            float width = 2f;
            if (s.getPathEdges().contains(key)) {
                c = Theme.V_FOUND;
                width = 6f;
            } else if (s.getTreeEdges().contains(key)) {
                c = Theme.V_ACTIVE;
                width = 3.5f;
            }
            Draw.line(g, a[0], a[1], b[0], b[1], c, width);
        }
        if (s.getEdgeFrom() != null && s.getEdgeTo() != null) {
            double[] a = xy.get(s.getEdgeFrom());
            double[] b = xy.get(s.getEdgeTo());
            if (a != null && b != null) {
                Draw.arrow(g, a[0], a[1], b[0] - (b[0] - a[0]) * 0.12, b[1] - (b[1] - a[1]) * 0.12, Theme.V_COMPARE, 4f);
            }
        }
        // airports
        double r = Math.max(16, Math.min(24, mapW / 34));
        for (Airport a : graph.getAirports()) {
            double[] p = xy.get(a.getCode());
            NodeMark m = s.markOf(a.getCode());
            Color fill;
            Color border;
            Color text = Theme.TEXT;
            switch (m) {
                case CURRENT:
                    fill = Theme.V_COMPARE;
                    border = Theme.shade(Theme.V_COMPARE, 0.3);
                    break;
                case WAITING:
                    fill = Theme.PRIMARY_SOFT;
                    border = Theme.V_ACTIVE;
                    break;
                case VISITED:
                    fill = Theme.tint(Theme.V_VISITED, 0.7);
                    border = Theme.V_VISITED;
                    break;
                case PATH:
                    fill = Theme.V_FOUND;
                    border = Theme.shade(Theme.V_FOUND, 0.3);
                    text = Color.WHITE;
                    break;
                default:
                    fill = Theme.SURFACE;
                    border = Theme.V_NEUTRAL;
                    break;
            }
            Draw.node(g, p[0], p[1], r, fill, border, a.getCode(), text, Theme.bold((float) Math.min(13, r * 0.55)));
            Draw.centeredText(g, a.getCity(), p[0], p[1] + r + 10, Theme.regular(11f), Theme.TEXT_MUTED);
            if (a.getCode().equals(s.getStart())) {
                Draw.chip(g, "START", p[0] + r + 4, p[1] - 12, Theme.ACCENT, Theme.TEXT);
            } else if (a.getCode().equals(s.getTarget())) {
                Draw.chip(g, "GOAL", p[0] - 22, p[1] - r - 28, Theme.DANGER, Color.WHITE);
            }
        }

        // visit order strip
        double oy = h - orderH - 38;
        double top = SidePanels.card(g, mapX, oy, mapW, orderH, "Visit order (" + s.getOrder().size() + " airports)", Theme.V_VISITED);
        SidePanels.chips(g, s.getOrder(), mapX + 14, top, mapW - 28, oy + orderH - 6, Theme.tint(Theme.V_VISITED, 0.75),
                Theme.TEXT, true, Theme.V_VISITED);

        // side panel: adjacency list + queue/stack
        double px = w - side - 16;
        List<Airport> all = graph.getAirports();
        double lineH = 17;
        double adjH = 40 + all.size() * lineH;
        double at = SidePanels.card(g, px, 14, side, adjH, "Adjacency list (who connects to who)", Theme.PURPLE);
        String focus = null;
        for (Airport a : all) {
            if (s.markOf(a.getCode()) == NodeMark.CURRENT) {
                focus = a.getCode();
            }
        }
        g.setFont(Theme.regular(12f));
        for (int i = 0; i < all.size(); i++) {
            String code = all.get(i).getCode();
            String line = code + ": " + String.join(", ", graph.neighbours(code));
            boolean hot = code.equals(focus);
            if (hot) {
                Draw.roundRect(g, px + 8, at - 2 + i * lineH, side - 16, lineH, 6, Theme.ACCENT_SOFT, null);
            }
            g.setFont(hot ? Theme.bold(12f) : Theme.regular(12f));
            Draw.leftText(g, Draw.ellipsize(g, line, (int) side - 30), px + 14, at + 6 + i * lineH,
                    hot ? Theme.bold(12f) : Theme.regular(12f), hot ? Theme.TEXT : Theme.TEXT_MUTED);
        }
        double sy = 14 + adjH + 10;
        double sh = h - 44 - sy;
        if (sh > 70) {
            String title = s.isQueue() ? "Queue (waiting line)" : "Stack (way back home)";
            double st = SidePanels.card(g, px, sy, side, sh, title, Theme.V_ACTIVE);
            List<String> items = new ArrayList<>(s.getFrontier());
            if (s.isQueue()) {
                Collections.reverse(items);
            }
            SidePanels.stackColumn(g, items, px + 12, st, side - 24, sh - 44, Theme.PRIMARY_SOFT, s.isQueue() ? "FRONT: " : "TOP: ");
        }

        Draw.legend(g, 18, h - 26, Theme.V_COMPARE, "Here now", Theme.PRIMARY_SOFT, "Waiting / seen",
                Theme.tint(Theme.V_VISITED, 0.7), "Visited", Theme.V_ACTIVE, "Route used", Theme.V_FOUND, "Best trip");
    }
}
