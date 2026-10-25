package com.airport.visualizer.structures.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Shape of a perfectly balanced BST built from n sorted keys (root = middle key).
 * Used to draw TreeMap / TreeSet and to show the comparison path a lookup takes.
 */
public final class BalancedShape {

    private final int[] depth;
    private final int[] parent;
    private final int root;

    public BalancedShape(int n) {
        depth = new int[n];
        parent = new int[n];
        root = n == 0 ? -1 : build(0, n - 1, -1, 0);
    }

    private int build(int lo, int hi, int par, int d) {
        if (lo > hi) {
            return -1;
        }
        int mid = (lo + hi) >>> 1;
        depth[mid] = d;
        parent[mid] = par;
        build(lo, mid - 1, mid, d + 1);
        build(mid + 1, hi, mid, d + 1);
        return mid;
    }

    public int getRoot() {
        return root;
    }

    public int depth(int i) {
        return depth[i];
    }

    public int parent(int i) {
        return parent[i];
    }

    public int maxDepth() {
        int m = 0;
        for (int d : depth) {
            m = Math.max(m, d);
        }
        return m;
    }

    /**
     * Indices visited when searching for {@code key} among sorted {@code keys}.
     * The last index is the match, or the node under which a new key would hang.
     */
    public static <K extends Comparable<K>> List<Integer> searchPath(List<K> keys, K key) {
        List<Integer> path = new ArrayList<>();
        int lo = 0;
        int hi = keys.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            path.add(mid);
            int c = key.compareTo(keys.get(mid));
            if (c == 0) {
                break;
            } else if (c < 0) {
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        return path;
    }
}
