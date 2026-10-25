package com.airport.visualizer.core;

import com.airport.visualizer.ui.theme.Draw;
import java.awt.Graphics2D;

/**
 * Base class that holds the common descriptive fields and does the type-safe cast from a
 * {@link Step}'s state to the visualizer's own snapshot type S.
 *
 * @param <S> the snapshot type this visualizer records and paints
 */
public abstract class AbstractVisualizer<S> implements Visualizer {

    private final String name;
    private final Category category;
    private final String summary;
    private final Class<S> stateType;

    protected AbstractVisualizer(String name, Category category, String summary, Class<S> stateType) {
        this.name = name;
        this.category = category;
        this.summary = summary;
        this.stateType = stateType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public final void paint(Graphics2D g, Step step, int width, int height) {
        Draw.setup(g);
        Object state = step == null ? null : step.getState();
        if (stateType.isInstance(state)) {
            paintState(g, stateType.cast(state), width, height);
        } else {
            Draw.emptyMessage(g, width, height, "Press Play or Step to start the adventure!");
        }
    }

    /** Paint the given snapshot. Called with anti-aliasing already enabled. */
    protected abstract void paintState(Graphics2D g, S state, int width, int height);
}
