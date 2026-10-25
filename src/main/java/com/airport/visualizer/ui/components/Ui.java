package com.airport.visualizer.ui.components;

import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/** Factory for consistently styled standard Swing widgets (labels, fields, combos, rows). */
public final class Ui {

    private Ui() {
    }

    public static JLabel title(String text) {
        return label(text, Theme.bold(24f), Theme.TEXT);
    }

    public static JLabel heading(String text) {
        return label(text, Theme.bold(16f), Theme.TEXT);
    }

    public static JLabel muted(String text) {
        return label(text, Theme.regular(13.5f), Theme.TEXT_MUTED);
    }

    public static JLabel fieldLabel(String text) {
        return label(text, Theme.bold(12.5f), Theme.TEXT_MUTED);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** Multi-line, wrapping, read-only text that looks like a label. */
    public static JTextArea paragraph(String text, float size, Color color) {
        JTextArea a = new JTextArea(text);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setFont(Theme.regular(size));
        a.setForeground(color);
        // small right margin: wrapping uses whole-pixel widths, fractional fonts can draw a bit wider
        a.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        return a;
    }

    public static JTextField textField(int columns, String tooltip) {
        JTextField f = new JTextField(columns);
        f.setFont(Theme.regular(14f));
        f.setToolTipText(tooltip);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_STRONG, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    public static <T> JComboBox<T> combo(T[] items, String tooltip) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(Theme.regular(13.5f));
        c.setToolTipText(tooltip);
        c.setPreferredSize(new Dimension(Math.max(160, c.getPreferredSize().width), 34));
        return c;
    }

    public static JSpinner spinner(int value, int min, int max, String tooltip) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        s.setFont(Theme.regular(14f));
        s.setToolTipText(tooltip);
        s.setPreferredSize(new Dimension(70, 34));
        return s;
    }

    /** Left-aligned row of components with even gaps. */
    public static JPanel row(Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        for (Component c : components) {
            p.add(c);
        }
        return p;
    }

    /** Label stacked on top of a field. */
    public static JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(fieldLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    /** Vertical stack (BoxLayout) with transparent background. */
    public static JPanel stack() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    /**
     * Vertical-only scrolling: the content is always exactly as wide as the visible area,
     * so wrapped text re-flows instead of hiding behind a horizontal scrollbar.
     */
    public static JScrollPane scrollVertical(JComponent c) {
        JPanel tracker = new WidthTrackingPanel();
        tracker.add(c, BorderLayout.CENTER);
        JScrollPane sp = scroll(tracker);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    private static final class WidthTrackingPanel extends JPanel implements javax.swing.Scrollable {

        WidthTrackingPanel() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientation, int direction) {
            return Math.max(18, r.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() > getPreferredSize().height;
        }
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getHorizontalScrollBar().setUnitIncrement(18);
        return sp;
    }
}
