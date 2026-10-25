package com.airport.visualizer.structures.lists;

import com.airport.visualizer.structures.Mark;
import com.airport.visualizer.structures.StructurePaint;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Draws a {@link RowState}: numbered boxes (wrapping onto rows if needed), pointers and move arrows. */
public final class RowPainter {

    private RowPainter() {
    }

    public static void paint(Graphics2D g, RowState s, int w, int h) {
        double y = StructurePaint.header(g, s.getTitle(), s.getCaption(), w);
        if (!s.getNote().isEmpty()) {
            Color c = s.getNoteMark() == Mark.NONE ? Theme.INFO : s.getNoteMark().color();
            y += StructurePaint.callout(g, s.getNote(), 20, y, Math.min(w - 40, 680), c) + 10;
        }
        List<String> cells = s.getCells();
        int n = Math.max(1, cells.size());
        double avail = w - 40.0;
        double gap = 8;
        double cellW = Math.max(62, Math.min(118, (avail - gap * (n - 1)) / n));
        int perRow = Math.max(1, (int) ((avail + gap) / (cellW + gap)));
        int rows = (cells.size() + perRow - 1) / Math.max(1, perRow);
        double cellH = 50;
        double rowH = cellH + 58;
        double bottomReserve = s.getExtra().isEmpty() && s.getExtraLabel().isEmpty() ? 40 : 110;
        double maxRowH = (h - y - bottomReserve) / Math.max(1, rows);
        if (maxRowH < rowH) {
            rowH = Math.max(70, maxRowH);
            cellH = Math.max(32, rowH - 46);
        }
        double top = y + 34;
        double[] cx = new double[cells.size()];
        double[] cy = new double[cells.size()];
        for (int i = 0; i < cells.size(); i++) {
            int row = i / perRow;
            int col = i % perRow;
            double x = 20 + col * (cellW + gap);
            double by = top + row * rowH;
            cx[i] = x + cellW / 2;
            cy[i] = by;
            String label = cells.get(i);
            Mark m = s.getMarks().get(i);
            Draw.centeredText(g, "[" + i + "]", x + cellW / 2, by - 10, Theme.bold(11f), Theme.TEXT_MUTED);
            if (label == null) {
                Draw.roundRect(g, x, by, cellW, cellH, 10, Theme.SURFACE_ALT, Theme.BORDER, 1.2f);
                Draw.textInBox(g, "empty", x, by, cellW, cellH, Theme.regular(11.5f), Theme.TEXT_MUTED);
                continue;
            }
            Color fill = Theme.PRIMARY_SOFT;
            Color border = Theme.tint(Theme.PRIMARY, 0.4);
            Color fg = Theme.TEXT;
            if (m != null && m != Mark.NONE) {
                fill = m.color();
                border = Theme.shade(fill, 0.25);
                fg = m.textColor();
            }
            String sub = i < s.getSubs().size() ? s.getSubs().get(i) : null;
            StructurePaint.flightCard(g, x, by, cellW, cellH, label, sub, fill, border, fg);
            String p = s.getPointers().get(i);
            if (p != null) {
                Draw.arrow(g, x + cellW / 2, by + cellH + 22, x + cellW / 2, by + cellH + 4, Theme.PURPLE, 2f);
                Draw.centeredText(g, p, x + cellW / 2, by + cellH + 32, Theme.bold(11.5f), Theme.PURPLE);
            }
        }
        int from = s.getArrowFrom();
        int to = s.getArrowTo();
        if (from >= 0 && to >= 0 && from < cells.size() && to < cells.size() && from != to) {
            Draw.curvedArrow(g, cx[from], cy[from] - 2, cx[to], cy[to] - 2, from < to ? -26 : 26, Theme.V_PIVOT, 2.4f);
        }
        if (!s.getExtraLabel().isEmpty()) {
            StructurePaint.strip(g, s.getExtraLabel(), s.getExtra(), 20, h - 34 - 70, w - 40, 0, -1, Mark.NONE, false);
        }
        StructurePaint.legendBottom(g, h, Theme.PRIMARY_SOFT, "stored", Theme.V_ACTIVE, "looking",
                Theme.V_COMPARE, "comparing", Theme.V_PIVOT, "moving", Theme.V_SORTED, "added",
                Theme.V_FOUND, "found", Theme.V_SWAP, "removed", Theme.V_VISITED, "visited");
    }
}
