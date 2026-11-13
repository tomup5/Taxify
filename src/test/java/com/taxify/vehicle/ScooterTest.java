package com.taxify.vehicle;

import com.taxify.TestFixtures;
import com.taxify.company.TaxiCompany;
import com.taxify.people.User;
import com.taxify.simulation.ApplicationLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.taxify.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ScooterTest {

    private Scooter scooter;
    private User user;

    @BeforeEach
    void setUp() {
        ApplicationLibrary.setSeed(3);
        TaxiCompany company = new TaxiCompany("Test", new ArrayList<>(), new ArrayList<>());
        scooter = new Scooter(100, at(0, 0), null, company);
        user = TestFixtures.adult(1, 'F');
    }

    private void ride() {
        scooter.pickService(TestFixtures.serviceOfDistance(user, 4));
        scooter.startService();
        scooter.endService(1);
    }

    @Test
    void startsFullyCharged() {
        assertEquals(100.0, scooter.getBattery());
    }

    @Test
    void eachRideUsesAQuarterOfTheBattery() {
        ride();
        assertEquals(75.0, scooter.getBattery());
        ride();
        assertEquals(50.0, scooter.getBattery());
    }

    @Test
    void rechargesBeforeStartingWhenBatteryIsLow() {
        ride();
        ride();
        ride(); // battery now 25%
        assertEquals(25.0, scooter.getBattery());

        ride(); // recharge to 100% at start, then use 25%
        assertEquals(75.0, scooter.getBattery());
    }

    @Test
    void reportsItsRentalType() {
        assertSame(RentalVehicleType.SCOOTER, scooter.getRentalType());
        assertSame(MicroVehicleStatus.FREE, scooter.getStatus());
    }
}
