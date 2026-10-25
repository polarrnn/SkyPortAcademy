package com.airport.visualizer.structures.maps;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.hash.HashDemoBase;
import com.airport.visualizer.structures.hash.KeyStore;
import java.util.List;

/** LinkedHashMap&lt;String, Flight&gt;: hash lockers PLUS a linked list remembering insertion order. */
public class LinkedHashMapDemo extends HashDemoBase {

    public LinkedHashMapDemo() {
        this(FlightData.first(10));
    }

    public LinkedHashMapDemo(List<Flight> flights) {
        super("LinkedHashMap", Category.MAPS_SETS,
                "Like a HashMap, but every item also holds hands with the one added before it, so the map remembers insertion order.",
                flights);
    }

    @Override
    protected KeyStore newStore() {
        return KeyStore.linkedHashMap();
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
        return true;
    }

    @Override
    protected String structureName() {
        return "LinkedHashMap";
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
                .row("put", "put(key, value)", "O(1) average", "Hash to a locker AND link the new item at the end of the order list. Putting an existing key keeps its old place.")
                .row("get", "get(key)", "O(1) average", "Same quick locker jump as HashMap.")
                .row("containsKey", "containsKey(key)", "O(1) average", "Locker jump, then yes or no.")
                .row("remove", "remove(key)", "O(1) average", "Unhook from the locker chain and from the order links.")
                .row("iterate", "iterate keySet()", "O(n)", "Follows the links, so you get the insertion order every time.")
                .whenToUse("You want HashMap speed but also a predictable order, e.g. flights shown in the order they were announced, or a simple cache.")
                .avoidWhen("You need sorted keys (TreeMap) or memory is very tight (the links cost extra).")
                .build();
    }
}
