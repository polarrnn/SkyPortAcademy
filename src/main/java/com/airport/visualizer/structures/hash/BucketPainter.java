package com.airport.visualizer.structures.hash;

import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructurePaint;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Draws a {@link BucketState}: numbered bucket "lockers", chains hanging below, and an order strip. */
public final class BucketPainter {

    private BucketPainter() {
    }

    public static void paint(Graphics2D g, BucketState s, int w, int h) {
        double y = StructurePaint.header(g, s.getTitle(), s.getCalc(), w);
        if (!s.getNote().isEmpty()) {
            Color c = s.getMark() == Mark.NONE ? Theme.INFO : s.getMark().color();
            y += StructurePaint.callout(g, s.getNote(), 20, y, Math.min(w - 40, 640), c) + 10;
        }
        int cap = Math.max(1, s.getCapacity());
        double left = 20;
        double colW = (w - 40.0) / cap;
        double cellW = Math.max(18, colW - 6);
        double stripTop = h - 34 - 74;
        double bucketY = y + 18;
        double bucketH = 30;

        // index labels + bucket boxes
        for (int i = 0; i < cap; i++) {
            double x = left + i * colW + (colW - cellW) / 2;
            boolean active = i == s.getActiveBucket();
            Draw.centeredText(g, String.valueOf(i), x + cellW / 2, y + 7, Theme.bold(11f),
                    active ? Theme.PRIMARY : Theme.TEXT_MUTED);
            List<String> chain = s.getBuckets().get(i);
            Color fill = chain.isEmpty() ? Theme.SURFACE_ALT : Theme.PRIMARY_SOFT;
            Color border = active ? s.getMark() == Mark.NONE ? Theme.PRIMARY : s.getMark().color() : Theme.BORDER_STRONG;
            Draw.roundRect(g, x, bucketY, cellW, bucketH, 8, fill, border, active ? 3f : 1.4f);
            if (chain.isEmpty()) {
                Draw.textInBox(g, "-", x, bucketY, cellW, bucketH, Theme.regular(12f), Theme.TEXT_MUTED);
            } else {
                Draw.textInBox(g, chain.size() + "", x, bucketY, cellW, bucketH, Theme.bold(12f), Theme.PRIMARY_DARK);
            }
        }

        // chains
        double chainTop = bucketY + bucketH + 10;
        int longest = 1;
        for (List<String> chain : s.getBuckets()) {
            longest = Math.max(longest, chain.size());
        }
        double avail = stripTop - chainTop - 8;
        double slot = Math.max(22, Math.min(44, avail / longest));
        double boxH = Math.max(18, slot - 12);
        for (int i = 0; i < cap; i++) {
            List<String> chain = s.getBuckets().get(i);
            double x = left + i * colW + (colW - cellW) / 2;
            double prevBottom = bucketY + bucketH;
            for (int j = 0; j < chain.size(); j++) {
                double by = chainTop + j * slot;
                if (by + boxH > stripTop) {
                    Draw.centeredText(g, "+" + (chain.size() - j) + " more", x + cellW / 2, by + 6, Theme.bold(10.5f), Theme.TEXT_MUTED);
                    break;
                }
                String key = chain.get(j);
                Color fill = Theme.SURFACE;
                Color border = Theme.BORDER_STRONG;
                Color fg = Theme.TEXT;
                if (i == s.getActiveBucket()) {
                    if (key.equals(s.getActiveKey()) && s.getMark() != Mark.NONE) {
                        fill = s.getMark().color();
                        border = Theme.shade(fill, 0.25);
                        fg = Color.WHITE;
                    } else if (j == s.getCompareIndex()) {
                        fill = Theme.V_COMPARE;
                        border = Theme.shade(fill, 0.25);
                        fg = Color.WHITE;
                    } else if (s.getCompareIndex() > j) {
                        fill = Theme.tint(Theme.V_COMPARE, 0.75);
                    }
                }
                Draw.arrow(g, x + cellW / 2, prevBottom + 1, x + cellW / 2, by - 2, Theme.TEXT_MUTED, 1.4f);
                Draw.roundRect(g, x, by, cellW, boxH, 8, fill, border, 1.6f);
                Draw.textInBox(g, key, x, by, cellW, boxH, Theme.bold(12f), fg);
                prevBottom = by + boxH;
            }
        }

        // order strip
        double sy = StructurePaint.strip(g, s.getOrderLabel(), s.getOrder(), 20, stripTop, w - 40,
                s.getOrderDone(), s.getOrderActive(), s.getOrderActive() >= 0 ? s.getMark() : Mark.NONE, s.isOrderLinked());
        if (!s.getRejected().isEmpty() && sy < h - 30) {
            Draw.leftText(g, "Rejected duplicates: " + String.join(", ", s.getRejected()), w / 2.0, stripTop + 14,
                    Theme.bold(12.5f), Theme.DANGER);
        }
        StructurePaint.legendBottom(g, h, Theme.PRIMARY_SOFT, "bucket with items", Theme.V_ACTIVE, "looking",
                Theme.V_COMPARE, "comparing", Theme.V_SORTED, "added", Theme.V_FOUND, "found",
                Theme.V_SWAP, "removed / rejected", Theme.V_VISITED, "visited");
    }
}
