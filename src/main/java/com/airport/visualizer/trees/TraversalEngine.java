package com.airport.visualizer.trees;

import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.trees.TreeSnapshot.Mark;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Walks a binary tree in pre-, in-, post- or level-order and narrates every move.
 * Recursive orders show the call stack; level-order shows the queue.
 */
public final class TraversalEngine {

    /** The four classic visiting orders. */
    public enum Order {
        PRE("Pre-order (root, left, right)", "preorder",
                "Pre-order rule: say hello to the captain FIRST, then visit the whole left team, then the right team."),
        IN("In-order (left, root, right)", "inorder",
                "In-order rule: left team first, then the captain, then the right team. In a binary search tree this reads everything in sorted order!"),
        POST("Post-order (left, right, root)", "postorder",
                "Post-order rule: both teams first and the captain LAST - like kids leaving the room before the parent switches off the light."),
        LEVEL("Level-order (breadth first)", "levelorder",
                "Level-order rule: row by row, like reading a book - the top row, then the next row, left to right.");

        private final String label;
        private final String key;
        private final String rule;

        Order(String label, String key, String rule) {
            this.label = label;
            this.key = key;
            this.rule = rule;
        }

        public String getLabel() {
            return label;
        }

        public String getKey() {
            return key;
        }

        public String getRule() {
            return rule;
        }

        public static Order fromLabel(String label) {
            for (Order o : values()) {
                if (o.label.equals(label)) {
                    return o;
                }
            }
            return PRE;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final BstNode root;
    private final Words words;
    private final List<Flight> tableFlights;
    private StepRecorder rec;
    private String caption;
    private final List<String> output = new ArrayList<>();
    private final Set<Integer> written = new LinkedHashSet<>();
    private final List<BstNode> stack = new ArrayList<>();

    public TraversalEngine(BstNode root, Words words, List<Flight> tableFlights) {
        this.root = root;
        this.words = words;
        this.tableFlights = tableFlights == null ? Collections.<Flight>emptyList() : tableFlights;
    }

    /** Plain traversal, no recording - returns labels in visiting order (used for timing and checks). */
    public static List<String> plain(BstNode root, Order order) {
        List<String> out = new ArrayList<>();
        if (order == Order.LEVEL) {
            Deque<BstNode> q = new ArrayDeque<>();
            if (root != null) {
                q.add(root);
            }
            while (!q.isEmpty()) {
                BstNode n = q.poll();
                out.add(n.item.getLabel());
                if (n.left != null) {
                    q.add(n.left);
                }
                if (n.right != null) {
                    q.add(n.right);
                }
            }
        } else {
            plainRec(root, order, out);
        }
        return out;
    }

    private static void plainRec(BstNode n, Order o, List<String> out) {
        if (n == null) {
            return;
        }
        if (o == Order.PRE) {
            out.add(n.item.getLabel());
        }
        plainRec(n.left, o, out);
        if (o == Order.IN) {
            out.add(n.item.getLabel());
        }
        plainRec(n.right, o, out);
        if (o == Order.POST) {
            out.add(n.item.getLabel());
        }
    }

    public List<String> run(Order order, StepRecorder recorder) {
        this.rec = recorder;
        this.caption = order.getLabel();
        output.clear();
        written.clear();
        stack.clear();
        rec.setOperation(order.getKey());
        if (root == null) {
            rec.record("Nothing to visit", "The tree is empty, so there is nobody to visit.", state(null, Collections.<BstNode>emptyList(), false));
            return output;
        }
        rec.record("Let's visit every " + words.thing(), order.getRule(), state(null, Collections.<BstNode>emptyList(), order == Order.LEVEL));
        if (order == Order.LEVEL) {
            level();
        } else {
            visit(root, order);
        }
        rec.record("Everyone visited!", "We visited all " + output.size() + " " + words.things() + " exactly once. "
                + "The order was: " + String.join(", ", output) + ".", state(null, Collections.<BstNode>emptyList(), order == Order.LEVEL));
        return output;
    }

    private void visit(BstNode n, Order order) {
        stack.add(n);
        rec.swap();
        rec.record("Arrive at " + n.item.describe(), stack.size() == 1 ? "We start at the root, the captain at the top. "
                + "We put it on our to-do stack so we remember where we are."
                : "We walked down to " + n.item.describe() + " and put it on top of the to-do stack.", state(n, stack, false));
        if (order == Order.PRE) {
            write(n, "Pre-order writes the captain down FIRST, before looking at any children.");
        }
        if (n.left != null) {
            rec.record("Go left from " + n.item.getLabel(), "Next we explore the LEFT side of " + n.item.getLabel() + ".", state(n, stack, false));
            visit(n.left, order);
            rec.swap();
            rec.record("Back at " + n.item.getLabel(), "The left side of " + n.item.getLabel() + " is finished, so we climb back up.",
                    state(n, stack, false));
        } else {
            rec.record("No left child", n.item.getLabel() + " has nobody on its left.", state(n, stack, false));
        }
        if (order == Order.IN) {
            write(n, "In-order writes the captain down after the whole left side and before the right side.");
        }
        if (n.right != null) {
            rec.record("Go right from " + n.item.getLabel(), "Now we explore the RIGHT side of " + n.item.getLabel() + ".", state(n, stack, false));
            visit(n.right, order);
            rec.swap();
            rec.record("Back at " + n.item.getLabel(), "The right side of " + n.item.getLabel() + " is finished too.", state(n, stack, false));
        }
        if (order == Order.POST) {
            write(n, "Both sides are done, so post-order finally writes down " + n.item.getLabel() + ".");
        }
        stack.remove(stack.size() - 1);
        rec.record("Done with " + n.item.getLabel(), "We take " + n.item.getLabel() + " off the to-do stack"
                + (stack.isEmpty() ? " - the stack is empty, so we are finished!" : " and go back to " + stack.get(stack.size() - 1).item.getLabel() + "."),
                state(stack.isEmpty() ? null : stack.get(stack.size() - 1), stack, false));
    }

    private void write(BstNode n, String why) {
        output.add(n.item.getLabel());
        written.add(n.id);
        rec.compare();
        rec.record("Write down " + n.item.getLabel(), why + " Our list so far: " + String.join(", ", output) + ".",
                stateWithFound(n, stack, false));
    }

    private void level() {
        List<BstNode> queue = new ArrayList<>();
        queue.add(root);
        rec.record("Put the root in line", "We use a QUEUE - a waiting line. First in line is first served. "
                + "The root " + root.item.getLabel() + " joins the line.", state(null, queue, true));
        while (!queue.isEmpty()) {
            BstNode n = queue.remove(0);
            rec.swap();
            output.add(n.item.getLabel());
            written.add(n.id);
            rec.compare();
            rec.record("Serve " + n.item.getLabel(), n.item.describe() + " was at the FRONT of the line, so we visit it and write it down. "
                    + "List so far: " + String.join(", ", output) + ".", stateWithFound(n, queue, true));
            if (n.left != null) {
                queue.add(n.left);
                rec.record(n.left.item.getLabel() + " joins the back", "Its left child " + n.left.item.getLabel()
                        + " waits at the BACK of the line.", state(n, queue, true));
            }
            if (n.right != null) {
                queue.add(n.right);
                rec.record(n.right.item.getLabel() + " joins the back", "Its right child " + n.right.item.getLabel()
                        + " waits at the BACK of the line.", state(n, queue, true));
            }
            if (n.left == null && n.right == null) {
                rec.record("No children", n.item.getLabel() + " has no children, so nobody new joins the line.", state(n, queue, true));
            }
        }
    }

    private TraversalState state(BstNode current, List<BstNode> pending, boolean queue) {
        return build(current, pending, queue, Mark.CURRENT);
    }

    private TraversalState stateWithFound(BstNode current, List<BstNode> pending, boolean queue) {
        return build(current, pending, queue, Mark.FOUND);
    }

    private TraversalState build(BstNode current, List<BstNode> pending, boolean queue, Mark currentMark) {
        TreeSnapshot.Builder b = TreeSnapshot.from(root);
        b.markAll(written, Mark.VISITED);
        List<String> names = new ArrayList<>();
        for (BstNode p : pending) {
            if (!written.contains(p.id) || queue) {
                b.mark(p.id, Mark.PATH);
            }
            names.add(p.item.getLabel());
        }
        Set<String> hi = new LinkedHashSet<>();
        if (current != null) {
            b.mark(current.id, currentMark);
            if (current.item.getFlight() != null) {
                hi.add(current.item.getFlight().getFlightNumber());
            }
        }
        return new TraversalState(b.build(), caption, queue, names, output, tableFlights, hi);
    }
}
