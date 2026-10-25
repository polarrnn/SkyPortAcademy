package com.airport.visualizer.ui.player;

import com.airport.visualizer.core.Step;
import com.airport.visualizer.core.Visualizer;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/** The drawing surface. It simply asks the current {@link Visualizer} to paint the current step. */
public class VisualCanvas extends JPanel {

    private Visualizer visualizer;
    private Step step;

    public VisualCanvas() {
        setBackground(Theme.SURFACE);
        setOpaque(true);
        setFocusable(true);
        setPreferredSize(new Dimension(700, 380));
        setMinimumSize(new Dimension(300, 200));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    public void show(Visualizer v, Step s) {
        this.visualizer = v;
        this.step = s;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Draw.setup(g);
        try {
            if (visualizer == null) {
                Draw.emptyMessage(g, getWidth(), getHeight(), "Pick a lesson to begin.");
            } else {
                visualizer.paint(g, step, getWidth(), getHeight());
            }
        } catch (RuntimeException ex) {
            // A painting bug must never freeze or crash the app (ISO 25010 fault tolerance).
            Draw.emptyMessage(g, getWidth(), getHeight(), "This picture could not be drawn: " + ex.getMessage());
        } finally {
            g.dispose();
        }
    }
}
