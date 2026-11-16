package com.taxify.simulation;

import com.taxify.company.RideMode;
import com.taxify.company.TaxiCompany;
import com.taxify.people.Driver;
import com.taxify.people.IUser;
import com.taxify.people.User;
import com.taxify.vehicle.Bike;
import com.taxify.vehicle.IVehicle;
import com.taxify.vehicle.RentalVehicleType;
import com.taxify.vehicle.Scooter;
import com.taxify.vehicle.Shuttle;
import com.taxify.vehicle.Taxi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Main {

    private static final int REQUEST_TICKS = 60;
    private static final int MAX_DRAIN_TICKS = 500;

    private Main() {
    }

    // usage: ./gradlew run --args="<seed>" (random seed if none given)
    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.nanoTime();
        ApplicationLibrary.setSeed(seed);
        System.out.println("Taxify simulation (seed " + seed + ")\n");

        Driver femaleDriver = new Driver("Ana Ruiz", 'F', LocalDate.of(1990, 7, 19), 5, 4.8);
        Driver maleDriver = new Driver("Ben Cole", 'M', LocalDate.of(1988, 3, 2), 8, 4.6);

        List<IUser> users = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            char gender = (i % 2 == 0) ? 'M' : 'F';
            LocalDate birthDate = LocalDate.of(1990 + (i % 10), 1 + (i % 12), 1 + (i % 28));
            users.add(new User(i, "First" + i, "Last" + i, gender, birthDate));
        }

        List<IVehicle> vehicles = new ArrayList<>();
        TaxiCompany company = new TaxiCompany("Taxify", users, vehicles);

        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                vehicles.add(new Taxi(i, ApplicationLibrary.randomLocation(), company, femaleDriver));
            } else {
                vehicles.add(new Shuttle(i, ApplicationLibrary.randomLocation(), company, maleDriver));
            }
        }
        vehicles.add(new Scooter(100, ApplicationLibrary.randomLocation(), null, company));
        vehicles.add(new Scooter(101, ApplicationLibrary.randomLocation(), null, company));
        vehicles.add(new Bike(102, ApplicationLibrary.randomLocation(), null, company));
        vehicles.add(new Bike(103, ApplicationLibrary.randomLocation(), null, company));

        ApplicationSimulator simulator = new ApplicationSimulator(company, users, vehicles);
        company.addObserver(simulator);

        for (int tick = 0; tick < REQUEST_TICKS; tick++) {
            if (ApplicationLibrary.chance() < 0.5) {
                issueRandomRequest(simulator);
            }
            simulator.update();
        }

        // Stop taking requests and let every ride in progress finish.
        for (int tick = 0; tick < MAX_DRAIN_TICKS && !simulator.allVehiclesFree(); tick++) {
            simulator.update();
        }

        simulator.showStatistics();
        System.out.println("\nTotal services: " + simulator.getTotalServices());
    }

    private static void issueRandomRequest(IApplicationSimulator simulator) {
        double roll = ApplicationLibrary.chance();
        if (roll < 0.15) {
            simulator.requestRentalService(RentalVehicleType.SCOOTER);
        } else if (roll < 0.30) {
            simulator.requestRentalService(RentalVehicleType.BIKE);
        } else {
            boolean pinkRide = ApplicationLibrary.chance() < 0.2;
            RideMode mode = ApplicationLibrary.chance() < 0.5 ? RideMode.STANDARD : RideMode.SILENT;
            simulator.requestService(pinkRide, mode);
        }
    }
}
