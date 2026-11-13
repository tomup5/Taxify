package com.taxify.company;

/** Noise preference a passenger can choose for a taxi or shuttle ride. */
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
