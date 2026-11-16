package com.taxify.company;

import com.taxify.map.ILocation;
import com.taxify.people.IUser;
import com.taxify.simulation.ApplicationLibrary;
import com.taxify.vehicle.IVehicle;
import com.taxify.vehicle.MicroVehicle;
import com.taxify.vehicle.RentalVehicleType;
import com.taxify.vehicle.Vehicle;
import com.taxify.vehicle.VehicleStatus;

import java.util.List;

public final class TaxiCompany implements ITaxiCompany, ISubject {

    public static final int MAX_SHARED_RIDERS = 2;

    private final String name;
    private final List<IUser> users;
    private final List<IVehicle> vehicles;
    private int totalServices;
    private IObserver observer;

    public TaxiCompany(String name, List<IUser> users, List<IVehicle> vehicles) {
        this.name = name;
        this.users = users;
        this.vehicles = vehicles;
        this.totalServices = 0;

        for (IUser user : this.users) {
            user.setCompany(this);
        }

        for (IVehicle vehicle : this.vehicles) {
            vehicle.setCompany(this);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getTotalServices() {
        return this.totalServices;
    }

    // use a free vehicle if there is one, otherwise try to share a ride that's still heading to a pickup
    @Override
    public boolean provideService(int userId, boolean pinkRide, RideMode rideMode) {
        IUser user = findUser(userId);
        if (user == null || user.getService()) {
            return false; // unknown user, or already in a ride
        }

        IVehicle vehicle = findFreeVehicle(pinkRide);
        IService service;

        if (vehicle != null) {
            ILocation origin;
            ILocation destination;
            do {
                origin = ApplicationLibrary.randomLocation();
                destination = ApplicationLibrary.randomLocation(origin);
            } while (ApplicationLibrary.distance(origin, vehicle.getLocation())
                     < ApplicationLibrary.MINIMUM_DISTANCE);

            service = new Service(user, origin, destination, pinkRide, rideMode);
            vehicle.pickService(service);
        } else {
            vehicle = findSharableVehicle(pinkRide, rideMode);
            if (vehicle == null || !everyoneAcceptsSharing(user, vehicle.getService())) {
                return false;
            }
            service = vehicle.getService();
            service.addUser(user);
        }

        user.setService(true);
        this.totalServices++;

        notifyObserver(
            "User " + user.getId() +
            " requests a " + (pinkRide ? "Pink " : "") + rideMode + " service from " + service +
            ", the ride is assigned to " + describe(vehicle) +
            " at location " + vehicle.getLocation() +
            (service.getUsers().size() > 1 ? " (shared ride)" : "")
        );

        return true;
    }

    // user starts at a random spot and takes the closest free bike/scooter
    @Override
    public boolean provideRentalService(int userId, RentalVehicleType vehicleType) {
        IUser user = findUser(userId);
        if (user == null || user.getService()) {
            return false;
        }

        ILocation userLocation = ApplicationLibrary.randomLocation();
        int vehicleIndex = findNearestRental(vehicleType, userLocation);
        if (vehicleIndex < 0) {
            return false;
        }

        IVehicle vehicle = this.vehicles.get(vehicleIndex);
        ILocation origin = vehicle.getLocation();
        ILocation destination = ApplicationLibrary.randomLocation(origin);

        IService service = new Service(user, origin, destination, false, RideMode.STANDARD);
        vehicle.pickService(service);
        user.setService(true);
        this.totalServices++;

        notifyObserver(
            "User " + user.getId() +
            " rents a " + vehicleType + " from " + service +
            ", the rental is assigned to " + describe(vehicle) +
            " at location " + vehicle.getLocation()
        );

        return true;
    }

    @Override
    public void arrivedAtPickupLocation(IVehicle vehicle) {
        IService service = vehicle.getService();
        notifyObserver(String.format("%-8s", vehicle.getClass().getSimpleName()) + vehicle.getId()
            + " picks up user(s) " + userIds(service));
        vehicle.startService();
    }

    // passengers can rate the ride, then they're released
    @Override
    public void arrivedAtDropoffLocation(IVehicle vehicle) {
        IService service = vehicle.getService();

        for (IUser user : service.getUsers()) {
            user.rateService(service);
            user.setService(false);
        }

        notifyObserver(String.format("%-8s", vehicle.getClass().getSimpleName()) + vehicle.getId()
            + " drops off user(s) " + userIds(service));

        vehicle.endService(service.getUsers().size());
    }

    @Override
    public void addObserver(IObserver observer) {
        this.observer = observer;
    }

    @Override
    public void notifyObserver(String message) {
        if (this.observer != null) {
            this.observer.updateObserver(message);
        }
    }

    @Override
    public int findNearestRental(RentalVehicleType vehicleType, ILocation location) {
        int nearestIndex = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < this.vehicles.size(); i++) {
            if (this.vehicles.get(i) instanceof MicroVehicle microVehicle
                    && microVehicle.getRentalType() == vehicleType
                    && microVehicle.isFree()) {
                int distance = ApplicationLibrary.distance(microVehicle.getLocation(), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestIndex = i;
                }
            }
        }
        return nearestIndex;
    }

    private IVehicle findFreeVehicle(boolean pinkRide) {
        for (IVehicle vehicle : this.vehicles) {
            if (vehicle instanceof Vehicle && vehicle.isFree() && driverAllowed(vehicle, pinkRide)) {
                return vehicle;
            }
        }
        return null;
    }

    // vehicle still on the way to a pickup, same ride options, and a free seat
    private IVehicle findSharableVehicle(boolean pinkRide, RideMode rideMode) {
        for (IVehicle vehicle : this.vehicles) {
            if (vehicle instanceof Vehicle taxi && taxi.getStatus() == VehicleStatus.PICKUP) {
                IService service = taxi.getService();
                if (service.getUsers().size() < MAX_SHARED_RIDERS
                        && service.isPinkRide() == pinkRide
                        && service.getRideMode() == rideMode) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    private static boolean driverAllowed(IVehicle vehicle, boolean pinkRide) {
        return !pinkRide || (vehicle.getDriver() != null && vehicle.getDriver().getGender() == 'F');
    }

    private static boolean everyoneAcceptsSharing(IUser newUser, IService service) {
        if (!newUser.acceptSharedRide()) {
            return false;
        }
        for (IUser rider : service.getUsers()) {
            if (!rider.acceptSharedRide()) {
                return false;
            }
        }
        return true;
    }

    private static String describe(IVehicle vehicle) {
        return vehicle.getClass().getSimpleName() + " " + vehicle.getId();
    }

    private static String userIds(IService service) {
        StringBuilder ids = new StringBuilder();
        for (IUser user : service.getUsers()) {
            if (ids.length() > 0) {
                ids.append(", ");
            }
            ids.append(user.getId());
        }
        return ids.toString();
    }

    private IUser findUser(int id) {
        for (IUser user : this.users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
}
