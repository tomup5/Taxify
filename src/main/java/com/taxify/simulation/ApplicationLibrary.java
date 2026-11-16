package com.taxify.simulation;

import com.taxify.map.ILocation;
import com.taxify.map.Location;

import java.util.Random;

public final class ApplicationLibrary {
    public static final int MINIMUM_DISTANCE = 3;
    public static final int MAP_WIDTH = 10;
    public static final int MAP_HEIGHT = 10;

    // one Random for everything so a seed reproduces the whole run
    private static Random random = new Random();

    private ApplicationLibrary() {
    }

    public static void setSeed(long seed) {
        random = new Random(seed);
    }

    public static int rand() {
        return random.nextInt(9767);
    }

    public static int rand(int max) {
        return random.nextInt(max);
    }

    public static double chance() {
        return random.nextDouble();
    }

    public static int distance(ILocation a, ILocation b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public static ILocation randomLocation() {
        return new Location(rand(MAP_WIDTH), rand(MAP_HEIGHT));
    }

    public static ILocation randomLocation(ILocation location) {
        ILocation destination;
        do {
            destination = randomLocation();
        } while (distance(location, destination) < MINIMUM_DISTANCE);
        return destination;
    }
}
