package com.taxify.simulation;

import com.taxify.company.RideMode;
import com.taxify.vehicle.RentalVehicleType;

/** Drives the simulation: moves vehicles, issues requests and reports results. */
public interface IApplicationSimulator {
    /**
     * Displays the current status of the taxi application.
     */
    public void show();

    /**
     * Displays aggregated statistics for all vehicles.
     */
    public void showStatistics();

    /**
     * Updates the application state during each simulation iteration.
     */
    public void update();

    /**
     * Requests a taxi or shuttle ride for a random user who is not already riding.
     * @param pinkRide if a ride is by women, for women/children
     * @param rideMode if a ride is Silent or Standard
     * @return true if a ride was assigned
     */
    public boolean requestService(boolean pinkRide, RideMode rideMode);

    /**
     * Requests a bike or scooter rental for a random user who is not already riding.
     * @param vehicleType SCOOTER or BIKE
     * @return true if a vehicle was rented
     */
    public boolean requestRentalService(RentalVehicleType vehicleType);

    /**
     * Whether every vehicle is free (no ride in progress).
     * @return true if no vehicle is serving a request
     */
    public boolean allVehiclesFree();

    /**
     * Gets the total number of services assigned.
     * @return the total service count
     */
    public int getTotalServices();
}
