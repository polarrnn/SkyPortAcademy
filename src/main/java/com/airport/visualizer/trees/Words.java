package com.airport.visualizer.trees;

/**
 * The kid-friendly words used in narration, so the same tree code can talk about
 * "earlier / later flights" or "smaller / bigger numbers".
 */
public final class Words {

    public static final Words FLIGHTS = new Words("earlier", "later", "earliest", "flight", "flights", "departure time");
    public static final Words NUMBERS = new Words("smaller", "bigger", "smallest", "number", "numbers", "value");

    private final String less;
    private final String more;
    private final String least;
    private final String thing;
    private final String things;
    private final String keyName;

    private Words(String less, String more, String least, String thing, String things, String keyName) {
        this.less = less;
        this.more = more;
        this.least = least;
        this.thing = thing;
        this.things = things;
        this.keyName = keyName;
    }

    /** "earliest" / "smallest". */
    public String least() {
        return least;
    }

    public String less() {
        return less;
    }

    public String more() {
        return more;
    }

    public String thing() {
        return thing;
    }

    public String things() {
        return things;
    }

    public String keyName() {
        return keyName;
    }
}
