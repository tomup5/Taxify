package com.taxify.company;

public enum RideMode {
    STANDARD("Standard"),
    SILENT("Silent");

    private final String label;

    RideMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
