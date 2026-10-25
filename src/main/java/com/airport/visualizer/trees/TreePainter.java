package com.airport.visualizer.trees;

import com.airport.visualizer.trees.TreeSnapshot.Mark;
import com.airport.visualizer.trees.TreeSnapshot.NodeView;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws any {@link TreeSnapshot}: x position = in-order rank (so the tree reads left-to-right
 * in sorted order), y position = depth. Everything scales to the space it is given.
 */
public final class TreePainter {

    private TreePainter() {
    }

    public static Color fill(Mark m) {
        switch (m) {
            case CURRENT:
                return Theme.V_COMPARE;
            case PATH:
                return Theme.PRIMARY_SOFT;
            case VISITED:
                return Theme.tint(Theme.V_VISITED, 0.72);
            case FOUND:
                return Theme.V_FOUND;
            case SPECIAL:
                return Theme.V_PIVOT;
            case NEW:
                return Theme.SUCCESS_SOFT;
            case REMOVED:
                return Theme.V_SWAP;
            default:
                return Theme.SURFACE;
        }
    }

    public static Color border(Mark m) {
        switch (m) {
            case CURRENT:
                return Theme.shade(Theme.V_COMPARE, 0.25);
            case PATH:
                return Theme.V_ACTIVE;
            case VISITED:
                return Theme.shade(Theme.V_VISITED, 0.1);
            case FOUND:
                return Theme.shade(Theme.V_FOUND, 0.3);
            case SPECIAL:
                return Theme.shade(Theme.V_PIVOT, 0.3);
            case NEW:
                return Theme.V_SORTED;
            case REMOVED:
                return Theme.shade(Theme.V_SWAP, 0.3);
            default:
                return Theme.V_NEUTRAL;
        }
    }

    public static Color text(Mark m) {
        return m == Mark.FOUND || m == Mark.SPECIAL || m == Mark.REMOVED ? Color.WHITE : Theme.TEXT;
    }

    public static String meaning(Mark m) {
        switch (m) {
            case CURRENT:
                return "Looking here now";
            case PATH:
                return "Path we walked";
            case VISITED:
                return "Already visited";
            case FOUND:
                return "Found / done";
            case SPECIAL:
                return "Helper node";
            case NEW:
                return "Just added";
            case REMOVED:
                return "Leaving the tree";
            default:
                return "Waiting";
        }
    }

    /** Legend for the given marks (colors are always explained in words). */
    public static void legend(Graphics2D g, double x, double y, Mark... marks) {
        Object[] pairs = new Object[marks.length * 2];
        for (int i = 0; i < marks.length; i++) {
            pairs[i * 2] = fill(marks[i]) == Theme.SURFACE ? Theme.V_DIM : fill(marks[i]);
            pairs[i * 2 + 1] = meaning(marks[i]);
        }
        Draw.legend(g, x, y, pairs);
    }

    public static void paint(Graphics2D g, TreeSnapshot t, double x, double y, double w, double h) {
        if (t == null || t.isEmpty()) {
            Draw.centeredText(g, "The tree is empty - add something to plant it!", x + w / 2, y + h / 2,
                    Theme.regular(15f), Theme.TEXT_MUTED);
            return;
        }
        Map<Integer, double[]> pos = new HashMap<>();   // id -> {rank, depth}
        List<int[]> edges = new ArrayList<>();          // {parent, child}
        int[] counter = {0};
        int[] maxDepth = {0};
        if (t.ghostParent == -3) {
            pos.put(TreeSnapshot.GHOST_ID, new double[]{0, 0});
            counter[0] = 1;
        } else {
            layout(t, t.rootId, 0, pos, edges, counter, maxDepth);
        }
        int n = Math.max(1, counter[0]);
        int levels = maxDepth[0] + 1;
        double colW = w / n;
        double top = y + (t.showIndex ? 16 : 4);
        double r = Math.max(12, Math.min(25, Math.min(colW * 0.44, (h - 30) / (levels * 2.4))));
        double avail = h - (top - y) - 2 * r - 22;
        double gap = levels > 1 ? Math.max(r * 2.2, Math.min(92, avail / (levels - 1))) : 0;
        if (levels > 1 && gap * (levels - 1) > avail) {
            gap = Math.max(r * 2 + 4, avail / (levels - 1));
        }
        Map<Integer, double[]> xy = new HashMap<>();
        for (Map.Entry<Integer, double[]> e : pos.entrySet()) {
            double cx = x + (e.getValue()[0] + 0.5) * colW;
            double cy = top + r + e.getValue()[1] * gap;
            xy.put(e.getKey(), new double[]{cx, cy});
        }
        // edges first so the circles sit on top
        for (int[] edge : edges) {
            double[] a = xy.get(edge[0]);
            double[] b = xy.get(edge[1]);
            if (a == null || b == null) {
                continue;
            }
            if (edge[1] == TreeSnapshot.GHOST_ID) {
                Draw.dashedLine(g, a[0], a[1], b[0], b[1], Theme.DANGER, 2f);
                continue;
            }
            boolean hot = isHot(t.markOf(edge[0])) && isHot(t.markOf(edge[1]));
            Draw.line(g, a[0], a[1], b[0], b[1], hot ? Theme.V_ACTIVE : Theme.BORDER_STRONG, hot ? 3.5f : 2f);
        }
        Font sub = new Font(r);
        for (Map.Entry<Integer, double[]> e : xy.entrySet()) {
            int id = e.getKey();
            double cx = e.getValue()[0];
            double cy = e.getValue()[1];
            if (id == TreeSnapshot.GHOST_ID) {
                Stroke old = g.getStroke();
                g.setColor(Theme.DANGER_SOFT);
                g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 5f}, 0f));
                g.setColor(Theme.DANGER);
                g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                g.setStroke(old);
                Draw.textInBox(g, t.ghostLabel, cx - r, cy - r, 2 * r, 2 * r, Theme.bold(sub.label), Theme.DANGER);
                Draw.centeredText(g, "empty spot", cx, cy + r + 10, Theme.regular(sub.small), Theme.DANGER);
                continue;
            }
            NodeView nv = t.nodes.get(id);
            Mark m = t.markOf(id);
            Draw.node(g, cx, cy, r, fill(m), border(m), nv.label, text(m), Theme.bold(sub.label));
            if (nv.sublabel != null && !nv.sublabel.isEmpty() && !nv.sublabel.equals(nv.label)) {
                Draw.centeredText(g, nv.sublabel, cx, cy + r + 10, Theme.bold(sub.small), Theme.TEXT_MUTED);
            }
            if (t.showIndex) {
                Draw.centeredText(g, "[" + id + "]", cx, cy - r - 9, Theme.bold(sub.small), Theme.PURPLE);
            }
        }
    }

    private static boolean isHot(Mark m) {
        return m == Mark.PATH || m == Mark.CURRENT || m == Mark.FOUND || m == Mark.SPECIAL;
    }

    private static void layout(TreeSnapshot t, int id, int depth, Map<Integer, double[]> pos,
            List<int[]> edges, int[] counter, int[] maxDepth) {
        NodeView nv = t.nodes.get(id);
        if (nv == null) {
            return;
        }
        maxDepth[0] = Math.max(maxDepth[0], depth);
        boolean ghostHere = t.ghostParent == id;
        if (nv.left >= 0) {
            edges.add(new int[]{id, nv.left});
            layout(t, nv.left, depth + 1, pos, edges, counter, maxDepth);
        } else if (ghostHere && t.ghostLeft) {
            addGhost(id, depth + 1, pos, edges, counter, maxDepth);
        }
        pos.put(id, new double[]{counter[0]++, depth});
        if (nv.right >= 0) {
            edges.add(new int[]{id, nv.right});
            layout(t, nv.right, depth + 1, pos, edges, counter, maxDepth);
        } else if (ghostHere && !t.ghostLeft) {
            addGhost(id, depth + 1, pos, edges, counter, maxDepth);
        }
    }

    private static void addGhost(int parent, int depth, Map<Integer, double[]> pos, List<int[]> edges,
            int[] counter, int[] maxDepth) {
        edges.add(new int[]{parent, TreeSnapshot.GHOST_ID});
        pos.put(TreeSnapshot.GHOST_ID, new double[]{counter[0]++, depth});
        maxDepth[0] = Math.max(maxDepth[0], depth);
    }

    /** Font sizes that follow the node radius. */
    private static final class Font {

        final float label;
        final float small;

        Font(double r) {
            label = (float) Math.max(9.5, Math.min(13.5, r * 0.56));
            small = (float) Math.max(9.5, Math.min(11.5, r * 0.48));
        }
    }
}
