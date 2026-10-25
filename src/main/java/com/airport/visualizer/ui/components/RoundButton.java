package com.airport.visualizer.ui.components;

import com.airport.visualizer.ui.theme.Draw;
import com.airport.visualizer.ui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A rounded, fully custom-painted button with hover / pressed / disabled / keyboard-focus states.
 * The focus ring makes keyboard use visible (ISO 25010 operability & accessibility).
 */
public class RoundButton extends JButton {

    public enum Variant {
        PRIMARY, SECONDARY, SUCCESS, DANGER, WARNING, GHOST
    }

    private Variant variant;

    public RoundButton(String text) {
        this(text, null, Variant.SECONDARY);
    }

    public RoundButton(String text, Variant variant) {
        this(text, null, variant);
    }

    public RoundButton(String text, Icon icon, Variant variant) {
        super(text, icon);
        this.variant = variant;
        setUI(new BasicButtonUI());
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(Theme.bold(13.5f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));
        setIconTextGap(8);
        applyForeground();
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
        applyForeground();
        repaint();
    }

    public Variant getVariant() {
        return variant;
    }

    private void applyForeground() {
        switch (variant) {
            case SECONDARY:
                setForeground(Theme.TEXT);
                break;
            case GHOST:
                setForeground(Theme.PRIMARY);
                break;
            case WARNING:
                setForeground(Theme.TEXT);
                break;
            default:
                setForeground(Color.WHITE);
                break;
        }
    }

    private Color baseColor() {
        switch (variant) {
            case PRIMARY:
                return Theme.PRIMARY;
            case SUCCESS:
                return Theme.SUCCESS;
            case DANGER:
                return Theme.DANGER;
            case WARNING:
                return Theme.ACCENT;
            case GHOST:
                return Theme.PRIMARY_SOFT;
            default:
                return Color.WHITE;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        Insets m = getMargin();
        int textW = getText() == null || getText().isEmpty() ? 0 : fm.stringWidth(getText());
        Icon icon = getIcon();
        int iconW = icon == null ? 0 : icon.getIconWidth() + (textW > 0 ? getIconTextGap() : 0);
        int h = Math.max(fm.getHeight(), icon == null ? 0 : icon.getIconHeight()) + m.top + m.bottom;
        return new Dimension(textW + iconW + m.left + m.right, Math.max(38, h));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Draw.setup(g);
        ButtonModel model = getModel();
        int w = getWidth();
        int h = getHeight();
        Color base = baseColor();
        Color fill = base;
        if (!isEnabled()) {
            fill = Theme.tint(base, 0.55);
        } else if (model.isPressed()) {
            fill = Theme.shade(base, variant == Variant.SECONDARY ? 0.06 : 0.18);
        } else if (model.isRollover()) {
            fill = variant == Variant.SECONDARY ? Theme.SURFACE_ALT : Theme.shade(base, 0.08);
        }
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 14, 14);
        if (isEnabled() && variant != Variant.GHOST) {
            g.setColor(Theme.withAlpha(Color.BLACK, 18));
            g.fill(new RoundRectangle2D.Double(1, 2.5, w - 2, h - 2, 14, 14));
        }
        g.setColor(fill);
        g.fill(shape);
        if (variant == Variant.SECONDARY) {
            g.setColor(model.isRollover() && isEnabled() ? Theme.BORDER_STRONG : Theme.BORDER);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(shape);
        }
        if (isFocusOwner()) {
            g.setColor(Theme.withAlpha(Theme.PRIMARY, 170));
            g.setStroke(new BasicStroke(2.4f));
            g.draw(new RoundRectangle2D.Double(2.5, 2.5, w - 5, h - 5, 12, 12));
        }

        // content: icon + text centered
        FontMetrics fm = g.getFontMetrics(getFont());
        String text = getText() == null ? "" : getText();
        Icon icon = getIcon();
        int textW = text.isEmpty() ? 0 : fm.stringWidth(text);
        int iconW = icon == null ? 0 : icon.getIconWidth();
        int gap = icon != null && textW > 0 ? getIconTextGap() : 0;
        int total = textW + iconW + gap;
        int x = (w - total) / 2;
        if (icon != null) {
            icon.paintIcon(this, g, x, (h - icon.getIconHeight()) / 2);
            x += iconW + gap;
        }
        if (!text.isEmpty()) {
            Color fg = getForeground();
            g.setColor(isEnabled() ? fg : Theme.withAlpha(fg, 140));
            g.setFont(getFont());
            g.drawString(text, x, (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        g.dispose();
    }
}
