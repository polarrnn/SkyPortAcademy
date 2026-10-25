package com.airport.visualizer.structures.sets;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.hash.HashDemoBase;
import com.airport.visualizer.structures.hash.KeyStore;
import java.util.List;

/** HashSet&lt;String&gt; of destinations: duplicates are rejected, order is not kept. */
public class HashSetDemo extends HashDemoBase {

    public HashSetDemo() {
        this(FlightData.first(12));
    }

    public HashSetDemo(List<Flight> flights) {
        super("HashSet", Category.MAPS_SETS,
                "Unique destinations. A set is a sticker album: each destination can be stuck in only once.",
                flights);
    }

    @Override
    protected KeyStore newStore() {
        return KeyStore.hashSet();
    }

    @Override
    protected String keyOf(Flight f) {
        return f.getDestination();
    }

    @Override
    protected boolean isMap() {
        return false;
    }

    @Override
    protected boolean isLinked() {
        return false;
    }

    @Override
    protected String structureName() {
        return "HashSet";
    }

    @Override
    protected String keyWord() {
        return "destination";
    }

    @Override
    protected String missingKey() {
        return "Paris";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) average")
                .row("add", "add(item)", "O(1) average", "Hash to a locker; if the item is already there, add answers false.")
                .row("contains", "contains(item)", "O(1) average", "One locker jump tells you yes or no.")
                .row("remove", "remove(item)", "O(1) average", "Find the locker, take the item out.")
                .row("iterate", "iterate", "O(n + lockers)", "Every locker is visited; order looks random.")
                .whenToUse("You want a list of things with NO repeats and quick 'is it there?' checks, like all the different cities we fly to.")
                .avoidWhen("You care about order: use TreeSet for sorted or LinkedHashSet for first-come order.")
                .build();
    }
}
