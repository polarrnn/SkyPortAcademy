package com.airport.visualizer.ui.player;

import com.airport.visualizer.core.Step;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.core.Visualizer;
import com.airport.visualizer.ui.components.Card;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * Reusable "video player" for any {@link Visualizer}: metrics on top, the picture in the middle,
 * the kid-friendly story and the controls underneath. Used by every lesson and every practice lab.
 */
public class PlayerPanel extends JPanel {

    private final PlaybackController controller = new PlaybackController();
    private final VisualCanvas canvas = new VisualCanvas();
    private final NarrationPanel narration = new NarrationPanel();
    private final MetricsBar metrics = new MetricsBar();
    private final ControlBar controls = new ControlBar(controller);
    private final Timer elapsedTicker;
    private Visualizer visualizer;

    public PlayerPanel() {
        this(true);
    }

    /**
     * @param includeNarration false when the owner places {@link #getNarration()} somewhere else
     *                         (e.g. beside the canvas) to give the picture more height
     */
    public PlayerPanel(boolean includeNarration) {
        super(new BorderLayout(0, 12));
        setOpaque(false);

        add(metrics, BorderLayout.NORTH);

        Card canvasCard = new Card(new BorderLayout());
        canvasCard.padding(6, 6, 8, 6);
        canvasCard.add(canvas, BorderLayout.CENTER);
        JPanel stage = new JPanel(new BorderLayout(12, 0));
        stage.setOpaque(false);
        stage.add(canvasCard, BorderLayout.CENTER);
        add(stage, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        if (includeNarration) {
            // Wide player: story beside the picture (keeps the picture tall). Narrow: story below it.
            bottom.add(narration, BorderLayout.CENTER);
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    boolean wide = getWidth() >= 900;
                    if (wide && narration.getParent() != stage) {
                        narration.setPreferredSize(new Dimension(Math.max(280, getWidth() / 4), 150));
                        stage.add(narration, BorderLayout.EAST);
                        revalidate();
                    } else if (!wide && narration.getParent() != bottom) {
                        narration.setPreferredSize(new Dimension(600, 150));
                        bottom.add(narration, BorderLayout.CENTER);
                        revalidate();
                    } else if (wide) {
                        narration.setPreferredSize(new Dimension(Math.max(280, getWidth() / 4), 150));
                    }
                }
            });
        }
        Card controlCard = new Card(new BorderLayout());
        controlCard.padding(10, 12, 10, 12);
        controlCard.add(controls, BorderLayout.CENTER);
        bottom.add(controlCard, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        elapsedTicker = new Timer(100, e -> metrics.updateElapsed(controller.getElapsedNanos()));

        controller.addListener(new PlaybackController.Listener() {
            @Override
            public void stepChanged(int index, Step step) {
                int total = controller.getTimeline().size();
                canvas.show(visualizer, step);
                narration.show(index, total, step);
                metrics.update(index, total, step, controller.getElapsedNanos());
                controls.refresh();
            }

            @Override
            public void playStateChanged(boolean playing) {
                if (playing) {
                    elapsedTicker.start();
                } else {
                    elapsedTicker.stop();
                    metrics.updateElapsed(controller.getElapsedNanos());
                }
                controls.refresh();
            }
        });
        installKeys();
    }

    /** Shows a new run. Safe to call again at any time (e.g. when the scenario changes). */
    public void load(Visualizer v, Timeline t) {
        this.visualizer = v;
        Timeline timeline = t == null ? Timeline.empty("") : t;
        metrics.configure(v, timeline);
        controller.load(timeline);
    }

    public PlaybackController getController() {
        return controller;
    }

    public Visualizer getVisualizer() {
        return visualizer;
    }

    public NarrationPanel getNarration() {
        return narration;
    }

    public void addStepListener(PlaybackController.Listener l) {
        controller.addListener(l);
    }

    /** Keyboard shortcuts while focus is anywhere inside the player (ISO 25010 operability). */
    private void installKeys() {
        InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePlay");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "stepForward");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "stepBack");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "reset");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "reset");
        am.put("togglePlay", action(controller::togglePlay));
        am.put("stepForward", action(controller::stepForward));
        am.put("stepBack", action(controller::stepBack));
        am.put("reset", action(controller::reset));
    }

    private static AbstractAction action(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                r.run();
            }
        };
    }
}
