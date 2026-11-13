package com.taxify.company;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsTest {

    @Test
    void averageStarsIsZeroWithoutReviews() {
        assertEquals(0.0, new Statistics().getStars());
    }

    @Test
    void averageStarsIsRoundedToTwoDecimals() {
        Statistics stats = new Statistics();
        for (int stars : new int[] {5, 4, 4}) {
            stats.updateStars(stars);
            stats.updateReviews();
        }
        assertEquals(4.33, stats.getStars());
        assertEquals(3, stats.getReviews());
    }

    @Test
    void totalsAccumulate() {
        Statistics stats = new Statistics();
        stats.updateServices();
        stats.updateServices();
        stats.updateDistance(4);
        stats.updateDistance(6);
        stats.updateBilling(10.5);
        stats.updateBilling(2.25);

        assertEquals(2, stats.getServices());
        assertEquals(10, stats.getDistance());
        assertEquals(12.75, stats.getBilling(), 1e-9);
    }
}
