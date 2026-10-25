package com.airport.visualizer.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central design tokens (colors and fonts) so every screen looks consistent.
 * Colors were picked for readable contrast (ISO 25010: user interface aesthetics, accessibility).
 */
public final class Theme {

    // ---- Surfaces ----
    public static final Color BG = new Color(0xF1F4F9);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_ALT = new Color(0xF8FAFC);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color BORDER_STRONG = new Color(0xCBD5E1);

    // ---- Sidebar (night-sky navy) ----
    public static final Color SIDEBAR = new Color(0x0F1B33);
    public static final Color SIDEBAR_HOVER = new Color(0x1A2A4A);
    public static final Color SIDEBAR_ACTIVE = new Color(0x24407A);
    public static final Color SIDEBAR_TEXT = new Color(0xE2E8F0);
    public static final Color SIDEBAR_MUTED = new Color(0x8FA3C4);

    // ---- Brand ----
    public static final Color PRIMARY = new Color(0x2563EB);
    public static final Color PRIMARY_DARK = new Color(0x1D4ED8);
    public static final Color PRIMARY_SOFT = new Color(0xDBEAFE);
    public static final Color ACCENT = new Color(0xF5B700);      // airport signage yellow
    public static final Color ACCENT_SOFT = new Color(0xFEF3C7);

    // ---- Text ----
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color TEXT_MUTED = new Color(0x64748B);
    public static final Color TEXT_ON_DARK = Color.WHITE;

    // ---- Semantic ----
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color SUCCESS_SOFT = new Color(0xDCFCE7);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_SOFT = new Color(0xFEE2E2);
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color WARNING_SOFT = new Color(0xFEF3C7);
    public static final Color INFO = new Color(0x0EA5E9);
    public static final Color INFO_SOFT = new Color(0xE0F2FE);
    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color PURPLE_SOFT = new Color(0xEDE9FE);
    public static final Color TEAL = new Color(0x0D9488);
    public static final Color TEAL_SOFT = new Color(0xCCFBF1);

    // ---- Visual-state colors used by every visualizer (always paired with a legend/label) ----
    public static final Color V_NEUTRAL = new Color(0x94A3B8);
    public static final Color V_COMPARE = new Color(0xF59E0B);
    public static final Color V_SWAP = new Color(0xEF4444);
    public static final Color V_SORTED = new Color(0x22C55E);
    public static final Color V_ACTIVE = new Color(0x3B82F6);
    public static final Color V_PIVOT = new Color(0x8B5CF6);
    public static final Color V_FOUND = new Color(0x10B981);
    public static final Color V_DIM = new Color(0xE2E8F0);
    public static final Color V_VISITED = new Color(0x06B6D4);

    private static final String FAMILY = pickFamily();
    private static final Map<String, Font> CACHE = new HashMap<>();

    private Theme() {
    }

    private static String pickFamily() {
        try {
            List<String> names = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());
            for (String candidate : new String[]{"Segoe UI", "Inter", "Helvetica Neue", "Arial"}) {
                if (names.contains(candidate)) {
                    return candidate;
                }
            }
        } catch (RuntimeException ignored) {
            // headless or restricted environment: fall back below
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, float size) {
        String key = style + ":" + size;
        return CACHE.computeIfAbsent(key, k -> new Font(FAMILY, style, Math.round(size)).deriveFont(size));
    }

    public static Font regular(float size) {
        return font(Font.PLAIN, size);
    }

    public static Font bold(float size) {
        return font(Font.BOLD, size);
    }

    public static Font mono(float size) {
        return new Font(Font.MONOSPACED, Font.BOLD, Math.round(size));
    }

    /** Mixes a color with white; amount 0 = original, 1 = white. */
    public static Color tint(Color c, double amount) {
        int r = (int) Math.round(c.getRed() + (255 - c.getRed()) * amount);
        int g = (int) Math.round(c.getGreen() + (255 - c.getGreen()) * amount);
        int b = (int) Math.round(c.getBlue() + (255 - c.getBlue()) * amount);
        return new Color(r, g, b);
    }

    /** Mixes a color with black; amount 0 = original, 1 = black. */
    public static Color shade(Color c, double amount) {
        int r = (int) Math.round(c.getRed() * (1 - amount));
        int g = (int) Math.round(c.getGreen() * (1 - amount));
        int b = (int) Math.round(c.getBlue() * (1 - amount));
        return new Color(r, g, b);
    }

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}
