package com.airport.visualizer.ui.views;

import com.airport.visualizer.core.FlightTableState;
import com.airport.visualizer.core.Step;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.core.Visualizer;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.FlightTable;
import com.airport.visualizer.ui.components.MessageBanner;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.player.PlaybackController;
import com.airport.visualizer.ui.player.PlayerPanel;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * Generic lesson screen for ANY {@link Visualizer}. Depends only on the interface, so the same
 * screen shows Bubble Sort, a HashMap or a graph traversal.
 */
public class VisualizerView extends JPanel implements ViewLifecycle {

    private final Visualizer visualizer;
    private final PlayerPanel player = new PlayerPanel(false);
    private final ComplexityPanel complexityPanel;
    private final FlightTable table = new FlightTable();
    private final JTextArea storyLog = Ui.paragraph("", 13.5f, Theme.TEXT);
    private final MessageBanner banner = new MessageBanner();
    private Timeline timeline = Timeline.empty("");
    private boolean tableShowsDefault;

    public VisualizerView(Visualizer visualizer) {
        super(new BorderLayout(12, 12));
        this.visualizer = visualizer;
        setOpaque(true);
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        ViewHeader header = new ViewHeader(visualizer.getCategory(), visualizer.getName(), visualizer.getSummary());
        List<String> scenarios = visualizer.getScenarios();
        if (scenarios.size() > 1) {
            JComboBox<String> combo = Ui.combo(scenarios.toArray(new String[0]), "Choose what to try");
            combo.addActionListener(e -> rebuild((String) combo.getSelectedItem()));
            header.addTool(Ui.labeled("Try this", combo));
        }
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(header, BorderLayout.CENTER);
        north.add(banner, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        add(player, BorderLayout.CENTER);

        complexityPanel = new ComplexityPanel(visualizer.getComplexity());
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.bold(13f));
        tabs.addTab("Big-O & Use", pad(complexityPanel));
        tabs.addTab("Flights", pad(table.inScrollPane()));
        storyLog.setFocusable(true);
        tabs.addTab("Story", pad(Ui.scrollVertical(storyLog)));
        tabs.setToolTipTextAt(0, "How fast each operation is, and when to choose this");
        tabs.setToolTipTextAt(1, "The flights used in this lesson (highlighted rows = being worked on)");
        tabs.setToolTipTextAt(2, "Every step explained so far");
        // The side tabs take about 28% of the screen (330..440 px) so the animation stays big on laptops.
        Card side = new Card(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                int w = VisualizerView.this.getWidth();
                int pw = w <= 0 ? 430 : Math.max(330, Math.min(440, (int) (w * 0.28)));
                return new Dimension(pw, 400);
            }
        };
        side.padding(8, 8, 10, 8);
        side.add(tabs, BorderLayout.CENTER);
        // Story card sits on top of the side column, right next to the picture it explains.
        JPanel sideColumn = new JPanel(new BorderLayout(0, 12)) {
            @Override
            public Dimension getPreferredSize() {
                return side.getPreferredSize();
            }
        };
        sideColumn.setOpaque(false);
        player.getNarration().setPreferredSize(new Dimension(300, 230));
        sideColumn.add(player.getNarration(), BorderLayout.NORTH);
        sideColumn.add(side, BorderLayout.CENTER);
        add(sideColumn, BorderLayout.EAST);

        player.addStepListener(new PlaybackController.Listener() {
            @Override
            public void stepChanged(int index, Step step) {
                onStep(index, step);
            }
        });
        rebuild(scenarios.isEmpty() ? "Default" : scenarios.get(0));
    }

    private static JPanel pad(java.awt.Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 6, 6, 6));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void rebuild(String scenario) {
        try {
            timeline = visualizer.buildTimeline(scenario);
            banner.clear();
            if (timeline.getResultSummary() != null && !timeline.getResultSummary().isEmpty()) {
                banner.info(timeline.getResultSummary());
            }
        } catch (RuntimeException ex) {
            timeline = Timeline.empty("");
            banner.error("This lesson could not be prepared: " + ex.getMessage());
        }
        tableShowsDefault = false;
        player.load(visualizer, timeline);
    }

    private void onStep(int index, Step step) {
        complexityPanel.highlight(step == null ? null : step.getOperationKey());
        Object state = step == null ? null : step.getState();
        if (state instanceof FlightTableState) {
            FlightTableState fts = (FlightTableState) state;
            table.setFlights(fts.getTableFlights());
            table.setHighlighted(fts.getHighlightedFlightNumbers());
            tableShowsDefault = false;
        } else if (!tableShowsDefault) {
            table.setFlights(visualizer.getFlights());
            table.setHighlighted(Collections.<String>emptyList());
            tableShowsDefault = true;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= index && i < timeline.size(); i++) {
            Step s = timeline.get(i);
            sb.append(i + 1).append(". ").append(s.getTitle()).append("\n   ").append(s.getNarration()).append("\n\n");
        }
        storyLog.setText(sb.toString());
        storyLog.setCaretPosition(storyLog.getDocument().getLength());
    }

    @Override
    public void onHide() {
        player.getController().pause();
    }
}
