package com.airport.visualizer.hashing.lab;

import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 * The name queue: one fixed-height row per name with its status badge on the SAME row.
 * Rows are painted to the list's real width, so long names get "..." instead of being clipped,
 * and the list follows the scroll pane's width (no sideways scrolling).
 */
public class NameQueueList extends JList<NameEntry> {

    public NameQueueList(DefaultListModel<NameEntry> model) {
        super(model);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFixedCellHeight(40);
        setCellRenderer(new Renderer());
        setBackground(Theme.SURFACE);
        setVisibleRowCount(6);
        getAccessibleContext().setAccessibleName("Name queue");
    }

    /** Keep preferred width small so the list always tracks the viewport width. */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(200, getFixedCellHeight() * getVisibleRowCount());
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int i = locationToIndex(e.getPoint());
        if (i < 0 || !getCellBounds(i, i).contains(e.getPoint())) {
            return null;
        }
        NameEntry n = getModel().getElementAt(i);
        return n.getName() + " - " + n.getStatus().getLabel() + (n.getDetail().isEmpty() ? "" : " (" + n.getDetail() + ")");
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (getModel().getSize() == 0) {
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            Draw.centeredText(g, "No names yet - type some above!", getWidth() / 2.0, 30, Theme.regular(13f), Theme.TEXT_MUTED);
            g.dispose();
        }
    }

    private static final class Renderer extends JComponent implements ListCellRenderer<NameEntry> {

        private NameEntry entry;
        private int index;
        private boolean selected;

        Renderer() {
            setPreferredSize(new Dimension(120, 42));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends NameEntry> list, NameEntry value, int index,
                boolean isSelected, boolean cellHasFocus) {
            this.entry = value;
            this.index = index;
            this.selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (entry == null) {
                return;
            }
            Graphics2D g = (Graphics2D) g0.create();
            Draw.setup(g);
            int w = getWidth();
            int h = getHeight();
            Color bg = selected ? Theme.PRIMARY_SOFT : (entry.getStatus() == NameStatus.CURRENT ? Theme.tint(Theme.PRIMARY, 0.92)
                    : (index % 2 == 0 ? Theme.SURFACE : Theme.SURFACE_ALT));
            g.setColor(bg);
            g.fillRect(0, 0, w, h);
            g.setColor(Theme.BORDER);
            g.fillRect(0, h - 1, w, 1);
            if (selected) {
                g.setColor(Theme.PRIMARY);
                g.fillRect(0, 0, 4, h);
            }

            // status badge (right side, same row)
            NameStatus st = entry.getStatus();
            Font bf = Theme.bold(11f);
            g.setFont(bf);
            String label = st.getLabel().toUpperCase();
            int bw = g.getFontMetrics().stringWidth(label) + 18;
            int bx = w - bw - 10;
            Draw.roundRect(g, bx, h / 2.0 - 11, bw, 22, 22, Theme.tint(st.getColor(), 0.85), Theme.tint(st.getColor(), 0.45), 1f);
            Draw.centeredText(g, label, bx + bw / 2.0, h / 2.0, bf, Theme.shade(st.getColor(), 0.35));

            // number + name + detail, ellipsized to the space left of the badge
            String num = (index + 1) + ".";
            Draw.leftText(g, num, 12, h / 2.0, Theme.regular(12f), Theme.TEXT_MUTED);
            int textX = 36;
            int maxW = Math.max(20, bx - textX - 8);
            Font nf = Theme.bold(13.5f);
            g.setFont(nf);
            String name = Draw.ellipsize(g, entry.getName(), maxW);
            if (entry.getDetail().isEmpty()) {
                Draw.leftText(g, name, textX, h / 2.0, nf, Theme.TEXT);
            } else {
                Draw.leftText(g, name, textX, h / 2.0 - 8, nf, Theme.TEXT);
                Font df = Theme.regular(11.5f);
                g.setFont(df);
                Draw.leftText(g, Draw.ellipsize(g, entry.getDetail(), maxW), textX, h / 2.0 + 10, df, Theme.TEXT_MUTED);
            }
            g.dispose();
        }
    }
}
