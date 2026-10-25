package com.airport.visualizer.algorithms.search;

import com.airport.visualizer.algorithms.sort.SortItem;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Graphics2D;

/** Draws a {@link SearchState} as a grid of boarding-pass cards. Shared by both search visualizers. */
final class SearchPainter {

    private SearchPainter() {
    }

    static void paint(Graphics2D g, SearchState s, int w, int h) {
        int n = s.size();
        // header: target and board order
        Draw.leftText(g, "Looking for:", 20, 22, Theme.bold(14f), Theme.TEXT_MUTED);
        g.setFont(Theme.bold(15f));
        int tw = g.getFontMetrics().stringWidth(s.getTargetText()) + 28;
        Draw.roundRect(g, 112, 8, tw, 28, 28, Theme.ACCENT, Theme.shade(Theme.ACCENT, 0.15), 1.2f);
        Draw.centeredText(g, s.getTargetText(), 112 + tw / 2.0, 22, Theme.bold(15f), Theme.TEXT);
        String order = s.isSortedBoard() ? "Board is SORTED by " + (s.hasFlights() ? "departure time" : "value")
                : "Board is NOT sorted";
        Draw.leftText(g, order, 112 + tw + 16, 22, Theme.regular(13f), Theme.TEXT_MUTED);
        String ruled = "Ruled out: " + s.ruledOutCount() + " of " + n;
        g.setFont(Theme.bold(12f));
        int cw = g.getFontMetrics().stringWidth(ruled) + 20;
        Draw.chip(g, ruled, w - cw - 18, 10, Theme.TEAL_SOFT, Theme.shade(Theme.TEAL, 0.3));
        Draw.legend(g, 20, 46,
                Theme.SURFACE_ALT, "Not checked yet",
                Theme.V_COMPARE, "Checking now",
                Theme.V_DIM, "Ruled out",
                Theme.V_FOUND, "Found!",
                Theme.PRIMARY_SOFT, "Still possible (search window)");
        if (n == 0) {
            Draw.emptyMessage(g, w, h, "The board is empty.");
            return;
        }

        double top = 82;
        double availW = w - 40;
        int cols = (int) Math.max(1, Math.min(n, Math.floor(availW / 78)));
        int rows = (int) Math.ceil(n / (double) cols);
        double cellSlotW = availW / cols;
        double cellW = Math.min(110, cellSlotW - 10);
        double rowSlotH = Math.min(118, (h - top - 10) / rows);
        double cellH = Math.max(34, rowSlotH - 40);
        boolean window = s.getLo() >= 0 && s.getHi() >= s.getLo();

        for (int i = 0; i < n; i++) {
            int r = i / cols;
            int c = i % cols;
            double x = 20 + c * cellSlotW + (cellSlotW - cellW) / 2.0;
            double y = top + r * rowSlotH + 14;
            SortItem it = s.get(i);
            boolean inWindow = window && i >= s.getLo() && i <= s.getHi();
            Color fill = Theme.SURFACE_ALT;
            Color border = Theme.BORDER_STRONG;
            Color text = Theme.TEXT;
            if (s.isRuledOut(i)) {
                fill = Theme.V_DIM;
                border = Theme.V_DIM;
                text = Theme.TEXT_MUTED;
            } else if (inWindow) {
                fill = Theme.withAlpha(Theme.PRIMARY_SOFT, 230);
                border = Theme.PRIMARY;
            }
            if (i == s.getChecking()) {
                fill = Theme.V_COMPARE;
                border = Theme.shade(Theme.V_COMPARE, 0.25);
                text = Theme.TEXT;
            }
            if (i == s.getFound()) {
                fill = Theme.V_FOUND;
                border = Theme.shade(Theme.V_FOUND, 0.25);
                text = Color.WHITE;
            }
            Draw.centeredText(g, "#" + (i + 1), x + cellW / 2.0, y - 7, Theme.regular(10.5f), Theme.TEXT_MUTED);
            Draw.roundRect(g, x, y, cellW, cellH, 12, fill, border, 1.8f);
            double valueH = it.isFlight() ? cellH * 0.6 : cellH;
            Draw.textInBox(g, it.getValueText(), x, y + (it.isFlight() ? 2 : 0), cellW, valueH, Theme.bold(15f), text);
            if (it.isFlight()) {
                Draw.textInBox(g, it.getLabel(), x, y + cellH * 0.52, cellW, cellH * 0.45, Theme.regular(11.5f), text);
            }
            if (s.isRuledOut(i)) {
                Draw.line(g, x + 8, y + cellH - 8, x + cellW - 8, y + 8, Theme.withAlpha(Theme.TEXT_MUTED, 120), 1.6f);
            }
            String p = pointerLabel(s, i);
            if (p != null) {
                g.setFont(Theme.bold(11f));
                int pw = g.getFontMetrics().stringWidth(p) + 14;
                Draw.roundRect(g, x + cellW / 2.0 - pw / 2.0, y + cellH + 4, pw, 19, 19, Theme.PRIMARY_DARK, null);
                Draw.centeredText(g, p, x + cellW / 2.0, y + cellH + 13.5, Theme.bold(11f), Color.WHITE);
            }
        }
    }

    private static String pointerLabel(SearchState s, int i) {
        StringBuilder sb = new StringBuilder();
        if (s.getLo() >= 0) {
            if (i == s.getLo()) {
                sb.append("lo");
            }
            if (i == s.getMid()) {
                sb.append(sb.length() > 0 ? "," : "").append("mid");
            }
            if (i == s.getHi()) {
                sb.append(sb.length() > 0 ? "," : "").append("hi");
            }
        } else if (i == s.getChecking()) {
            sb.append("check");
        }
        return sb.length() == 0 ? null : sb.toString();
    }
}
