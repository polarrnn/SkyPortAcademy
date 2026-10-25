package com.airport.visualizer.graphs;

/** A vertex of the route map: a 3-letter code, a city and a fixed spot on the map (0..1 coordinates). */
public final class Airport {

    private final String code;
    private final String city;
    private final double x;
    private final double y;

    public Airport(String code, String city, double x, double y) {
        this.code = code;
        this.city = city;
        this.x = x;
        this.y = y;
    }

    public String getCode() {
        return code;
    }

    public String getCity() {
        return city;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return code + " (" + city + ")";
    }
}
