package com.airport.visualizer.ui.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Small, reusable drawing helpers shared by every visualizer canvas.
 * Keeping them here avoids copy-pasted painting code (DRY) and keeps the look consistent.
 */
public final class Draw {

    private Draw() {
    }

    public static void setup(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static void roundRect(Graphics2D g, double x, double y, double w, double h, double arc,
            Color fill, Color border) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
        if (fill != null) {
            g.setColor(fill);
            g.fill(r);
        }
        if (border != null) {
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(border);
            g.draw(r);
            g.setStroke(old);
        }
    }

    public static void roundRect(Graphics2D g, double x, double y, double w, double h, double arc,
            Color fill, Color border, float borderWidth) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
        if (fill != null) {
            g.setColor(fill);
            g.fill(r);
        }
        if (border != null) {
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(borderWidth));
            g.setColor(border);
            g.draw(r);
            g.setStroke(old);
        }
    }

    /** Draws text centered on (cx, cy). */
    public static void centeredText(Graphics2D g, String text, double cx, double cy, Font font, Color color) {
        if (text == null) {
            return;
        }
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        g.drawString(text, (float) (cx - tw / 2.0), (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }

    /** Draws text left-aligned with its vertical middle at cy. */
    public static void leftText(Graphics2D g, String text, double x, double cy, Font font, Color color) {
        if (text == null) {
            return;
        }
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (float) x, (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }

    /** Text centered inside a box, shrinking the font and adding "..." so it never overflows. */
    public static void textInBox(Graphics2D g, String text, double x, double y, double w, double h,
            Font font, Color color) {
        if (text == null) {
            return;
        }
        Font f = font;
        g.setFont(f);
        while (g.getFontMetrics().stringWidth(text) > w - 6 && f.getSize2D() > 9f) {
            f = f.deriveFont(f.getSize2D() - 1f);
            g.setFont(f);
        }
        centeredText(g, ellipsize(g, text, (int) (w - 6)), x + w / 2.0, y + h / 2.0, f, color);
    }

    public static String ellipsize(Graphics2D g, String text, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String dots = "...";
        int end = text.length();
        while (end > 0 && fm.stringWidth(text.substring(0, end) + dots) > maxWidth) {
            end--;
        }
        return end <= 0 ? dots : text.substring(0, end) + dots;
    }

    /** Box with label (used for array cells, buckets, queue slots...). */
    public static void cell(Graphics2D g, double x, double y, double w, double h, Color fill, Color border,
            String text, Color textColor, Font font) {
        roundRect(g, x, y, w, h, 12, fill, border, 2f);
        textInBox(g, text, x, y, w, h, font, textColor);
    }

    public static void node(Graphics2D g, double cx, double cy, double r, Color fill, Color border,
            String text, Color textColor, Font font) {
        Ellipse2D e = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        g.setColor(Theme.withAlpha(Color.BLACK, 25));
        g.fill(new Ellipse2D.Double(cx - r + 1.5, cy - r + 3, r * 2, r * 2));
        g.setColor(fill);
        g.fill(e);
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(border);
        g.draw(e);
        g.setStroke(old);
        textInBox(g, text, cx - r, cy - r, r * 2, r * 2, font, textColor);
    }

    public static void line(Graphics2D g, double x1, double y1, double x2, double y2, Color color, float width) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);
        g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
        g.setStroke(old);
    }

    public static void dashedLine(Graphics2D g, double x1, double y1, double x2, double y2, Color color, float width) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
                new float[]{6f, 6f}, 0f));
        g.setColor(color);
        g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
        g.setStroke(old);
    }

    /** Straight arrow from (x1,y1) to (x2,y2) with a filled head. */
    public static void arrow(Graphics2D g, double x1, double y1, double x2, double y2, Color color, float width) {
        line(g, x1, y1, x2, y2, color, width);
        arrowHead(g, x1, y1, x2, y2, color, 7 + width * 1.5);
    }

    /** Curved arrow (quadratic) bending by 'bend' pixels; handy for "next" links that hop over boxes. */
    public static void curvedArrow(Graphics2D g, double x1, double y1, double x2, double y2, double bend,
            Color color, float width) {
        double mx = (x1 + x2) / 2.0;
        double my = (y1 + y2) / 2.0;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.max(1, Math.hypot(dx, dy));
        double cx = mx - dy / len * bend;
        double cy = my + dx / len * bend;
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);
        g.draw(new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2));
        g.setStroke(old);
        arrowHead(g, cx, cy, x2, y2, color, 7 + width * 1.5);
    }

    public static void arrowHead(Graphics2D g, double fromX, double fromY, double tipX, double tipY,
            Color color, double size) {
        double angle = Math.atan2(tipY - fromY, tipX - fromX);
        Path2D head = new Path2D.Double();
        head.moveTo(tipX, tipY);
        head.lineTo(tipX - size * Math.cos(angle - Math.PI / 7), tipY - size * Math.sin(angle - Math.PI / 7));
        head.lineTo(tipX - size * Math.cos(angle + Math.PI / 7), tipY - size * Math.sin(angle + Math.PI / 7));
        head.closePath();
        g.setColor(color);
        g.fill(head);
    }

    /** Small pill-shaped label. Returns its width so callers can lay out several in a row. */
    public static int chip(Graphics2D g, String text, double x, double y, Color bg, Color fg) {
        Font f = Theme.bold(12f);
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(text) + 20;
        int h = 24;
        roundRect(g, x, y, w, h, h, bg, null);
        centeredText(g, text, x + w / 2.0, y + h / 2.0, f, fg);
        return w;
    }

    /** Draws a legend row: colored swatches with names. Colors are always explained in words. */
    public static void legend(Graphics2D g, double x, double y, Object... colorLabelPairs) {
        Font f = Theme.regular(12.5f);
        g.setFont(f);
        double cx = x;
        for (int i = 0; i + 1 < colorLabelPairs.length; i += 2) {
            Color c = (Color) colorLabelPairs[i];
            String label = String.valueOf(colorLabelPairs[i + 1]);
            roundRect(g, cx, y, 14, 14, 5, c, Theme.shade(c, 0.2));
            leftText(g, label, cx + 20, y + 7, f, Theme.TEXT_MUTED);
            cx += 20 + g.getFontMetrics().stringWidth(label) + 18;
        }
    }

    /** Big heading painted at the top-left of a canvas. */
    public static void heading(Graphics2D g, String text, double x, double y) {
        leftText(g, text, x, y, Theme.bold(16f), Theme.TEXT);
    }

    public static void caption(Graphics2D g, String text, double x, double y) {
        leftText(g, text, x, y, Theme.regular(13f), Theme.TEXT_MUTED);
    }

    public static void emptyMessage(Graphics2D g, int w, int h, String text) {
        centeredText(g, text, w / 2.0, h / 2.0, Theme.regular(16f), Theme.TEXT_MUTED);
    }

    /** Wraps text into lines that fit maxWidth using the given font. */
    public static List<String> wrap(Graphics2D g, String text, Font font, int maxWidth) {
        return wrap(g.getFontMetrics(font), text, maxWidth);
    }

    /** Wraps text into lines that fit maxWidth (works without a Graphics, e.g. for preferred sizes). */
    public static List<String> wrap(FontMetrics fm, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }

    /** Paints wrapped text starting at (x, topY); returns the y just below the last line. */
    public static double paragraph(Graphics2D g, String text, double x, double topY, int maxWidth,
            Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        double y = topY + fm.getAscent();
        for (String line : wrap(g, text, font, maxWidth)) {
            g.drawString(line, (float) x, (float) y);
            y += fm.getHeight();
        }
        return y - fm.getAscent();
    }

    /** A tiny airplane silhouette, used as a friendly mascot / marker. */
    public static void plane(Graphics2D g, double cx, double cy, double size, Color color, double angleRad) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(cx, cy);
        g2.rotate(angleRad);
        double s = size / 24.0;
        g2.scale(s, s);
        Path2D p = new Path2D.Double();
        p.moveTo(12, 0);          // nose
        p.lineTo(4, -1.6);
        p.lineTo(-1, -10);        // left wing tip
        p.lineTo(-4, -10);
        p.lineTo(-1, -1.6);
        p.lineTo(-8, -1.4);
        p.lineTo(-11, -5);        // tail
        p.lineTo(-12.5, -5);
        p.lineTo(-11, 0);
        p.lineTo(-12.5, 5);
        p.lineTo(-11, 5);
        p.lineTo(-8, 1.4);
        p.lineTo(-1, 1.6);
        p.lineTo(-4, 10);         // right wing tip
        p.lineTo(-1, 10);
        p.lineTo(4, 1.6);
        p.closePath();
        g2.setColor(color);
        g2.fill(p);
        g2.dispose();
    }
}
