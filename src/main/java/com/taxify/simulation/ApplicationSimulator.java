package com.taxify.simulation;

import com.taxify.company.IObserver;
import com.taxify.company.ITaxiCompany;
import com.taxify.company.RideMode;
import com.taxify.people.IUser;
import com.taxify.vehicle.IVehicle;
import com.taxify.vehicle.RentalVehicleType;

import java.util.ArrayList;
import java.util.List;

/**
 * ApplicationSimulator acts as the main orchestrator of the taxi company simulation.
 * It observes the company, updates vehicle movement, triggers service requests,
 * and displays system status and statistics.
 */
public class ApplicationSimulator implements IApplicationSimulator, IObserver {

    private final ITaxiCompany company;
    private final List<IUser> users;
    private final List<IVehicle> vehicles;

    /**
     * Creates the simulator for a company and its users and fleet.
     * @param company the taxi company
     * @param users all users
     * @param vehicles all vehicles (taxis, shuttles, bikes and scooters)
     */
    public ApplicationSimulator(ITaxiCompany company, List<IUser> users, List<IVehicle> vehicles) {
        this.company = company;
        this.users = users;
        this.vehicles = vehicles;
    }

    /**
     * Displays the current status of all vehicles in the simulation.
     */
    @Override
    public void show() {
        System.out.println("\n" + this.company.getName() + " status \n");

        for (IVehicle vehicle : this.vehicles) {
            System.out.println(vehicle.toString());
        }
    }

    /**
     * Prints aggregated statistics for each vehicle:
     * services completed, distance traveled, billing, reviews and average stars.
     */
    @Override
    public void showStatistics() {
        StringBuilder s = new StringBuilder("\n" + this.company.getName() + " statistics\n");

        for (IVehicle vehicle : this.vehicles) {
            s.append('\n')
             .append(String.format("%-8s", vehicle.getClass().getSimpleName()))
             .append(String.format("%3s", vehicle.getId())).append(' ')
             .append(String.format("%2d", vehicle.getStatistics().getServices())).append(" services ")
             .append(String.format("%3d", vehicle.getStatistics().getDistance())).append(" km. ")
             .append(String.format("%6.2f", vehicle.getStatistics().getBilling())).append(" eur. ")
             .append(String.format("%2d", vehicle.getStatistics().getReviews())).append(" reviews ")
             .append(String.format("%.2f", vehicle.getStatistics().getStars())).append(" stars");
        }

        System.out.println(s);
    }

    /**
     * Moves every vehicle one step along its route.
     */
    @Override
    public void update() {
        for (IVehicle vehicle : this.vehicles) {
            vehicle.move();
        }
    }

    /**
     * Requests a ride for a random user who is free. For pink rides, only
     * eligible users (women and children) are considered.
     * @param pinkRide if a ride is by women, for women/children
     * @param rideMode if a ride is Silent or Standard
     * @return true if a ride was assigned
     */
    @Override
    public boolean requestService(boolean pinkRide, RideMode rideMode) {
        IUser user = randomFreeUser(pinkRide);
        if (user == null) {
            return false;
        }
        return this.company.provideService(user.getId(), pinkRide, rideMode);
    }

    /**
     * Requests a rental for a random user who is free.
     * @param vehicleType SCOOTER or BIKE
     * @return true if a vehicle was rented
     */
    @Override
    public boolean requestRentalService(RentalVehicleType vehicleType) {
        IUser user = randomFreeUser(false);
        if (user == null) {
            return false;
        }
        return this.company.provideRentalService(user.getId(), vehicleType);
    }

    /**
     * Whether every vehicle is free.
     * @return true if no vehicle is serving a request
     */
    @Override
    public boolean allVehiclesFree() {
        for (IVehicle vehicle : this.vehicles) {
            if (!vehicle.isFree()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the total number of services assigned by the company.
     */
    @Override
    public int getTotalServices() {
        return this.company.getTotalServices();
    }

    /**
     * Observer callback: prints any message sent by the observed subject.
     */
    @Override
    public void updateObserver(String message) {
        System.out.println(message);
    }

    private IUser randomFreeUser(boolean pinkRide) {
        List<IUser> candidates = new ArrayList<>();
        for (IUser user : this.users) {
            if (!user.getService() && (!pinkRide || user.isEligibleForPinkRide())) {
                candidates.add(user);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ApplicationLibrary.rand(candidates.size()));
    }
}
