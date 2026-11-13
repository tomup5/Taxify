package com.taxify.map;

import com.taxify.simulation.ApplicationLibrary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.taxify.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteTest {

    @Test
    void distanceIsManhattan() {
        assertEquals(7, ApplicationLibrary.distance(at(1, 2), at(4, 6)));
        assertEquals(7, ApplicationLibrary.distance(at(4, 6), at(1, 2)));
        assertEquals(0, ApplicationLibrary.distance(at(3, 3), at(3, 3)));
    }

    @ParameterizedTest
    @CsvSource({"0,0,9,9", "9,9,0,0", "5,2,5,8", "2,5,8,5", "7,1,3,6"})
    void routeTakesOneGridStepAtATimeAndEndsAtDestination(int x1, int y1, int x2, int y2) {
        ILocation start = at(x1, y1);
        ILocation end = at(x2, y2);
        Route route = new Route(start, end);

        ILocation previous = start;
        int steps = 0;
        while (route.hasLocations()) {
            ILocation next = route.getNextLocation();
            assertEquals(1, ApplicationLibrary.distance(previous, next), "each step moves one cell");
            previous = next;
            steps++;
        }

        assertEquals(ApplicationLibrary.distance(start, end), steps, "shortest path length");
        assertEquals(x2, previous.getX());
        assertEquals(y2, previous.getY());
    }

    @Test
    void routeToSameLocationIsEmpty() {
        assertFalse(new Route(at(4, 4), at(4, 4)).hasLocations());
    }

    @Test
    void randomDestinationRespectsMinimumDistanceAndMapBounds() {
        ApplicationLibrary.setSeed(1);
        ILocation origin = at(5, 5);
        for (int i = 0; i < 500; i++) {
            ILocation destination = ApplicationLibrary.randomLocation(origin);
            assertTrue(ApplicationLibrary.distance(origin, destination) >= ApplicationLibrary.MINIMUM_DISTANCE);
            assertTrue(destination.getX() >= 0 && destination.getX() < ApplicationLibrary.MAP_WIDTH);
            assertTrue(destination.getY() >= 0 && destination.getY() < ApplicationLibrary.MAP_HEIGHT);
        }
    }

    @Test
    void sameSeedGivesSameSequence() {
        ApplicationLibrary.setSeed(99);
        ILocation a = ApplicationLibrary.randomLocation();
        ApplicationLibrary.setSeed(99);
        ILocation b = ApplicationLibrary.randomLocation();
        assertEquals(a.toString(), b.toString());
    }
}
