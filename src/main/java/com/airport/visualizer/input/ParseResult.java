package com.airport.visualizer.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of reading user input: the clean values plus friendly errors (stop) and warnings (FYI).
 * Callers never get an exception for bad input, so the app can't crash on typos.
 */
public final class ParseResult<T> {

    private final List<T> values;
    private final List<String> errors;
    private final List<String> warnings;

    ParseResult(List<T> values, List<String> errors, List<String> warnings) {
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    public List<T> getValues() {
        return values;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    /** True when there is at least one usable value and no blocking error. */
    public boolean isOk() {
        return errors.isEmpty() && !values.isEmpty();
    }

    public String errorText() {
        return String.join(" ", errors);
    }

    public String warningText() {
        return String.join(" ", warnings);
    }
}
