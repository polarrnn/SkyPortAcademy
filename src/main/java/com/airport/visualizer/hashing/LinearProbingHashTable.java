package com.airport.visualizer.hashing;

import com.airport.visualizer.hashing.HashSnapshot.Mark;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Open addressing with linear probing: one key per slot. On a collision we try the next slot
 * (home, home+1, home+2 ... wrapping from the last slot back to slot 0).
 * Deleting leaves a DELETED marker (tombstone) so later searches don't stop too early.
 */
public final class LinearProbingHashTable implements HashTableModel {

    private final String[] slots;
    private final boolean[] deleted;
    private int count;

    public LinearProbingHashTable(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("table size must be at least 1");
        }
        slots = new String[size];
        deleted = new boolean[size];
    }

    @Override
    public HashSnapshot.Mode getMode() {
        return HashSnapshot.Mode.PROBING;
    }

    @Override
    public int getSize() {
        return slots.length;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean isFull() {
        return count >= slots.length;
    }

    public String getSlot(int i) {
        return slots[i];
    }

    public boolean isDeletedMarker(int i) {
        return deleted[i];
    }

    @Override
    public boolean contains(String key) {
        return find(key, HashStepSink.QUIET, null, new ArrayList<Integer>(), false) >= 0;
    }

    private static boolean isEmpty(String[] s, boolean[] d, int i) {
        return s[i] == null && !d[i];
    }

    private int next(int i) {
        return (i + 1) % slots.length;
    }

    /** "slot 4" or "slot 6, the last one, so we wrap around to slot 0". */
    private String nextText(int idx) {
        return idx == slots.length - 1
                ? "slot " + idx + " is the LAST slot, so we wrap around to slot 0 (like a merry-go-round)"
                : "we try the next slot, " + next(idx);
    }

    // ------------------------------------------------------------------ insert

    @Override
    public HashOutcome insert(String key, HashStepSink sink) {
        HashFunction.Calculation calc = HashFunction.calculate(key, slots.length);
        int home = calc.getHome();
        explainHash(key, calc, sink);
        List<Integer> path = new ArrayList<>();
        int firstTomb = -1;
        int idx = home;
        for (int i = 0; i < slots.length; i++, idx = next(idx)) {
            path.add(idx);
            sink.probe();
            boolean lastTry = i == slots.length - 1;
            if (isEmpty(slots, deleted, idx)) {
                int target = firstTomb >= 0 ? firstTomb : idx;
                if (firstTomb >= 0 && sink.isRecording()) {
                    sink.step("Slot " + idx + " is EMPTY - " + key + " is not further along",
                            "An EMPTY slot ends the probe trail, so we now know " + key + " is not already stored. "
                            + "We go back to the DELETED spot we remembered (slot " + firstTomb + ") and reuse it.",
                            snapshot().name(key).calc(calc).active(idx).path(path).mark(Mark.CHECKING).build());
                }
                place(key, target);
                HashOutcome out = new HashOutcome(HashOutcome.Type.INSERTED, key, HashSnapshot.Mode.PROBING, calc, target, -1, path);
                if (sink.isRecording()) {
                    sink.step(key + " is stored in slot " + target,
                            (target == home ? "Slot " + home + " (the home slot) was free, so " + key + " moves right in. No collision!"
                                    : key + " could not have its home slot " + home + ", so it sits in slot " + target
                                    + " instead. It took " + path.size() + " probes: " + listText(path) + ".")
                            + " Load factor is now " + count + "/" + slots.length + ".",
                            snapshot().name(key).calc(calc).calcSuffix(out.storageText()).active(target).path(path)
                                    .mark(Mark.INSERTED).build());
                }
                return out;
            }
            if (deleted[idx]) {
                if (firstTomb < 0) {
                    firstTomb = idx;
                }
                if (sink.isRecording()) {
                    sink.step("Slot " + idx + " has a DELETED marker",
                            "Someone used to sit in slot " + idx + ". We could reuse it, so we remember it - but first we keep checking, "
                            + "because " + key + " might already be stored further along."
                            + (lastTry ? " That was the last slot to check." : " So " + nextText(idx) + "."),
                            snapshot().name(key).calc(calc).active(idx).path(path).mark(Mark.CHECKING).build());
                }
                continue;
            }
            sink.compare();
            if (slots[idx].equalsIgnoreCase(key)) {
                if (sink.isRecording()) {
                    sink.step(key + " is already here",
                            "Slot " + idx + " already holds " + slots[idx] + ". Each key is stored only once, so we stop.",
                            snapshot().name(key).calc(calc).active(idx).path(path).mark(Mark.FOUND).build());
                }
                return new HashOutcome(HashOutcome.Type.DUPLICATE, key, HashSnapshot.Mode.PROBING, calc, idx, -1, path);
            }
            if (sink.isRecording()) {
                sink.step("Slot " + idx + " is taken by " + slots[idx] + " - collision!",
                        "Two keys want the same slot. Linear probing has a simple rule: look at the next slot."
                        + (lastTry ? " But that was the last slot we had not checked yet." : " So " + nextText(idx) + "."),
                        snapshot().name(key).calc(calc).active(idx).path(path).mark(Mark.COLLISION).build());
            }
        }
        if (firstTomb >= 0) {
            place(key, firstTomb);
            HashOutcome out = new HashOutcome(HashOutcome.Type.INSERTED, key, HashSnapshot.Mode.PROBING, calc, firstTomb, -1, path);
            if (sink.isRecording()) {
                sink.step(key + " reuses DELETED slot " + firstTomb,
                        "We checked every slot and " + key + " is not stored anywhere, so it takes the DELETED spot we remembered.",
                        snapshot().name(key).calc(calc).calcSuffix(out.storageText()).active(firstTomb).path(path)
                                .mark(Mark.INSERTED).build());
            }
            return out;
        }
        HashOutcome out = new HashOutcome(HashOutcome.Type.FULL, key, HashSnapshot.Mode.PROBING, calc, -1, -1, path);
        if (sink.isRecording()) {
            sink.step("The table is full!",
                    "We checked all " + slots.length + " slots and every one is taken. There is no room for " + key
                    + ". (Real hash tables grow bigger before this happens - Java's HashMap resizes when it is 75% full.)",
                    snapshot().name(key).calc(calc).calcSuffix(out.storageText()).path(path).mark(Mark.FULL).build());
        }
        return out;
    }

    private void place(String key, int slot) {
        slots[slot] = key;
        deleted[slot] = false;
        count++;
    }

    // ------------------------------------------------------------------ search / delete

    @Override
    public HashOutcome search(String key, HashStepSink sink) {
        HashFunction.Calculation calc = HashFunction.calculate(key, slots.length);
        explainHash(key, calc, sink);
        List<Integer> path = new ArrayList<>();
        int at = find(key, sink, calc, path, false);
        if (at < 0) {
            return new HashOutcome(HashOutcome.Type.NOT_FOUND, key, HashSnapshot.Mode.PROBING, calc, -1, -1, path);
        }
        return new HashOutcome(HashOutcome.Type.FOUND, key, HashSnapshot.Mode.PROBING, calc, at, -1, path);
    }

    @Override
    public HashOutcome delete(String key, HashStepSink sink) {
        HashFunction.Calculation calc = HashFunction.calculate(key, slots.length);
        explainHash(key, calc, sink);
        List<Integer> path = new ArrayList<>();
        int at = find(key, sink, calc, path, true);
        if (at < 0) {
            return new HashOutcome(HashOutcome.Type.NOT_FOUND, key, HashSnapshot.Mode.PROBING, calc, -1, -1, path);
        }
        String stored = slots[at];
        slots[at] = null;
        deleted[at] = true;
        count--;
        if (sink.isRecording()) {
            sink.step("Leave a DELETED marker in slot " + at,
                    stored + " leaves, but we don't make slot " + at + " EMPTY. We put a DELETED marker (a \"tombstone\") there. "
                    + "Why? Keys that collided earlier may sit AFTER this slot. A search stops at an EMPTY slot, "
                    + "so an empty hole here would hide them. The marker says \"keep looking!\" - and a new key may reuse the spot.",
                    snapshot().name(key).calc(calc).calcSuffix("removed from slot " + at + ", DELETED marker left")
                            .active(at).path(path).mark(Mark.DELETED).build());
        }
        return new HashOutcome(HashOutcome.Type.DELETED, key, HashSnapshot.Mode.PROBING, calc, at, -1, path);
    }

    /**
     * Follows the probe sequence: skips DELETED markers, stops at an EMPTY slot or after
     * visiting every slot once. Returns the slot of the key or -1.
     */
    private int find(String key, HashStepSink sink, HashFunction.Calculation calc, List<Integer> path, boolean forDelete) {
        int idx = HashFunction.hash(key, slots.length);
        for (int i = 0; i < slots.length; i++, idx = next(idx)) {
            path.add(idx);
            sink.probe();
            boolean lastTry = i == slots.length - 1;
            if (isEmpty(slots, deleted, idx)) {
                if (sink.isRecording()) {
                    sink.step("Slot " + idx + " is EMPTY - stop",
                            "An EMPTY slot means the probe trail ends here: if " + key + " had been stored, it would have been "
                            + "placed here or before. So " + key + " is not in the table"
                            + (forDelete ? " and there is nothing to delete." : "."),
                            snapshot().name(key).calc(calc).calcSuffix("not in the table").active(idx).path(path)
                                    .mark(Mark.MISSING).build());
                }
                return -1;
            }
            if (deleted[idx]) {
                if (sink.isRecording()) {
                    sink.step("Slot " + idx + ": DELETED marker - keep going",
                            "A DELETED marker means \"someone used to be here\". It is NOT empty, so we must not stop."
                            + (lastTry ? " That was the last slot to check." : " So " + nextText(idx) + "."),
                            snapshot().name(key).calc(calc).active(idx).path(path).mark(Mark.CHECKING).build());
                }
                continue;
            }
            sink.compare();
            if (slots[idx].equalsIgnoreCase(key)) {
                if (sink.isRecording()) {
                    sink.step("Found " + key + " in slot " + idx + "!",
                            "Slot " + idx + " holds " + slots[idx] + " - a match after " + path.size() + " probe"
                            + (path.size() == 1 ? "" : "s") + " (" + listText(path) + ")."
                            + (forDelete ? " Now we can remove it." : ""),
                            snapshot().name(key).calc(calc).calcSuffix("found at slot " + idx).active(idx).path(path)
                                    .mark(Mark.FOUND).build());
                }
                return idx;
            }
            if (sink.isRecording()) {
                sink.step("Slot " + idx + " has " + slots[idx] + ", not " + key,
                        "Not a match. " + key + " may have been pushed further along by a collision."
                        + (lastTry ? " That was the last slot to check." : " So " + nextText(idx) + "."),
                        snapshot().name(key).calc(calc).active(idx).path(path).mark(Mark.CHECKING).build());
            }
        }
        if (sink.isRecording()) {
            sink.step(key + " is not in the table",
                    "We visited every slot once (" + listText(path) + ") without finding " + key + ", so we stop - no endless loop!",
                    snapshot().name(key).calc(calc).calcSuffix("not in the table").path(path).mark(Mark.MISSING).build());
        }
        return -1;
    }

    private void explainHash(String key, HashFunction.Calculation calc, HashStepSink sink) {
        if (!sink.isRecording()) {
            return;
        }
        sink.step("Hash " + key + ": add up its letters",
                "Every letter has a secret number called a character code (A is 65, B is 66...). We make " + key
                + " uppercase and add the codes: " + calc.sumText() + ".",
                snapshot().name(key).calc(calc).build());
        sink.step("Remainder = home slot",
                calc.remainderText() + ". The remainder is what's left after sharing " + calc.getSum() + " into groups of "
                + calc.getSize() + ", so " + key + "'s home slot is " + calc.getHome() + ". We start looking there.",
                snapshot().name(key).calc(calc).active(calc.getHome()).mark(Mark.CHECKING).build());
    }

    private static String listText(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int p : path) {
            if (sb.length() > 0) {
                sb.append(HashFunction.ARROW.trim()).append(' ');
            }
            sb.append(p).append(' ');
        }
        return sb.toString().trim();
    }

    @Override
    public HashSnapshot.Builder snapshot() {
        return HashSnapshot.probing(slots.length, slots, deleted)
                .title("Open addressing (linear probing) - " + slots.length + " slots, one key each");
    }

    @Override
    public List<String> getKeys() {
        List<String> keys = new ArrayList<>();
        for (String s : slots) {
            if (s != null) {
                keys.add(s);
            }
        }
        return Collections.unmodifiableList(keys);
    }

    @Override
    public HashTableModel copy() {
        LinearProbingHashTable c = new LinearProbingHashTable(slots.length);
        System.arraycopy(slots, 0, c.slots, 0, slots.length);
        System.arraycopy(deleted, 0, c.deleted, 0, deleted.length);
        c.count = count;
        return c;
    }
}
