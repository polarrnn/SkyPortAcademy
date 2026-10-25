package com.airport.visualizer.ui.views;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.FlightTable;
import com.airport.visualizer.ui.components.RoundButton;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.nav.NavItem;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Welcome screen: what this app is, category cards, how to use the player, and today's flights. */
public class HomeView extends JPanel {

    public HomeView(List<NavItem> items, Consumer<NavItem> navigate) {
        super(new BorderLayout());
        setBackground(Theme.BG);
        setOpaque(true);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 22, 22, 22));
        content.add(new Hero(), BorderLayout.NORTH);

        Map<Category, List<NavItem>> grouped = new EnumMap<>(Category.class);
        for (NavItem item : items) {
            grouped.computeIfAbsent(item.getCategory(), c -> new ArrayList<>()).add(item);
        }
        JPanel cards = new JPanel(new GridLayout(0, 3, 16, 16));
        cards.setOpaque(false);
        for (Map.Entry<Category, List<NavItem>> e : grouped.entrySet()) {
            cards.add(categoryCard(e.getKey(), e.getValue(), navigate));
        }

        JPanel lower = new JPanel(new BorderLayout(18, 0));
        lower.setOpaque(false);
        JPanel help = new JPanel(new GridLayout(2, 1, 0, 16));
        help.setOpaque(false);
        help.add(howToCard());
        help.add(new LegendCard());
        help.setPreferredSize(new Dimension(420, 420));
        lower.add(help, BorderLayout.WEST);
        lower.add(boardCard(), BorderLayout.CENTER);

        JPanel middle = new JPanel(new BorderLayout(0, 18));
        middle.setOpaque(false);
        middle.add(cards, BorderLayout.NORTH);
        middle.add(lower, BorderLayout.CENTER);
        content.add(middle, BorderLayout.CENTER);
        add(Ui.scrollVertical(content), BorderLayout.CENTER);
    }

    private static JComponent categoryCard(Category cat, List<NavItem> items, Consumer<NavItem> navigate) {
        Card card = new Card(new BorderLayout(0, 8));
        card.setAccent(cat.getColor());
        card.padding(14, 20, 14, 16);
        JPanel top = new JPanel(new BorderLayout(0, 2));
        top.setOpaque(false);
        top.add(Ui.label(cat.getTitle(), Theme.bold(17f), Theme.TEXT), BorderLayout.NORTH);
        top.add(Ui.muted(cat.getTagline() + "  -  " + items.size() + (items.size() == 1 ? " lesson" : " lessons")), BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            names.append(items.get(i).getTitle());
        }
        JTextArea list = Ui.paragraph(names.toString(), 13f, Theme.TEXT_MUTED);
        list.setRows(2);
        card.add(list, BorderLayout.CENTER);
        RoundButton open = new RoundButton("Start " + cat.getTitle(), RoundButton.Variant.GHOST);
        open.setToolTipText("Open the first " + cat.getTitle() + " lesson");
        open.addActionListener(e -> navigate.accept(items.get(0)));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(open, BorderLayout.WEST);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private static JComponent howToCard() {
        Card card = new Card(new BorderLayout(0, 10));
        card.padding(14, 20, 14, 16);
        card.add(Ui.heading("How to use a lesson"), BorderLayout.NORTH);
        card.add(Ui.paragraph(
                "1. Pick a lesson on the left (or a card above).\n"
                + "2. Press Play to watch it move, or Step to go one tiny move at a time.\n"
                + "3. Read the yellow story box - it explains every move in simple words.\n"
                + "4. Use the Speed slider to go slower or faster, and Reset to start over.\n"
                + "5. Check the side tabs for Big-O, the flight table and the full story.\n"
                + "6. Ready to experiment? Open Practice Labs and type your own data!",
                14f, Theme.TEXT), BorderLayout.CENTER);
        return card;
    }

    private static JComponent boardCard() {
        Card card = new Card(new BorderLayout(0, 10));
        card.padding(14, 16, 14, 16);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Ui.heading("Today's departures & arrivals board"), BorderLayout.WEST);
        top.add(Ui.muted(FlightData.sample().size() + " sample flights used by every lesson"), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        FlightTable table = new FlightTable();
        table.setFlights(FlightData.sample());
        card.add(table.inScrollPane(), BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(600, 420));
        return card;
    }

    /** Painted banner with a night-sky gradient and a little plane. */
    private static final class Hero extends JComponent {

        Hero() {
            setPreferredSize(new Dimension(800, 168));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            int w = getWidth();
            int h = getHeight();
            g.setPaint(new GradientPaint(0, 0, Theme.SIDEBAR, w, h, Theme.PRIMARY_DARK));
            g.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, w, h, 22, 22));
            // runway dashes
            for (int x = w - 380; x < w - 20; x += 46) {
                Draw.roundRect(g, x, h - 30, 26, 6, 6, Theme.withAlpha(Color.WHITE, 60), null);
            }
            // flight path
            Draw.dashedLine(g, w - 360, h - 40, w - 120, 50, Theme.withAlpha(Theme.ACCENT, 160), 2f);
            Draw.plane(g, w - 110, 46, 58, Theme.ACCENT, -0.75);
            for (int i = 0; i < 18; i++) {
                double sx = (i * 137 % Math.max(1, w - 420)) + 380;
                double sy = (i * 53 % (h - 60)) + 14;
                g.setColor(Theme.withAlpha(Color.WHITE, 70 + (i * 19) % 90));
                g.fillOval((int) sx, (int) sy, 3, 3);
            }
            Draw.leftText(g, "WELCOME ABOARD", 32, 36, Theme.bold(12.5f), Theme.ACCENT);
            Draw.leftText(g, "SkyPort Algorithm Academy", 32, 72, Theme.bold(30f), Color.WHITE);
            Draw.paragraph(g, "Learn how computers sort, search and organise airport flights - one tiny, "
                    + "friendly step at a time. Every picture comes with a story a 9-year-old can follow.",
                    32, 96, Math.max(300, w - 480), Theme.regular(15f), Theme.tint(Theme.PRIMARY_SOFT, 0.2));
            g.dispose();
        }
    }

    /** Explains the color code used by every visual (never color alone: each color has a word). */
    private static final class LegendCard extends Card {

        LegendCard() {
            super(new BorderLayout());
            padding(14, 20, 14, 16);
            add(Ui.heading("What the colors mean"), BorderLayout.NORTH);
            add(new JComponent() {
                @Override
                protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Draw.setup(g);
                    Object[][] rows = {
                        {Theme.V_COMPARE, "Yellow - being compared (who is earlier?)"},
                        {Theme.V_SWAP, "Red - swapping places / being removed"},
                        {Theme.V_SORTED, "Green - finished, found, or safely stored"},
                        {Theme.V_ACTIVE, "Blue - the one we are working on now"},
                        {Theme.V_PIVOT, "Purple - a special helper (pivot, key, root)"},
                        {Theme.V_DIM, "Grey - not needed any more (ruled out)"},
                    };
                    int y = 14;
                    for (Object[] r : rows) {
                        Color c = (Color) r[0];
                        Draw.roundRect(g, 0, y - 9, 18, 18, 6, c, Theme.shade(c, 0.2));
                        Draw.leftText(g, (String) r[1], 30, y, Theme.regular(14f), Theme.TEXT);
                        y += 28;
                    }
                    g.dispose();
                }
            }, BorderLayout.CENTER);
        }
    }
}
