package com.airport.visualizer.ui.components;

import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** White rounded panel with a soft shadow; the basic building block of every screen. */
public class Card extends JPanel {

    private Color fill = Theme.SURFACE;
    private Color accent;

    public Card() {
        this(new BorderLayout());
    }

    public Card(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
    }

    public Card padding(int top, int left, int bottom, int right) {
        setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return this;
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    /** Colored strip on the left edge (used for the narration card and messages). */
    public void setAccent(Color accent) {
        this.accent = accent;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Draw.setup(g);
        int w = getWidth();
        int h = getHeight();
        g.setColor(Theme.withAlpha(Color.BLACK, 14));
        g.fillRoundRect(1, 3, w - 2, h - 3, 18, 18);
        Draw.roundRect(g, 0.5, 0.5, w - 1.5, h - 3, 18, fill, Theme.BORDER, 1f);
        if (accent != null) {
            // clip() intersects with the current clip, so the strip never paints outside a scroll viewport
            g.clip(new java.awt.geom.RoundRectangle2D.Double(0.5, 0.5, w - 1.5, h - 3, 18, 18));
            g.setColor(accent);
            g.fillRect(0, 0, 6, h);
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
