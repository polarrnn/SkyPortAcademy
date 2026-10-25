package com.airport.visualizer.hashing;

import java.util.List;

/**
 * A hash table that can explain itself. Two strategies implement it:
 * {@link ChainedHashTable} (separate chaining) and {@link LinearProbingHashTable} (open addressing).
 * Keys are compared ignoring upper / lower case, matching {@link HashFunction}.
 */
public interface HashTableModel {

    HashSnapshot.Mode getMode();

    /** Number of buckets / slots. */
    int getSize();

    /** Number of keys stored. */
    int getCount();

    /** Quiet membership test (no steps). */
    boolean contains(String key);

    /** True when no more keys can be added (only possible with open addressing). */
    boolean isFull();

    HashOutcome insert(String key, HashStepSink sink);

    HashOutcome search(String key, HashStepSink sink);

    HashOutcome delete(String key, HashStepSink sink);

    /** The current structure with no highlights; operations add highlights to it. */
    HashSnapshot.Builder snapshot();

    /** Stored keys, bucket by bucket / slot by slot. */
    List<String> getKeys();

    /** An independent copy (used to time an operation without changing the real table). */
    HashTableModel copy();

    default double getLoadFactor() {
        return getSize() == 0 ? 0 : (double) getCount() / getSize();
    }
}
