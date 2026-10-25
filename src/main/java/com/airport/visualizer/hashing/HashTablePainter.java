package com.airport.visualizer.hashing;

import com.airport.visualizer.hashing.HashSnapshot.Link;
import com.airport.visualizer.hashing.HashSnapshot.Mark;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Draws a {@link HashSnapshot}: the hash calculation strip on top, then either buckets with
 * linked-list chains (separate chaining) or a row of slots with the probe trail (linear probing).
 * Sizes adapt to the canvas so nothing is clipped, from 3 to 13 buckets.
 */
public final class HashTablePainter {

    private static final int M = 16;

    private HashTablePainter() {
    }

    public static Color markColor(Mark m) {
        switch (m) {
            case CHECKING:
                return Theme.V_COMPARE;
            case COLLISION:
                return Theme.PURPLE;
            case FOUND:
                return Theme.V_FOUND;
            case INSERTED:
                return Theme.V_ACTIVE;
            case DELETED:
                return Theme.V_SWAP;
            case MISSING:
                return Theme.TEXT_MUTED;
            case FULL:
                return Theme.DANGER;
            default:
                return Theme.V_ACTIVE;
        }
    }

    public static void paint(Graphics2D g, HashSnapshot s, int w, int h) {
        boolean chaining = s.getMode() == HashSnapshot.Mode.CHAINING;
        // ---- title + load factor
        String load = s.getItemCount() + " stored / " + s.getSize() + (chaining ? " buckets" : " slots")
                + "   load factor " + String.format("%.2f", s.getSize() == 0 ? 0.0 : (double) s.getItemCount() / s.getSize());
        g.setFont(Theme.regular(12.5f));
        int loadW = g.getFontMetrics().stringWidth(load);
        g.setFont(Theme.bold(15f));
        Draw.leftText(g, Draw.ellipsize(g, s.getTitle(), w - 2 * M - loadW - 20), M, 16, Theme.bold(15f), Theme.TEXT);
        Draw.leftText(g, load, w - M - loadW, 16, Theme.regular(12.5f), Theme.TEXT_MUTED);

        // ---- calculation strip (wraps to several lines instead of clipping)
        String line = s.getCalcLine();
        boolean hasCalc = line != null;
        if (!hasCalc) {
            line = "Hash = (sum of the UPPERCASE character codes) % " + s.getSize() + HashFunction.ARROW
                    + (chaining ? "bucket number" : "home slot (then try the next slots if it is taken)");
        }
        Font f = Theme.bold(13f);
        int maxTextW = w - 2 * M - 24;
        List<String> lines = Draw.wrap(g, line, f, maxTextW);
        if (lines.size() > 2) {
            f = Theme.bold(11.5f);
            lines = Draw.wrap(g, line, f, maxTextW);
        }
        if (lines.size() > 3) {
            lines = lines.subList(0, 3);
        }
        int lineH = g.getFontMetrics(f).getHeight();
        int sy = 30;
        int sh = lines.size() * lineH + 10;
        Draw.roundRect(g, M, sy, w - 2 * M, sh, 10, hasCalc ? Theme.ACCENT_SOFT : Theme.SURFACE_ALT,
                hasCalc ? Theme.tint(Theme.ACCENT, 0.35) : Theme.BORDER, 1.2f);
        g.setFont(f);
        g.setColor(hasCalc ? Theme.shade(Theme.ACCENT, 0.62) : Theme.TEXT_MUTED);
        int ty = sy + 5 + g.getFontMetrics().getAscent();
        for (String l : lines) {
            g.drawString(l, M + 12, ty);
            ty += lineH;
        }

        int top = sy + sh + 12;
        int bottom = h - 30;
        if (bottom - top < 40) {
            bottom = top + 40;
        }
        if (chaining) {
            paintChains(g, s, M, top, w - 2 * M, bottom - top);
            Draw.legend(g, M, h - 22, Theme.V_COMPARE, "Checking", Theme.PURPLE, "Collision", Theme.V_FOUND, "Found",
                    Theme.V_ACTIVE, "Stored / new link", Theme.V_SWAP, "Removed / arrow change");
        } else {
            paintSlots(g, s, M, top, w - 2 * M, bottom - top);
            Draw.legend(g, M, h - 22, Theme.V_COMPARE, "Probed", Theme.PURPLE, "Collision", Theme.V_FOUND, "Found",
                    Theme.V_ACTIVE, "Stored", Theme.V_SWAP, "Removed", Theme.SURFACE_ALT, "EMPTY", Theme.BORDER_STRONG, "DELETED marker");
        }
    }

    // ================================================================== chaining

    private static void paintChains(Graphics2D g, HashSnapshot s, double x, double y, double w, double h) {
        int size = s.getSize();
        int cols = 1;
        int rows = size;
        double rowH = h / rows;
        if (rowH < 26 && size > 4) {
            cols = 2;
            rows = (size + 1) / 2;
            rowH = h / rows;
        }
        rowH = Math.min(rowH, 58);
        double colGap = 30;
        double colW = (w - (cols - 1) * colGap) / cols;
        double cellH = clamp(rowH - 7, 15, 40);
        float fs = (float) clamp(cellH * 0.42, 9.5, 14);
        Font nodeFont = Theme.bold(fs);
        Color mc = markColor(s.getMark());

        for (int i = 0; i < size; i++) {
            int col = i / rows;
            int r = i % rows;
            double cx = x + col * (colW + colGap);
            double cy = y + r * rowH + (rowH - cellH) / 2;
            double mid = cy + cellH / 2;
            boolean active = i == s.getActiveIndex();
            double ib = Math.min(54, Math.max(30, cellH * 1.35));

            // bucket index box
            if (active) {
                Draw.roundRect(g, cx - 4, cy - 3, colW + 8, cellH + 6, 12, Theme.withAlpha(mc, 22), null);
            }
            Draw.roundRect(g, cx, cy, ib, cellH, 10, active ? mc : Theme.SIDEBAR, null);
            Draw.textInBox(g, String.valueOf(i), cx, cy, ib, cellH, Theme.bold(fs), Color.WHITE);

            List<String> chain = s.getChain(i);
            int n = chain.size();
            double startX = cx + ib + 24;
            double avail = colW - ib - 24 - 44;
            double gap = 24;
            Color arrowC = Theme.TEXT_MUTED;

            if (n == 0) {
                boolean headLink = active && s.getLink() == Link.NEW_LINK;
                Draw.arrow(g, cx + ib + 3, mid, startX - 4, mid, arrowC, 1.6f);
                Draw.leftText(g, "null", startX, mid, Theme.regular(Math.max(10f, fs - 1)), Theme.TEXT_MUTED);
                if (headLink) {
                    Draw.arrow(g, cx + ib + 3, mid, startX - 4, mid, Theme.V_ACTIVE, 2.6f);
                }
                continue;
            }
            int visible = n;
            double nodeW = Math.min(126, (avail - (n - 1) * gap) / n);
            double minW = 58;
            int first = 0;
            if (nodeW < minW) {
                visible = Math.max(1, (int) ((avail + gap) / (minW + gap)));
                nodeW = Math.min(126, (avail - (visible - 1) * gap) / visible);
                // keep the highlighted node (and the node a new arrow points to) on screen
                int needed = -1;
                if (active) {
                    needed = s.getActiveNode();
                    if (s.getLink() == Link.BYPASS && s.getLinkTo() != HashSnapshot.NULL) {
                        needed = Math.max(needed, s.getLinkTo());
                    }
                }
                if (needed >= visible) {
                    first = Math.min(n - visible, needed - visible + 1);
                }
            }
            nodeW = Math.max(24, nodeW);
            double ptrW = Math.min(16, nodeW * 0.22);

            // head arrow
            boolean headNew = active && s.getLink() == Link.NEW_LINK && s.getLinkFrom() == HashSnapshot.HEAD;
            boolean headOld = active && s.getLink() == Link.BYPASS && s.getLinkFrom() == HashSnapshot.HEAD;
            if (first > 0) {
                Draw.dashedLine(g, cx + ib + 3, mid, startX - 4, mid, arrowC, 1.6f);
                Draw.centeredText(g, "+" + first, (cx + ib + startX) / 2, cy - 2, Theme.bold(10.5f), Theme.TEXT_MUTED);
            } else if (headOld) {
                Draw.dashedLine(g, cx + ib + 3, mid, startX - 4, mid, Theme.tint(Theme.V_SWAP, 0.4), 1.6f);
            } else {
                Draw.arrow(g, cx + ib + 3, mid, startX - 4, mid, headNew ? Theme.V_ACTIVE : arrowC, headNew ? 2.8f : 1.6f);
            }

            int last = first + visible - 1;
            for (int k = first; k <= last; k++) {
                double bx = startX + (k - first) * (nodeW + gap);
                boolean isActiveNode = active && k == s.getActiveNode();
                boolean bypassed = isActiveNode && s.getLink() == Link.BYPASS;
                Color fill = isActiveNode ? Theme.tint(mc, 0.72) : Color.WHITE;
                Color border = isActiveNode ? mc : Theme.BORDER_STRONG;
                if (bypassed) {
                    fill = Theme.tint(Theme.V_SWAP, 0.86);
                }
                paintNode(g, bx, cy, nodeW, cellH, ptrW, fill, border, bypassed, chain.get(k), nodeFont);

                double dotX = bx + nodeW - ptrW / 2;
                boolean outgoingIsNew = active && s.getLink() == Link.NEW_LINK && s.getLinkFrom() == k;
                boolean outgoingIsOld = active && s.getLink() == Link.BYPASS
                        && (k == s.getActiveNode() || k == s.getLinkFrom());
                Color c = outgoingIsNew ? Theme.V_ACTIVE : arrowC;
                float wdt = outgoingIsNew ? 2.8f : 1.6f;
                if (k < n - 1 && k < last) {
                    if (outgoingIsOld) {
                        Draw.dashedLine(g, dotX, mid, bx + nodeW + gap - 4, mid, Theme.tint(Theme.V_SWAP, 0.4), 1.6f);
                    } else {
                        Draw.arrow(g, dotX, mid, bx + nodeW + gap - 4, mid, c, wdt);
                    }
                } else if (k == n - 1) {
                    Draw.arrow(g, dotX, mid, bx + nodeW + 12, mid, outgoingIsOld ? Theme.tint(Theme.V_SWAP, 0.4) : arrowC, 1.4f);
                    Draw.leftText(g, "null", bx + nodeW + 14, mid, Theme.regular(Math.max(9.5f, fs - 2)), Theme.TEXT_MUTED);
                } else {
                    Draw.dashedLine(g, dotX, mid, bx + nodeW + 14, mid, arrowC, 1.4f);
                    Draw.leftText(g, "+" + (n - 1 - last), bx + nodeW + 16, mid, Theme.bold(10.5f), Theme.TEXT_MUTED);
                }
            }

            // the arrow that jumps over a removed node
            if (active && s.getLink() == Link.BYPASS) {
                double fx;
                if (s.getLinkFrom() == HashSnapshot.HEAD) {
                    fx = cx + ib - 6;
                } else {
                    fx = startX + (s.getLinkFrom() - first) * (nodeW + gap) + nodeW - ptrW / 2;
                }
                double tx;
                if (s.getLinkTo() == HashSnapshot.NULL) {
                    tx = startX + (Math.min(n - 1, last) - first) * (nodeW + gap) + nodeW + 22;
                } else {
                    tx = startX + (s.getLinkTo() - first) * (nodeW + gap) + 8;
                }
                Draw.curvedArrow(g, fx, cy + 1, tx, cy + 1, -Math.max(16, cellH * 0.9), Theme.V_SWAP, 2.6f);
            }
        }
    }

    private static void paintNode(Graphics2D g, double x, double y, double w, double h, double ptrW, Color fill,
            Color border, boolean dashed, String text, Font font) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
        g.setColor(Theme.withAlpha(Color.BLACK, 18));
        g.fill(new RoundRectangle2D.Double(x + 1, y + 2, w, h, 10, 10));
        g.setColor(fill);
        g.fill(r);
        Stroke old = g.getStroke();
        g.setStroke(dashed ? new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 4f}, 0f)
                : new BasicStroke(2f));
        g.setColor(border);
        g.draw(r);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new java.awt.geom.Line2D.Double(x + w - ptrW, y + 3, x + w - ptrW, y + h - 3));
        g.setStroke(old);
        double dr = Math.max(2.5, Math.min(4, ptrW / 4));
        g.fill(new Ellipse2D.Double(x + w - ptrW / 2 - dr, y + h / 2 - dr, dr * 2, dr * 2));
        Draw.textInBox(g, text, x, y, w - ptrW, h, font, dashed ? Theme.shade(Theme.V_SWAP, 0.3) : Theme.TEXT);
    }

    // ================================================================== probing

    private static void paintSlots(Graphics2D g, HashSnapshot s, double x, double y, double w, double h) {
        int n = s.getSize();
        double gap = n > 10 ? 4 : 7;
        double cellW = Math.min(124, (w - (n - 1) * gap) / n);
        double totalW = n * cellW + (n - 1) * gap;
        double x0 = x + (w - totalW) / 2;
        double arcSpace = Math.min(34, Math.max(18, cellW * 0.5));
        double below = 36;
        double cellH = clamp(h - arcSpace - 18 - below, 28, 72);
        double block = arcSpace + 18 + cellH + below;
        double cy = y + Math.max(0, (h - block) / 2) + arcSpace + 18;
        Color mc = markColor(s.getMark());
        List<Integer> path = s.getProbePath();
        int home = s.getCalc() == null ? -1 : s.getCalc().getHome();
        float fs = (float) clamp(Math.min(cellW * 0.2, cellH * 0.3), 9, 14);

        for (int i = 0; i < n; i++) {
            double bx = x0 + i * (cellW + gap);
            double cxm = bx + cellW / 2;
            // slot number (home slot in a yellow pill)
            String label = String.valueOf(i);
            if (i == home) {
                Draw.roundRect(g, cxm - 15, cy - 19, 30, 16, 16, Theme.ACCENT, null);
                Draw.centeredText(g, label, cxm, cy - 11, Theme.bold(11f), Theme.TEXT);
            } else {
                Draw.centeredText(g, label, cxm, cy - 11, Theme.bold(11f), Theme.TEXT_MUTED);
            }
            String name = s.getSlot(i);
            boolean tomb = s.isTombstone(i);
            int pos = path.indexOf(i);
            boolean active = i == s.getActiveIndex();
            Color fill = name != null ? Color.WHITE : Theme.SURFACE_ALT;
            Color border = Theme.BORDER_STRONG;
            if (pos >= 0) {
                fill = Theme.tint(Theme.V_COMPARE, 0.82);
                border = Theme.tint(Theme.V_COMPARE, 0.3);
            }
            if (active) {
                fill = Theme.tint(mc, 0.7);
                border = mc;
            }
            RoundRectangle2D cell = new RoundRectangle2D.Double(bx, cy, cellW, cellH, 10, 10);
            g.setColor(fill);
            g.fill(cell);
            if (tomb) {
                Shape oldClip = g.getClip();
                g.clip(cell);
                g.setColor(Theme.withAlpha(Theme.TEXT_MUTED, 45));
                g.setStroke(new BasicStroke(1.2f));
                for (double d = -cellH; d < cellW; d += 8) {
                    g.draw(new java.awt.geom.Line2D.Double(bx + d, cy + cellH, bx + d + cellH, cy));
                }
                g.setClip(oldClip);
            }
            g.setStroke(new BasicStroke(active ? 2.6f : 1.6f));
            g.setColor(border);
            g.draw(cell);
            g.setStroke(new BasicStroke(1f));
            if (name != null) {
                Draw.textInBox(g, name, bx, cy, cellW, cellH, Theme.bold(fs), Theme.TEXT);
            } else if (tomb) {
                Draw.textInBox(g, "DELETED", bx, cy, cellW, cellH, Theme.bold(Math.max(8.5f, fs - 2)), Theme.shade(Theme.V_SWAP, 0.25));
            } else {
                Draw.textInBox(g, "EMPTY", bx, cy, cellW, cellH, Theme.regular(Math.max(8.5f, fs - 2)), Theme.TEXT_MUTED);
            }
            if (pos >= 0) {
                double r = 9;
                double bxr = bx + cellW - 4;
                double byr = cy + 4;
                Draw.node(g, bxr, byr, r, active ? mc : Theme.V_COMPARE, Color.WHITE, String.valueOf(pos + 1), Color.WHITE, Theme.bold(9.5f));
            }
        }

        // probe trail arrows
        Color trail = Theme.shade(Theme.V_COMPARE, 0.2);
        for (int k = 1; k < path.size(); k++) {
            int a = path.get(k - 1);
            int b = path.get(k);
            double ax = x0 + a * (cellW + gap) + cellW / 2;
            double bxx = x0 + b * (cellW + gap) + cellW / 2;
            if (b == a + 1) {
                Draw.curvedArrow(g, ax, cy - 21, bxx, cy - 21, -arcSpace * 0.9, trail, 2f);
            } else if (a == n - 1 && b == 0) {
                double yb = cy + cellH + 20;
                Draw.line(g, ax, cy + cellH + 2, ax, yb, Theme.PURPLE, 2.2f);
                Draw.line(g, ax, yb, bxx, yb, Theme.PURPLE, 2.2f);
                Draw.arrow(g, bxx, yb, bxx, cy + cellH + 4, Theme.PURPLE, 2.2f);
                Draw.centeredText(g, "wrap around: after slot " + (n - 1) + " comes slot 0", (ax + bxx) / 2, yb + 10,
                        Theme.bold(11.5f), Theme.PURPLE);
            }
        }
        if (s.getMark() == Mark.FULL) {
            Draw.centeredText(g, "TABLE FULL - every slot is taken", x + w / 2, Math.min(y + h - 4, cy + cellH + 48),
                    Theme.bold(14f), Theme.DANGER);
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
