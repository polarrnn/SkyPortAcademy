package com.airport.visualizer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects {@link Step}s while an algorithm runs and keeps the comparison / swap counters.
 * Algorithms call {@link #compare()} / {@link #swap()} when they do real work and
 * {@link #record} whenever the picture should be shown to the learner.
 */
public final class StepRecorder {

    private final List<Step> steps = new ArrayList<>();
    private int primary;
    private int secondary;
    private String operationKey;

    /** Count one comparison (or the visualizer's primary counter). */
    public void compare() {
        primary++;
    }

    public void compare(int n) {
        primary += n;
    }

    /** Count one swap / move / visit (the visualizer's secondary counter). */
    public void swap() {
        secondary++;
    }

    public void swap(int n) {
        secondary += n;
    }

    public int getPrimary() {
        return primary;
    }

    public int getSecondary() {
        return secondary;
    }

    public void resetCounters() {
        primary = 0;
        secondary = 0;
    }

    /** The operation (Big-O row key) the following steps belong to, e.g. "put" or "remove". */
    public void setOperation(String key) {
        this.operationKey = key;
    }

    public String getOperation() {
        return operationKey;
    }

    public void record(String title, String narration, Object state) {
        steps.add(new Step(title, narration, state, primary, secondary, operationKey));
    }

    public int size() {
        return steps.size();
    }

    public List<Step> getSteps() {
        return Collections.unmodifiableList(new ArrayList<>(steps));
    }
}
