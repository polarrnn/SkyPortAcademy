package com.airport.visualizer.trees;

import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Painting helpers for the info boxes beside a tree or graph (stack, queue, output, comparison trail).
 * Every helper keeps text inside its box: long lists show the newest items and a "+N earlier" chip.
 */
public final class SidePanels {

    private static final double CHIP_H = 26;
    private static final double GAP = 6;

    private SidePanels() {
    }

    /** Draws a titled card and returns the y where its content starts. */
    public static double card(Graphics2D g, double x, double y, double w, double h, String title, Color accent) {
        Draw.roundRect(g, x, y, w, h, 14, Theme.SURFACE_ALT, Theme.BORDER, 1f);
        Draw.roundRect(g, x + 10, y + 12, 4, 14, 4, accent, null);
        g.setFont(Theme.bold(12.5f));
        Draw.leftText(g, Draw.ellipsize(g, title, (int) w - 30), x + 20, y + 19, Theme.bold(12.5f), Theme.TEXT);
        return y + 34;
    }

    /** Flowing chips. highlightLast paints the newest chip in the accent color. */
    public static double chips(Graphics2D g, List<String> items, double x, double y, double w, double maxY,
            Color fill, Color fg, boolean highlightLast, Color accent) {
        if (items == null || items.isEmpty()) {
            Draw.leftText(g, "(empty)", x, y + CHIP_H / 2, Theme.regular(12.5f), Theme.TEXT_MUTED);
            return y + CHIP_H;
        }
        Font f = Theme.bold(12f);
        FontMetrics fm = g.getFontMetrics(f);
        for (int skip = 0; skip < items.size(); skip++) {
            List<String> shown = new ArrayList<>();
            if (skip > 0) {
                shown.add("+" + skip + " earlier");
            }
            shown.addAll(items.subList(skip, items.size()));
            double end = flow(fm, shown, x, y, w, null);
            if (end <= maxY || skip == items.size() - 1) {
                double cx = x;
                double cy = y;
                for (int i = 0; i < shown.size(); i++) {
                    String s = shown.get(i);
                    double cw = Math.min(w, fm.stringWidth(s) + 18);
                    if (cx + cw > x + w && cx > x) {
                        cx = x;
                        cy += CHIP_H + GAP;
                    }
                    boolean more = skip > 0 && i == 0;
                    boolean last = highlightLast && i == shown.size() - 1;
                    Color bg = more ? Theme.V_DIM : last ? accent : fill;
                    Color tc = more ? Theme.TEXT_MUTED : last ? Color.WHITE : fg;
                    Draw.roundRect(g, cx, cy, cw, CHIP_H, CHIP_H, bg, null);
                    g.setFont(f);
                    Draw.centeredText(g, Draw.ellipsize(g, s, (int) cw - 10), cx + cw / 2, cy + CHIP_H / 2, f, tc);
                    cx += cw + GAP;
                }
                return cy + CHIP_H;
            }
        }
        return y + CHIP_H;
    }

    private static double flow(FontMetrics fm, List<String> items, double x, double y, double w, Object unused) {
        double cx = x;
        double cy = y;
        for (String s : items) {
            double cw = Math.min(w, fm.stringWidth(s) + 18);
            if (cx + cw > x + w && cx > x) {
                cx = x;
                cy += CHIP_H + GAP;
            }
            cx += cw + GAP;
        }
        return cy + CHIP_H;
    }

    /**
     * A row of boxes, e.g. a queue (front on the left) or a stack drawn sideways.
     * If there are too many, the first ones are squeezed into a "+N" box.
     */
    public static void boxRow(Graphics2D g, List<String> items, double x, double y, double w, double boxH,
            Color fill, Color border, String leftTag, String rightTag) {
        double tagW = 0;
        Font tagFont = Theme.bold(10.5f);
        if (leftTag != null) {
            tagW = g.getFontMetrics(tagFont).stringWidth(leftTag) + 8;
            Draw.leftText(g, leftTag, x, y + boxH / 2, tagFont, Theme.TEXT_MUTED);
        }
        double rightW = rightTag == null ? 0 : g.getFontMetrics(tagFont).stringWidth(rightTag) + 8;
        double bx = x + tagW;
        double avail = w - tagW - rightW;
        if (items == null || items.isEmpty()) {
            Draw.roundRect(g, bx, y, Math.max(60, avail), boxH, 10, Theme.SURFACE, Theme.BORDER, 1f);
            Draw.centeredText(g, "(empty)", bx + Math.max(60, avail) / 2, y + boxH / 2, Theme.regular(12f), Theme.TEXT_MUTED);
            return;
        }
        double minBox = 58;
        int fit = Math.max(1, (int) ((avail + 4) / (minBox + 4)));
        List<String> shown = new ArrayList<>(items);
        if (shown.size() > fit) {
            int hidden = shown.size() - fit + 1;
            List<String> tail = new ArrayList<>(shown.subList(0, fit - 1));
            tail.add("+" + hidden + " more");
            shown = tail;
        }
        double bw = Math.min(90, (avail - 4 * (shown.size() - 1)) / shown.size());
        for (int i = 0; i < shown.size(); i++) {
            double cx = bx + i * (bw + 4);
            boolean more = shown.get(i).startsWith("+");
            Draw.cell(g, cx, y, bw, boxH, more ? Theme.V_DIM : (i == 0 ? fill : Theme.SURFACE), border,
                    shown.get(i), Theme.TEXT, Theme.bold(12f));
        }
        if (rightTag != null) {
            double endX = bx + shown.size() * (bw + 4);
            Draw.leftText(g, rightTag, endX + 2, y + boxH / 2, tagFont, Theme.TEXT_MUTED);
        }
    }

    /** Vertical stack of boxes: index 0 = bottom, last = top (drawn at the top). */
    public static void stackColumn(Graphics2D g, List<String> items, double x, double y, double w, double maxH,
            Color topFill) {
        stackColumn(g, items, x, y, w, maxH, topFill, "TOP: ");
    }

    /** Same as above with a custom tag for the top box (e.g. "FRONT: " for a queue). */
    public static void stackColumn(Graphics2D g, List<String> items, double x, double y, double w, double maxH,
            Color topFill, String topTag) {
        if (items == null || items.isEmpty()) {
            Draw.cell(g, x, y, w, 28, Theme.SURFACE, Theme.BORDER, "(empty)", Theme.TEXT_MUTED, Theme.regular(12f));
            return;
        }
        double boxH = 28;
        int fit = Math.max(1, (int) ((maxH + 4) / (boxH + 4)));
        List<String> topFirst = new ArrayList<>();
        for (int i = items.size() - 1; i >= 0; i--) {
            topFirst.add(items.get(i));
        }
        if (topFirst.size() > fit) {
            int hidden = topFirst.size() - fit + 1;
            topFirst = new ArrayList<>(topFirst.subList(0, fit - 1));
            topFirst.add("+" + hidden + " below");
        }
        for (int i = 0; i < topFirst.size(); i++) {
            String s = topFirst.get(i);
            boolean more = s.startsWith("+");
            Color fill = more ? Theme.V_DIM : i == 0 ? topFill : Theme.SURFACE;
            Draw.cell(g, x, y + i * (boxH + 4), w, boxH, fill, i == 0 ? Theme.shade(topFill, 0.2) : Theme.BORDER_STRONG,
                    (i == 0 && !more ? topTag : "") + s, Theme.TEXT, Theme.bold(12f));
        }
    }

    /** Wrapped text lines, newest last and bold; older lines scroll away if there is no room. */
    public static void lines(Graphics2D g, List<String> lines, double x, double y, double w, double maxY) {
        if (lines == null || lines.isEmpty()) {
            Draw.leftText(g, "(nothing yet)", x, y + 10, Theme.regular(12.5f), Theme.TEXT_MUTED);
            return;
        }
        Font normal = Theme.regular(12.5f);
        Font bold = Theme.bold(12.5f);
        int lineH = g.getFontMetrics(normal).getHeight();
        List<String[]> wrapped = new ArrayList<>();   // {text, "b" or ""}
        for (int i = 0; i < lines.size(); i++) {
            boolean last = i == lines.size() - 1;
            for (String part : Draw.wrap(g, lines.get(i), last ? bold : normal, (int) w - 12)) {
                wrapped.add(new String[]{part, last ? "b" : ""});
            }
        }
        int fit = Math.max(1, (int) ((maxY - y) / lineH));
        int start = Math.max(0, wrapped.size() - fit);
        double cy = y;
        for (int i = start; i < wrapped.size(); i++) {
            boolean b = "b".equals(wrapped.get(i)[1]);
            g.setFont(b ? bold : normal);
            g.setColor(b ? Theme.TEXT : Theme.TEXT_MUTED);
            g.drawString(wrapped.get(i)[0], (float) x, (float) (cy + g.getFontMetrics().getAscent()));
            cy += lineH;
        }
    }
}
