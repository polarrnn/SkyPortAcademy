package com.airport.visualizer.ui.components;

import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightType;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Styled JTable showing flights like an airport departures board. Rows can be highlighted
 * (by flight number) to mirror what the animation is doing.
 */
public class FlightTable extends JTable {

    private static final String[] COLUMNS = {"#", "Flight", "Airline", "Type", "Destination", "Gate", "Time"};

    private final FlightModel model = new FlightModel();
    private Set<String> highlighted = Collections.emptySet();

    public FlightTable() {
        setModel(model);
        setRowHeight(30);
        setFont(Theme.regular(13f));
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(Theme.BORDER);
        setIntercellSpacing(new Dimension(0, 1));
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionBackground(Theme.PRIMARY_SOFT);
        setSelectionForeground(Theme.TEXT);
        setAutoCreateRowSorter(false);
        getTableHeader().setReorderingAllowed(false);
        JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(10, 32));
        setDefaultRenderer(Object.class, new RowRenderer());
        int[] widths = {34, 70, 150, 90, 120, 50, 60};
        for (int i = 0; i < widths.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        getColumnModel().getColumn(0).setMaxWidth(44);
    }

    public void setFlights(List<Flight> flights) {
        model.setFlights(flights);
    }

    public List<Flight> getFlights() {
        return model.flights;
    }

    public Flight getSelectedFlight() {
        int row = getSelectedRow();
        return row < 0 || row >= model.flights.size() ? null : model.flights.get(row);
    }

    public void setHighlighted(Collection<String> flightNumbers) {
        highlighted = flightNumbers == null ? Collections.<String>emptySet() : new HashSet<>(flightNumbers);
        repaint();
        // keep the first highlighted row visible
        for (int i = 0; i < model.flights.size(); i++) {
            if (highlighted.contains(model.flights.get(i).getFlightNumber())) {
                scrollRectToVisible(getCellRect(i, 0, true));
                break;
            }
        }
    }

    /** Wraps the table in a borderless scroll pane. */
    public JScrollPane inScrollPane() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Theme.SURFACE);
        return sp;
    }

    private static final class FlightModel extends AbstractTableModel {

        private List<Flight> flights = new ArrayList<>();

        void setFlights(List<Flight> list) {
            flights = list == null ? new ArrayList<Flight>() : new ArrayList<>(list);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return flights.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Flight f = flights.get(row);
            switch (col) {
                case 0:
                    return row + 1;
                case 1:
                    return f.getFlightNumber();
                case 2:
                    return f.getAirline();
                case 3:
                    return f.getType();
                case 4:
                    return f.getRouteText();
                case 5:
                    return f.getGate();
                default:
                    return f.getTimeText();
            }
        }
    }

    private final class RowRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            l.setFont(column == 1 ? Theme.bold(13f) : Theme.regular(13f));
            l.setHorizontalAlignment(column == 0 || column == 5 || column == 6 ? SwingConstants.CENTER : SwingConstants.LEFT);
            Flight f = model.flights.get(row);
            boolean hi = highlighted.contains(f.getFlightNumber());
            Color bg = row % 2 == 0 ? Theme.SURFACE : Theme.SURFACE_ALT;
            Color fg = Theme.TEXT;
            if (hi) {
                bg = Theme.ACCENT_SOFT;
                l.setFont(Theme.bold(13f));
            }
            if (isSelected) {
                bg = Theme.PRIMARY_SOFT;
            }
            if (column == 3) {
                fg = f.getType() == FlightType.DEPARTURE ? Theme.PRIMARY_DARK : Theme.TEAL;
                l.setFont(Theme.bold(12.5f));
            }
            if (column == 0) {
                fg = Theme.TEXT_MUTED;
            }
            l.setBackground(bg);
            l.setForeground(fg);
            return l;
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, column);
            l.setOpaque(true);
            l.setBackground(Theme.SIDEBAR);
            l.setForeground(Theme.ACCENT);
            l.setFont(Theme.bold(12.5f));
            l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            l.setHorizontalAlignment(column == 0 || column == 5 || column == 6 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return l;
        }
    }
}
