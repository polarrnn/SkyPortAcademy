package com.airport.visualizer.algorithms.sort;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Shows any {@link SortAlgorithm} as a row of bars. The algorithm is plugged in from outside
 * (Strategy pattern), so this one class draws Bubble, Selection, Insertion, Merge and Quick Sort.
 */
public final class SortVisualizer extends AbstractVisualizer<SortState> {

    public static final String MIXED = "Mixed-up board (12 flights)";
    public static final String NEARLY = "Nearly sorted board";
    public static final String REVERSED = "Reverse order board";
    public static final String BIG = "Big board (all 24 flights)";
    public static final String LAB = "Your numbers";

    private static final int SMALL_BOARD = 12;

    private final SortAlgorithm algorithm;
    private final List<Integer> labValues;

    /** Lesson mode: sorts the sample flights by departure time. */
    public SortVisualizer(SortAlgorithm algorithm) {
        this(algorithm, null);
    }

    /** Lab mode: sorts the learner's own numbers. */
    public SortVisualizer(SortAlgorithm algorithm, List<Integer> values) {
        super(algorithm.getName(), Category.SORT, algorithm.getSummary(), SortState.class);
        this.algorithm = algorithm;
        this.labValues = values == null ? null : Collections.unmodifiableList(new ArrayList<>(values));
    }

    public SortAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public ComplexityInfo getComplexity() {
        return algorithm.getComplexity();
    }

    @Override
    public String[] getCounterLabels() {
        return algorithm.getCounterLabels();
    }

    @Override
    public List<String> getScenarios() {
        return labValues != null ? Collections.singletonList(LAB) : Arrays.asList(MIXED, NEARLY, REVERSED, BIG);
    }

    @Override
    public Timeline buildTimeline(String scenario) {
        List<SortItem> data;
        SortAlgorithm.InputShape shape;
        if (labValues != null) {
            data = SortItem.fromNumbers(labValues);
            shape = shapeOf(data);
        } else {
            List<Flight> board = new ArrayList<>(FlightData.first(SMALL_BOARD));
            if (NEARLY.equals(scenario)) {
                Collections.sort(board);
                Collections.swap(board, 2, 3);
                Collections.swap(board, 8, 9);
                shape = SortAlgorithm.InputShape.NEARLY_SORTED;
            } else if (REVERSED.equals(scenario)) {
                board.sort(Comparator.reverseOrder());
                shape = SortAlgorithm.InputShape.REVERSED;
            } else if (BIG.equals(scenario)) {
                board = new ArrayList<>(FlightData.sample());
                shape = SortAlgorithm.InputShape.MIXED;
            } else {
                shape = SortAlgorithm.InputShape.MIXED;
            }
            data = SortItem.fromFlights(board);
        }

        StepRecorder recorder = new StepRecorder();
        recorder.setOperation(algorithm.caseFor(shape));
        SortArray recorded = new SortArray(data, recorder);
        if (data.isEmpty()) {
            return Timeline.empty("There is nothing to sort.");
        }
        algorithm.sort(recorded);
        long nanos = measure(data);

        String caseKey = algorithm.caseFor(shape);
        ComplexityInfo.Row row = algorithm.getComplexity().findRow(caseKey);
        String summary = algorithm.getName() + " sorted " + data.size() + " "
                + (data.size() == 1 ? recorded.noun() : recorded.nouns()) + " with "
                + recorded.getComparisons() + " comparisons and " + recorded.getSwaps() + " "
                + algorithm.getCounterLabels()[1].toLowerCase() + "."
                + (row == null ? "" : " This start is the " + caseKey + " case: " + row.getBigO() + ".");
        return Timeline.of(recorder, nanos, summary);
    }

    /** Median of several silent runs (no pictures) = how fast the plain algorithm is. */
    private long measure(List<SortItem> data) {
        long[] times = new long[7];
        for (int r = 0; r < times.length; r++) {
            SortArray silent = new SortArray(data, null);
            long t0 = System.nanoTime();
            algorithm.sort(silent);
            times[r] = System.nanoTime() - t0;
        }
        Arrays.sort(times);
        return times[times.length / 2];
    }

    private static SortAlgorithm.InputShape shapeOf(List<SortItem> data) {
        boolean asc = true;
        boolean desc = true;
        for (int i = 1; i < data.size(); i++) {
            int c = Integer.compare(data.get(i - 1).getValue(), data.get(i).getValue());
            asc &= c <= 0;
            desc &= c >= 0;
        }
        if (data.size() > 1 && asc) {
            return SortAlgorithm.InputShape.NEARLY_SORTED;
        }
        if (data.size() > 1 && desc) {
            return SortAlgorithm.InputShape.REVERSED;
        }
        return SortAlgorithm.InputShape.MIXED;
    }

    // ------------------------------------------------------------------ painting

    @Override
    protected void paintState(Graphics2D g, SortState s, int w, int h) {
        int n = s.size();
        Marks m = s.getMarks();
        boolean flights = s.hasFlights();

        Draw.heading(g, flights ? "Departure board: earliest on the left, latest on the right"
                : "Your numbers: smallest on the left, biggest on the right", 20, 22);
        String done = "In place: " + s.sortedCount() + " of " + n;
        g.setFont(Theme.bold(12f));
        int chipW = g.getFontMetrics().stringWidth(done) + 20;
        Draw.chip(g, done, w - chipW - 18, 10, Theme.SUCCESS_SOFT, Theme.shade(Theme.SUCCESS, 0.3));
        Draw.legend(g, 20, 40,
                Theme.V_NEUTRAL, "Waiting",
                Theme.V_COMPARE, "Comparing",
                Theme.V_SWAP, "Swapping / moving",
                Theme.V_ACTIVE, "Held / writing",
                Theme.V_PIVOT, "Pivot",
                Theme.V_SORTED, "In place",
                Theme.PRIMARY_SOFT, "Working area");
        if (n == 0) {
            Draw.emptyMessage(g, w, h, "Nothing to sort.");
            return;
        }

        int trayH = m.hasTrays() ? 78 : 0;
        double top = 74;
        double bottomLabels = 50;
        double barBottom = h - bottomLabels - trayH - 8;
        double valueSpace = 20;
        double areaH = Math.max(30, barBottom - top - valueSpace);
        double left = 24;
        double slot = (w - 48) / (double) n;
        double barW = Math.min(64, slot * 0.72);

        int minV = Integer.MAX_VALUE;
        int maxV = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            minV = Math.min(minV, s.get(i).getValue());
            maxV = Math.max(maxV, s.get(i).getValue());
        }
        for (SortItem it : m.getLeftTray()) {
            minV = Math.min(minV, it.getValue());
            maxV = Math.max(maxV, it.getValue());
        }
        for (SortItem it : m.getRightTray()) {
            minV = Math.min(minV, it.getValue());
            maxV = Math.max(maxV, it.getValue());
        }

        if (m.hasRange()) {
            double x0 = left + m.getLo() * slot + 2;
            double x1 = left + (m.getHi() + 1) * slot - 2;
            Draw.roundRect(g, x0, top - 6, x1 - x0, barBottom - top + 12, 14,
                    Theme.withAlpha(Theme.PRIMARY_SOFT, 200), Theme.tint(Theme.PRIMARY, 0.6), 1.2f);
        }

        float valueSize = (float) Math.max(9, Math.min(13, slot / 4.4));
        float labelSize = (float) Math.max(8.5, Math.min(12.5, slot / 4.8));
        Font valueFont = Theme.bold(valueSize);
        Font labelFont = Theme.regular(labelSize);

        for (int i = 0; i < n; i++) {
            SortItem it = s.get(i);
            double cx = left + i * slot + slot / 2.0;
            double x = cx - barW / 2.0;
            if (m.isEmptySlot(i)) {
                g.setColor(Theme.BORDER_STRONG);
                Draw.dashedLine(g, x, barBottom, x + barW, barBottom, Theme.BORDER_STRONG, 2f);
                Draw.dashedLine(g, x, barBottom, x, barBottom - 26, Theme.BORDER_STRONG, 1.5f);
                Draw.dashedLine(g, x + barW, barBottom, x + barW, barBottom - 26, Theme.BORDER_STRONG, 1.5f);
                Draw.centeredText(g, "empty", cx, barBottom - 13, Theme.regular(Math.max(8.5f, labelSize - 1)), Theme.TEXT_MUTED);
            } else {
                double bh = barHeight(it.getValue(), minV, maxV, areaH);
                Color c = colorFor(s, m, i);
                Draw.roundRect(g, x, barBottom - bh, barW, bh, Math.min(12, barW / 2.5), c, Theme.shade(c, 0.18), 1.2f);
                Draw.textInBox(g, it.getValueText(), cx - slot / 2.0, barBottom - bh - valueSpace, slot, valueSpace,
                        valueFont, Theme.TEXT);
            }
            String label = flights ? it.getLabel() : "#" + (i + 1);
            Draw.textInBox(g, label, cx - slot / 2.0, barBottom + 4, slot, 18, labelFont, Theme.TEXT_MUTED);
            String p = m.getPointer(i);
            if (p != null) {
                g.setFont(Theme.bold(11f));
                int pw = Math.max(22, g.getFontMetrics().stringWidth(p) + 12);
                Draw.roundRect(g, cx - pw / 2.0, barBottom + 24, pw, 20, 20, Theme.PRIMARY_DARK, null);
                Draw.centeredText(g, p, cx, barBottom + 34, Theme.bold(11f), Color.WHITE);
            }
        }

        if (m.getSplit() >= 0 && m.getSplit() < n - 1) {
            double sx = left + (m.getSplit() + 1) * slot;
            Draw.dashedLine(g, sx, top - 10, sx, barBottom + 2, Theme.DANGER, 2.2f);
            Draw.centeredText(g, "cut", sx, top - 16, Theme.bold(11f), Theme.DANGER);
        }

        if (trayH > 0) {
            double ty = h - trayH - 4;
            Draw.roundRect(g, 16, ty, w - 32, trayH - 4, 14, Theme.SURFACE_ALT, Theme.BORDER, 1f);
            paintTray(g, "Left half waiting", m.getLeftTray(), 28, ty + 10, w - 56);
            paintTray(g, "Right half waiting", m.getRightTray(), 28, ty + 40, w - 56);
        }
    }

    private static double barHeight(int v, int minV, int maxV, double areaH) {
        if (maxV == minV) {
            return areaH * 0.6;
        }
        return areaH * (0.15 + 0.85 * (v - minV) / (double) (maxV - minV));
    }

    private static Color colorFor(SortState s, Marks m, int i) {
        if (m.isSwapping(i)) {
            return Theme.V_SWAP;
        }
        if (m.isComparing(i)) {
            return Theme.V_COMPARE;
        }
        if (m.getPivot() == i) {
            return Theme.V_PIVOT;
        }
        if (m.getKey() == i || m.getWriting() == i) {
            return Theme.V_ACTIVE;
        }
        if (s.isSorted(i)) {
            return Theme.V_SORTED;
        }
        return Theme.V_NEUTRAL;
    }

    private static void paintTray(Graphics2D g, String title, List<SortItem> items, double x, double y, double maxW) {
        Draw.leftText(g, title, x, y + 12, Theme.bold(12f), Theme.TEXT_MUTED);
        double cx = x + 140;
        if (items.isEmpty()) {
            Draw.leftText(g, "(empty)", cx, y + 12, Theme.regular(12f), Theme.TEXT_MUTED);
            return;
        }
        double cellW = Math.max(34, Math.min(62, (maxW - 140) / Math.max(1, items.size()) - 6));
        for (int i = 0; i < items.size(); i++) {
            Color c = i == 0 ? Theme.V_COMPARE : Theme.tint(Theme.V_NEUTRAL, 0.35);
            Draw.cell(g, cx, y, cellW, 24, c, Theme.shade(c, 0.2), items.get(i).getValueText(), Theme.TEXT, Theme.bold(11.5f));
            cx += cellW + 6;
        }
    }
}
