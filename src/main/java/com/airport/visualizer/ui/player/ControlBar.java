package com.airport.visualizer.ui.player;

import com.airport.visualizer.ui.components.Icons;
import com.airport.visualizer.ui.components.RoundButton;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/** Play / Pause / Step / Back / Reset buttons, the speed slider and a timeline scrubber. */
public class ControlBar extends JPanel {

    private final PlaybackController controller;
    private final RoundButton play = new RoundButton("Play", Icons.of(Icons.Shape.PLAY), RoundButton.Variant.PRIMARY);
    private final RoundButton pause = new RoundButton("Pause", Icons.of(Icons.Shape.PAUSE), RoundButton.Variant.SECONDARY);
    private final RoundButton step = new RoundButton("Step", Icons.of(Icons.Shape.STEP), RoundButton.Variant.SECONDARY);
    private final RoundButton back = new RoundButton("Back", Icons.of(Icons.Shape.BACK), RoundButton.Variant.SECONDARY);
    private final RoundButton reset = new RoundButton("Reset", Icons.of(Icons.Shape.RESET), RoundButton.Variant.SECONDARY);
    private final JSlider speed = new JSlider(PlaybackController.MIN_SPEED, PlaybackController.MAX_SPEED, 5);
    private final JSlider scrubber = new JSlider(0, 0, 0);
    private boolean updatingScrubber;

    public ControlBar(PlaybackController controller) {
        super(new BorderLayout(16, 4));
        this.controller = controller;
        setOpaque(false);

        play.setToolTipText("Play the animation (Space)");
        pause.setToolTipText("Pause (Space)");
        step.setToolTipText("Go forward one step (Right arrow)");
        back.setToolTipText("Go back one step (Left arrow)");
        reset.setToolTipText("Start again from the first step (R or Home)");
        play.addActionListener(e -> controller.play());
        pause.addActionListener(e -> controller.pause());
        step.addActionListener(e -> controller.stepForward());
        back.addActionListener(e -> controller.stepBack());
        reset.addActionListener(e -> controller.reset());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(play);
        buttons.add(pause);
        buttons.add(back);
        buttons.add(step);
        buttons.add(reset);
        // Row 1: buttons. Row 2: timeline (stretches) + speed. Two rows keep every
        // control usable on 1366-px laptop screens.
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.add(buttons, BorderLayout.WEST);
        add(topRow, BorderLayout.NORTH);

        scrubber.setOpaque(false);
        scrubber.setToolTipText("Drag to jump to any step");
        scrubber.addChangeListener(e -> {
            if (!updatingScrubber) {
                controller.jumpTo(scrubber.getValue());
            }
        });
        scrubber.getAccessibleContext().setAccessibleName("Step position");
        JPanel middle = new JPanel(new BorderLayout(14, 0));
        middle.setOpaque(false);
        middle.add(Ui.fieldLabel("Timeline "), BorderLayout.WEST);
        middle.add(scrubber, BorderLayout.CENTER);
        add(middle, BorderLayout.CENTER);

        speed.setOpaque(false);
        speed.setMajorTickSpacing(1);
        speed.setSnapToTicks(true);
        speed.setPaintTicks(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(1, Ui.label("Slow", Theme.regular(11f), Theme.TEXT_MUTED));
        labels.put(10, Ui.label("Fast", Theme.regular(11f), Theme.TEXT_MUTED));
        speed.setLabelTable(labels);
        speed.setPaintLabels(true);
        speed.setPreferredSize(new Dimension(150, 40));
        speed.setMinimumSize(new Dimension(110, 40));
        speed.setToolTipText("Animation speed");
        speed.getAccessibleContext().setAccessibleName("Animation speed");
        speed.addChangeListener(e -> controller.setSpeed(speed.getValue()));
        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);
        right.add(Ui.fieldLabel("Speed"), BorderLayout.WEST);
        right.add(speed, BorderLayout.CENTER);
        middle.add(right, BorderLayout.EAST);

        controller.setSpeed(speed.getValue());
        refresh();
    }

    /** Enables / disables buttons to match the controller state (prevents invalid actions). */
    public void refresh() {
        int total = controller.getTimeline().size();
        boolean has = total > 0;
        boolean playing = controller.isPlaying();
        play.setEnabled(has && !playing);
        pause.setEnabled(playing);
        step.setEnabled(has && !controller.isAtEnd());
        back.setEnabled(has && !controller.isAtStart());
        reset.setEnabled(has);
        play.setText(has && controller.isAtEnd() && !playing ? "Replay" : "Play");
        updatingScrubber = true;
        scrubber.setEnabled(has);
        scrubber.setMaximum(Math.max(0, total - 1));
        scrubber.setValue(Math.max(0, controller.getIndex()));
        updatingScrubber = false;
    }
}
