package com.airport.visualizer.ui.views;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** Title card used at the top of every lesson and lab: category chip, title, summary, extra controls. */
public class ViewHeader extends Card {

    private final JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

    public ViewHeader(Category category, String title, String summary) {
        super(new BorderLayout(16, 4));
        padding(14, 20, 14, 18);
        setAccent(category.getColor());
        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        JPanel titleRow = new JPanel(new BorderLayout(10, 0));
        titleRow.setOpaque(false);
        JPanel chipHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        chipHolder.setOpaque(false);
        chipHolder.add(new CategoryChip(category));
        titleRow.add(chipHolder, BorderLayout.WEST);
        titleRow.add(new FitTitle(title), BorderLayout.CENTER);
        text.add(titleRow, BorderLayout.NORTH);
        text.add(Ui.paragraph(summary, 14.5f, Theme.TEXT_MUTED), BorderLayout.CENTER);
        add(text, BorderLayout.CENTER);
        tools.setOpaque(false);
        add(tools, BorderLayout.EAST);
    }

    /** Adds a control (combo, button...) to the right side of the header. */
    public void addTool(JComponent c) {
        tools.add(c);
    }

    /** Page title that shrinks its font to fit the available width instead of being cut off. */
    static final class FitTitle extends javax.swing.JLabel {

        private final String text;

        FitTitle(String text) {
            super(text);
            this.text = text;
            setPreferredSize(new Dimension(200, 34));
            setToolTipText(text);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            java.awt.Font f = Theme.bold(24f);
            while (g.getFontMetrics(f).stringWidth(text) > getWidth() && f.getSize2D() > 15f) {
                f = f.deriveFont(f.getSize2D() - 1f);
            }
            g.setFont(f);
            Draw.leftText(g, Draw.ellipsize(g, text, getWidth()), 0, getHeight() / 2.0, f, Theme.TEXT);
            g.dispose();
        }
    }

    static final class CategoryChip extends JComponent {

        private final Category category;

        CategoryChip(Category category) {
            this.category = category;
            int w = getFontMetrics(Theme.bold(12f)).stringWidth(category.getTitle().toUpperCase()) + 22;
            setPreferredSize(new Dimension(w, 26));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            Draw.roundRect(g, 0, 1, getWidth() - 1, getHeight() - 2, getHeight(), Theme.tint(category.getColor(), 0.85), null);
            Draw.centeredText(g, category.getTitle().toUpperCase(), getWidth() / 2.0, getHeight() / 2.0,
                    Theme.bold(12f), Theme.shade(category.getColor(), 0.3));
            g.dispose();
        }
    }
}
