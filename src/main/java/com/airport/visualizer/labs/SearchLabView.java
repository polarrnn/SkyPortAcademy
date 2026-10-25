package com.airport.visualizer.labs;

import com.airport.visualizer.algorithms.search.BinarySearchVisualizer;
import com.airport.visualizer.algorithms.search.LinearSearchVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.Timeline;
import com.airport.visualizer.core.Visualizer;
import com.airport.visualizer.input.InputParser;
import com.airport.visualizer.input.ParseResult;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.Icons;
import com.airport.visualizer.ui.components.MessageBanner;
import com.airport.visualizer.ui.components.RoundButton;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.player.PlayerPanel;
import com.airport.visualizer.ui.theme.Theme;
import com.airport.visualizer.ui.views.ViewHeader;
import com.airport.visualizer.ui.views.ViewLifecycle;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Practice lab: the learner types a list and a target, then watches Linear or Binary Search hunt for it. */
public class SearchLabView extends JPanel implements ViewLifecycle {

    private static final int MAX_COUNT = 16;
    private static final String LINEAR = "Linear Search";
    private static final String BINARY = "Binary Search";

    private final JTextField numbers = Ui.textField(26, "Numbers from 0 to 999, separated by commas");
    private final JTextField target = Ui.textField(6, "The one number to look for");
    private final JComboBox<String> method = Ui.combo(new String[]{LINEAR, BINARY}, "Which search to use");
    private final MessageBanner banner = new MessageBanner();
    private final PlayerPanel player = new PlayerPanel();
    private final Random random = new Random();

    public SearchLabView() {
        super(new BorderLayout(0, 12));
        setOpaque(true);
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        RoundButton run = new RoundButton("Search", Icons.of(Icons.Shape.SEARCH), RoundButton.Variant.PRIMARY);
        run.setToolTipText("Check the input and build the animation (Enter)");
        run.addActionListener(e -> runLab());
        RoundButton randomBtn = new RoundButton("Random numbers", Icons.of(Icons.Shape.SHUFFLE), RoundButton.Variant.SECONDARY);
        randomBtn.setToolTipText("Fill in 12 random numbers and pick one of them as the target");
        randomBtn.addActionListener(e -> fillRandom());
        numbers.addActionListener(e -> runLab());
        target.addActionListener(e -> runLab());
        numbers.getAccessibleContext().setAccessibleName("Numbers to search");
        target.getAccessibleContext().setAccessibleName("Number to find");

        Card input = new Card(new GridBagLayout());
        input.padding(12, 16, 12, 16);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 12);
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        input.add(Ui.labeled("Your numbers (0-999, commas, up to " + MAX_COUNT + ")", numbers), c);
        c.weightx = 0;
        input.add(Ui.labeled("Find", target), c);
        input.add(Ui.labeled("Method", method), c);
        c.fill = GridBagConstraints.NONE;
        input.add(run, c);
        c.insets = new Insets(0, 0, 0, 0);
        input.add(randomBtn, c);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(new ViewHeader(Category.LABS, "Search Lab",
                "Hide a number in your own list and send a search after it. Binary Search sorts your list first - "
                + "watch how few peeks it needs compared with Linear Search!"),
                BorderLayout.NORTH);
        north.add(input, BorderLayout.CENTER);
        north.add(banner, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(player, BorderLayout.CENTER);

        numbers.setText("34, 8, 91, 15, 62, 47, 3, 77, 29, 50");
        target.setText("62");
        runLab();
    }

    private void runLab() {
        try {
            ParseResult<Integer> list = InputParser.parseIntegers(numbers.getText(), MAX_COUNT, 0, 999, true);
            ParseResult<Integer> goal = InputParser.parseSingleInteger(target.getText(), 0, 999);
            List<String> errors = new ArrayList<>();
            if (!list.isOk()) {
                errors.add(list.errorText());
            }
            if (!goal.isOk()) {
                errors.add("Target: " + (goal.getErrors().isEmpty() ? "please type one number." : goal.errorText()));
            }
            if (!errors.isEmpty()) {
                banner.error(String.join(" ", errors));
                (list.isOk() ? target : numbers).requestFocusInWindow();
                return;
            }
            int t = goal.getValues().get(0);
            boolean binary = BINARY.equals(method.getSelectedItem());
            Visualizer viz = binary
                    ? new BinarySearchVisualizer(list.getValues(), t)
                    : new LinearSearchVisualizer(list.getValues(), t);
            Timeline timeline = viz.buildTimeline(viz.getScenarios().get(0));
            player.load(viz, timeline);
            String warn = list.warningText();
            if (!warn.isEmpty()) {
                banner.warning(warn + (binary ? "" : " Linear Search stops at the first match.") + " "
                        + timeline.getResultSummary());
            } else {
                banner.success(timeline.getResultSummary() + " Press Play or Step!");
            }
        } catch (RuntimeException ex) {
            banner.error("Something went wrong while preparing the animation: " + ex.getMessage());
        }
    }

    private void fillRandom() {
        List<Integer> vals = new ArrayList<>();
        while (vals.size() < 12) {
            int v = random.nextInt(100);
            if (!vals.contains(v)) {
                vals.add(v);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vals.get(i));
        }
        numbers.setText(sb.toString());
        target.setText(String.valueOf(vals.get(random.nextInt(vals.size()))));
        runLab();
    }

    @Override
    public void onHide() {
        player.getController().pause();
    }
}
