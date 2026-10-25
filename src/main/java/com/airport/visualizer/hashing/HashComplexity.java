package com.airport.visualizer.hashing;

import com.airport.visualizer.core.ComplexityInfo;

/** Big-O facts shared by the hashing lesson and the Name Hashing Lab. */
public final class HashComplexity {

    private HashComplexity() {
    }

    public static ComplexityInfo info() {
        return ComplexityInfo.builder("O(1) avg")
                .row("hash", "Hash a key", "O(k)",
                        "Add the codes of the k letters and take the remainder. Keys are short, so this is basically instant.")
                .row("insert", "Insert (put)", "O(1) avg",
                        "Usually the home bucket is free or has a short chain. Worst case O(n): every key collided into one spot.")
                .row("search", "Search (get)", "O(1) avg",
                        "We only look in ONE bucket (or a short probe trail) instead of the whole table. Worst case O(n).")
                .row("delete", "Delete (remove)", "O(1) avg",
                        "Chaining: change one next arrow. Linear probing: leave a DELETED marker. Worst case O(n).")
                .row("load", "Load factor", "keep < 0.75",
                        "Load factor = keys / buckets. The fuller the table, the more collisions, the longer the chains "
                        + "and probe trails. Java's HashMap doubles its size when it passes 0.75.")
                .whenToUse("Use a hash table when you need to find things by a key super fast - like looking up a flight "
                        + "by its number or checking if a name is on the guest list. Java's HashMap and HashSet are hash "
                        + "tables that use separate chaining.")
                .avoidWhen("You need the keys in sorted order (use a TreeMap), or your hash function is poor and sends "
                        + "most keys to the same bucket - then it becomes as slow as checking a list one by one.")
                .build();
    }
}
