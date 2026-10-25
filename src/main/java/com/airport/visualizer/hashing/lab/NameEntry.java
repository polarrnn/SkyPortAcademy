package com.airport.visualizer.hashing.lab;

/** One name in the lab's queue together with its status and where it ended up. */
public final class NameEntry {

    private final String name;
    private NameStatus status = NameStatus.WAITING;
    private String detail = "";

    public NameEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public NameStatus getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public void set(NameStatus status, String detail) {
        this.status = status;
        this.detail = detail == null ? "" : detail;
    }

    public boolean matches(String other) {
        return other != null && name.equalsIgnoreCase(other.trim());
    }

    @Override
    public String toString() {
        return name + " (" + status.getLabel() + ")";
    }
}
