package com.airport.visualizer.ui.components;

import com.airport.visualizer.ui.theme.Draw;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Slim rounded scrollbar without arrow buttons (used on the dark sidebar). */
public class ThinScrollBarUI extends BasicScrollBarUI {

    private final Color thumb;
    private final Color track;

    public ThinScrollBarUI(Color thumb, Color track) {
        this.thumb = thumb;
        this.track = track;
    }

    public static void apply(JScrollBar bar, Color thumb, Color track) {
        bar.setUI(new ThinScrollBarUI(thumb, track));
        bar.setPreferredSize(new Dimension(10, 10));
        bar.setOpaque(false);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    private static JButton zeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        if (track != null) {
            g.setColor(track);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    @Override
    protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
        if (r.isEmpty()) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        Draw.setup(g);
        Draw.roundRect(g, r.x + 2, r.y + 2, r.width - 4, r.height - 4, r.width - 4, thumb, null);
        g.dispose();
    }
}
