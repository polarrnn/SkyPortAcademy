package com.airport.visualizer.structures.compare;

import com.airport.visualizer.structures.StructurePaint;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;

/** Paints three structures in side-by-side columns, or the final comparison table. */
final class ComparePainter {

    private static final Color[] COLUMN_COLORS = {Theme.PRIMARY, Theme.SUCCESS, Theme.PURPLE};

    private ComparePainter() {
    }

    static void paint(Graphics2D g, CompareState s, int w, int h) {
        double top = StructurePaint.header(g, s.title, s.caption, w) + 6;
        if (s.summary && s.summaryRows != null) {
            paintSummary(g, s, top, w, h);
            return;
        }
        int cols = s.columns.size();
        double gap = 16;
        double colW = (w - 40 - gap * (cols - 1)) / cols;
        double bottom = h - 40;
        for (int c = 0; c < cols; c++) {
            CompareState.Column col = s.columns.get(c);
            Color color = COLUMN_COLORS[c % COLUMN_COLORS.length];
            double x = 20 + c * (colW + gap);
            Draw.roundRect(g, x, top, colW, bottom - top, 14, Theme.SURFACE_ALT, Theme.BORDER, 1f);
            Draw.roundRect(g, x, top, colW, 54, 14, Theme.tint(color, 0.85), null);
            Draw.textInBox(g, col.name, x, top + 2, colW, 28, Theme.bold(15f), Theme.shade(color, 0.3));
            Draw.textInBox(g, col.rule + "  |  " + col.speed, x, top + 28, colW, 22, Theme.regular(12f), Theme.shade(color, 0.2));
            int n = col.items.size();
            double listTop = top + 64;
            double itemH = n == 0 ? 28 : Math.max(18, Math.min(30, (bottom - listTop - 34) / n));
            for (int i = 0; i < n; i++) {
                String item = col.items.get(i);
                boolean active = item.equals(col.active);
                double y = listTop + i * itemH;
                Color fill = active ? (col.rejected ? Theme.V_SWAP : Theme.V_SORTED) : Theme.SURFACE;
                Color fg = active ? Color.WHITE : Theme.TEXT;
                Draw.roundRect(g, x + 12, y, colW - 24, itemH - 4, 8, fill, Theme.BORDER_STRONG, 1f);
                Draw.leftText(g, (i + 1) + ".", x + 20, y + (itemH - 4) / 2, Theme.bold(11f), active ? Color.WHITE : Theme.TEXT_MUTED);
                Draw.textInBox(g, item, x + 40, y, colW - 60, itemH - 4, Theme.bold(12.5f), fg);
            }
            if (n == 0) {
                Draw.centeredText(g, "(empty)", x + colW / 2, listTop + 20, Theme.regular(13f), Theme.TEXT_MUTED);
            }
            if (col.rejected) {
                Draw.textInBox(g, "said NO to the duplicate", x, bottom - 30, colW, 24, Theme.bold(12.5f), Theme.DANGER);
            } else {
                Draw.textInBox(g, "size = " + n, x, bottom - 30, colW, 24, Theme.bold(12.5f), Theme.TEXT_MUTED);
            }
        }
        StructurePaint.legendBottom(g, h, Theme.V_SORTED, "just added (see where it landed)", Theme.V_SWAP, "duplicate rejected / replaced");
    }

    private static void paintSummary(Graphics2D g, CompareState s, double top, int w, int h) {
        String[][] rows = s.summaryRows;
        int cols = rows[0].length;
        double tableW = w - 40;
        double firstW = Math.min(170, tableW * 0.22);
        double colW = (tableW - firstW) / (cols - 1);
        double rowH = Math.max(34, Math.min(56, (h - top - 30) / rows.length));
        for (int r = 0; r < rows.length; r++) {
            double y = top + r * rowH;
            Color bg = r == 0 ? Theme.SIDEBAR : (r % 2 == 0 ? Theme.SURFACE_ALT : Theme.SURFACE);
            Draw.roundRect(g, 20, y, tableW, rowH - 4, 10, bg, r == 0 ? null : Theme.BORDER);
            for (int c = 0; c < cols; c++) {
                double x = c == 0 ? 20 : 20 + firstW + (c - 1) * colW;
                double cw = c == 0 ? firstW : colW;
                Color fg = r == 0 ? (c == 0 ? Theme.ACCENT : Color.WHITE) : (c == 0 ? Theme.TEXT_MUTED : Theme.TEXT);
                Draw.textInBox(g, rows[r][c], x + 4, y, cw - 8, rowH - 4, r == 0 || c == 0 ? Theme.bold(13f) : Theme.regular(13f), fg);
            }
        }
    }
}
