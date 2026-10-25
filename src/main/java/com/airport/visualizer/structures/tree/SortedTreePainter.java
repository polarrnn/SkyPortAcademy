package com.airport.visualizer.structures.tree;

import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructurePaint;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;

/** Draws a {@link SortedTreeState} as a balanced tree with the sorted order strip below. */
public final class SortedTreePainter {

    private SortedTreePainter() {
    }

    public static void paint(Graphics2D g, SortedTreeState s, int w, int h) {
        double y = StructurePaint.header(g, s.getTitle(), "Smaller keys go LEFT, bigger keys go RIGHT. Reading left to right gives sorted order.", w);
        if (!s.getNote().isEmpty()) {
            Color c = s.getMark() == Mark.NONE ? Theme.INFO : s.getMark().color();
            y += StructurePaint.callout(g, s.getNote(), 20, y, Math.min(w - 40, 640), c) + 8;
        }
        int n = s.getLabels().size();
        double stripTop = h - 34 - 74;
        if (n == 0) {
            Draw.centeredText(g, "(empty tree)", w / 2.0, (y + stripTop) / 2, Theme.regular(15f), Theme.TEXT_MUTED);
        } else {
            BalancedShape shape = new BalancedShape(n);
            boolean twoLines = !s.getSubLabels().isEmpty();
            double colW = (w - 40.0) / n;
            double nodeW = Math.max(30, Math.min(120, colW - 6));
            double nodeH = twoLines ? 40 : 32;
            int levels = shape.maxDepth() + 1;
            double levelH = Math.max(nodeH + 6, Math.min(78, (stripTop - y - 10 - nodeH) / Math.max(1, levels - 1)));
            double[] cx = new double[n];
            double[] cy = new double[n];
            for (int i = 0; i < n; i++) {
                cx[i] = 20 + colW * (i + 0.5);
                cy[i] = y + 6 + nodeH / 2 + shape.depth(i) * levelH;
            }
            for (int i = 0; i < n; i++) {
                int p = shape.parent(i);
                if (p >= 0) {
                    boolean onPath = s.getPath().contains(i) && s.getPath().contains(p);
                    Draw.line(g, cx[p], cy[p] + nodeH / 2, cx[i], cy[i] - nodeH / 2,
                            onPath ? Theme.V_COMPARE : Theme.BORDER_STRONG, onPath ? 3f : 1.6f);
                }
            }
            for (int i = 0; i < n; i++) {
                Color fill = Theme.SURFACE;
                Color border = Theme.BORDER_STRONG;
                Color fg = Theme.TEXT;
                if (i < s.getVisited()) {
                    fill = Theme.tint(Theme.V_VISITED, 0.75);
                    border = Theme.V_VISITED;
                }
                if (s.getPath().contains(i)) {
                    fill = Theme.tint(Theme.V_COMPARE, 0.7);
                    border = Theme.V_COMPARE;
                }
                if (i == s.getActive() && s.getMark() != Mark.NONE) {
                    fill = s.getMark().color();
                    border = Theme.shade(fill, 0.25);
                    fg = Color.WHITE;
                }
                if (i == shape.getRoot()) {
                    Draw.centeredText(g, "ROOT", cx[i], cy[i] - nodeH / 2 - 8, Theme.bold(10f), Theme.TEXT_MUTED);
                }
                StructurePaint.flightCard(g, cx[i] - nodeW / 2, cy[i] - nodeH / 2, nodeW, nodeH, s.getLabels().get(i),
                        twoLines ? s.getSubLabels().get(i) : null, fill, border, fg);
            }
        }
        double sy = StructurePaint.strip(g, "Sorted order (what iteration gives you)", s.getLabels(), 20, stripTop, w - 40,
                s.getVisited(), s.getActive(), s.getMark(), false);
        if (!s.getRejected().isEmpty() && sy < h) {
            Draw.leftText(g, "Rejected duplicates: " + String.join(", ", s.getRejected()), w / 2.0, stripTop + 14,
                    Theme.bold(12.5f), Theme.DANGER);
        }
        StructurePaint.legendBottom(g, h, Theme.V_COMPARE, "compare path", Theme.V_SORTED, "added",
                Theme.V_FOUND, "found", Theme.V_PIVOT, "value replaced", Theme.V_SWAP, "removed / rejected",
                Theme.V_VISITED, "visited");
    }
}
