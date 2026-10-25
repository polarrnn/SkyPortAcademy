package com.airport.visualizer.hashing;

import com.airport.visualizer.hashing.HashSnapshot.Link;
import com.airport.visualizer.hashing.HashSnapshot.Mark;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Separate chaining: every bucket holds a real singly linked list built from {@link Node}s
 * (each node has a {@code next} pointer). Keys that collide simply share the same bucket's chain.
 */
public final class ChainedHashTable implements HashTableModel {

    /** One link in a chain: the key plus a pointer to the next node (null = end of the chain). */
    public static final class Node {

        private final String key;
        private Node next;

        Node(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public Node getNext() {
            return next;
        }
    }

    private final Node[] heads;
    private int count;

    public ChainedHashTable(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("table size must be at least 1");
        }
        heads = new Node[size];
    }

    @Override
    public HashSnapshot.Mode getMode() {
        return HashSnapshot.Mode.CHAINING;
    }

    @Override
    public int getSize() {
        return heads.length;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    /** The head node of bucket i (for tests and teaching). */
    public Node getHead(int bucket) {
        return heads[bucket];
    }

    @Override
    public boolean contains(String key) {
        for (Node n = heads[HashFunction.hash(key, heads.length)]; n != null; n = n.next) {
            if (n.key.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ insert

    @Override
    public HashOutcome insert(String key, HashStepSink sink) {
        HashFunction.Calculation calc = HashFunction.calculate(key, heads.length);
        int b = calc.getHome();
        explainHash(key, calc, sink, "find " + key + "'s bucket");

        Node cur = heads[b];
        if (cur == null) {
            heads[b] = new Node(key);
            count++;
            HashOutcome out = outcome(HashOutcome.Type.INSERTED, key, calc, b, 0);
            if (sink.isRecording()) {
                sink.step("Bucket " + b + " is empty - " + key + " moves in",
                        "Nobody lives in bucket " + b + " yet, so " + key + " becomes the first node. "
                        + "The bucket's head arrow now points to " + key + ", and " + key + "'s next arrow points to null "
                        + "(null means \"nothing after me\").",
                        snapshot().name(key).calc(calc).calcSuffix(out.storageText()).active(b).node(0)
                                .mark(Mark.INSERTED).link(Link.NEW_LINK, HashSnapshot.HEAD, 0).build());
            }
            return out;
        }
        if (sink.isRecording()) {
            sink.step("Collision in bucket " + b + "!",
                    "Bucket " + b + " already has " + chainText(b) + ". That's a collision - two keys want the same bucket. "
                    + "With separate chaining that's fine: they share the bucket by forming a chain. "
                    + "First we walk the chain to make sure " + key + " isn't already there.",
                    snapshot().name(key).calc(calc).active(b).mark(Mark.COLLISION).build());
        }
        Node last = null;
        int pos = 0;
        while (cur != null) {
            sink.compare();
            boolean same = cur.key.equalsIgnoreCase(key);
            if (same) {
                if (sink.isRecording()) {
                    sink.step(key + " is already here",
                            "Node " + (pos + 1) + " is " + cur.key + " - the very same key! A hash table keeps each key only once, "
                            + "so we don't add it again.",
                            snapshot().name(key).calc(calc).active(b).node(pos).mark(Mark.FOUND).build());
                }
                return outcome(HashOutcome.Type.DUPLICATE, key, calc, b, pos);
            }
            if (sink.isRecording()) {
                sink.step("Compare " + key + " with " + cur.key,
                        "Is " + key + " the same as " + cur.key + "? No. "
                        + (cur.next == null
                                ? cur.key + "'s next arrow points to null, so this is the end of the chain."
                                : "Follow the next arrow to " + cur.next.key + "."),
                        snapshot().name(key).calc(calc).active(b).node(pos).mark(Mark.CHECKING).build());
            }
            last = cur;
            cur = cur.next;
            if (cur != null) {
                sink.probe();
            }
            pos++;
        }
        Node fresh = new Node(key);
        last.next = fresh;
        count++;
        HashOutcome out = outcome(HashOutcome.Type.INSERTED, key, calc, b, pos);
        if (sink.isRecording()) {
            sink.step("Link " + key + " to the end of the chain",
                    "We change one arrow: " + last.key + ".next = " + key + ". Now bucket " + b + " holds "
                    + chainText(b) + ". All of them live in the same bucket - that's separate chaining!",
                    snapshot().name(key).calc(calc).calcSuffix(out.storageText()).active(b).node(pos)
                            .mark(Mark.INSERTED).link(Link.NEW_LINK, pos - 1, pos).build());
        }
        return out;
    }

    // ------------------------------------------------------------------ search

    @Override
    public HashOutcome search(String key, HashStepSink sink) {
        HashFunction.Calculation calc = HashFunction.calculate(key, heads.length);
        int b = calc.getHome();
        explainHash(key, calc, sink, "know which bucket to look in");
        int pos = 0;
        Node cur = heads[b];
        if (cur == null && sink.isRecording()) {
            sink.step("Bucket " + b + " is empty",
                    "Bucket " + b + "'s head arrow points to null - nobody is there. So " + key
                    + " is not in the table. We only had to look in ONE bucket to know that!",
                    snapshot().name(key).calc(calc).calcSuffix("not in the table").active(b).mark(Mark.MISSING).build());
        }
        while (cur != null) {
            sink.compare();
            if (cur.key.equalsIgnoreCase(key)) {
                HashOutcome out = outcome(HashOutcome.Type.FOUND, key, calc, b, pos);
                if (sink.isRecording()) {
                    sink.step("Found " + key + "!",
                            "Node " + (pos + 1) + " in bucket " + b + " is " + cur.key + " - a match! "
                            + "We found it after " + (pos + 1) + " comparison" + (pos == 0 ? "" : "s") + " instead of checking the whole table.",
                            snapshot().name(key).calc(calc).calcSuffix(out.storageText()).active(b).node(pos).mark(Mark.FOUND).build());
                }
                return out;
            }
            if (sink.isRecording()) {
                sink.step("Compare " + key + " with " + cur.key,
                        "Is " + cur.key + " the one we want? No. "
                        + (cur.next == null ? "Its next arrow points to null, so there is nobody left to check."
                                : "Hop along the next arrow to " + cur.next.key + "."),
                        snapshot().name(key).calc(calc).active(b).node(pos).mark(Mark.CHECKING).build());
            }
            cur = cur.next;
            if (cur != null) {
                sink.probe();
            }
            pos++;
            if (cur == null && sink.isRecording()) {
                sink.step(key + " is not here",
                        "We reached null at the end of bucket " + b + "'s chain without a match, so " + key + " is not in the table.",
                        snapshot().name(key).calc(calc).calcSuffix("not in the table").active(b).mark(Mark.MISSING).build());
            }
        }
        return outcome(HashOutcome.Type.NOT_FOUND, key, calc, -1, -1);
    }

    // ------------------------------------------------------------------ delete

    @Override
    public HashOutcome delete(String key, HashStepSink sink) {
        HashFunction.Calculation calc = HashFunction.calculate(key, heads.length);
        int b = calc.getHome();
        explainHash(key, calc, sink, "know which bucket to remove it from");
        Node prev = null;
        Node cur = heads[b];
        int pos = 0;
        if (cur == null && sink.isRecording()) {
            sink.step("Bucket " + b + " is empty",
                    "Bucket " + b + " has no nodes, so " + key + " isn't stored. Nothing to delete!",
                    snapshot().name(key).calc(calc).calcSuffix("not in the table").active(b).mark(Mark.MISSING).build());
        }
        while (cur != null) {
            sink.compare();
            if (cur.key.equalsIgnoreCase(key)) {
                String nextName = cur.next == null ? "null" : cur.next.key;
                int toNode = cur.next == null ? HashSnapshot.NULL : pos + 1;
                if (sink.isRecording()) {
                    sink.step("Found " + key + " - now unlink it",
                            prev == null
                                    ? cur.key + " is the head (the first node). We move bucket " + b + "'s head arrow to "
                                    + cur.key + ".next, which is " + nextName + ". The arrow now skips over " + cur.key + "."
                                    : "We change ONE arrow: " + prev.key + ".next = " + cur.key + ".next (" + nextName + "). "
                                    + "Now " + prev.key + " points past " + cur.key + ", straight to " + nextName + ".",
                            snapshot().name(key).calc(calc).active(b).node(pos).mark(Mark.DELETED)
                                    .link(Link.BYPASS, prev == null ? HashSnapshot.HEAD : pos - 1, toNode).build());
                }
                if (prev == null) {
                    heads[b] = cur.next;
                } else {
                    prev.next = cur.next;
                }
                cur.next = null;
                count--;
                if (sink.isRecording()) {
                    sink.step(key + " is removed",
                            "No arrow points to " + key + " any more, so it has left the table. Bucket " + b
                            + (heads[b] == null ? " is empty again." : " still holds a connected chain: " + chainText(b) + "."),
                            snapshot().name(key).calc(calc).calcSuffix("removed from bucket " + b).active(b).mark(Mark.DELETED).build());
                }
                return outcome(HashOutcome.Type.DELETED, key, calc, b, pos);
            }
            if (sink.isRecording()) {
                sink.step("Compare " + key + " with " + cur.key,
                        "Is " + cur.key + " the one to delete? No. We remember " + cur.key + " as the node BEFORE the next one "
                        + "(we'll need it to fix the arrow) and "
                        + (cur.next == null ? "see its next arrow is null." : "hop to " + cur.next.key + "."),
                        snapshot().name(key).calc(calc).active(b).node(pos).mark(Mark.CHECKING).build());
            }
            prev = cur;
            cur = cur.next;
            if (cur != null) {
                sink.probe();
            }
            pos++;
            if (cur == null && sink.isRecording()) {
                sink.step(key + " was not found",
                        "We reached the end of the chain (null) without finding " + key + ", so nothing is deleted.",
                        snapshot().name(key).calc(calc).calcSuffix("not in the table").active(b).mark(Mark.MISSING).build());
            }
        }
        return outcome(HashOutcome.Type.NOT_FOUND, key, calc, -1, -1);
    }

    // ------------------------------------------------------------------ helpers

    private void explainHash(String key, HashFunction.Calculation calc, HashStepSink sink, String why) {
        if (!sink.isRecording()) {
            return;
        }
        sink.step("Hash " + key + ": add up its letters",
                "Every letter has a secret number called a character code (A is 65, B is 66...). We make " + key
                + " uppercase and add the codes: " + calc.sumText() + ". We do this to " + why + ".",
                snapshot().name(key).calc(calc).mark(Mark.NONE).build());
        sink.step("Remainder = bucket number",
                calc.remainderText() + ". The remainder is what's left over after sharing " + calc.getSum() + " into groups of "
                + calc.getSize() + ". It is always between 0 and " + (calc.getSize() - 1) + ", so it is a real bucket: bucket "
                + calc.getHome() + ".",
                snapshot().name(key).calc(calc).active(calc.getHome()).mark(Mark.CHECKING).build());
    }

    private HashOutcome outcome(HashOutcome.Type type, String key, HashFunction.Calculation calc, int index, int node) {
        return new HashOutcome(type, key, HashSnapshot.Mode.CHAINING, calc, index, node, null);
    }

    /** "head → AMY → MAY → null". */
    private String chainText(int b) {
        StringBuilder sb = new StringBuilder("head");
        for (Node n = heads[b]; n != null; n = n.next) {
            sb.append(HashFunction.ARROW).append(n.key);
        }
        return sb.append(HashFunction.ARROW).append("null").toString();
    }

    @Override
    public HashSnapshot.Builder snapshot() {
        List<List<String>> chains = new ArrayList<>();
        for (Node head : heads) {
            List<String> c = new ArrayList<>();
            for (Node n = head; n != null; n = n.next) {
                c.add(n.key);
            }
            chains.add(c);
        }
        return HashSnapshot.chaining(heads.length, chains)
                .title("Separate chaining - " + heads.length + " buckets, each a linked list");
    }

    @Override
    public List<String> getKeys() {
        List<String> keys = new ArrayList<>();
        for (Node head : heads) {
            for (Node n = head; n != null; n = n.next) {
                keys.add(n.key);
            }
        }
        return Collections.unmodifiableList(keys);
    }

    @Override
    public HashTableModel copy() {
        ChainedHashTable c = new ChainedHashTable(heads.length);
        for (String k : getKeys()) {
            c.insert(k, HashStepSink.QUIET);
        }
        return c;
    }
}
