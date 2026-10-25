package com.airport.visualizer.ui.player;

import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.Step;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.core.Visualizer;
import com.airport.visualizer.ui.components.StatTile;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.GridLayout;
import javax.swing.JPanel;

/** Row of live numbers: step, comparisons, swaps, elapsed time, real run time and Big-O. */
public class MetricsBar extends JPanel {

    private final StatTile step = new StatTile("Step", Theme.PRIMARY);
    private final StatTile primary = new StatTile("Comparisons", Theme.V_COMPARE);
    private final StatTile secondary = new StatTile("Swaps", Theme.V_SWAP);
    private final StatTile elapsed = new StatTile("Elapsed", Theme.TEAL);
    private final StatTile real = new StatTile("Real run", Theme.PURPLE);
    private final StatTile bigO = new StatTile("Big-O", Theme.SUCCESS);

    private ComplexityInfo complexity;

    public MetricsBar() {
        super(new GridLayout(1, 6, 10, 0));
        setOpaque(false);
        add(step);
        add(primary);
        add(secondary);
        add(elapsed);
        add(real);
        add(bigO);
        primary.setToolTipText("How many times two things were compared");
        secondary.setToolTipText("How many times things were swapped or moved");
        elapsed.setToolTipText("How long the animation has been playing");
        real.setToolTipText("How long the same work takes the computer without any drawing");
        bigO.setToolTipText("How the work grows when there are more flights");
    }

    public void configure(Visualizer v, Timeline t) {
        String[] labels = v.getCounterLabels();
        primary.setCaption(labels.length > 0 ? labels[0] : "Comparisons");
        secondary.setCaption(labels.length > 1 ? labels[1] : "Swaps");
        complexity = v.getComplexity();
        real.setValue(formatNanos(t.getRealRunNanos()));
        bigO.setValue(complexity == null ? "-" : complexity.getHeadline());
    }

    public void update(int index, int total, Step s, long elapsedNanos) {
        step.setValue(total == 0 ? "0 / 0" : (index + 1) + " / " + total);
        primary.setValue(s == null ? "0" : String.valueOf(s.getPrimaryCount()));
        secondary.setValue(s == null ? "0" : String.valueOf(s.getSecondaryCount()));
        updateElapsed(elapsedNanos);
        if (complexity != null) {
            ComplexityInfo.Row row = s == null ? null : complexity.findRow(s.getOperationKey());
            bigO.setCaption(row == null ? "Big-O" : "Big-O: " + row.getLabel());
            bigO.setValue(row == null ? complexity.getHeadline() : row.getBigO());
        }
    }

    public void updateElapsed(long elapsedNanos) {
        elapsed.setValue(String.format("%.1f s", elapsedNanos / 1e9));
    }

    public static String formatNanos(long nanos) {
        if (nanos <= 0) {
            return "< 1 µs";
        }
        if (nanos < 1_000_000) {
            return String.format("%.1f µs", nanos / 1e3);
        }
        return String.format("%.2f ms", nanos / 1e6);
    }
}
