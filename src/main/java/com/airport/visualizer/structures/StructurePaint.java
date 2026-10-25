package com.airport.visualizer.structures;

import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

/** Painting pieces shared by all structure demos (header, callout, item strips, legend). */
public final class StructurePaint {

    private StructurePaint() {
    }

    /** Title + caption at the top-left. Returns the y where content can start. */
    public static double header(Graphics2D g, String title, String caption, int w) {
        Draw.leftText(g, title, 20, 22, Theme.bold(16f), Theme.TEXT);
        if (caption != null && !caption.isEmpty()) {
            g.setFont(Theme.regular(13f));
            Draw.leftText(g, Draw.ellipsize(g, caption, w - 40), 20, 44, Theme.regular(13f), Theme.TEXT_MUTED);
            return 60;
        }
        return 40;
    }

    /** Legend along the bottom edge. */
    public static void legendBottom(Graphics2D g, int h, Object... pairs) {
        Draw.legend(g, 20, h - 24, pairs);
    }

    /** Rounded "speech bubble" with wrapped text. Returns its height. */
    public static double callout(Graphics2D g, String text, double x, double y, double maxW, Color color) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        Font f = Theme.bold(13f);
        List<String> lines = Draw.wrap(g, text, f, (int) maxW - 24);
        double h = 14 + lines.size() * 18;
        double textW = 0;
        for (String l : lines) {
            textW = Math.max(textW, g.getFontMetrics(f).stringWidth(l));
        }
        double boxW = Math.min(maxW, textW + 26);
        Draw.roundRect(g, x, y, boxW, h, 12, Theme.tint(color, 0.85), Theme.tint(color, 0.35), 1.2f);
        g.setFont(f);
        g.setColor(Theme.shade(color, 0.45));
        double ty = y + 7 + g.getFontMetrics().getAscent();
        for (String l : lines) {
            g.drawString(l, (float) (x + 13), (float) ty);
            ty += 18;
        }
        return h;
    }

    /**
     * Row of chips that wraps onto more lines if needed (never clips). Items before {@code doneCount}
     * are drawn "visited"; {@code active} is drawn in the mark color.
     *
     * @param arrows draw small arrows between items (for linked orders)
     * @return the y just below the strip
     */
    public static double strip(Graphics2D g, String label, List<String> items, double x, double y, double w,
            int doneCount, int active, Mark activeMark, boolean arrows) {
        Draw.leftText(g, label, x, y + 14, Theme.bold(12.5f), Theme.TEXT_MUTED);
        double cx = x;
        double cy = y + 30;
        double chipH = 28;
        Font f = Theme.bold(12.5f);
        if (items.isEmpty()) {
            Draw.leftText(g, "(empty)", cx, cy + chipH / 2, Theme.regular(12.5f), Theme.TEXT_MUTED);
            return cy + chipH + 6;
        }
        for (int i = 0; i < items.size(); i++) {
            String text = items.get(i);
            double chipW = Math.min(150, g.getFontMetrics(f).stringWidth(text) + 22);
            double extra = arrows && i < items.size() - 1 ? 26 : 8;
            if (cx + chipW > x + w && cx > x) {
                cx = x;
                cy += chipH + 10;
            }
            Color fill = Theme.SURFACE_ALT;
            Color border = Theme.BORDER_STRONG;
            Color fg = Theme.TEXT;
            if (i < doneCount) {
                fill = Theme.tint(Theme.V_VISITED, 0.8);
                border = Theme.V_VISITED;
            }
            if (i == active && activeMark != null && activeMark != Mark.NONE) {
                fill = activeMark.color();
                border = Theme.shade(activeMark.color(), 0.25);
                fg = Color.WHITE;
            }
            Draw.roundRect(g, cx, cy, chipW, chipH, 12, fill, border, 1.6f);
            Draw.textInBox(g, text, cx, cy, chipW, chipH, f, fg);
            if (arrows && i < items.size() - 1 && cx + chipW + extra + 40 <= x + w) {
                Draw.arrow(g, cx + chipW + 3, cy + chipH / 2, cx + chipW + extra - 3, cy + chipH / 2, Theme.TEXT_MUTED, 1.6f);
            }
            cx += chipW + extra;
        }
        return cy + chipH + 6;
    }

    /** A labelled flight card: big flight number on top and a small line underneath. */
    public static void flightCard(Graphics2D g, double x, double y, double w, double h, String top, String bottom,
            Color fill, Color border, Color fg) {
        Draw.roundRect(g, x, y, w, h, 12, fill, border, 2f);
        if (bottom == null || bottom.isEmpty() || h < 36) {
            Draw.textInBox(g, top, x, y, w, h, Theme.bold(13f), fg);
        } else {
            Draw.textInBox(g, top, x, y + 2, w, h * 0.55, Theme.bold(13f), fg);
            Draw.textInBox(g, bottom, x, y + h * 0.48, w, h * 0.45, Theme.regular(11f),
                    fg == Color.WHITE ? Color.WHITE : Theme.TEXT_MUTED);
        }
    }
}
