package com.airport.visualizer.ui.views;

import com.airport.visualizer.app.LessonCatalog;
import com.airport.visualizer.ui.components.MessageBanner;
import com.airport.visualizer.ui.nav.NavItem;
import com.airport.visualizer.ui.nav.Sidebar;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * The whole application surface: sidebar on the left, the current screen on the right.
 * It is a plain JPanel bean with a no-argument constructor, so NetBeans can render it inside
 * MainFrame's Design tab.
 */
public class AppShell extends JPanel {

    private static final String HOME = "home";

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<NavItem, String> keys = new HashMap<>();
    private final Map<String, JComponent> views = new HashMap<>();
    private final Sidebar sidebar;
    private JComponent current;

    public AppShell() {
        super(new BorderLayout());
        setBackground(Theme.BG);
        setPreferredSize(new Dimension(1280, 760));

        List<NavItem> items = LessonCatalog.items();
        sidebar = new Sidebar(items, this::navigate, this::showHome);
        add(sidebar, BorderLayout.WEST);

        content.setBackground(Theme.BG);
        HomeView home = new HomeView(items, this::navigate);
        content.add(home, HOME);
        views.put(HOME, home);
        add(content, BorderLayout.CENTER);
        current = home;
        sidebar.setActive(null);
    }

    public void showHome() {
        switchTo(HOME);
        sidebar.setActive(null);
    }

    /** Opens a lesson or lab, building its screen the first time (lazy loading keeps start-up fast). */
    public void navigate(NavItem item) {
        String key = keys.get(item);
        if (key == null) {
            key = "view" + keys.size();
            JComponent view;
            try {
                view = item.createView();
            } catch (RuntimeException ex) {
                view = errorView(item, ex);
            }
            keys.put(item, key);
            views.put(key, view);
            content.add(view, key);
        }
        switchTo(key);
        sidebar.setActive(item);
    }

    private void switchTo(String key) {
        if (current instanceof ViewLifecycle) {
            ((ViewLifecycle) current).onHide();
        }
        cards.show(content, key);
        current = views.get(key);
        if (current instanceof ViewLifecycle) {
            ((ViewLifecycle) current).onShow();
        }
        focusFirst(current);
    }

    private static void focusFirst(Component c) {
        if (c instanceof JComponent) {
            ((JComponent) c).requestFocusInWindow();
        }
    }

    private static JComponent errorView(NavItem item, RuntimeException ex) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        MessageBanner banner = new MessageBanner();
        banner.error("\"" + item.getTitle() + "\" could not be opened (" + ex.getClass().getSimpleName()
                + ": " + ex.getMessage() + "). The rest of the app still works - pick another lesson.");
        p.add(banner, BorderLayout.NORTH);
        return p;
    }
}
