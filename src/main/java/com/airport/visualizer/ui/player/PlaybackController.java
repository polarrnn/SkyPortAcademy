package com.airport.visualizer.ui.player;

import com.airport.visualizer.core.Step;
import com.airport.visualizer.core.Timeline;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

/**
 * Owns "where are we in the animation?" and the Swing timer that moves forward while playing.
 * It knows nothing about painting or buttons (Single Responsibility).
 */
public final class PlaybackController {

    /** Notified on the Swing event thread. */
    public interface Listener {

        void stepChanged(int index, Step step);

        default void playStateChanged(boolean playing) {
        }

        default void timelineLoaded(Timeline timeline) {
        }
    }

    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 10;

    private final List<Listener> listeners = new ArrayList<>();
    private final Timer timer;
    private Timeline timeline = Timeline.empty("");
    private int index = -1;
    private int speed = 5;
    private long accumulatedNanos;
    private long playStartedNanos = -1;

    public PlaybackController() {
        timer = new Timer(delayFor(speed), e -> tick());
        timer.setInitialDelay(150);
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void load(Timeline t) {
        pause();
        timeline = t == null ? Timeline.empty("") : t;
        accumulatedNanos = 0;
        index = timeline.isEmpty() ? -1 : 0;
        for (Listener l : listeners) {
            l.timelineLoaded(timeline);
        }
        fireStep();
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public int getIndex() {
        return index;
    }

    public Step getCurrentStep() {
        return index < 0 || index >= timeline.size() ? null : timeline.get(index);
    }

    public boolean isPlaying() {
        return timer.isRunning();
    }

    public boolean isAtEnd() {
        return index >= timeline.size() - 1;
    }

    public boolean isAtStart() {
        return index <= 0;
    }

    public void play() {
        if (timeline.isEmpty() || timer.isRunning()) {
            return;
        }
        if (isAtEnd()) {
            index = 0;
            accumulatedNanos = 0;
            fireStep();
        }
        playStartedNanos = System.nanoTime();
        timer.start();
        firePlayState();
    }

    public void pause() {
        if (!timer.isRunning()) {
            return;
        }
        timer.stop();
        if (playStartedNanos >= 0) {
            accumulatedNanos += System.nanoTime() - playStartedNanos;
            playStartedNanos = -1;
        }
        firePlayState();
    }

    public void togglePlay() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void stepForward() {
        pause();
        if (!isAtEnd()) {
            index++;
            fireStep();
        }
    }

    public void stepBack() {
        pause();
        if (index > 0) {
            index--;
            fireStep();
        }
    }

    public void jumpTo(int newIndex) {
        if (timeline.isEmpty()) {
            return;
        }
        int clamped = Math.max(0, Math.min(timeline.size() - 1, newIndex));
        if (clamped != index) {
            index = clamped;
            fireStep();
        }
    }

    public void reset() {
        pause();
        accumulatedNanos = 0;
        index = timeline.isEmpty() ? -1 : 0;
        fireStep();
    }

    /** 1 (slow, about 2 s per step) .. 10 (fast, about 0.1 s per step). */
    public void setSpeed(int s) {
        speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, s));
        timer.setDelay(delayFor(speed));
    }

    public int getSpeed() {
        return speed;
    }

    public int getDelayMillis() {
        return delayFor(speed);
    }

    /** Time spent playing the animation so far. */
    public long getElapsedNanos() {
        long running = playStartedNanos >= 0 ? System.nanoTime() - playStartedNanos : 0;
        return accumulatedNanos + running;
    }

    /** Stops the timer for good (call when the screen is thrown away). */
    public void dispose() {
        timer.stop();
    }

    private static int delayFor(int speed) {
        return (int) Math.round(2000 * Math.pow(0.72, speed - 1));
    }

    private void tick() {
        if (isAtEnd()) {
            pause();
            return;
        }
        index++;
        fireStep();
        if (isAtEnd()) {
            pause();
        }
    }

    private void fireStep() {
        Step s = getCurrentStep();
        for (Listener l : listeners) {
            l.stepChanged(index, s);
        }
    }

    private void firePlayState() {
        boolean playing = isPlaying();
        for (Listener l : listeners) {
            l.playStateChanged(playing);
        }
    }
}
