package com.airport.visualizer.hashing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** What an insert / search / delete ended with, and where. */
public final class HashOutcome {

    public enum Type {
        INSERTED, DUPLICATE, FULL, FOUND, NOT_FOUND, DELETED
    }

    private final Type type;
    private final String key;
    private final HashSnapshot.Mode mode;
    private final HashFunction.Calculation calc;
    private final int index;
    private final int node;
    private final List<Integer> probes;

    public HashOutcome(Type type, String key, HashSnapshot.Mode mode, HashFunction.Calculation calc,
            int index, int node, List<Integer> probes) {
        this.type = type;
        this.key = key;
        this.mode = mode;
        this.calc = calc;
        this.index = index;
        this.node = node;
        this.probes = probes == null ? Collections.<Integer>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(probes));
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public HashFunction.Calculation getCalc() {
        return calc;
    }

    /** Bucket (chaining) or slot (probing); -1 when nowhere. */
    public int getIndex() {
        return index;
    }

    /** Position inside the chain (0 = head), chaining only; -1 otherwise. */
    public int getNode() {
        return node;
    }

    /** Probing: every slot looked at, in order. */
    public List<Integer> getProbes() {
        return probes;
    }

    /** "bucket 3, node 2" or "slot 4", or "-" when the key is nowhere. */
    public String locationText() {
        if (index < 0) {
            return "-";
        }
        if (mode == HashSnapshot.Mode.CHAINING) {
            return "bucket " + index + ", node " + (node + 1);
        }
        return "slot " + index;
    }

    /** "stored at slot 2 (probed 0, 1, 2)" style suffix for the calculation line. */
    public String storageText() {
        if (type == Type.FULL) {
            return "table full, not stored (probed " + joined() + ")";
        }
        if (index < 0) {
            return "not in the table";
        }
        if (mode == HashSnapshot.Mode.CHAINING) {
            return "bucket " + index + " (node " + (node + 1) + " of its chain)";
        }
        return "stored at slot " + index + (probes.size() > 1 ? " (probed " + joined() + ")" : "");
    }

    private String joined() {
        StringBuilder sb = new StringBuilder();
        for (int p : probes) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(p);
        }
        return sb.toString();
    }
}
