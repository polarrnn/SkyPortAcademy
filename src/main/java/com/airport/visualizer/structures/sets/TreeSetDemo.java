package com.airport.visualizer.structures.sets;

import com.airport.visualizer.core.Category;
import com.airport.visualizer.core.ComplexityInfo;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.model.FlightData;
import com.airport.visualizer.structures.tree.SortedStore;
import com.airport.visualizer.structures.tree.SortedTreeDemoBase;
import java.util.List;

/** TreeSet&lt;String&gt; of airlines: no duplicates, always alphabetical. */
public class TreeSetDemo extends SortedTreeDemoBase<String> {

    public TreeSetDemo() {
        this(FlightData.first(12));
    }

    public TreeSetDemo(List<Flight> flights) {
        super("TreeSet", Category.MAPS_SETS,
                "Sorted airlines: every airline once, always in ABC order - like a dictionary that never has the same word twice.",
                flights);
    }

    @Override
    protected SortedStore<String> newStore() {
        return SortedStore.treeSet();
    }

    @Override
    protected String keyOf(Flight f) {
        return f.getAirline();
    }

    @Override
    protected String keyText(String key) {
        return key;
    }

    @Override
    protected boolean isMap() {
        return false;
    }

    @Override
    protected String structureName() {
        return "TreeSet";
    }

    @Override
    protected String keyWord() {
        return "airline";
    }

    @Override
    protected String missingKey() {
        return "Zebra Air";
    }

    @Override
    public ComplexityInfo getComplexity() {
        return ComplexityInfo.builder("O(log n)")
                .row("add", "add(item)", "O(log n)", "Walk one path to find the spot; repeats answer false.")
                .row("contains", "contains(item)", "O(log n)", "Left or right at each step, like a guessing game with hints.")
                .row("first", "first() / last()", "O(log n)", "All the way left = first in ABC order.")
                .row("remove", "remove(item)", "O(log n)", "Find, unhook, re-balance.")
                .row("iterate", "iterate", "O(n)", "Always alphabetical.")
                .whenToUse("You want unique items that stay sorted, like the list of airlines in ABC order, or the earliest / latest item quickly.")
                .avoidWhen("You don't need sorting - HashSet is faster.")
                .build();
    }
}
