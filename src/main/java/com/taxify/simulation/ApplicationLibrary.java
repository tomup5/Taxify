package com.taxify.simulation;

import com.taxify.map.ILocation;
import com.taxify.map.Location;

import java.util.Random;

/**
 * Shared helpers for the simulation: random numbers, random map locations,
 * and Manhattan distance on the city grid.
 *
 * <p>All randomness flows through a single {@link Random} instance so that a
 * run can be reproduced exactly by calling {@link #setSeed(long)}.
 */
public final class ApplicationLibrary {
    /** Minimum distance between a trip's pickup and drop-off. */
    public static final int MINIMUM_DISTANCE = 3;
    /** Width of the city grid. */
    public static final int MAP_WIDTH = 10;
    /** Height of the city grid. */
    public static final int MAP_HEIGHT = 10;

    private static Random random = new Random();

    private ApplicationLibrary() {
    }

    /**
     * Re-seeds the shared random generator so the simulation is reproducible.
     * @param seed the seed value
     */
    public static void setSeed(long seed) {
        random = new Random(seed);
    }

    /**
     * Returns a non-negative random integer.
     * @return a random integer in [0, 9767)
     */
    public static int rand() {
        return random.nextInt(9767);
    }

    /**
     * Returns a random integer in [0, max).
     * @param max exclusive upper bound, must be positive
     * @return a random integer in [0, max)
     */
    public static int rand(int max) {
        return random.nextInt(max);
    }

    /**
     * Returns a random double in [0, 1).
     * @return a random probability value
     */
    public static double chance() {
        return random.nextDouble();
    }

    /**
     * Manhattan distance between two grid locations.
     * @param a first location
     * @param b second location
     * @return |ax - bx| + |ay - by|
     */
    public static int distance(ILocation a, ILocation b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Returns a random location on the grid.
     * @return a random location
     */
    public static ILocation randomLocation() {
        return new Location(rand(MAP_WIDTH), rand(MAP_HEIGHT));
    }

    /**
     * Returns a random location at least {@link #MINIMUM_DISTANCE} away from the given one.
     * @param location the reference location
     * @return a random location far enough from {@code location}
     */
    public static ILocation randomLocation(ILocation location) {
        ILocation destination;
        do {
            destination = randomLocation();
        } while (distance(location, destination) < MINIMUM_DISTANCE);
        return destination;
    }
}
