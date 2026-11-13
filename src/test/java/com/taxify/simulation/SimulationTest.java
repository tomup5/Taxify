package com.taxify.simulation;

import com.taxify.TestFixtures;
import com.taxify.company.RideMode;
import com.taxify.company.TaxiCompany;
import com.taxify.people.IUser;
import com.taxify.people.User;
import com.taxify.vehicle.Bike;
import com.taxify.vehicle.IVehicle;
import com.taxify.vehicle.RentalVehicleType;
import com.taxify.vehicle.Scooter;
import com.taxify.vehicle.Shuttle;
import com.taxify.vehicle.Taxi;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Runs whole simulations with many seeds and checks that system-wide rules
 * hold at every step.
 */
class SimulationTest {

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987})
    void noUserIsEverInTwoRidesAndEveryRideFinishes(long seed) {
        ApplicationLibrary.setSeed(seed);

        List<IUser> users = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            users.add(new User(i, "First" + i, "Last" + i, i % 2 == 0 ? 'M' : 'F', LocalDate.of(1995, 1, 1)));
        }
        List<IVehicle> vehicles = new ArrayList<>();
        TaxiCompany company = new TaxiCompany("Sim", users, vehicles);
        for (int i = 0; i < 4; i++) {
            vehicles.add(i < 2
                ? new Taxi(i, ApplicationLibrary.randomLocation(), company, TestFixtures.femaleDriver())
                : new Shuttle(i, ApplicationLibrary.randomLocation(), company, TestFixtures.maleDriver()));
        }
        vehicles.add(new Scooter(100, ApplicationLibrary.randomLocation(), null, company));
        vehicles.add(new Bike(101, ApplicationLibrary.randomLocation(), null, company));

        ApplicationSimulator simulator = new ApplicationSimulator(company, users, vehicles);
        company.addObserver(message -> { });

        for (int tick = 0; tick < 80; tick++) {
            double roll = ApplicationLibrary.chance();
            if (roll < 0.15) {
                simulator.requestRentalService(RentalVehicleType.SCOOTER);
            } else if (roll < 0.30) {
                simulator.requestRentalService(RentalVehicleType.BIKE);
            } else if (roll < 0.90) {
                simulator.requestService(roll < 0.4, roll < 0.65 ? RideMode.STANDARD : RideMode.SILENT);
            }
            simulator.update();
            assertConsistent(users, vehicles);
        }

        for (int tick = 0; tick < 500 && !simulator.allVehiclesFree(); tick++) {
            simulator.update();
            assertConsistent(users, vehicles);
        }

        assertTrue(simulator.allVehiclesFree(), "all rides should finish");
        for (IUser user : users) {
            assertFalse(user.getService(), "user " + user.getId() + " still marked as riding");
        }
        int completed = vehicles.stream().mapToInt(v -> v.getStatistics().getServices()).sum();
        assertTrue(completed > 0, "simulation should complete some rides");
    }

    /** Every riding user is in exactly one active service, and nobody else is. */
    private static void assertConsistent(List<IUser> users, List<IVehicle> vehicles) {
        Set<Integer> seen = new HashSet<>();
        for (IVehicle vehicle : vehicles) {
            if (vehicle.getService() == null) {
                continue;
            }
            for (IUser user : vehicle.getService().getUsers()) {
                if (!seen.add(user.getId())) {
                    fail("user " + user.getId() + " is in two rides at once");
                }
            }
        }
        for (IUser user : users) {
            assertEquals(user.getService(), seen.contains(user.getId()),
                "user " + user.getId() + " riding flag disagrees with vehicle assignments");
        }
    }
}
