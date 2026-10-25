package com.airport.visualizer.ui.nav;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicButtonUI;

/** Dark navigation column: brand, Home, and collapsible category sections with their lessons. */
public class Sidebar extends JPanel {

    private final Consumer<NavItem> onNavigate;
    private final Runnable onHome;
    private final Map<NavItem, NavButton> buttons = new LinkedHashMap<>();
    private final Map<Category, JPanel> sectionBodies = new EnumMap<>(Category.class);
    private final Map<Category, SectionButton> sectionHeaders = new EnumMap<>(Category.class);
    private final NavButton homeButton;

    public Sidebar(List<NavItem> items, Consumer<NavItem> onNavigate, Runnable onHome) {
        super(new BorderLayout());
        this.onNavigate = onNavigate;
        this.onHome = onHome;
        setBackground(Theme.SIDEBAR);
        setOpaque(true);
        setPreferredSize(new Dimension(272, 600));

        add(new Brand(), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 12));

        homeButton = new NavButton("Home - start here", null, null);
        homeButton.addActionListener(e -> onHome.run());
        list.add(full(homeButton));
        list.add(Box.createVerticalStrut(8));

        Map<Category, List<NavItem>> grouped = new EnumMap<>(Category.class);
        for (NavItem item : items) {
            grouped.computeIfAbsent(item.getCategory(), c -> new ArrayList<>()).add(item);
        }
        for (Map.Entry<Category, List<NavItem>> e : grouped.entrySet()) {
            Category cat = e.getKey();
            SectionButton header = new SectionButton(cat, e.getValue().size());
            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 0));
            for (NavItem item : e.getValue()) {
                NavButton b = new NavButton(item.getTitle(), item.getSubtitle(), cat.getColor());
                b.addActionListener(ev -> onNavigate.accept(item));
                buttons.put(item, b);
                body.add(full(b));
                body.add(Box.createVerticalStrut(2));
            }
            body.setVisible(false);
            header.addActionListener(ev -> toggle(cat));
            sectionHeaders.put(cat, header);
            sectionBodies.put(cat, body);
            list.add(full(header));
            list.add(full(body));
            list.add(Box.createVerticalStrut(4));
        }
        list.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        com.airport.visualizer.ui.components.ThinScrollBarUI.apply(sp.getVerticalScrollBar(),
                Theme.withAlpha(Color.WHITE, 70), null);
        add(sp, BorderLayout.CENTER);

        add(new KeyHelp(), BorderLayout.SOUTH);
    }

    private static JComponent full(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private void toggle(Category cat) {
        JPanel body = sectionBodies.get(cat);
        boolean show = !body.isVisible();
        body.setVisible(show);
        sectionHeaders.get(cat).setExpanded(show);
        revalidate();
        repaint();
    }

    /** Marks the active entry and opens its section. */
    public void setActive(NavItem active) {
        homeButton.setActive(active == null);
        for (Map.Entry<NavItem, NavButton> e : buttons.entrySet()) {
            e.getValue().setActive(e.getKey() == active);
        }
        if (active != null) {
            JPanel body = sectionBodies.get(active.getCategory());
            if (body != null && !body.isVisible()) {
                toggle(active.getCategory());
            }
        }
    }

    /** Opens a category section (used by the Home screen cards). */
    public void expand(Category cat) {
        JPanel body = sectionBodies.get(cat);
        if (body != null && !body.isVisible()) {
            toggle(cat);
        }
    }

    // ------------------------------------------------------------------ widgets

    private static final class Brand extends JComponent {

        Brand() {
            setPreferredSize(new Dimension(260, 92));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            Draw.roundRect(g, 18, 22, 46, 46, 14, Theme.ACCENT, null);
            Draw.plane(g, 41, 45, 30, Theme.SIDEBAR, -Math.PI / 4);
            Draw.leftText(g, "SkyPort", 76, 36, Theme.bold(21f), Color.WHITE);
            Draw.leftText(g, "ALGORITHM ACADEMY", 77, 59, Theme.bold(11f), Theme.ACCENT);
            g.setColor(Theme.withAlpha(Color.WHITE, 30));
            g.fillRect(18, getHeight() - 1, getWidth() - 36, 1);
            g.dispose();
        }
    }

    private static final class KeyHelp extends JComponent {

        KeyHelp() {
            setPreferredSize(new Dimension(260, 64));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            g.setColor(Theme.withAlpha(Color.WHITE, 30));
            g.fillRect(18, 0, getWidth() - 36, 1);
            Draw.leftText(g, "KEYBOARD", 20, 18, Theme.bold(10.5f), Theme.SIDEBAR_MUTED);
            Draw.leftText(g, "Space  play / pause    R  reset", 20, 36, Theme.regular(12f), Theme.SIDEBAR_TEXT);
            Draw.leftText(g, "Left / Right arrows  step back / forward", 20, 53, Theme.regular(12f), Theme.SIDEBAR_TEXT);
            g.dispose();
        }
    }

    /** Category header with a colored dot, lesson count and a chevron. */
    private static final class SectionButton extends JButton {

        private final Category category;
        private final int count;
        private boolean expanded;

        SectionButton(Category category, int count) {
            this.category = category;
            this.count = count;
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(category.getTitle() + ": " + category.getTagline());
            getAccessibleContext().setAccessibleName(category.getTitle() + " section");
            setPreferredSize(new Dimension(240, 50));
        }

        void setExpanded(boolean expanded) {
            this.expanded = expanded;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            int w = getWidth();
            int h = getHeight();
            if (getModel().isRollover() || isFocusOwner()) {
                Draw.roundRect(g, 0, 2, w, h - 4, 12, Theme.SIDEBAR_HOVER, isFocusOwner() ? Theme.ACCENT : null);
            }
            Draw.roundRect(g, 12, h / 2.0 - 6, 12, 12, 12, category.getColor(), null);
            Draw.leftText(g, category.getTitle(), 34, h / 2.0 - 7, Theme.bold(14.5f), Color.WHITE);
            Draw.leftText(g, category.getTagline(), 34, h / 2.0 + 10, Theme.regular(11.5f), Theme.SIDEBAR_MUTED);
            String c = String.valueOf(count);
            Draw.roundRect(g, w - 58, h / 2.0 - 10, 26, 20, 20, Theme.withAlpha(Color.WHITE, 30), null);
            Draw.centeredText(g, c, w - 45, h / 2.0, Theme.bold(11f), Theme.SIDEBAR_TEXT);
            double cx = w - 18;
            double cy = h / 2.0;
            if (expanded) {
                Draw.line(g, cx - 5, cy - 2, cx, cy + 3, Theme.SIDEBAR_MUTED, 2f);
                Draw.line(g, cx, cy + 3, cx + 5, cy - 2, Theme.SIDEBAR_MUTED, 2f);
            } else {
                Draw.line(g, cx - 2, cy - 5, cx + 3, cy, Theme.SIDEBAR_MUTED, 2f);
                Draw.line(g, cx + 3, cy, cx - 2, cy + 5, Theme.SIDEBAR_MUTED, 2f);
            }
            g.dispose();
        }
    }

    /** A single lesson entry. */
    private static final class NavButton extends JButton {

        private final Color color;
        private boolean active;

        NavButton(String title, String subtitle, Color color) {
            super(title);
            this.color = color;
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(subtitle);
            setPreferredSize(new Dimension(230, 36));
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            int w = getWidth();
            int h = getHeight();
            if (active) {
                Draw.roundRect(g, 0, 1, w, h - 2, 10, Theme.SIDEBAR_ACTIVE, null);
                Draw.roundRect(g, 0, 7, 4, h - 14, 4, color == null ? Theme.ACCENT : color, null);
            } else if (getModel().isRollover()) {
                Draw.roundRect(g, 0, 1, w, h - 2, 10, Theme.SIDEBAR_HOVER, null);
            }
            if (isFocusOwner()) {
                Draw.roundRect(g, 1, 2, w - 2, h - 4, 10, null, Theme.ACCENT, 1.5f);
            }
            g.setFont(Theme.font(active ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 13.5f));
            String text = Draw.ellipsize(g, getText(), w - 28);
            Draw.leftText(g, text, 16, h / 2.0, g.getFont(), active ? Color.WHITE : Theme.SIDEBAR_TEXT);
            g.dispose();
        }
    }
}
