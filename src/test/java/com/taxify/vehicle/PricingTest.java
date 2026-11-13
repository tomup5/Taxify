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

/** Fares are 1 + distance, scaled per vehicle type. */
class PricingTest {

    private TaxiCompany company;
    private User user;

    @BeforeEach
    void setUp() {
        ApplicationLibrary.setSeed(7);
        company = new TaxiCompany("Test", new ArrayList<>(), new ArrayList<>());
        user = TestFixtures.adult(1, 'M');
    }

    @Test
    void taxiChargesDoubleTheBaseFare() {
        Taxi taxi = new Taxi(1, at(0, 0), company, TestFixtures.maleDriver());
        taxi.pickService(TestFixtures.serviceOfDistance(user, 5));
        assertEquals((1 + 5) * 2.0, taxi.calculateCost(), 1e-9);
    }

    @Test
    void shuttleChargesOneAndAHalfTimesTheBaseFare() {
        Shuttle shuttle = new Shuttle(1, at(0, 0), company, TestFixtures.maleDriver());
        shuttle.pickService(TestFixtures.serviceOfDistance(user, 5));
        assertEquals((1 + 5) * 1.5, shuttle.calculateCost(), 1e-9);
    }

    @Test
    void bikeChargesBaseFeePlusQuarterOfDistance() {
        Bike bike = new Bike(1, at(0, 0), null, company);
        bike.pickService(TestFixtures.serviceOfDistance(user, 8));
        assertEquals(1 + 8 * 0.25, bike.calculateCost(), 1e-9);
    }

    @Test
    void scooterChargesBaseFeePlusFifteenPercentOfDistance() {
        Scooter scooter = new Scooter(1, at(0, 0), null, company);
        scooter.pickService(TestFixtures.serviceOfDistance(user, 8));
        assertEquals(1 + 8 * 0.15, scooter.calculateCost(), 1e-9);
    }

    @Test
    void sharedRideBillsEachRiderSixtyPercent() {
        Taxi taxi = new Taxi(1, at(0, 0), company, TestFixtures.maleDriver());
        var service = TestFixtures.serviceOfDistance(user, 5);
        service.addUser(TestFixtures.adult(2, 'F'));
        taxi.pickService(service);
        double singleFare = taxi.calculateCost();

        taxi.startService();
        taxi.endService(2);

        assertEquals(singleFare * 0.6 * 2, taxi.getStatistics().getBilling(), 1e-9);
        assertEquals(1, taxi.getStatistics().getServices());
        assertEquals(5, taxi.getStatistics().getDistance());
    }
}
