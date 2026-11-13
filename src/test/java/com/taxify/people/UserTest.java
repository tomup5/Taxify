package com.taxify.people;

import com.taxify.TestFixtures;
import com.taxify.company.RideMode;
import com.taxify.company.TaxiCompany;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void womenAndChildrenAreEligibleForPinkRides() {
        assertTrue(TestFixtures.adult(1, 'F').isEligibleForPinkRide());
        assertTrue(TestFixtures.child(2, 'M').isEligibleForPinkRide());
        assertTrue(TestFixtures.child(3, 'F').isEligibleForPinkRide());
        assertFalse(TestFixtures.adult(4, 'M').isEligibleForPinkRide());
    }

    @Test
    void adultMenCannotRequestPinkRides() {
        User man = TestFixtures.adult(1, 'M');
        new TaxiCompany("Test", new ArrayList<>(List.of(man)), new ArrayList<>());

        assertThrows(IllegalArgumentException.class, () -> man.requestService(true, RideMode.STANDARD));
    }

    @Test
    void nameIsSplitIntoFirstAndLast() {
        Driver driver = TestFixtures.femaleDriver();
        assertEquals("Ana", driver.getFirstName());
        assertEquals("Ruiz", driver.getLastName());
        assertEquals("Ana Ruiz", driver.getName());
    }
}
