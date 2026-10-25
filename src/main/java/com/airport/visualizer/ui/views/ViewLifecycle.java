package com.airport.visualizer.ui.views;

/** Optional hooks for screens: e.g. pause the animation when the learner leaves the screen. */
public interface ViewLifecycle {

    default void onShow() {
    }

    default void onHide() {
    }
}
