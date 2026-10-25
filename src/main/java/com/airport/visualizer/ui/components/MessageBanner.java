package com.airport.visualizer.ui.components;

import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.JComponent;

/**
 * Inline feedback strip (success / info / warning / error). Wraps long text and grows in height,
 * so messages are never clipped. Used instead of pop-ups so learners keep their place.
 */
public class MessageBanner extends JComponent {

    public enum Kind {
        INFO(Theme.INFO, Theme.INFO_SOFT, "Tip"),
        SUCCESS(Theme.SUCCESS, Theme.SUCCESS_SOFT, "Done"),
        WARNING(Theme.WARNING, Theme.WARNING_SOFT, "Heads up"),
        ERROR(Theme.DANGER, Theme.DANGER_SOFT, "Oops");

        final Color strong;
        final Color soft;
        final String word;

        Kind(Color strong, Color soft, String word) {
            this.strong = strong;
            this.soft = soft;
            this.word = word;
        }
    }

    private Kind kind = Kind.INFO;
    private String text = "";

    public MessageBanner() {
        setVisible(false);
    }

    public void show(Kind kind, String text) {
        this.kind = kind;
        this.text = text == null ? "" : text;
        setVisible(!this.text.isEmpty());
        revalidate();
        repaint();
    }

    public void info(String t) {
        show(Kind.INFO, t);
    }

    public void success(String t) {
        show(Kind.SUCCESS, t);
    }

    public void warning(String t) {
        show(Kind.WARNING, t);
    }

    public void error(String t) {
        show(Kind.ERROR, t);
    }

    public void clear() {
        show(Kind.INFO, "");
    }

    /** When the width changes the number of wrapped lines may change, so ask for a new layout. */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        int oldWidth = getWidth();
        super.setBounds(x, y, width, height);
        if (width != oldWidth && isVisible() && getPreferredSize().height != height) {
            javax.swing.SwingUtilities.invokeLater(this::revalidate);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int w = getWidth() > 0 ? getWidth() : 500;
        int lines = Math.max(1, Draw.wrap(getFontMetrics(Theme.regular(13.5f)), kind.word + ": " + text, w - 40).size());
        return new Dimension(200, 18 + lines * 19);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Draw.setup(g);
        int w = getWidth();
        int h = getHeight();
        Draw.roundRect(g, 0.5, 0.5, w - 1.5, h - 1.5, 12, kind.soft, Theme.tint(kind.strong, 0.5), 1f);
        g.setColor(kind.strong);
        g.fillRoundRect(0, 0, 5, h, 4, 4);
        List<String> lines = Draw.wrap(g, kind.word + ": " + text, Theme.regular(13.5f), w - 40);
        g.setFont(Theme.regular(13.5f));
        int y = 9 + g.getFontMetrics().getAscent();
        for (String line : lines) {
            g.setColor(Theme.shade(kind.strong, 0.45));
            g.drawString(line, 18, y);
            y += 19;
        }
        g.dispose();
    }
}
