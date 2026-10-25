package com.airport.visualizer.labs;

import com.airport.visualizer.core.AbstractVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.input.InputParser;
import com.airport.visualizer.input.ParseResult;
import com.airport.visualizer.trees.BstEngine;
import com.airport.visualizer.trees.BstState;
import com.airport.visualizer.trees.BstVisualizer;
import com.airport.visualizer.trees.HeapEngine;
import com.airport.visualizer.trees.HeapState;
import com.airport.visualizer.trees.HeapVisualizer;
import com.airport.visualizer.trees.TraversalEngine;
import com.airport.visualizer.trees.TraversalState;
import com.airport.visualizer.trees.TraversalVisualizer;
import com.airport.visualizer.trees.TreeItem;
import com.airport.visualizer.trees.Words;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.Icons;
import com.airport.visualizer.ui.components.MessageBanner;
import com.airport.visualizer.ui.components.RoundButton;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.player.PlayerPanel;
import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import com.airport.visualizer.ui.views.ViewHeader;
import com.airport.visualizer.ui.views.ViewLifecycle;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Practice lab: the learner types numbers and grows their own Binary Search Tree or Min-Heap.
 * The structure is kept between operations; every operation is animated step by step.
 * All input is validated, so typos, duplicates or a full tree never crash the app.
 */
public class TreeLabView extends JPanel implements ViewLifecycle {

    private static final String MODE_BST = "Binary Search Tree";
    private static final String MODE_HEAP = "Min-Heap";
    private static final int MAX_NODES = 15;

    private final BstEngine bst = new BstEngine(Words.NUMBERS, null);
    private final HeapEngine heap = new HeapEngine(Words.NUMBERS, null);
    private final LabVisualizer visualizer = new LabVisualizer();
    private final PlayerPanel player = new PlayerPanel();
    private final MessageBanner banner = new MessageBanner();
    private final JComboBox<String> mode = Ui.combo(new String[]{MODE_BST, MODE_HEAP}, "Which structure to grow");
    private final JTextField numbers = Ui.textField(22, "One number or several separated by commas, 0 to 999");
    private final JComboBox<TraversalEngine.Order> order = Ui.combo(TraversalEngine.Order.values(), "Visiting order for Traverse");
    private final RoundButton insert = new RoundButton("Insert", Icons.of(Icons.Shape.PLUS), RoundButton.Variant.PRIMARY);
    private final RoundButton search = new RoundButton("Search", Icons.of(Icons.Shape.SEARCH), RoundButton.Variant.SECONDARY);
    private final RoundButton delete = new RoundButton("Delete", Icons.of(Icons.Shape.TRASH), RoundButton.Variant.DANGER);
    private final RoundButton traverse = new RoundButton("Traverse", Icons.of(Icons.Shape.LIST), RoundButton.Variant.SECONDARY);
    private final RoundButton random = new RoundButton("Random tree", Icons.of(Icons.Shape.SHUFFLE), RoundButton.Variant.SECONDARY);
    private final RoundButton clear = new RoundButton("Clear", Icons.of(Icons.Shape.RESET), RoundButton.Variant.GHOST);
    private final JLabel status = Ui.muted("");
    private final Random rnd = new Random();

    public TreeLabView() {
        super(new BorderLayout(0, 12));
        setOpaque(true);
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        ViewHeader header = new ViewHeader(Category.LABS, "Tree Lab",
                "Type your own numbers and grow a Binary Search Tree (small numbers go left, big ones go right) "
                + "or a Min-Heap (the smallest number always sits on top). Every move is explained step by step.");

        Card input = new Card(new BorderLayout(0, 8));
        input.padding(14, 18, 14, 18);
        numbers.getAccessibleContext().setAccessibleName("Numbers to use");
        insert.setToolTipText("Add the typed numbers (Enter)");
        search.setToolTipText("Find one typed number in the tree");
        delete.setToolTipText("BST: delete the typed number. Heap: remove the smallest number.");
        traverse.setToolTipText("Visit every number in the chosen order");
        random.setToolTipText("Start over with 7 random numbers");
        clear.setToolTipText("Remove everything and start again");
        JPanel row1 = Ui.row(Ui.labeled("Structure", mode), Ui.labeled("Numbers (e.g. 50, 30, 70, 20)", numbers), insert);
        JPanel row2 = Ui.row(search, delete, Ui.labeled("Order", order), traverse, random, clear);
        JPanel rows = Ui.stack();
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        status.setAlignmentX(LEFT_ALIGNMENT);
        status.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 0));
        rows.add(row1);
        rows.add(row2);
        rows.add(status);
        input.add(rows, BorderLayout.CENTER);
        banner.setAlignmentX(LEFT_ALIGNMENT);
        input.add(banner, BorderLayout.SOUTH);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        north.add(input, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
        player.setPreferredSize(new Dimension(800, 520));
        add(player, BorderLayout.CENTER);

        insert.addActionListener(e -> safely(this::doInsert));
        numbers.addActionListener(e -> safely(this::doInsert));
        search.addActionListener(e -> safely(this::doSearch));
        delete.addActionListener(e -> safely(this::doDelete));
        traverse.addActionListener(e -> safely(this::doTraverse));
        random.addActionListener(e -> safely(this::doRandom));
        clear.addActionListener(e -> safely(this::doClear));
        mode.addActionListener(e -> safely(this::modeChanged));

        modeChanged();
        banner.info("Type one number or several separated by commas, then press Insert. Or press Random tree to get started.");
    }

    private boolean isBst() {
        return MODE_BST.equals(mode.getSelectedItem());
    }

    private int structureSize() {
        return isBst() ? bst.size() : heap.size();
    }

    // ------------------------------------------------------------------ actions

    private void doInsert() {
        ParseResult<Integer> r = InputParser.parseIntegers(numbers.getText(), MAX_NODES, 0, 999, false);
        if (!r.isOk()) {
            banner.error(r.errorText());
            numbers.requestFocusInWindow();
            return;
        }
        List<String> notes = new ArrayList<>(r.getWarnings());
        List<Integer> values = new ArrayList<>();
        Set<Integer> already = new LinkedHashSet<>();
        for (Integer v : r.getValues()) {
            if (isBst() && bst.contains(v)) {
                already.add(v);
            } else {
                values.add(v);
            }
        }
        if (!already.isEmpty()) {
            notes.add("Already in your tree (a BST keeps one of each): " + joinInts(already) + ".");
        }
        int room = MAX_NODES - structureSize();
        if (room <= 0) {
            banner.error("Your " + (isBst() ? "tree" : "heap") + " is full (" + MAX_NODES + " numbers) so everything fits on screen. "
                    + "Delete something or press Clear first.");
            return;
        }
        if (values.isEmpty()) {
            banner.warning(String.join(" ", notes));
            return;
        }
        if (values.size() > room) {
            notes.add("Only room for " + room + " more, so these were skipped: " + joinInts(values.subList(room, values.size())) + ".");
            values = new ArrayList<>(values.subList(0, room));
        }
        StepRecorder rec = new StepRecorder();
        for (Integer v : values) {
            if (isBst()) {
                bst.insert(TreeItem.of(v), rec);
            } else {
                heap.insert(TreeItem.of(v), rec);
            }
        }
        play(rec, isBst() ? "insert" : "heap");
        numbers.setText("");
        String done = "Inserted " + joinInts(values) + ". Press Play to watch where each one goes.";
        if (notes.isEmpty()) {
            banner.success(done);
        } else {
            banner.warning(done + " " + String.join(" ", notes));
        }
    }

    private void doSearch() {
        if (!isBst()) {
            banner.info("A heap is not built for searching - only the smallest number is easy to find. Switch to Binary Search Tree.");
            return;
        }
        if (bst.size() == 0) {
            banner.warning("Your tree is empty - insert some numbers first.");
            return;
        }
        ParseResult<Integer> r = InputParser.parseSingleInteger(numbers.getText(), 0, 999);
        if (!r.isOk()) {
            banner.error(r.getErrors().isEmpty() ? "Type the one number you want to find." : r.errorText());
            numbers.requestFocusInWindow();
            return;
        }
        int v = r.getValues().get(0);
        StepRecorder rec = new StepRecorder();
        boolean found = bst.search(TreeItem.of(v), rec);
        play(rec, "insert");
        if (found) {
            banner.success("Found " + v + " after " + rec.getPrimary() + " comparisons (out of " + bst.size() + " numbers).");
        } else {
            banner.warning(v + " is not in your tree - we knew after only " + rec.getPrimary() + " comparisons.");
        }
    }

    private void doDelete() {
        StepRecorder rec = new StepRecorder();
        if (!isBst()) {
            if (heap.size() == 0) {
                banner.warning("Your heap is empty - there is nothing to remove.");
                return;
            }
            TreeItem min = heap.removeMin(rec);
            play(rec, "heap");
            banner.success("Removed the smallest number, " + min.getLabel() + ". A heap can only take from the top.");
            return;
        }
        if (bst.size() == 0) {
            banner.warning("Your tree is empty - there is nothing to delete.");
            return;
        }
        ParseResult<Integer> r = InputParser.parseSingleInteger(numbers.getText(), 0, 999);
        if (!r.isOk()) {
            banner.error(r.getErrors().isEmpty() ? "Type the one number you want to delete." : r.errorText());
            numbers.requestFocusInWindow();
            return;
        }
        int v = r.getValues().get(0);
        boolean removed = bst.delete(TreeItem.of(v), rec);
        play(rec, "insert");
        if (removed) {
            banner.success("Deleted " + v + ". Watch which of the three delete cases it was.");
            numbers.setText("");
        } else {
            banner.warning(v + " is not in your tree, so nothing was deleted.");
        }
    }

    private void doTraverse() {
        if (!isBst()) {
            banner.info("Traversals are shown on the Binary Search Tree. Switch the structure to try them.");
            return;
        }
        if (bst.size() == 0) {
            banner.warning("Your tree is empty - insert some numbers first.");
            return;
        }
        TraversalEngine.Order o = (TraversalEngine.Order) order.getSelectedItem();
        StepRecorder rec = new StepRecorder();
        List<String> out = new TraversalEngine(bst.getRoot(), Words.NUMBERS, null).run(o, rec);
        play(rec, "traverse");
        banner.success(o.getLabel() + ": " + String.join(", ", out));
    }

    private void doRandom() {
        Set<Integer> picks = new LinkedHashSet<>();
        while (picks.size() < 7) {
            picks.add(1 + rnd.nextInt(99));
        }
        if (isBst()) {
            bst.clear();
        } else {
            heap.clear();
        }
        numbers.setText(joinInts(picks));
        doInsert();
    }

    private void doClear() {
        if (isBst()) {
            bst.clear();
        } else {
            heap.clear();
        }
        showCurrent();
        banner.info("Cleared. Type new numbers and press Insert.");
    }

    private void modeChanged() {
        boolean b = isBst();
        search.setEnabled(b);
        traverse.setEnabled(b);
        order.setEnabled(b);
        delete.setText(b ? "Delete" : "Remove smallest");
        showCurrent();
        banner.clear();
    }

    // ------------------------------------------------------------------ helpers

    private void showCurrent() {
        StepRecorder rec = new StepRecorder();
        if (isBst()) {
            rec.setOperation("insert");
            rec.record(bst.size() == 0 ? "Plant your tree" : "Your tree", bst.size() == 0
                    ? "Type numbers and press Insert. Small numbers go to the left, big numbers go to the right."
                    : "This is your tree with " + bst.size() + " numbers. Try Search, Delete or Traverse!",
                    bst.plainState("Your Binary Search Tree"));
            play(rec, "insert");
        } else {
            rec.setOperation("insert");
            rec.record(heap.size() == 0 ? "Start your heap" : "Your heap", heap.size() == 0
                    ? "Type numbers and press Insert. The smallest number will always float to the top."
                    : "This is your heap with " + heap.size() + " numbers. The smallest is on top.",
                    heap.plainState("Your Min-Heap"));
            play(rec, "heap");
        }
    }

    private void play(StepRecorder rec, String kind) {
        visualizer.configure(kind);
        player.load(visualizer, Timeline.of(rec, 0, ""));
        updateStatus();
    }

    private void updateStatus() {
        if (isBst()) {
            List<String> labels = new ArrayList<>();
            for (TreeItem t : bst.inOrder()) {
                labels.add(t.getLabel());
            }
            status.setText(bst.size() == 0 ? "Your tree is empty (room for " + MAX_NODES + " numbers)."
                    : "Your tree has " + bst.size() + "/" + MAX_NODES + " numbers. Sorted (in-order): " + String.join(", ", labels));
        } else {
            List<String> labels = new ArrayList<>();
            for (TreeItem t : heap.items()) {
                labels.add(t.getLabel());
            }
            status.setText(heap.size() == 0 ? "Your heap is empty (room for " + MAX_NODES + " numbers)."
                    : "Your heap has " + heap.size() + "/" + MAX_NODES + " numbers. Array: [" + String.join(", ", labels) + "]");
        }
    }

    private void safely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            banner.error("Something went wrong, but your work is safe: " + ex.getMessage());
        }
    }

    private static String joinInts(Iterable<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (Integer v : values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(v);
        }
        return sb.toString();
    }

    @Override
    public void onHide() {
        player.getController().pause();
    }

    /** Paints whichever tree snapshot the current lab run produced. */
    private static final class LabVisualizer extends AbstractVisualizer<Object> {

        private ComplexityInfo complexity = BstVisualizer.complexity();
        private String[] labels = {"Comparisons", "Links changed"};

        LabVisualizer() {
            super("Tree Lab", Category.LABS, "Your own tree", Object.class);
        }

        void configure(String kind) {
            switch (kind) {
                case "heap":
                    complexity = HeapVisualizer.complexity();
                    labels = new String[]{"Comparisons", "Swaps"};
                    break;
                case "traverse":
                    complexity = TraversalVisualizer.complexity();
                    labels = new String[]{"Nodes written", "Moves"};
                    break;
                default:
                    complexity = BstVisualizer.complexity();
                    labels = new String[]{"Comparisons", "Links changed"};
                    break;
            }
        }

        @Override
        public ComplexityInfo getComplexity() {
            return complexity;
        }

        @Override
        public String[] getCounterLabels() {
            return labels;
        }

        @Override
        public Timeline buildTimeline(String scenario) {
            return Timeline.empty("");
        }

        @Override
        protected void paintState(Graphics2D g, Object state, int w, int h) {
            if (state instanceof BstState) {
                BstVisualizer.render(g, (BstState) state, w, h);
            } else if (state instanceof TraversalState) {
                TraversalVisualizer.render(g, (TraversalState) state, w, h);
            } else if (state instanceof HeapState) {
                HeapVisualizer.render(g, (HeapState) state, w, h);
            } else {
                Draw.emptyMessage(g, w, h, "Insert some numbers to begin.");
            }
        }
    }
}
