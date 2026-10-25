package com.airport.visualizer.ui.components;

import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/** Small metric tile: a muted caption on top and a big value underneath. */
public class StatTile extends JComponent {

    private String caption;
    private String value = "-";
    private final Color color;

    public StatTile(String caption, Color color) {
        this.caption = caption;
        this.color = color;
        setPreferredSize(new Dimension(130, 62));
        setMinimumSize(new Dimension(90, 62));
    }

    public void setValue(String value) {
        if (value == null) {
            value = "-";
        }
        if (!value.equals(this.value)) {
            this.value = value;
            setToolTipText(caption + ": " + value);
            repaint();
        }
    }

    public void setCaption(String caption) {
        this.caption = caption;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Draw.setup(g);
        int w = getWidth();
        int h = getHeight();
        Draw.roundRect(g, 0.5, 0.5, w - 1.5, h - 1.5, 14, Theme.SURFACE, Theme.BORDER, 1f);
        Draw.roundRect(g, 10, 12, 4, h - 24, 4, color, null);
        // shrink before cutting, so captions like "COMPARISONS" stay readable on small screens
        java.awt.Font capFont = fit(g, caption.toUpperCase(), Theme.bold(10.5f), w - 30, 8.5f);
        g.setFont(capFont);
        Draw.leftText(g, Draw.ellipsize(g, caption.toUpperCase(), w - 30), 22, 20, capFont, Theme.TEXT_MUTED);
        java.awt.Font valFont = fit(g, value, Theme.bold(18f), w - 30, 12f);
        g.setFont(valFont);
        Draw.leftText(g, Draw.ellipsize(g, value, w - 30), 22, h - 21, valFont, Theme.TEXT);
        g.dispose();
    }

    private static java.awt.Font fit(Graphics2D g, String text, java.awt.Font font, int maxWidth, float minSize) {
        java.awt.Font f = font;
        while (g.getFontMetrics(f).stringWidth(text) > maxWidth && f.getSize2D() > minSize) {
            f = f.deriveFont(f.getSize2D() - 0.5f);
        }
        return f;
    }
}
