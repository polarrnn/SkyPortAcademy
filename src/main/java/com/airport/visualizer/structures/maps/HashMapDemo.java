package com.airport.visualizer.structures.maps;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.hash.HashDemoBase;
import com.airport.visualizer.structures.hash.KeyStore;
import java.util.List;

/** HashMap&lt;String flightNumber, Flight&gt;: hash lockers, chains on collisions, no order. */
public class HashMapDemo extends HashDemoBase {

    public HashMapDemo() {
        this(FlightData.first(10));
    }

    public HashMapDemo(List<Flight> flights) {
        super("HashMap", Category.MAPS_SETS,
                "flightNumber -> Flight. A hash turns each flight number into a locker number, so finding a flight takes one jump.",
                flights);
    }

    @Override
    protected KeyStore newStore() {
        return KeyStore.hashMap();
    }

    @Override
    protected String keyOf(Flight f) {
        return f.getFlightNumber();
    }

    @Override
    protected boolean isMap() {
        return true;
    }

    @Override
    protected boolean isLinked() {
        return false;
    }

    @Override
    protected String structureName() {
        return "HashMap";
    }

    @Override
    protected String keyWord() {
        return "flight number";
    }

    @Override
    protected String missingKey() {
        return "ZZ999";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(1) average")
                .row("put", "put(key, value)", "O(1) average", "Hash the key, jump to its locker, drop it in. Worst case O(n) if every key lands in one locker.")
                .row("get", "get(key)", "O(1) average", "One hash, one jump, maybe a peek at a short chain.")
                .row("containsKey", "containsKey(key)", "O(1) average", "Same trip as get - just answers yes or no.")
                .row("remove", "remove(key)", "O(1) average", "Find the locker, unhook the item from the chain.")
                .row("iterate", "iterate keySet()", "O(n + lockers)", "Visits every locker once. The order looks random.")
                .whenToUse("You want to look things up by a key (flight number -> flight) as fast as possible and you do not care about order. The most used map in Java!")
                .avoidWhen("You need the keys sorted (use TreeMap) or in the order you added them (use LinkedHashMap).")
                .build();
    }
}
