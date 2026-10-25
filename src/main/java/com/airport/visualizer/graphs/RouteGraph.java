package com.airport.visualizer.graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Undirected graph of airports joined by routes, stored as an adjacency list
 * (each airport keeps a list of its neighbours, in alphabetical order so walks are predictable).
 */
public final class RouteGraph {

    private final Map<String, Airport> airports = new LinkedHashMap<>();
    private final Map<String, TreeSet<String>> adjacency = new LinkedHashMap<>();
    private final List<String[]> routes = new ArrayList<>();

    public void addAirport(Airport a) {
        airports.put(a.getCode(), a);
        adjacency.put(a.getCode(), new TreeSet<String>());
    }

    public void addRoute(String a, String b) {
        if (!airports.containsKey(a) || !airports.containsKey(b) || a.equals(b)) {
            throw new IllegalArgumentException("Unknown airport in route " + a + "-" + b);
        }
        if (adjacency.get(a).add(b)) {
            adjacency.get(b).add(a);
            routes.add(new String[]{a, b});
        }
    }

    public Airport get(String code) {
        return airports.get(code);
    }

    public List<Airport> getAirports() {
        return new ArrayList<>(airports.values());
    }

    public List<String> neighbours(String code) {
        TreeSet<String> n = adjacency.get(code);
        return n == null ? Collections.<String>emptyList() : new ArrayList<>(n);
    }

    public List<String[]> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public int airportCount() {
        return airports.size();
    }

    public int routeCount() {
        return routes.size();
    }

    /** The demo network: 11 airports around Asia-Pacific plus Dubai, Doha and Los Angeles. */
    public static RouteGraph sample() {
        RouteGraph g = new RouteGraph();
        g.addAirport(new Airport("DXB", "Dubai", 0.06, 0.30));
        g.addAirport(new Airport("DOH", "Doha", 0.05, 0.62));
        g.addAirport(new Airport("BKK", "Bangkok", 0.30, 0.50));
        g.addAirport(new Airport("SIN", "Singapore", 0.33, 0.84));
        g.addAirport(new Airport("HKG", "Hong Kong", 0.47, 0.32));
        g.addAirport(new Airport("MNL", "Manila", 0.55, 0.56));
        g.addAirport(new Airport("CEB", "Cebu", 0.64, 0.76));
        g.addAirport(new Airport("ICN", "Seoul", 0.58, 0.08));
        g.addAirport(new Airport("NRT", "Tokyo", 0.74, 0.22));
        g.addAirport(new Airport("SYD", "Sydney", 0.78, 0.95));
        g.addAirport(new Airport("LAX", "Los Angeles", 0.96, 0.45));
        g.addRoute("DXB", "DOH");
        g.addRoute("DXB", "BKK");
        g.addRoute("DXB", "HKG");
        g.addRoute("DOH", "SIN");
        g.addRoute("BKK", "SIN");
        g.addRoute("BKK", "HKG");
        g.addRoute("HKG", "MNL");
        g.addRoute("HKG", "ICN");
        g.addRoute("SIN", "MNL");
        g.addRoute("SIN", "SYD");
        g.addRoute("MNL", "CEB");
        g.addRoute("MNL", "NRT");
        g.addRoute("ICN", "NRT");
        g.addRoute("CEB", "SYD");
        g.addRoute("NRT", "LAX");
        g.addRoute("SYD", "LAX");
        return g;
    }
}
