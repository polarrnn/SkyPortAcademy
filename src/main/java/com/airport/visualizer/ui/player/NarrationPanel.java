package com.airport.visualizer.ui.player;

import com.airport.visualizer.core.Step;
import com.airport.visualizer.ui.components.Card;
import com.airport.visualizer.ui.components.Icons;
import com.airport.visualizer.ui.components.Ui;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** "What's happening?" card: the kid-friendly explanation of the current step. */
public class NarrationPanel extends Card {

    private final JLabel stepLabel = Ui.label("STEP 0 OF 0", Theme.bold(11.5f), Theme.PRIMARY);
    private final JLabel title = Ui.label("Ready for take-off!", Theme.bold(18f), Theme.TEXT);
    private final JTextArea body = Ui.paragraph("Press Play to watch, or Step to go one move at a time.", 15.5f, Theme.TEXT);

    public NarrationPanel() {
        super(new BorderLayout(14, 0));
        padding(12, 20, 12, 16);
        setAccent(Theme.ACCENT);
        JLabel icon = new JLabel(Icons.of(Icons.Shape.PLANE, 30));
        icon.setForeground(Theme.PRIMARY);
        icon.setVerticalAlignment(JLabel.TOP);
        icon.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        add(icon, BorderLayout.WEST);

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(0, 2));
        top.setOpaque(false);
        top.add(stepLabel, BorderLayout.NORTH);
        top.add(title, BorderLayout.CENTER);
        text.add(top, BorderLayout.NORTH);
        JScrollPane sp = Ui.scroll(body);
        sp.setPreferredSize(new Dimension(400, 78));
        text.add(sp, BorderLayout.CENTER);
        add(text, BorderLayout.CENTER);
        setPreferredSize(new Dimension(600, 150));
        getAccessibleContext().setAccessibleName("Step explanation");
    }

    public void show(int index, int total, Step step) {
        if (step == null) {
            stepLabel.setText("STEP 0 OF " + total);
            title.setText("Ready for take-off!");
            body.setText(total == 0 ? "Nothing to show yet." : "Press Play to watch, or Step to go one move at a time.");
        } else {
            stepLabel.setText("STEP " + (index + 1) + " OF " + total);
            title.setText(step.getTitle());
            body.setText(step.getNarration());
        }
        body.setCaretPosition(0);
    }

    /** Lets the learner make the story text bigger or smaller (accessibility). */
    public void setTextSize(float size) {
        body.setFont(Theme.regular(size));
        title.setFont(Theme.bold(size + 2.5f));
    }
}
