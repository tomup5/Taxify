package com.taxify;

import com.taxify.company.IService;
import com.taxify.company.RideMode;
import com.taxify.company.Service;
import com.taxify.map.ILocation;
import com.taxify.map.Location;
import com.taxify.people.Driver;
import com.taxify.people.User;
import com.taxify.vehicle.IVehicle;

import java.time.LocalDate;

/** Shared builders for tests. */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Driver femaleDriver() {
        return new Driver("Ana Ruiz", 'F', LocalDate.of(1990, 1, 1), 5, 4.8);
    }

    public static Driver maleDriver() {
        return new Driver("Ben Cole", 'M', LocalDate.of(1990, 1, 1), 5, 4.8);
    }

    public static User adult(int id, char gender) {
        return new User(id, "First" + id, "Last" + id, gender, LocalDate.now().minusYears(30));
    }

    public static User child(int id, char gender) {
        return new User(id, "First" + id, "Last" + id, gender, LocalDate.now().minusYears(10));
    }

    /** A user who always agrees to share a ride. */
    public static User sharer(int id) {
        return new User(id, "First" + id, "Last" + id, 'M', LocalDate.now().minusYears(30)) {
            @Override
            public boolean acceptSharedRide() {
                return true;
            }
        };
    }

    /** A user who never agrees to share a ride. */
    public static User loner(int id) {
        return new User(id, "First" + id, "Last" + id, 'M', LocalDate.now().minusYears(30)) {
            @Override
            public boolean acceptSharedRide() {
                return false;
            }
        };
    }

    public static ILocation at(int x, int y) {
        return new Location(x, y);
    }

    /** A service of the given grid distance, starting at (0,0) and heading along the x axis. */
    public static IService serviceOfDistance(User user, int distance) {
        return new Service(user, at(0, 0), at(distance, 0), false, RideMode.STANDARD);
    }

    /** Moves the vehicle until it is free again, failing if that takes unreasonably long. */
    public static void runUntilFree(IVehicle vehicle) {
        for (int i = 0; i < 1000 && !vehicle.isFree(); i++) {
            vehicle.move();
        }
        if (!vehicle.isFree()) {
            throw new AssertionError("vehicle " + vehicle.getId() + " never finished its ride");
        }
    }
}
