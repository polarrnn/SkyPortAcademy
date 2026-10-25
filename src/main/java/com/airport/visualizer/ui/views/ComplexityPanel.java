package com.airport.visualizer.ui.views;

import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Big-O table + "when to use it" advice, with the row of the current step highlighted. */
public class ComplexityPanel extends JPanel {

    private final Map<String, Card> rowCards = new LinkedHashMap<>();

    public ComplexityPanel(ComplexityInfo info) {
        super(new BorderLayout());
        setOpaque(false);
        JPanel stack = Ui.stack();

        JPanel headline = new JPanel(new BorderLayout(10, 0));
        headline.setOpaque(false);
        headline.add(Ui.heading("Overall Big-O"), BorderLayout.WEST);
        headline.add(new Badge(info.getHeadline(), Theme.SUCCESS), BorderLayout.EAST);
        stack.add(align(headline));
        stack.add(Box.createVerticalStrut(10));

        for (ComplexityInfo.Row r : info.getRows()) {
            Card card = new Card(new BorderLayout(8, 4));
            card.padding(10, 14, 10, 12);
            JPanel top = new JPanel(new BorderLayout(8, 0));
            top.setOpaque(false);
            JLabel label = Ui.label(r.getLabel(), Theme.bold(13.5f), Theme.TEXT);
            top.add(label, BorderLayout.CENTER);
            top.add(new Badge(r.getBigO(), Theme.PRIMARY), BorderLayout.EAST);
            card.add(top, BorderLayout.NORTH);
            if (r.getNote() != null && !r.getNote().isEmpty()) {
                card.add(Ui.paragraph(r.getNote(), 13f, Theme.TEXT_MUTED), BorderLayout.CENTER);
            }
            if (r.getKey() != null) {
                rowCards.put(r.getKey(), card);
            }
            stack.add(align(card));
            stack.add(Box.createVerticalStrut(8));
        }

        if (info.getWhenToUse() != null && !info.getWhenToUse().isEmpty()) {
            stack.add(Box.createVerticalStrut(6));
            stack.add(align(adviceCard("When should I use it?", info.getWhenToUse(), Theme.SUCCESS)));
            stack.add(Box.createVerticalStrut(8));
        }
        if (info.getAvoidWhen() != null && !info.getAvoidWhen().isEmpty()) {
            stack.add(align(adviceCard("When is it a bad idea?", info.getAvoidWhen(), Theme.DANGER)));
            stack.add(Box.createVerticalStrut(8));
        }
        stack.add(align(adviceCard("Big-O in kid words",
                "O(1): instant, like knowing your gate number already.\n"
                + "O(log n): cut the pile in half again and again.\n"
                + "O(n): look at every flight once.\n"
                + "O(n log n): split into halves, then tidy every flight.\n"
                + "O(n²): every flight is compared with every other flight - slow for big airports!",
                Theme.INFO)));
        stack.add(Box.createVerticalGlue());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(stack, BorderLayout.NORTH);
        add(Ui.scrollVertical(top), BorderLayout.CENTER);
    }

    /** Highlights the Big-O row for the operation the current step is doing. */
    public void highlight(String key) {
        for (Map.Entry<String, Card> e : rowCards.entrySet()) {
            boolean on = e.getKey().equals(key);
            e.getValue().setFill(on ? Theme.ACCENT_SOFT : Theme.SURFACE);
            e.getValue().setAccent(on ? Theme.ACCENT : null);
        }
    }

    private static Card adviceCard(String title, String text, Color color) {
        Card c = new Card(new BorderLayout(0, 6));
        c.padding(10, 18, 12, 12);
        c.setAccent(color);
        c.add(Ui.label(title, Theme.bold(13.5f), Theme.shade(color, 0.25)), BorderLayout.NORTH);
        JTextArea body = Ui.paragraph(text, 13f, Theme.TEXT);
        c.add(body, BorderLayout.CENTER);
        return c;
    }

    private static JComponent align(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return c;
    }

    /** Pill showing a Big-O value. */
    static final class Badge extends JComponent {

        private final String text;
        private final Color color;

        Badge(String text, Color color) {
            this.text = text == null ? "-" : text;
            this.color = color;
            int w = getFontMetrics(Theme.bold(13f)).stringWidth(this.text) + 24;
            setPreferredSize(new Dimension(w, 26));
            setMinimumSize(getPreferredSize());
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            Draw.roundRect(g, 0, 0, getWidth() - 1, getHeight() - 1, getHeight(), Theme.tint(color, 0.85), Theme.tint(color, 0.5), 1f);
            Draw.centeredText(g, text, getWidth() / 2.0, getHeight() / 2.0, Theme.bold(13f), Theme.shade(color, 0.3));
            g.dispose();
        }
    }
}
