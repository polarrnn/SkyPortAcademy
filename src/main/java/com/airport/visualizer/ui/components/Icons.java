package com.airport.visualizer.ui.components;

import com.airport.visualizer.ui.theme.Draw;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.Icon;

/**
 * Vector icons painted with Java2D, so they stay crisp at any screen scale and need no image
 * files (and no emoji fonts). The icon takes the button's text color.
 */
public final class Icons {

    public enum Shape {
        PLAY, PAUSE, STEP, BACK, RESET, SEARCH, PLUS, TRASH, SHUFFLE, PLANE, LIST, CHECK
    }

    private Icons() {
    }

    public static Icon of(Shape shape) {
        return of(shape, 16);
    }

    public static Icon of(Shape shape, int size) {
        return new VectorIcon(shape, size);
    }

    private static final class VectorIcon implements Icon {

        private final Shape shape;
        private final int size;

        VectorIcon(Shape shape, int size) {
            this.shape = shape;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            Color color = c != null ? c.getForeground() : Color.DARK_GRAY;
            if (c instanceof AbstractButton && !c.isEnabled()) {
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 110);
            }
            g.setColor(color);
            g.translate(x, y);
            double s = size;
            g.setStroke(new BasicStroke((float) Math.max(1.6, s / 9), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (shape) {
                case PLAY: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(s * 0.25, s * 0.15);
                    p.lineTo(s * 0.85, s * 0.5);
                    p.lineTo(s * 0.25, s * 0.85);
                    p.closePath();
                    g.fill(p);
                    break;
                }
                case PAUSE:
                    g.fill(new RoundRectangle2D.Double(s * 0.22, s * 0.15, s * 0.2, s * 0.7, 3, 3));
                    g.fill(new RoundRectangle2D.Double(s * 0.58, s * 0.15, s * 0.2, s * 0.7, 3, 3));
                    break;
                case STEP: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(s * 0.18, s * 0.18);
                    p.lineTo(s * 0.68, s * 0.5);
                    p.lineTo(s * 0.18, s * 0.82);
                    p.closePath();
                    g.fill(p);
                    g.fill(new RoundRectangle2D.Double(s * 0.7, s * 0.18, s * 0.14, s * 0.64, 2, 2));
                    break;
                }
                case BACK: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(s * 0.82, s * 0.18);
                    p.lineTo(s * 0.32, s * 0.5);
                    p.lineTo(s * 0.82, s * 0.82);
                    p.closePath();
                    g.fill(p);
                    g.fill(new RoundRectangle2D.Double(s * 0.16, s * 0.18, s * 0.14, s * 0.64, 2, 2));
                    break;
                }
                case RESET: {
                    g.draw(new Arc2D.Double(s * 0.15, s * 0.15, s * 0.7, s * 0.7, 60, 280, Arc2D.OPEN));
                    Path2D p = new Path2D.Double();
                    p.moveTo(s * 0.62, s * 0.02);
                    p.lineTo(s * 0.86, s * 0.2);
                    p.lineTo(s * 0.58, s * 0.34);
                    p.closePath();
                    g.fill(p);
                    break;
                }
                case SEARCH:
                    g.draw(new Ellipse2D.Double(s * 0.12, s * 0.12, s * 0.52, s * 0.52));
                    g.draw(new java.awt.geom.Line2D.Double(s * 0.56, s * 0.56, s * 0.88, s * 0.88));
                    break;
                case PLUS:
                    g.draw(new java.awt.geom.Line2D.Double(s * 0.5, s * 0.15, s * 0.5, s * 0.85));
                    g.draw(new java.awt.geom.Line2D.Double(s * 0.15, s * 0.5, s * 0.85, s * 0.5));
                    break;
                case TRASH:
                    g.draw(new java.awt.geom.Line2D.Double(s * 0.15, s * 0.25, s * 0.85, s * 0.25));
                    g.draw(new java.awt.geom.Line2D.Double(s * 0.4, s * 0.12, s * 0.6, s * 0.12));
                    g.draw(new RoundRectangle2D.Double(s * 0.25, s * 0.3, s * 0.5, s * 0.58, 4, 4));
                    break;
                case SHUFFLE:
                    g.draw(new java.awt.geom.Line2D.Double(s * 0.12, s * 0.28, s * 0.85, s * 0.72));
                    g.draw(new java.awt.geom.Line2D.Double(s * 0.12, s * 0.72, s * 0.85, s * 0.28));
                    Draw.arrowHead(g, s * 0.5, s * 0.5, s * 0.9, s * 0.25, color, s * 0.3);
                    Draw.arrowHead(g, s * 0.5, s * 0.5, s * 0.9, s * 0.75, color, s * 0.3);
                    break;
                case PLANE:
                    Draw.plane(g, s / 2, s / 2, s, color, -Math.PI / 4);
                    break;
                case LIST:
                    for (int i = 0; i < 3; i++) {
                        double yy = s * (0.25 + i * 0.25);
                        g.fill(new Ellipse2D.Double(s * 0.1, yy - s * 0.06, s * 0.12, s * 0.12));
                        g.draw(new java.awt.geom.Line2D.Double(s * 0.34, yy, s * 0.9, yy));
                    }
                    break;
                case CHECK: {
                    Path2D p = new Path2D.Double();
                    p.moveTo(s * 0.15, s * 0.52);
                    p.lineTo(s * 0.4, s * 0.76);
                    p.lineTo(s * 0.86, s * 0.24);
                    g.draw(p);
                    break;
                }
                default:
                    break;
            }
            g.dispose();
        }
    }
}
