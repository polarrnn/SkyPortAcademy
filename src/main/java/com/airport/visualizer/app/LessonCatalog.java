package com.airport.visualizer.app;

import com.airport.visualizer.algorithms.search.BinarySearchVisualizer;
import com.airport.visualizer.algorithms.search.LinearSearchVisualizer;
import com.airport.visualizer.algorithms.sort.BubbleSort;
import com.airport.visualizer.algorithms.sort.InsertionSort;
import com.airport.visualizer.algorithms.sort.MergeSort;
import com.airport.visualizer.algorithms.sort.QuickSort;
import com.airport.visualizer.algorithms.sort.SelectionSort;
import com.airport.visualizer.algorithms.sort.SortVisualizer;
import com.airport.visualizer.core.Category;
import com.airport.visualizer.graphs.GraphTraversalVisualizer;
import com.airport.visualizer.hashing.FlightHashTableVisualizer;
import com.airport.visualizer.labs.FlightCollectionsLabView;
import com.airport.visualizer.labs.HashingLabView;
import com.airport.visualizer.labs.SearchLabView;
import com.airport.visualizer.labs.SortLabView;
import com.airport.visualizer.labs.TreeLabView;
import com.airport.visualizer.structures.compare.MapComparisonVisualizer;
import com.airport.visualizer.structures.compare.SetComparisonVisualizer;
import com.airport.visualizer.structures.lists.ArrayListDemo;
import com.airport.visualizer.structures.lists.CollectionsUtilityDemo;
import com.airport.visualizer.structures.lists.DequeDemo;
import com.airport.visualizer.structures.lists.LinkedListDemo;
import com.airport.visualizer.structures.lists.PriorityQueueDemo;
import com.airport.visualizer.structures.maps.HashMapDemo;
import com.airport.visualizer.structures.maps.LinkedHashMapDemo;
import com.airport.visualizer.structures.maps.TreeMapDemo;
import com.airport.visualizer.structures.sets.HashSetDemo;
import com.airport.visualizer.structures.sets.LinkedHashSetDemo;
import com.airport.visualizer.structures.sets.TreeSetDemo;
import com.airport.visualizer.trees.BstVisualizer;
import com.airport.visualizer.trees.HeapVisualizer;
import com.airport.visualizer.trees.TraversalVisualizer;
import com.airport.visualizer.ui.nav.NavItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The single place where lessons and labs are registered. To add a lesson, write a class that
 * implements {@link com.airport.visualizer.core.Visualizer} and add one line here (Open/Closed).
 * The sidebar groups items by category in the order listed.
 */
public final class LessonCatalog {

    private LessonCatalog() {
    }

    public static List<NavItem> items() {
        return Collections.unmodifiableList(Arrays.asList(
                // Sort I and Sort II
                NavItem.lesson(new SortVisualizer(new BubbleSort())),
                NavItem.lesson(new SortVisualizer(new SelectionSort())),
                NavItem.lesson(new SortVisualizer(new InsertionSort())),
                NavItem.lesson(new SortVisualizer(new MergeSort())),
                NavItem.lesson(new SortVisualizer(new QuickSort())),
                // Search
                NavItem.lesson(new LinearSearchVisualizer()),
                NavItem.lesson(new BinarySearchVisualizer()),
                // Trees & Graphs
                NavItem.lesson(new TraversalVisualizer()),
                NavItem.lesson(new BstVisualizer()),
                NavItem.lesson(new HeapVisualizer()),
                NavItem.lesson(new GraphTraversalVisualizer()),
                // Maps & Sets (+ hashing and comparisons)
                NavItem.lesson(new HashMapDemo()),
                NavItem.lesson(new TreeMapDemo()),
                NavItem.lesson(new LinkedHashMapDemo()),
                NavItem.lesson(new HashSetDemo()),
                NavItem.lesson(new TreeSetDemo()),
                NavItem.lesson(new LinkedHashSetDemo()),
                NavItem.lesson(new MapComparisonVisualizer()),
                NavItem.lesson(new SetComparisonVisualizer()),
                NavItem.lesson(new FlightHashTableVisualizer()),
                // Collections
                NavItem.lesson(new ArrayListDemo()),
                NavItem.lesson(new LinkedListDemo()),
                NavItem.lesson(new DequeDemo()),
                NavItem.lesson(new PriorityQueueDemo()),
                NavItem.lesson(new CollectionsUtilityDemo()),
                // Practice labs (learner input, kept separate from the guided lessons)
                NavItem.screen("Sorting Lab", "Type your own numbers and watch them get sorted", Category.LABS, SortLabView::new),
                NavItem.screen("Search Lab", "Hide a number and send a search after it", Category.LABS, SearchLabView::new),
                NavItem.screen("My Flights Lab", "Type your own flights into any Map, Set or Collection", Category.LABS, FlightCollectionsLabView::new),
                NavItem.screen("Tree Lab", "Grow your own BST or min-heap", Category.LABS, TreeLabView::new),
                NavItem.screen("Name Hashing Lab", "Hash your own names: chaining vs linear probing", Category.LABS, HashingLabView::new)
        ));
    }
}
