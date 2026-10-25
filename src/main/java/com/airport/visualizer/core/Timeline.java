package com.airport.visualizer.core;

import java.util.Collections;
import java.util.List;

/** All the steps of one run plus how long the real (un-animated) algorithm took. */
public final class Timeline {

    private final List<Step> steps;
    private final long realRunNanos;
    private final String resultSummary;

    public Timeline(List<Step> steps, long realRunNanos, String resultSummary) {
        this.steps = steps == null ? Collections.<Step>emptyList() : Collections.unmodifiableList(steps);
        this.realRunNanos = Math.max(0, realRunNanos);
        this.resultSummary = resultSummary == null ? "" : resultSummary;
    }

    public static Timeline of(StepRecorder recorder, long realRunNanos, String resultSummary) {
        return new Timeline(recorder.getSteps(), realRunNanos, resultSummary);
    }

    public static Timeline empty(String message) {
        return new Timeline(Collections.<Step>emptyList(), 0, message);
    }

    public List<Step> getSteps() {
        return steps;
    }

    public int size() {
        return steps.size();
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public Step get(int index) {
        return steps.get(index);
    }

    /** Time the plain algorithm needs on the same data without drawing anything. */
    public long getRealRunNanos() {
        return realRunNanos;
    }

    public String getResultSummary() {
        return resultSummary;
    }
}
