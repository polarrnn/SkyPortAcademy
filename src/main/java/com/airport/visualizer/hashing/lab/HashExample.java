package com.airport.visualizer.hashing.lab;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Ready-made name sets that show one idea each. All use the Simple Hash (sum of codes % size).
 * Sums for size 7: Amy/May/Yam 231 -> 0, Eli 218 -> 1, Tom 240 -> 2, Ben/Hal/Carl/Ivy/Ola/Uma 3,
 * Ana/Mia/Ray 5, Jo/Max 6, Fay/Leo 0, Gus/Kim 1, Val/Zed 227 -> 3.
 */
public final class HashExample {

    private final String title;
    private final int size;
    private final List<String> names;
    private final String explanation;

    private HashExample(String title, int size, String explanation, String... names) {
        this.title = title;
        this.size = size;
        this.explanation = explanation;
        this.names = Collections.unmodifiableList(Arrays.asList(names));
    }

    public static List<HashExample> all() {
        return Arrays.asList(
                new HashExample("Simple Hash: No collisions", 7,
                        "Every name lands in a different home spot (0, 1, 2, 3, 5, 6), so nobody has to share or move.",
                        "Amy", "Eli", "Tom", "Ben", "Ana", "Jo"),
                new HashExample("Simple Hash: With collisions", 7,
                        "Jo and Max both hash to 6, Amy wants 0, Ben and Hal both want 3. In linear probing Max finds slot 6 "
                        + "taken and WRAPS AROUND to slot 0, so Amy then gets pushed to slot 1.",
                        "Jo", "Ana", "Max", "Amy", "Ben", "Hal"),
                new HashExample("Long chain", 7,
                        "Ben, Carl, Hal, Ivy, Ola and Uma all hash to 3 - one very long chain (or a long probe trail that wraps around).",
                        "Ben", "Carl", "Hal", "Ivy", "Ola", "Uma", "Ana"),
                new HashExample("Multiple chains", 7,
                        "Three groups collide: Amy/Fay/Leo -> 0, Eli/Gus/Kim -> 1, Ana/Mia -> 5. With linear probing 8 names "
                        + "can't fit in 7 slots, so the last one finds the table FULL.",
                        "Amy", "Fay", "Leo", "Eli", "Gus", "Kim", "Ana", "Mia"),
                new HashExample("Equal character sums", 7,
                        "Amy, May and Yam use the same letters, so their codes add up to the same 231. Uma, Val and Zed are "
                        + "different letters but also add up to the same 227. Same sum = same home spot!",
                        "Amy", "May", "Yam", "Uma", "Val", "Zed"));
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public List<String> getNames() {
        return names;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return title;
    }
}
