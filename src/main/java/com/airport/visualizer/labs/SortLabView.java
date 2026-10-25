package com.airport.visualizer.labs;

import com.airport.visualizer.algorithms.sort.BubbleSort;
import com.airport.visualizer.algorithms.sort.InsertionSort;
import com.airport.visualizer.algorithms.sort.MergeSort;
import com.airport.visualizer.algorithms.sort.QuickSort;
import com.airport.visualizer.algorithms.sort.SelectionSort;
import com.airport.visualizer.algorithms.sort.SortAlgorithm;
import com.airport.visualizer.algorithms.sort.SortVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.Timeline;
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
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Practice lab: the learner types numbers, picks a sorting algorithm and watches it work. */
public class SortLabView extends JPanel implements ViewLifecycle {

    private static final SortAlgorithm[] ALGORITHMS = {
        new BubbleSort(), new SelectionSort(), new InsertionSort(), new MergeSort(), new QuickSort()
    };
    private static final int MAX_COUNT = 16;

    private final JTextField numbers = Ui.textField(28, "Numbers from 0 to 999, separated by commas");
    private final JComboBox<String> algorithm;
    private final MessageBanner banner = new MessageBanner();
    private final PlayerPanel player = new PlayerPanel();
    private final Random random = new Random();

    public SortLabView() {
        super(new BorderLayout(0, 12));
        setOpaque(true);
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        String[] names = new String[ALGORITHMS.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = ALGORITHMS[i].getName();
        }
        algorithm = Ui.combo(names, "Which sorting algorithm to use");

        RoundButton run = new RoundButton("Sort my numbers", Icons.of(Icons.Shape.PLAY), RoundButton.Variant.PRIMARY);
        run.setToolTipText("Check the numbers and build the animation (Enter)");
        run.addActionListener(e -> runLab());
        RoundButton randomBtn = new RoundButton("Random numbers", Icons.of(Icons.Shape.SHUFFLE), RoundButton.Variant.SECONDARY);
        randomBtn.setToolTipText("Fill in 10 random numbers");
        randomBtn.addActionListener(e -> {
            numbers.setText(randomNumbers(10));
            runLab();
        });
        numbers.addActionListener(e -> runLab());
        numbers.getAccessibleContext().setAccessibleName("Numbers to sort");

        Card input = new Card(new GridBagLayout());
        input.padding(12, 16, 12, 16);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 12);
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        input.add(Ui.labeled("Your numbers (0-999, separated by commas, up to " + MAX_COUNT + ")", numbers), c);
        c.weightx = 0;
        input.add(Ui.labeled("Algorithm", algorithm), c);
        c.fill = GridBagConstraints.NONE;
        input.add(run, c);
        c.insets = new Insets(0, 0, 0, 0);
        input.add(randomBtn, c);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(new ViewHeader(Category.LABS, "Sorting Lab",
                "Type your own numbers, choose a sorting algorithm and watch it tidy them up step by step. "
                + "Try a list that is already sorted, or one that is backwards, and compare the counters!"),
                BorderLayout.NORTH);
        north.add(input, BorderLayout.CENTER);
        north.add(banner, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(player, BorderLayout.CENTER);

        numbers.setText("42, 7, 19, 88, 3, 56, 23, 71");
        runLab();
    }

    private void runLab() {
        try {
            ParseResult<Integer> parsed = InputParser.parseIntegers(numbers.getText(), MAX_COUNT, 0, 999, true);
            if (!parsed.isOk()) {
                banner.error(parsed.errorText());
                numbers.requestFocusInWindow();
                return;
            }
            SortAlgorithm alg = ALGORITHMS[Math.max(0, algorithm.getSelectedIndex())];
            SortVisualizer viz = new SortVisualizer(alg, parsed.getValues());
            Timeline t = viz.buildTimeline(SortVisualizer.LAB);
            player.load(viz, t);
            StringBuilder msg = new StringBuilder();
            if (parsed.getValues().size() == 1) {
                msg.append("Only one number - it is already sorted! Add more to see the algorithm work. ");
            }
            if (!parsed.getWarnings().isEmpty()) {
                msg.append(parsed.warningText()).append(' ');
                msg.append(t.getResultSummary());
                banner.warning(msg.toString().trim());
            } else if (msg.length() > 0) {
                banner.info(msg.toString().trim());
            } else {
                banner.success(t.getResultSummary() + " Press Play or Step!");
            }
        } catch (RuntimeException ex) {
            banner.error("Something went wrong while preparing the animation: " + ex.getMessage());
        }
    }

    private String randomNumbers(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(random.nextInt(100));
        }
        return sb.toString();
    }

    @Override
    public void onHide() {
        player.getController().pause();
    }
}
