package com.airport.visualizer.hashing.lab;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.hashing.HashComplexity;
import com.airport.visualizer.hashing.HashSnapshot;
import com.airport.visualizer.hashing.HashStepSink;
import com.airport.visualizer.hashing.HashTableModel;
import com.airport.visualizer.hashing.HashTablePainter;
import java.awt.Graphics2D;

/**
 * Adapter that lets the shared PlayerPanel show the lab's hash table. The lab builds one
 * {@link Timeline} per operation with {@link #run}; the table itself lives in the lab.
 */
public class NameHashingVisualizer extends AbstractVisualizer<HashSnapshot> {

    /** One table operation to record. */
    public interface Operation {

        void run(HashTableModel table, HashStepSink sink);
    }

    private HashTableModel table;

    public NameHashingVisualizer(HashTableModel table) {
        super("Name Hashing Lab", Category.LABS, "Hash your own names.", HashSnapshot.class);
        this.table = table;
    }

    public void setTable(HashTableModel table) {
        this.table = table;
    }

    @Override
    public ComplexityInfo getComplexity() {
        return HashComplexity.info();
    }

    @Override
    public String[] getCounterLabels() {
        return new String[]{"Comparisons", "Probes / hops"};
    }

    /** A one-step timeline that just shows the table as it is now. */
    @Override
    public Timeline buildTimeline(String scenario) {
        StepRecorder rec = new StepRecorder();
        rec.record("Your hash table", "This is the table right now. Add names, then press Insert to watch each one find its spot.",
                table.snapshot().build());
        return Timeline.of(rec, 0, "");
    }

    /** Shows the table with a custom title / narration (used after reset or loading an example). */
    public Timeline still(String title, String narration) {
        StepRecorder rec = new StepRecorder();
        rec.record(title, narration, table.snapshot().build());
        return Timeline.of(rec, 0, "");
    }

    /**
     * Records the operation step by step on the real table. The same operation is first run on a
     * copy without any recording, to measure how long the computer really needs.
     */
    public Timeline run(String operationKey, Operation op) {
        HashTableModel copy = table.copy();
        long start = System.nanoTime();
        op.run(copy, HashStepSink.QUIET);
        long nanos = System.nanoTime() - start;

        StepRecorder rec = new StepRecorder();
        rec.setOperation(operationKey);
        op.run(table, HashStepSink.recording(rec, s -> s));
        return Timeline.of(rec, nanos, "");
    }

    @Override
    protected void paintState(Graphics2D g, HashSnapshot state, int width, int height) {
        HashTablePainter.paint(g, state, width, height);
    }
}
