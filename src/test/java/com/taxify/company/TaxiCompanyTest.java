package com.taxify.company;

import com.taxify.TestFixtures;
import com.taxify.people.IUser;
import com.taxify.people.User;
import com.taxify.simulation.ApplicationLibrary;
import com.taxify.vehicle.Bike;
import com.taxify.vehicle.IVehicle;
import com.taxify.vehicle.RentalVehicleType;
import com.taxify.vehicle.Scooter;
import com.taxify.vehicle.Taxi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.taxify.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaxiCompanyTest {

    private List<IUser> users;
    private List<IVehicle> vehicles;
    private TaxiCompany company;
    private List<String> events;

    @BeforeEach
    void setUp() {
        ApplicationLibrary.setSeed(11);
        users = new ArrayList<>();
        vehicles = new ArrayList<>();
        company = new TaxiCompany("Test", users, vehicles);
        events = new ArrayList<>();
        company.addObserver(events::add);
    }

    private <U extends IUser> U addUser(U user) {
        users.add(user);
        user.setCompany(company);
        return user;
    }

    private Taxi addTaxi(int id, boolean femaleDriver) {
        Taxi taxi = new Taxi(id, ApplicationLibrary.randomLocation(), company,
            femaleDriver ? TestFixtures.femaleDriver() : TestFixtures.maleDriver());
        vehicles.add(taxi);
        return taxi;
    }

    @Test
    void assignsAFreeTaxiAndMarksTheUserAsRiding() {
        Taxi taxi = addTaxi(1, false);
        User user = addUser(TestFixtures.adult(1, 'M'));

        assertTrue(company.provideService(1, false, RideMode.STANDARD));

        assertTrue(user.getService());
        assertFalse(taxi.isFree());
        assertSame(user, taxi.getService().getUsers().get(0));
        assertEquals(1, company.getTotalServices());
        assertTrue(events.get(0).contains("assigned to Taxi 1"));
    }

    @Test
    void userAlreadyRidingCannotBookASecondRide() {
        addTaxi(1, false);
        Taxi second = addTaxi(2, false);
        addUser(TestFixtures.adult(1, 'M'));

        assertTrue(company.provideService(1, false, RideMode.STANDARD));
        assertFalse(company.provideService(1, false, RideMode.SILENT));
        assertFalse(company.provideRentalService(1, RentalVehicleType.BIKE));

        assertTrue(second.isFree());
        assertEquals(1, company.getTotalServices());
    }

    @Test
    void taxiRequestsAreNeverAssignedToBikesOrScooters() {
        vehicles.add(new Bike(100, at(0, 0), null, company));
        vehicles.add(new Scooter(101, at(1, 1), null, company));
        addUser(TestFixtures.adult(1, 'M'));

        assertFalse(company.provideService(1, false, RideMode.STANDARD));
    }

    @Test
    void pinkRidesOnlyGoToFemaleDrivers() {
        Taxi maleDriven = addTaxi(1, false);
        Taxi femaleDriven = addTaxi(2, true);
        addUser(TestFixtures.adult(1, 'F'));

        assertTrue(company.provideService(1, true, RideMode.STANDARD));

        assertTrue(maleDriven.isFree());
        assertFalse(femaleDriven.isFree());
        assertTrue(femaleDriven.getService().isPinkRide());
    }

    @Test
    void pinkRideFailsWithoutAFemaleDriver() {
        addTaxi(1, false);
        addUser(TestFixtures.adult(1, 'F'));

        assertFalse(company.provideService(1, true, RideMode.STANDARD));
    }

    @Test
    void usersShareARideWhenNoTaxiIsFreeAndEveryoneAgrees() {
        Taxi taxi = addTaxi(1, false);
        addUser(TestFixtures.sharer(1));
        addUser(TestFixtures.sharer(2));
        User third = addUser(TestFixtures.sharer(3));

        assertTrue(company.provideService(1, false, RideMode.SILENT));
        assertTrue(company.provideService(2, false, RideMode.SILENT));

        assertEquals(2, taxi.getService().getUsers().size());
        assertTrue(events.get(1).contains("(shared ride)"));

        // ride is full
        assertFalse(company.provideService(3, false, RideMode.SILENT));
        assertFalse(third.getService());
    }

    @Test
    void sharingNeedsConsentFromEveryPassenger() {
        addTaxi(1, false);
        addUser(TestFixtures.loner(1));
        User second = addUser(TestFixtures.sharer(2));

        assertTrue(company.provideService(1, false, RideMode.STANDARD));
        assertFalse(company.provideService(2, false, RideMode.STANDARD));
        assertFalse(second.getService());
    }

    @Test
    void sharedRidesMustUseTheSameRideMode() {
        addTaxi(1, false);
        addUser(TestFixtures.sharer(1));
        addUser(TestFixtures.sharer(2));

        assertTrue(company.provideService(1, false, RideMode.SILENT));
        assertFalse(company.provideService(2, false, RideMode.STANDARD));
    }

    @Test
    void findsTheNearestFreeRentalOfTheRequestedType() {
        vehicles.add(new Bike(100, at(0, 0), null, company));
        vehicles.add(new Bike(101, at(9, 9), null, company));
        vehicles.add(new Scooter(102, at(8, 8), null, company));

        assertEquals(1, company.findNearestRental(RentalVehicleType.BIKE, at(8, 8)));
        assertEquals(0, company.findNearestRental(RentalVehicleType.BIKE, at(1, 0)));
        assertEquals(2, company.findNearestRental(RentalVehicleType.SCOOTER, at(0, 0)));
        assertEquals(-1, new TaxiCompany("Empty", new ArrayList<>(), new ArrayList<>())
            .findNearestRental(RentalVehicleType.BIKE, at(0, 0)));
    }

    @Test
    void completedRideReleasesThePassengerAndRecordsStatistics() {
        Taxi taxi = addTaxi(1, false);
        User user = addUser(TestFixtures.adult(1, 'M'));
        company.provideService(1, false, RideMode.STANDARD);
        int tripDistance = taxi.getService().calculateDistance();

        TestFixtures.runUntilFree(taxi);

        assertFalse(user.getService());
        assertNull(taxi.getService());
        assertEquals(1, taxi.getStatistics().getServices());
        assertEquals(tripDistance, taxi.getStatistics().getDistance());
        assertEquals((1 + tripDistance) * 2.0, taxi.getStatistics().getBilling(), 1e-9);
        assertTrue(events.stream().anyMatch(e -> e.contains("picks up user(s) 1")));
        assertTrue(events.stream().anyMatch(e -> e.contains("drops off user(s) 1")));
    }

    @Test
    void sharedRideReleasesEveryPassenger() {
        Taxi taxi = addTaxi(1, false);
        User first = addUser(TestFixtures.sharer(1));
        User second = addUser(TestFixtures.sharer(2));
        company.provideService(1, false, RideMode.STANDARD);
        company.provideService(2, false, RideMode.STANDARD);

        TestFixtures.runUntilFree(taxi);

        assertFalse(first.getService());
        assertFalse(second.getService());
        assertTrue(events.stream().anyMatch(e -> e.contains("drops off user(s) 1, 2")));
    }

    @Test
    void rentalRideStartsAtTheVehicle() {
        Bike bike = new Bike(100, at(2, 2), null, company);
        vehicles.add(bike);
        User user = addUser(TestFixtures.adult(1, 'F'));

        assertTrue(company.provideRentalService(1, RentalVehicleType.BIKE));
        assertEquals("(2,2)", bike.getService().getPickupLocation().toString());

        TestFixtures.runUntilFree(bike);
        assertFalse(user.getService());
        assertEquals(1, bike.getStatistics().getServices());
    }
}
