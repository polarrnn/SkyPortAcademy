package com.airport.visualizer.labs;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.core.Visualizer;
import com.airport.visualizer.input.FlightValidator;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.model.FlightType;
import com.airport.visualizer.structures.StructureDemo;
import com.airport.visualizer.structures.compare.MapComparisonVisualizer;
import com.airport.visualizer.structures.compare.SetComparisonVisualizer;
import com.airport.visualizer.structures.lists.ArrayListDemo;
import com.airport.visualizer.structures.lists.DequeDemo;
import com.airport.visualizer.structures.lists.LinkedListDemo;
import com.airport.visualizer.structures.lists.PriorityQueueDemo;
import com.airport.visualizer.structures.maps.HashMapDemo;
import com.airport.visualizer.structures.maps.LinkedHashMapDemo;
import com.airport.visualizer.structures.maps.TreeMapDemo;
import com.airport.visualizer.structures.sets.HashSetDemo;
import com.airport.visualizer.structures.sets.LinkedHashSetDemo;
import com.airport.visualizer.structures.sets.TreeSetDemo;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.FlightTable;
import com.airport.visualizer.ui.components.Icons;
import com.airport.visualizer.ui.components.MessageBanner;
import com.airport.visualizer.ui.components.RoundButton;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.player.PlayerPanel;
import com.airport.visualizer.ui.theme.Theme;
import com.airport.visualizer.ui.views.ViewHeader;
import com.airport.visualizer.ui.views.ViewLifecycle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * "My Flights Lab": the learner types their own flights (fully validated), then sends them into
 * any Map / Set / Collection demo and watches it work step by step.
 */
public class FlightCollectionsLabView extends JPanel implements ViewLifecycle {

    private static final int MAX_FLIGHTS = StructureDemo.MAX_FLIGHTS;

    /** Structure name -> how to build its demo from the learner's flights (Open/Closed: add a line to add a structure). */
    private static final Map<String, Function<List<Flight>, Visualizer>> STRUCTURES = new LinkedHashMap<>();

    static {
        STRUCTURES.put("HashMap (flight number -> flight)", HashMapDemo::new);
        STRUCTURES.put("TreeMap (time -> flight)", TreeMapDemo::new);
        STRUCTURES.put("LinkedHashMap (flight number -> flight)", LinkedHashMapDemo::new);
        STRUCTURES.put("HashSet (destinations)", HashSetDemo::new);
        STRUCTURES.put("TreeSet (airlines)", TreeSetDemo::new);
        STRUCTURES.put("LinkedHashSet (gates)", LinkedHashSetDemo::new);
        STRUCTURES.put("ArrayList", ArrayListDemo::new);
        STRUCTURES.put("LinkedList", LinkedListDemo::new);
        STRUCTURES.put("Deque (boarding line)", DequeDemo::new);
        STRUCTURES.put("PriorityQueue (next departure)", PriorityQueueDemo::new);
        STRUCTURES.put("Compare the 3 maps", MapComparisonVisualizer::new);
        STRUCTURES.put("Compare the 3 sets", SetComparisonVisualizer::new);
    }

    private final List<Flight> myFlights = new ArrayList<>();
    private final JTextField number = Ui.textField(8, "Flight number, e.g. PR102");
    private final JTextField airline = Ui.textField(12, "Airline name, e.g. Philippine Airlines");
    private final JTextField destination = Ui.textField(12, "City, e.g. Tokyo");
    private final JTextField gate = Ui.textField(6, "Gate, e.g. A3");
    private final JTextField time = Ui.textField(6, "24-hour time HH:mm, e.g. 08:15");
    private final JComboBox<FlightType> type = Ui.combo(FlightType.values(), "Is the flight leaving or arriving?");
    private final FlightTable table = new FlightTable();
    private final JComboBox<String> structure = Ui.combo(STRUCTURES.keySet().toArray(new String[0]), "Which structure should hold your flights?");
    private final JComboBox<String> operation = Ui.combo(new String[]{StructureDemo.ALL}, "Which operation to watch");
    private final MessageBanner banner = new MessageBanner();
    private final PlayerPanel player = new PlayerPanel();
    private final Card tableCard = new Card(new BorderLayout(0, 8));

    public FlightCollectionsLabView() {
        super(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        ViewHeader header = new ViewHeader(Category.LABS, "My Flights Lab",
                "Make your own airport board! Type flights, then drop them into a HashMap, TreeSet, Queue... and watch what happens step by step.");
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(header, BorderLayout.CENTER);
        north.add(banner, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        JPanel left = Ui.stack();
        left.add(full(formCard()));
        left.add(Box.createVerticalStrut(10));
        left.add(full(tableCard()));
        left.add(Box.createVerticalStrut(10));
        left.add(full(visualizeCard()));
        JComponent leftScroll = Ui.scroll(left);
        leftScroll.setPreferredSize(new Dimension(430, 400));
        add(leftScroll, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        center.add(player, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        structure.addActionListener(e -> refreshOperations());
        loadSample();
        banner.info("We loaded 6 sample flights. Add your own (max " + MAX_FLIGHTS + "), pick a structure and press Visualize.");
    }

    private static JComponent full(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private Card formCard() {
        Card card = new Card(new BorderLayout(0, 10));
        card.add(Ui.heading("1. Add a flight"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 8));
        grid.setOpaque(false);
        grid.add(Ui.labeled("Flight number", number));
        grid.add(Ui.labeled("Airline", airline));
        grid.add(Ui.labeled("Destination / from", destination));
        grid.add(Ui.labeled("Gate", gate));
        grid.add(Ui.labeled("Time (HH:mm)", time));
        grid.add(Ui.labeled("Type", type));
        card.add(grid, BorderLayout.CENTER);
        RoundButton add = new RoundButton("Add flight", Icons.of(Icons.Shape.PLUS), RoundButton.Variant.PRIMARY);
        add.setToolTipText("Check the fields and add the flight (Enter)");
        add.addActionListener(e -> addFlight());
        RoundButton sample = new RoundButton("Load sample", Icons.of(Icons.Shape.LIST), RoundButton.Variant.SECONDARY);
        sample.setToolTipText("Replace your list with 6 sample flights");
        sample.addActionListener(e -> {
            loadSample();
            banner.success("Sample flights loaded.");
        });
        RoundButton clear = new RoundButton("Clear all", Icons.of(Icons.Shape.TRASH), RoundButton.Variant.GHOST);
        clear.setToolTipText("Remove every flight from your list");
        clear.addActionListener(e -> {
            myFlights.clear();
            refreshTable();
            banner.info("Your list is empty. Add a flight to start.");
        });
        card.add(Ui.row(add, sample, clear), BorderLayout.SOUTH);
        for (JTextField f : new JTextField[]{number, airline, destination, gate, time}) {
            f.addActionListener(e -> addFlight());
        }
        return card;
    }

    private Card tableCard() {
        tableCard.add(Ui.heading("2. Your flights"), BorderLayout.NORTH);
        JComponent sp = table.inScrollPane();
        sp.setPreferredSize(new Dimension(380, 210));
        tableCard.add(sp, BorderLayout.CENTER);
        RoundButton remove = new RoundButton("Remove selected", Icons.of(Icons.Shape.TRASH), RoundButton.Variant.DANGER);
        remove.setToolTipText("Delete the highlighted row");
        remove.addActionListener(e -> removeSelected());
        tableCard.add(Ui.row(remove), BorderLayout.SOUTH);
        return tableCard;
    }

    private Card visualizeCard() {
        Card card = new Card(new BorderLayout(0, 10));
        card.add(Ui.heading("3. Pick a structure"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 8));
        grid.setOpaque(false);
        grid.add(Ui.labeled("Structure", structure));
        grid.add(Ui.labeled("Operation", operation));
        card.add(grid, BorderLayout.CENTER);
        RoundButton go = new RoundButton("Visualize", Icons.of(Icons.Shape.PLAY), RoundButton.Variant.SUCCESS);
        go.setToolTipText("Build the structure with your flights and play it");
        go.addActionListener(e -> visualize());
        card.add(Ui.row(go), BorderLayout.SOUTH);
        return card;
    }

    private void addFlight() {
        if (myFlights.size() >= MAX_FLIGHTS) {
            banner.error("Your board is full (" + MAX_FLIGHTS + " flights) so everything fits on screen. Remove one first.");
            return;
        }
        FlightValidator.Result result = FlightValidator.validate(number.getText(), airline.getText(), destination.getText(),
                gate.getText(), time.getText(), (FlightType) type.getSelectedItem(), myFlights);
        if (!result.isOk()) {
            banner.error(result.errorText());
            return;
        }
        Flight f = result.getFlight();
        myFlights.add(f);
        refreshTable();
        String warn = "";
        for (Flight other : myFlights) {
            if (other != f && other.getDepartureTime().equals(f.getDepartureTime())) {
                warn = " Note: " + other.getFlightNumber() + " has the same time - watch what a TreeMap keyed by time does!";
                break;
            }
        }
        banner.success("Added " + f.getFlightNumber() + " to " + f.getRouteText() + " at " + f.getTimeText() + "." + warn);
        for (JTextField t : new JTextField[]{number, airline, destination, gate, time}) {
            t.setText("");
        }
        number.requestFocusInWindow();
    }

    private void removeSelected() {
        Flight f = table.getSelectedFlight();
        if (f == null) {
            banner.warning(myFlights.isEmpty() ? "There are no flights to remove." : "Click a row in the table first, then press Remove selected.");
            return;
        }
        myFlights.remove(f);
        refreshTable();
        banner.success("Removed " + f.getFlightNumber() + ".");
    }

    private void loadSample() {
        myFlights.clear();
        myFlights.addAll(FlightData.first(6));
        refreshTable();
    }

    private void refreshTable() {
        table.setFlights(myFlights);
        refreshOperations();
    }

    private void refreshOperations() {
        Object keep = operation.getSelectedItem();
        Visualizer v = createDemo();
        List<String> scenarios = v.getScenarios();
        operation.setModel(new DefaultComboBoxModel<>(scenarios.toArray(new String[0])));
        if (keep != null && scenarios.contains(keep)) {
            operation.setSelectedItem(keep);
        }
    }

    private Visualizer createDemo() {
        String key = (String) structure.getSelectedItem();
        Function<List<Flight>, Visualizer> factory = STRUCTURES.get(key);
        if (factory == null) {
            factory = HashMapDemo::new;
        }
        return factory.apply(new ArrayList<>(myFlights));
    }

    private void visualize() {
        if (myFlights.isEmpty()) {
            banner.error("Add at least one flight (or press Load sample) before visualizing.");
            return;
        }
        try {
            Visualizer v = createDemo();
            String scenario = (String) operation.getSelectedItem();
            Timeline t = v.buildTimeline(scenario == null ? v.getScenarios().get(0) : scenario);
            player.load(v, t);
            banner.success("Showing " + v.getName() + " with your " + myFlights.size() + " flight(s): " + scenario
                    + ". Press Play or Step!");
            player.getController().play();
        } catch (RuntimeException ex) {
            banner.error("Something went wrong building this demo: " + ex.getMessage());
        }
    }

    @Override
    public void onHide() {
        player.getController().pause();
    }
}
