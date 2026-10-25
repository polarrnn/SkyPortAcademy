package com.airport.visualizer.ui.nav;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.Visualizer;
import com.airport.visualizer.ui.views.VisualizerView;
import java.util.function.Supplier;
import javax.swing.JComponent;

/** One entry in the sidebar: a title, its category and a factory that builds its screen on demand. */
public final class NavItem {

    private final String title;
    private final String subtitle;
    private final Category category;
    private final Supplier<JComponent> factory;

    public NavItem(String title, String subtitle, Category category, Supplier<JComponent> factory) {
        this.title = title;
        this.subtitle = subtitle;
        this.category = category;
        this.factory = factory;
    }

    /** A standard lesson: any Visualizer shown in the generic {@link VisualizerView}. */
    public static NavItem lesson(Visualizer v) {
        return new NavItem(v.getName(), v.getSummary(), v.getCategory(), () -> new VisualizerView(v));
    }

    /** A custom screen such as a practice lab or comparison view. */
    public static NavItem screen(String title, String subtitle, Category category, Supplier<JComponent> factory) {
        return new NavItem(title, subtitle, category, factory);
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Category getCategory() {
        return category;
    }

    public JComponent createView() {
        return factory.get();
    }
}
