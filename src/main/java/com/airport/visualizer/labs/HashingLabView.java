package com.airport.visualizer.labs;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.Step;
import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.hashing.ChainedHashTable;
import com.airport.visualizer.hashing.HashFunction;
import com.airport.visualizer.hashing.HashOutcome;
import com.airport.visualizer.hashing.HashSnapshot;
import com.airport.visualizer.hashing.HashStepSink;
import com.airport.visualizer.hashing.HashTableModel;
import com.airport.visualizer.hashing.LinearProbingHashTable;
import com.airport.visualizer.hashing.lab.HashExample;
import com.airport.visualizer.hashing.lab.NameEntry;
import com.airport.visualizer.hashing.lab.NameHashingVisualizer;
import com.airport.visualizer.hashing.lab.NameQueueList;
import com.airport.visualizer.hashing.lab.NameStatus;
import com.airport.visualizer.input.InputParser;
import com.airport.visualizer.input.ParseResult;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.Icons;
import com.airport.visualizer.ui.components.MessageBanner;
import com.airport.visualizer.ui.components.RoundButton;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.player.PlaybackController;
import com.airport.visualizer.ui.player.PlayerPanel;
import com.airport.visualizer.ui.theme.Theme;
import com.airport.visualizer.ui.views.ViewHeader;
import com.airport.visualizer.ui.views.ViewLifecycle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.Scrollable;

/**
 * Practice lab: the learner types names, chooses separate chaining or linear probing and a table
 * size, then inserts, searches and deletes names while every step is animated and explained.
 * The table keeps its contents between operations; each operation gets its own timeline.
 */
public class HashingLabView extends JPanel implements ViewLifecycle {

    private static final String MODE_CHAINING = "Separate chaining";
    private static final String MODE_PROBING = "Open addressing (linear probing)";
    private static final int MAX_QUEUE = 40;
    private static final int MAX_NAME_LENGTH = 16;

    private enum Kind {
        INSERT, SEARCH, DELETE
    }

    /** One name's part of a recorded timeline, used to show "current" while it plays. */
    private static final class Task {

        final Kind kind;
        final String name;
        final NameEntry entry;
        NameStatus before;
        String beforeDetail;
        NameStatus after;
        String afterDetail;
        HashOutcome outcome;
        int start;
        int end;

        Task(Kind kind, String name, NameEntry entry) {
            this.kind = kind;
            this.name = name;
            this.entry = entry;
            if (entry != null) {
                before = entry.getStatus();
                beforeDetail = entry.getDetail();
                after = before;
                afterDetail = beforeDetail;
            }
        }
    }

    private HashTableModel table = new ChainedHashTable(7);
    private final NameHashingVisualizer visualizer = new NameHashingVisualizer(table);
    private final PlayerPanel player = new PlayerPanel();
    private final DefaultListModel<NameEntry> queueModel = new DefaultListModel<>();
    private final NameQueueList queueList = new NameQueueList(queueModel);
    private final MessageBanner banner = new FitBanner();
    private final JTextField namesField = Ui.textField(16, "One name, or several separated by commas: Ana, Ben, Carla");
    private final JTextField targetField = Ui.textField(12, "Name to search for or delete (or click a name in the queue)");
    private final JComboBox<String> modeCombo = Ui.combo(new String[]{MODE_CHAINING, MODE_PROBING},
            "How should we handle two names that want the same spot?");
    private final JSpinner sizeSpinner = Ui.spinner(7, 3, 13, "How many buckets / slots the table has (3 to 13)");
    private final JComboBox<HashExample> exampleCombo = Ui.combo(HashExample.all().toArray(new HashExample[0]),
            "Ready-made name sets that show one idea each");
    private final JTextArea calcArea = Ui.paragraph("", 13f, Theme.TEXT);
    private final JTabbedPane tabs = new JTabbedPane();
    private final Map<String, String> calcLines = new LinkedHashMap<>();
    private final List<Task> segments = new ArrayList<>();
    private boolean adjusting;

    public HashingLabView() {
        super(new BorderLayout(16, 12));
        setOpaque(true);
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        ViewHeader header = new ViewHeader(Category.LABS, "Name Hashing Lab",
                "Every name gets a locker number from its letters. What if two names want the same locker?");
        RoundButton load = new RoundButton("Load", RoundButton.Variant.SECONDARY);
        load.setToolTipText("Replace the queue with this example and start a fresh table");
        load.addActionListener(e -> loadExample());
        JPanel exampleRow = new JPanel(new BorderLayout(8, 0));
        exampleRow.setOpaque(false);
        exampleRow.add(exampleCombo, BorderLayout.CENTER);
        exampleRow.add(load, BorderLayout.EAST);
        header.addTool(Ui.labeled("Try an example", exampleRow));
        add(header, BorderLayout.NORTH);

        // ---------------- left column: controls + queue / calculations
        Card input = buildInputCard();
        JPanel queueCard = buildQueueCard();
        ColumnPanel left = new ColumnPanel(new BorderLayout(0, 12), input);
        left.add(input, BorderLayout.NORTH);
        left.add(queueCard, BorderLayout.CENTER);
        JScrollPane leftScroll = Ui.scroll(left);
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.setPreferredSize(new Dimension(350, 400));
        leftScroll.setMinimumSize(new Dimension(300, 200));
        add(leftScroll, BorderLayout.WEST);

        // ---------------- center: the animation player
        player.getNarration().setPreferredSize(new Dimension(500, 124));
        add(player, BorderLayout.CENTER);
        player.addStepListener(new PlaybackController.Listener() {
            @Override
            public void stepChanged(int index, Step step) {
                applyProgress(index);
            }
        });

        wireActions();
        player.load(visualizer, visualizer.still("Welcome to the Name Hashing Lab!",
                "A hash table is like a wall of lockers. To pick a locker for a name we add up the secret numbers "
                + "(character codes) of its letters and keep the remainder after dividing by the number of lockers. "
                + "Type some names (or load an example), then press Insert next or Insert all."));
        banner.info("Start by typing names like \"Amy, May, Ben\" or load an example.");
    }

    // ================================================================== layout

    private Card buildInputCard() {
        Card card = new Card(new BorderLayout());
        card.padding(14, 16, 14, 16);
        JPanel s = Ui.stack();

        s.add(section("1. Type names (comma separated)"));
        RoundButton add = new RoundButton("Add", Icons.of(Icons.Shape.PLUS), RoundButton.Variant.PRIMARY);
        add.setToolTipText("Add the typed names to the queue (Enter)");
        add.addActionListener(e -> addNames());
        namesField.addActionListener(e -> addNames());
        namesField.getAccessibleContext().setAccessibleName("Names to add");
        s.add(fieldWithButton(namesField, add));

        s.add(section("2. How to handle collisions"));
        JPanel modeRow = new JPanel(new BorderLayout(8, 0));
        modeRow.setOpaque(false);
        modeRow.add(modeCombo, BorderLayout.CENTER);
        JPanel sizeBox = new JPanel(new BorderLayout(6, 0));
        sizeBox.setOpaque(false);
        sizeBox.add(Ui.fieldLabel("Size"), BorderLayout.WEST);
        sizeBox.add(sizeSpinner, BorderLayout.CENTER);
        modeRow.add(sizeBox, BorderLayout.EAST);
        s.add(align(modeRow));

        s.add(section("3. Store the waiting names"));
        RoundButton next = new RoundButton("Insert next", Icons.of(Icons.Shape.STEP), RoundButton.Variant.SUCCESS);
        next.setToolTipText("Hash and store the next waiting name, step by step");
        next.addActionListener(e -> insert(false));
        RoundButton all = new RoundButton("Insert all", Icons.of(Icons.Shape.PLAY), RoundButton.Variant.PRIMARY);
        all.setToolTipText("Hash and store every waiting name, one after another");
        all.addActionListener(e -> insert(true));
        s.add(pair(next, all));

        s.add(section("4. Find or remove a name"));
        targetField.addActionListener(e -> searchOrDelete(Kind.SEARCH));
        targetField.getAccessibleContext().setAccessibleName("Name to search or delete");
        s.add(align(targetField));
        s.add(Box.createVerticalStrut(6));
        RoundButton search = new RoundButton("Search", Icons.of(Icons.Shape.SEARCH), RoundButton.Variant.SECONDARY);
        search.setToolTipText("Find the name and highlight where it lives (Enter)");
        search.addActionListener(e -> searchOrDelete(Kind.SEARCH));
        RoundButton delete = new RoundButton("Delete", Icons.of(Icons.Shape.TRASH), RoundButton.Variant.DANGER);
        delete.setToolTipText("Remove the name typed above or selected in the queue");
        delete.addActionListener(e -> searchOrDelete(Kind.DELETE));
        s.add(pair(search, delete));
        s.add(Box.createVerticalStrut(10));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        s.add(banner);
        card.add(s, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQueueCard() {
        Card card = new Card(new BorderLayout());
        card.padding(8, 8, 10, 8);
        JScrollPane listScroll = new JScrollPane(queueList);
        listScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        listScroll.getVerticalScrollBar().setUnitIncrement(14);
        queueList.addListSelectionListener(e -> {
            NameEntry sel = queueList.getSelectedValue();
            if (!e.getValueIsAdjusting() && sel != null) {
                targetField.setText(sel.getName());
            }
        });

        calcArea.setFont(Theme.regular(13f));
        calcArea.setFocusable(true);
        calcArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        calcArea.setText("No calculations yet. Each stored name will show:\n"
                + "  letter codes added up → sum % size → home index → actual spot.");
        JScrollPane calcScroll = new JScrollPane(calcArea);
        calcScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        calcScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        tabs.setFont(Theme.bold(12.5f));
        tabs.addTab("Name queue (0)", listScroll);
        tabs.addTab("Hash calculations", calcScroll);
        tabs.setToolTipTextAt(0, "Every name you typed with its status: current, waiting, stored, deleted or full");
        tabs.setToolTipTextAt(1, "The full hash calculation of every stored name");
        tabs.setPreferredSize(new Dimension(300, 240));
        card.add(tabs, BorderLayout.CENTER);

        RoundButton reset = new RoundButton("Reset table", Icons.of(Icons.Shape.RESET), RoundButton.Variant.GHOST);
        reset.setToolTipText("Empty the table; names go back to waiting");
        reset.addActionListener(e -> resetTable("The table is empty again. Your names are waiting in the queue."));
        RoundButton clear = new RoundButton("Clear queue", RoundButton.Variant.GHOST);
        clear.setToolTipText("Remove every name from the queue and empty the table");
        clear.addActionListener(e -> {
            queueModel.clear();
            resetTable("Queue cleared and table emptied. Type new names to begin.");
        });
        JComponent buttons = pair(reset, clear);
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private static JComponent section(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        p.add(Ui.label(text, Theme.bold(12.5f), Theme.TEXT), BorderLayout.CENTER);
        return align(p);
    }

    private static JComponent fieldWithButton(JComponent field, JComponent button) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        p.add(button, BorderLayout.EAST);
        return align(p);
    }

    private static JComponent pair(JComponent a, JComponent b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        return align(p);
    }

    private static JComponent align(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private void wireActions() {
        modeCombo.addActionListener(e -> {
            if (!adjusting) {
                resetTable("You switched to " + modeCombo.getSelectedItem() + ". The table starts empty again; "
                        + "your names are back in the queue as waiting.");
            }
        });
        sizeSpinner.addChangeListener(e -> {
            if (!adjusting) {
                resetTable("The table now has " + sizeSpinner.getValue() + " spots, so it starts empty again; "
                        + "your names are back in the queue as waiting.");
            }
        });
    }

    // ================================================================== actions

    private void addNames() {
        ParseResult<String> r = InputParser.parseNames(namesField.getText(), 20, MAX_NAME_LENGTH);
        if (!r.getErrors().isEmpty() || r.getValues().isEmpty()) {
            String msg = r.errorText();
            banner.error(msg.isEmpty() ? "Please type at least one name." : msg + " Nothing was added - please fix and try again.");
            namesField.requestFocusInWindow();
            return;
        }
        List<String> added = new ArrayList<>();
        List<String> stored = new ArrayList<>();
        List<String> queued = new ArrayList<>();
        List<String> again = new ArrayList<>();
        boolean tooMany = false;
        for (String name : r.getValues()) {
            NameEntry existing = findEntry(name);
            if (table.contains(name)) {
                stored.add(name);
                continue;
            }
            if (existing != null) {
                if (existing.getStatus() == NameStatus.DELETED || existing.getStatus() == NameStatus.FULL) {
                    existing.set(NameStatus.WAITING, "");
                    again.add(existing.getName());
                } else {
                    queued.add(existing.getName());
                }
                continue;
            }
            if (queueModel.size() >= MAX_QUEUE) {
                tooMany = true;
                break;
            }
            queueModel.addElement(new NameEntry(name));
            added.add(name);
        }
        StringBuilder msg = new StringBuilder();
        if (!added.isEmpty()) {
            msg.append("Added to the queue: ").append(String.join(", ", added)).append(". ");
        }
        if (!again.isEmpty()) {
            msg.append("Waiting again: ").append(String.join(", ", again)).append(". ");
        }
        StringBuilder warn = new StringBuilder();
        if (!stored.isEmpty()) {
            warn.append("Already stored in the table, not added again: ").append(String.join(", ", stored)).append(". ");
        }
        if (!queued.isEmpty()) {
            warn.append("Already waiting in the queue: ").append(String.join(", ", queued)).append(". ");
        }
        if (!r.getWarnings().isEmpty()) {
            warn.append(r.warningText()).append(' ');
        }
        if (tooMany) {
            warn.append("The queue holds at most ").append(MAX_QUEUE).append(" names. ");
        }
        if (warn.length() > 0) {
            banner.warning((msg.toString() + warn).trim());
        } else {
            banner.success(msg.append("Press Insert next or Insert all.").toString().trim());
        }
        if (!added.isEmpty() || !again.isEmpty()) {
            namesField.setText("");
        }
        updateQueueTitle();
        queueList.repaint();
    }

    private void loadExample() {
        HashExample ex = (HashExample) exampleCombo.getSelectedItem();
        if (ex == null) {
            return;
        }
        adjusting = true;
        sizeSpinner.setValue(ex.getSize());
        adjusting = false;
        queueModel.clear();
        for (String n : ex.getNames()) {
            queueModel.addElement(new NameEntry(n));
        }
        resetTable(null);
        banner.info(ex.getTitle() + ": " + ex.getExplanation() + " Press Insert all to watch (try both collision modes!).");
    }

    private void insert(boolean all) {
        finishSegments();
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < queueModel.size(); i++) {
            NameEntry e = queueModel.get(i);
            if (e.getStatus() == NameStatus.WAITING) {
                tasks.add(new Task(Kind.INSERT, e.getName(), e));
                if (!all) {
                    break;
                }
            }
        }
        if (tasks.isEmpty()) {
            banner.warning(queueModel.isEmpty() ? "The queue is empty - type some names first."
                    : "Nobody is waiting. Add more names, or press Reset table to try again.");
            return;
        }
        runTasks("insert", tasks);
        List<String> placed = new ArrayList<>();
        List<String> full = new ArrayList<>();
        List<String> dupes = new ArrayList<>();
        for (Task t : tasks) {
            if (t.outcome.getType() == HashOutcome.Type.INSERTED) {
                placed.add(t.name + " → " + t.outcome.locationText());
            } else if (t.outcome.getType() == HashOutcome.Type.FULL) {
                full.add(t.name);
            } else {
                dupes.add(t.name);
            }
        }
        StringBuilder sb = new StringBuilder();
        if (placed.size() == 1) {
            sb.append("Stored ").append(placed.get(0)).append(". ");
        } else if (!placed.isEmpty()) {
            sb.append("Stored ").append(placed.size()).append(" names (see the queue for each spot). ");
        }
        if (full.isEmpty() && dupes.isEmpty()) {
            banner.success(sb.append("Watch how each spot was chosen.").toString());
        } else {
            if (!full.isEmpty()) {
                sb.append("Table full - could not store: ").append(String.join(", ", full))
                        .append(". Try a bigger size or Separate chaining. ");
            }
            if (!dupes.isEmpty()) {
                sb.append("Already stored: ").append(String.join(", ", dupes)).append(". ");
            }
            banner.warning(sb.toString().trim());
        }
    }

    private void searchOrDelete(Kind kind) {
        finishSegments();
        String raw = targetField.getText().trim();
        if (raw.isEmpty() && queueList.getSelectedValue() != null) {
            raw = queueList.getSelectedValue().getName();
        }
        String verb = kind == Kind.SEARCH ? "search for" : "delete";
        if (raw.isEmpty()) {
            banner.error("Type a name to " + verb + ", or click a name in the queue.");
            targetField.requestFocusInWindow();
            return;
        }
        ParseResult<String> r = InputParser.parseNames(raw, 20, MAX_NAME_LENGTH);
        if (!r.getErrors().isEmpty() || r.getValues().isEmpty()) {
            banner.error(r.errorText());
            return;
        }
        if (r.getValues().size() > 1) {
            banner.error("Please " + verb + " one name at a time.");
            return;
        }
        String name = r.getValues().get(0);
        if (table.getCount() == 0 && kind == Kind.DELETE) {
            banner.warning("The table is empty, so there is nothing to delete yet. Insert some names first.");
            return;
        }
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(kind, name, findEntry(name)));
        runTasks(kind == Kind.SEARCH ? "search" : "delete", tasks);
        HashOutcome o = tasks.get(0).outcome;
        boolean probing = table.getMode() == HashSnapshot.Mode.PROBING;
        switch (o.getType()) {
            case FOUND:
                banner.success("Found " + name + " at " + o.locationText()
                        + (probing ? " after " + o.getProbes().size() + " probe(s)." : "."));
                break;
            case DELETED:
                banner.success(name + " was removed from " + o.locationText()
                        + (probing ? ". A DELETED marker was left so later searches keep going."
                                : " by changing just one arrow in the linked list."));
                break;
            default:
                banner.warning(name + " is not in the table" + (kind == Kind.DELETE ? ", so nothing was deleted." : "."));
                break;
        }
    }

    /** Makes a brand-new table (same mode and size as chosen) and puts every name back to waiting. */
    private void resetTable(String message) {
        finishSegments();
        int size = (Integer) sizeSpinner.getValue();
        table = MODE_PROBING.equals(modeCombo.getSelectedItem()) ? new LinearProbingHashTable(size) : new ChainedHashTable(size);
        visualizer.setTable(table);
        for (int i = 0; i < queueModel.size(); i++) {
            queueModel.get(i).set(NameStatus.WAITING, "");
        }
        calcLines.clear();
        renderCalcLines();
        updateQueueTitle();
        queueList.repaint();
        boolean probing = table.getMode() == HashSnapshot.Mode.PROBING;
        player.load(visualizer, visualizer.still("A fresh, empty table with " + size + (probing ? " slots" : " buckets"),
                probing
                        ? "Open addressing: every slot holds ONE name. If a name's home slot is taken we try the next slot, "
                        + "then the next, wrapping from the last slot back to slot 0."
                        : "Separate chaining: every bucket holds a linked list. Names that land in the same bucket "
                        + "simply join the chain, one node pointing to the next."));
        if (message != null) {
            banner.info(message);
        }
    }

    // ================================================================== recording

    private HashOutcome apply(HashTableModel t, Task task, HashStepSink sink) {
        switch (task.kind) {
            case INSERT:
                return t.insert(task.name, sink);
            case SEARCH:
                return t.search(task.name, sink);
            default:
                return t.delete(task.name, sink);
        }
    }

    private void runTasks(String operationKey, List<Task> tasks) {
        try {
            HashTableModel copy = table.copy();
            long t0 = System.nanoTime();
            for (Task task : tasks) {
                apply(copy, task, HashStepSink.QUIET);
            }
            long nanos = System.nanoTime() - t0;

            StepRecorder rec = new StepRecorder();
            rec.setOperation(operationKey);
            HashStepSink sink = HashStepSink.recording(rec, s -> s);
            for (Task task : tasks) {
                task.start = rec.size();
                task.outcome = apply(table, task, sink);
                task.end = Math.max(task.start, rec.size() - 1);
                decideFinalStatus(task);
                rememberCalc(task);
            }
            segments.clear();
            segments.addAll(tasks);
            refreshStoredDetails();
            renderCalcLines();
            updateQueueTitle();
            player.load(visualizer, Timeline.of(rec, nanos, ""));
            player.getController().play();
        } catch (RuntimeException ex) {
            banner.error("Something went wrong while preparing the animation: " + ex.getMessage());
        }
    }

    private void decideFinalStatus(Task t) {
        if (t.entry == null) {
            return;
        }
        switch (t.outcome.getType()) {
            case INSERTED:
            case DUPLICATE:
                t.after = NameStatus.STORED;
                t.afterDetail = detailFor(t.name);
                break;
            case FULL:
                t.after = NameStatus.FULL;
                t.afterDetail = "no free slot - table full";
                break;
            case DELETED:
                t.after = NameStatus.DELETED;
                t.afterDetail = "removed from " + t.outcome.locationText();
                break;
            default:
                break;
        }
    }

    /** e.g. "231 % 7 = 0 → slot 2". */
    private String detailFor(String name) {
        HashOutcome o = table.search(name, HashStepSink.QUIET);
        if (o.getType() != HashOutcome.Type.FOUND) {
            return "";
        }
        return o.getCalc().remainderText() + HashFunction.ARROW + o.locationText();
    }

    private void refreshStoredDetails() {
        java.util.Set<NameEntry> inTasks = new java.util.HashSet<>();
        for (Task t : segments) {
            if (t.entry != null) {
                inTasks.add(t.entry);
            }
        }
        for (int i = 0; i < queueModel.size(); i++) {
            NameEntry e = queueModel.get(i);
            if (!inTasks.contains(e) && e.getStatus() == NameStatus.STORED) {
                e.set(NameStatus.STORED, detailFor(e.getName()));
            }
        }
        for (Task t : segments) {
            if (t.entry != null && t.after == NameStatus.STORED) {
                t.afterDetail = detailFor(t.name);
            }
        }
    }

    private void rememberCalc(Task t) {
        HashOutcome o = t.outcome;
        String key = t.name.toLowerCase(Locale.ROOT);
        HashFunction.Calculation c = o.getCalc();
        if (t.kind == Kind.INSERT && (o.getType() == HashOutcome.Type.INSERTED || o.getType() == HashOutcome.Type.FULL)) {
            calcLines.put(key, t.name + "\n   " + c.sumText() + "\n   " + c.remainderText() + HashFunction.ARROW
                    + "home index " + c.getHome() + "\n   " + HashFunction.ARROW.trim() + " " + o.storageText());
        } else if (t.kind == Kind.DELETE && o.getType() == HashOutcome.Type.DELETED) {
            String old = calcLines.get(key);
            calcLines.put(key, (old == null ? t.name + "\n   " + c.text() : old) + "\n   "
                    + HashFunction.ARROW.trim() + " deleted from " + o.locationText());
        }
    }

    private void renderCalcLines() {
        if (calcLines.isEmpty()) {
            calcArea.setText("No calculations yet. Each stored name will show:\n"
                    + "  letter codes added up → sum % size → home index → actual spot.");
        } else {
            calcArea.setText("Letters are made UPPERCASE first, so \"amy\" and \"AMY\" hash the same.\n\n"
                    + String.join("\n\n", calcLines.values()));
        }
        calcArea.setCaretPosition(0);
    }

    /** Shows "current" on the name being animated, the old status before it, the result after it. */
    private void applyProgress(int index) {
        if (segments.isEmpty()) {
            return;
        }
        int last = player.getController().getTimeline().size() - 1;
        for (Task t : segments) {
            if (t.entry == null) {
                continue;
            }
            if (index >= t.end || index >= last) {
                t.entry.set(t.after, t.afterDetail);
            } else if (index >= t.start) {
                t.entry.set(NameStatus.CURRENT, t.kind == Kind.INSERT ? "being hashed and stored now"
                        : t.kind == Kind.SEARCH ? "being searched for now" : "being deleted now");
            } else {
                t.entry.set(t.before, t.beforeDetail);
            }
        }
        queueList.repaint();
        int current = -1;
        for (int i = 0; i < queueModel.size(); i++) {
            if (queueModel.get(i).getStatus() == NameStatus.CURRENT) {
                current = i;
                break;
            }
        }
        if (current >= 0) {
            Rectangle r = queueList.getCellBounds(current, current);
            if (r != null) {
                queueList.scrollRectToVisible(r);
            }
        }
    }

    /** Jumps every recorded name to its final status (the table already holds the result). */
    private void finishSegments() {
        for (Task t : segments) {
            if (t.entry != null) {
                t.entry.set(t.after, t.afterDetail);
            }
        }
        segments.clear();
        queueList.repaint();
    }

    private NameEntry findEntry(String name) {
        for (int i = 0; i < queueModel.size(); i++) {
            if (queueModel.get(i).matches(name)) {
                return queueModel.get(i);
            }
        }
        return null;
    }

    private void updateQueueTitle() {
        tabs.setTitleAt(0, "Name queue (" + queueModel.size() + ")");
    }

    @Override
    public void onHide() {
        player.getController().pause();
    }

    /**
     * MessageBanner measures its wrapped height from its own width; before its first layout that
     * width is 0, so borrow the parent's width to avoid clipping the first message.
     */
    private static final class FitBanner extends MessageBanner {

        @Override
        public void show(Kind kind, String text) {
            if (getWidth() <= 0 && getParent() != null && getParent().getWidth() > 0) {
                setSize(getParent().getWidth(), Math.max(1, getHeight()));
            }
            super.show(kind, text);
        }
    }

    /** Left column: follows the viewport size, but scrolls when the window is too short. */
    private static final class ColumnPanel extends JPanel implements Scrollable {

        private final JComponent top;

        ColumnPanel(LayoutManager layout, JComponent top) {
            super(layout);
            this.top = top;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 120;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport
                    && getParent().getHeight() >= top.getPreferredSize().height + 180;
        }
    }
}
