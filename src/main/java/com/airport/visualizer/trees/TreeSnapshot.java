package com.airport.visualizer.trees;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable picture of a binary tree at one moment: the nodes, their links and how each node
 * should be colored. Built from a live {@link BstNode} tree or from a heap array.
 */
public final class TreeSnapshot {

    /** How a node is drawn. Every mark has a legend entry in {@link TreePainter}. */
    public enum Mark {
        NONE, CURRENT, PATH, VISITED, FOUND, SPECIAL, NEW, REMOVED
    }

    /** One drawn node. left / right are child ids, or -1 when empty. */
    public static final class NodeView {

        final int id;
        final String label;
        final String sublabel;
        final int left;
        final int right;

        NodeView(int id, String label, String sublabel, int left, int right) {
            this.id = id;
            this.label = label;
            this.sublabel = sublabel;
            this.left = left;
            this.right = right;
        }
    }

    public static final int GHOST_ID = -2;

    final Map<Integer, NodeView> nodes;
    final int rootId;
    final Map<Integer, Mark> marks;
    final int ghostParent;
    final boolean ghostLeft;
    final String ghostLabel;
    final boolean showIndex;

    private TreeSnapshot(Builder b) {
        this.nodes = Collections.unmodifiableMap(new LinkedHashMap<>(b.nodes));
        this.rootId = b.rootId;
        this.marks = Collections.unmodifiableMap(new HashMap<>(b.marks));
        this.ghostParent = b.ghostParent;
        this.ghostLeft = b.ghostLeft;
        this.ghostLabel = b.ghostLabel;
        this.showIndex = b.showIndex;
    }

    public boolean isEmpty() {
        return rootId < 0 && ghostParent == -1;
    }

    public int size() {
        return nodes.size();
    }

    public Mark markOf(int id) {
        Mark m = marks.get(id);
        return m == null ? Mark.NONE : m;
    }

    /** Snapshot of a linked tree (ids = node ids). */
    public static Builder from(BstNode root) {
        Builder b = new Builder();
        b.rootId = root == null ? -1 : root.id;
        copy(root, b.nodes);
        return b;
    }

    private static void copy(BstNode n, Map<Integer, NodeView> out) {
        if (n == null) {
            return;
        }
        out.put(n.id, new NodeView(n.id, n.item.getLabel(), n.item.getSublabel(),
                n.left == null ? -1 : n.left.id, n.right == null ? -1 : n.right.id));
        copy(n.left, out);
        copy(n.right, out);
    }

    /** Snapshot of a heap array: node i has children 2i+1 and 2i+2 (ids = array indexes). */
    public static Builder fromArray(List<TreeItem> items) {
        Builder b = new Builder();
        int n = items.size();
        b.rootId = n == 0 ? -1 : 0;
        b.showIndex = true;
        for (int i = 0; i < n; i++) {
            int l = 2 * i + 1;
            int r = 2 * i + 2;
            TreeItem it = items.get(i);
            b.nodes.put(i, new NodeView(i, it.getLabel(), it.getSublabel(), l < n ? l : -1, r < n ? r : -1));
        }
        return b;
    }

    /** Collects marks before freezing the snapshot. */
    public static final class Builder {

        private final Map<Integer, NodeView> nodes = new LinkedHashMap<>();
        private final Map<Integer, Mark> marks = new HashMap<>();
        private int rootId = -1;
        private int ghostParent = -1;
        private boolean ghostLeft;
        private String ghostLabel = "?";
        private boolean showIndex;

        public Builder mark(int id, Mark m) {
            if (id >= 0 || id == GHOST_ID) {
                marks.put(id, m);
            }
            return this;
        }

        public Builder markAll(Iterable<Integer> ids, Mark m) {
            for (Integer id : ids) {
                mark(id, m);
            }
            return this;
        }

        /** Shows a dashed "empty seat" where a missing value would have been. Parent -1 = empty tree. */
        public Builder ghost(int parentId, boolean asLeftChild, String label) {
            this.ghostParent = parentId < 0 ? -3 : parentId;
            this.ghostLeft = asLeftChild;
            this.ghostLabel = label;
            return this;
        }

        public TreeSnapshot build() {
            return new TreeSnapshot(this);
        }
    }
}
