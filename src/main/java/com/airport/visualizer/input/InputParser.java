package com.airport.visualizer.input;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns raw text typed by the learner into safe, validated values.
 * Accepts one value or several separated by commas (spaces and empty entries are tolerated).
 */
public final class InputParser {

    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z '\\-]*");

    private InputParser() {
    }

    /**
     * Reads whole numbers such as "42, 7, 19".
     *
     * @param allowDuplicates when false, repeated numbers are dropped with a warning
     */
    public static ParseResult<Integer> parseIntegers(String raw, int maxCount, int min, int max,
            boolean allowDuplicates) {
        List<Integer> values = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> tokens = tokens(raw);
        if (tokens.isEmpty()) {
            errors.add("Please type at least one number (for example: 42, 7, 19).");
            return new ParseResult<>(values, errors, warnings);
        }
        List<String> bad = new ArrayList<>();
        List<String> outOfRange = new ArrayList<>();
        Set<Integer> seen = new LinkedHashSet<>();
        Set<Integer> dupes = new LinkedHashSet<>();
        for (String t : tokens) {
            int v;
            try {
                v = Integer.parseInt(t);
            } catch (NumberFormatException ex) {
                bad.add("\"" + t + "\"");
                continue;
            }
            if (v < min || v > max) {
                outOfRange.add(t);
                continue;
            }
            if (!seen.add(v)) {
                dupes.add(v);
                if (!allowDuplicates) {
                    continue;
                }
            }
            values.add(v);
        }
        if (!bad.isEmpty()) {
            errors.add("These are not whole numbers: " + String.join(", ", bad) + ".");
        }
        if (!outOfRange.isEmpty()) {
            errors.add("Numbers must be between " + min + " and " + max + ": " + String.join(", ", outOfRange) + ".");
        }
        if (!dupes.isEmpty()) {
            warnings.add((allowDuplicates ? "Repeated numbers kept: " : "Repeated numbers skipped: ")
                    + joinInts(dupes) + ".");
        }
        if (values.size() > maxCount) {
            warnings.add("Only the first " + maxCount + " numbers are used so everything fits on screen.");
            values = new ArrayList<>(values.subList(0, maxCount));
        }
        return new ParseResult<>(values, errors, warnings);
    }

    /** Reads a single whole number, e.g. a search target. */
    public static ParseResult<Integer> parseSingleInteger(String raw, int min, int max) {
        ParseResult<Integer> r = parseIntegers(raw, Integer.MAX_VALUE, min, max, true);
        if (!r.isOk()) {
            return r;
        }
        if (r.getValues().size() > 1) {
            List<String> errors = new ArrayList<>();
            errors.add("Please type just one number here.");
            return new ParseResult<>(new ArrayList<>(), errors, new ArrayList<>());
        }
        return r;
    }

    /**
     * Reads names such as "Ana, Ben, Carla". Letters, spaces, hyphens and apostrophes are allowed.
     * Names keep the learner's spelling but duplicates are compared ignoring upper/lower case.
     */
    public static ParseResult<String> parseNames(String raw, int maxCount, int maxLength) {
        List<String> values = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> tokens = tokens(raw);
        if (tokens.isEmpty()) {
            errors.add("Please type a name, or several names separated by commas (Ana, Ben, Carla).");
            return new ParseResult<>(values, errors, warnings);
        }
        List<String> bad = new ArrayList<>();
        List<String> tooLong = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        List<String> dupes = new ArrayList<>();
        for (String t : tokens) {
            String name = t.replaceAll("\\s+", " ");
            if (!NAME.matcher(name).matches()) {
                bad.add("\"" + name + "\"");
                continue;
            }
            if (name.length() > maxLength) {
                tooLong.add(name);
                continue;
            }
            if (!seen.add(name.toLowerCase())) {
                dupes.add(name);
                continue;
            }
            values.add(name);
        }
        if (!bad.isEmpty()) {
            errors.add("Names may only use letters, spaces, - and ': " + String.join(", ", bad) + ".");
        }
        if (!tooLong.isEmpty()) {
            errors.add("Names must be " + maxLength + " letters or fewer: " + String.join(", ", tooLong) + ".");
        }
        if (!dupes.isEmpty()) {
            warnings.add("Typed twice, used once: " + String.join(", ", dupes) + ".");
        }
        if (values.size() > maxCount) {
            warnings.add("Only the first " + maxCount + " names are used.");
            values = new ArrayList<>(values.subList(0, maxCount));
        }
        return new ParseResult<>(values, errors, warnings);
    }

    /** Splits on commas (and semicolons), trims, and drops empty pieces like "a,,b". */
    public static List<String> tokens(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        for (String piece : raw.split("[,;]")) {
            String t = piece.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private static String joinInts(Set<Integer> values) {
        StringBuilder sb = new StringBuilder();
        for (Integer v : values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(v);
        }
        return sb.toString();
    }
}
