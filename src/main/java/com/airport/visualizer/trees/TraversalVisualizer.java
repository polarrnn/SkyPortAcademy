package com.airport.visualizer.trees;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.trees.TraversalEngine.Order;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** Pre-, in-, post- and level-order walks over a tree of 11 flights (a BST keyed by departure time). */
public class TraversalVisualizer extends AbstractVisualizer<TraversalState> {

    private final List<Flight> flights = FlightData.first(11);

    public TraversalVisualizer() {
        super("Tree Traversals", Category.TREE,
                "Four ways to visit every flight in a tree exactly once - pick an order and watch the walk.",
                TraversalState.class);
    }

    @Override
    public List<String> getScenarios() {
        List<String> s = new ArrayList<>();
        for (Order o : Order.values()) {
            s.add(o.getLabel());
        }
        return s;
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Nodes written", "Moves"};
    }

    @Override
    public List<Flight> getFlights() {
        return flights;
    }

    @Override
    public ComplexityInfo getComplexity() {
        return complexity();
    }

    /** Shared with the Tree Lab. */
    public static ComplexityInfo complexity() {
        return ComplexityInfo.builder("O(n)")
                .row("preorder", "Pre-order (root, left, right)", "O(n) time, O(h) memory",
                        "Every node once. The to-do stack is only as tall as the tree (h). Great for copying a tree.")
                .row("inorder", "In-order (left, root, right)", "O(n) time, O(h) memory",
                        "Every node once. On a binary search tree it lists things in sorted order.")
                .row("postorder", "Post-order (left, right, root)", "O(n) time, O(h) memory",
                        "Every node once, children before parents. Great for deleting or adding up a tree.")
                .row("levelorder", "Level-order (breadth first)", "O(n) time, O(w) memory",
                        "Every node once, row by row. The waiting line can hold a whole row (w = widest row).")
                .whenToUse("Use a traversal whenever you must touch every item: printing the board in time order (in-order), "
                        + "saving a tree (pre-order), freeing memory (post-order) or showing it floor by floor (level-order).")
                .avoidWhen("If you only need ONE item, do not walk the whole tree - a BST search finds it in O(log n).")
                .build();
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        BstEngine bst = new BstEngine(Words.FLIGHTS, flights);
        for (Flight f : flights) {
            bst.insertQuietly(TreeItem.of(f));
        }
        Order order = Order.fromLabel(scenario);
        StepRecorder rec = new StepRecorder();
        List<String> out = new TraversalEngine(bst.getRoot(), Words.FLIGHTS, flights).run(order, rec);
        long nanos = BstVisualizer.time(() -> TraversalEngine.plain(bst.getRoot(), order));
        return Timeline.of(rec, nanos, order.getLabel() + " visits: " + String.join(" -> ", out));
    }

    @Override
    protected void paintState(Graphics2D g, TraversalState s, int w, int h) {
        render(g, s, w, h);
    }

    /** Shared with the Tree Lab. */
    public static void render(Graphics2D g, TraversalState s, int w, int h) {
        Draw.heading(g, s.getCaption(), 18, 22);
        double side = Math.max(190, Math.min(260, w * 0.24));
        double treeW = w - side - 44;
        double outH = 92;
        TreePainter.paint(g, s.getTree(), 14, 44, treeW, h - 44 - outH - 40);

        double oy = h - outH - 34;
        double top = SidePanels.card(g, 14, oy, treeW, outH, "Written down so far (" + s.getOutput().size() + ")", Theme.V_FOUND);
        SidePanels.chips(g, s.getOutput(), 28, top, treeW - 28, oy + outH - 6, Theme.tint(Theme.V_VISITED, 0.75), Theme.TEXT, true, Theme.V_FOUND);

        double px = w - side - 16;
        String title = s.isQueue() ? "Queue (waiting line)" : "Call stack (to-do list)";
        double ctop = SidePanels.card(g, px, 14, side, h - 58, title, Theme.V_ACTIVE);
        List<String> items = new ArrayList<>(s.getStructure());
        if (s.isQueue()) {
            // stackColumn draws the last item on top, so reverse to show the FRONT at the top
            List<String> rev = new ArrayList<>();
            for (int i = items.size() - 1; i >= 0; i--) {
                rev.add(items.get(i));
            }
            items = rev;
        }
        SidePanels.stackColumn(g, items, px + 12, ctop, side - 24, h - 58 - (ctop - 14) - 30, Theme.PRIMARY_SOFT,
                s.isQueue() ? "FRONT: " : "TOP: ");
        Draw.caption(g, s.isQueue() ? "First in line is served first" : "Newest box is on top", px + 12, h - 58);

        Draw.legend(g, 18, h - 26, Theme.V_COMPARE, "Visiting now", Theme.PRIMARY_SOFT, "On the to-do list",
                Theme.tint(Theme.V_VISITED, 0.72), "Already written", Theme.V_FOUND, "Just written");
    }
}
