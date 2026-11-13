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

/**
 * The ride-hailing company. Matches ride and rental requests to vehicles and
 * reacts to vehicles arriving at pickup and drop-off points. Acts as the
 * subject in the Observer pattern: every event is reported to the observer.
 */
public final class TaxiCompany implements ITaxiCompany, ISubject {

    /** Maximum number of passengers that can share one taxi or shuttle ride. */
    public static final int MAX_SHARED_RIDERS = 2;

    private final String name;
    private final List<IUser> users;
    private final List<IVehicle> vehicles;
    private int totalServices;
    private IObserver observer;

    /**
     * Initializes company and links users and vehicles.
     * @param name company name
     * @param users users list
     * @param vehicles vehicles list
     */
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

    /** @return company name */
    @Override
    public String getName() {
        return this.name;
    }

    /** @return number of services the company has assigned */
    @Override
    public int getTotalServices() {
        return this.totalServices;
    }

    /**
     * Assigns a taxi or shuttle ride to a user. A free vehicle is preferred; if
     * none is available, the user may join a vehicle that is on its way to pick
     * up another passenger, but only if every passenger agrees to share.
     * @param userId user ID
     * @param pinkRide require a female driver
     * @param rideMode Standard or Silent ride
     * @return true if the ride was assigned
     */
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

    /**
     * Rents the nearest free bike or scooter to a user. The user is placed at a
     * random location, walks to the nearest matching vehicle, and rides it to a
     * random destination.
     * @param userId user ID
     * @param vehicleType type of rental: SCOOTER or BIKE
     * @return true if a vehicle was rented
     */
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

    /**
     * Called when a vehicle reaches the pickup location.
     * @param vehicle vehicle
     */
    @Override
    public void arrivedAtPickupLocation(IVehicle vehicle) {
        IService service = vehicle.getService();
        notifyObserver(String.format("%-8s", vehicle.getClass().getSimpleName()) + vehicle.getId()
            + " picks up user(s) " + userIds(service));
        vehicle.startService();
    }

    /**
     * Called when a vehicle reaches the drop-off location. Every passenger may
     * rate the ride and is released, then the vehicle records the trip.
     * @param vehicle vehicle
     */
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

    /**
     * Registers the observer that receives event messages.
     * @param observer observer
     */
    @Override
    public void addObserver(IObserver observer) {
        this.observer = observer;
    }

    /**
     * Sends a message to the observer, if one is registered.
     * @param message message
     */
    @Override
    public void notifyObserver(String message) {
        if (this.observer != null) {
            this.observer.updateObserver(message);
        }
    }

    /**
     * Finds the free rental vehicle of the given type closest to a location.
     * @param vehicleType type of rental: SCOOTER or BIKE
     * @param location where the user is standing
     * @return the index of the nearest free rental vehicle, or -1 if none
     */
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

    /**
     * Finds the first free taxi or shuttle. Pink rides need a female driver.
     * @param pinkRide require a female driver
     * @return a free vehicle, or null if none
     */
    private IVehicle findFreeVehicle(boolean pinkRide) {
        for (IVehicle vehicle : this.vehicles) {
            if (vehicle instanceof Vehicle && vehicle.isFree() && driverAllowed(vehicle, pinkRide)) {
                return vehicle;
            }
        }
        return null;
    }

    /**
     * Finds a taxi or shuttle still on its way to a pickup whose ride matches
     * the requested options and has a free seat.
     * @param pinkRide require a pink ride
     * @param rideMode required ride mode
     * @return a vehicle whose ride can be shared, or null if none
     */
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

    /**
     * Finds a user by ID.
     * @param id user ID
     * @return the user, or null if not found
     */
    private IUser findUser(int id) {
        for (IUser user : this.users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
}
