package com.taxify.company;

import com.taxify.map.ILocation;
import com.taxify.vehicle.IVehicle;
import com.taxify.vehicle.RentalVehicleType;

public interface ITaxiCompany {

    String getName();

    int getTotalServices();

    boolean provideService(int user, boolean pinkRide, RideMode rideMode);

    boolean provideRentalService(int user, RentalVehicleType vehicleType);

    void arrivedAtPickupLocation(IVehicle vehicle);

    void arrivedAtDropoffLocation(IVehicle vehicle);

    void addObserver(IObserver observer);

    void notifyObserver(String message);

    int findNearestRental(RentalVehicleType vehicleType, ILocation location);
}
