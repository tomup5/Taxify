package com.taxify.simulation;

import com.taxify.company.IObserver;
import com.taxify.company.ITaxiCompany;
import com.taxify.company.RideMode;
import com.taxify.people.IUser;
import com.taxify.vehicle.IVehicle;
import com.taxify.vehicle.RentalVehicleType;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSimulator implements IApplicationSimulator, IObserver {

    private final ITaxiCompany company;
    private final List<IUser> users;
    private final List<IVehicle> vehicles;

    public ApplicationSimulator(ITaxiCompany company, List<IUser> users, List<IVehicle> vehicles) {
        this.company = company;
        this.users = users;
        this.vehicles = vehicles;
    }

    @Override
    public void show() {
        System.out.println("\n" + this.company.getName() + " status \n");

        for (IVehicle vehicle : this.vehicles) {
            System.out.println(vehicle.toString());
        }
    }

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

    @Override
    public void update() {
        for (IVehicle vehicle : this.vehicles) {
            vehicle.move();
        }
    }

    @Override
    public boolean requestService(boolean pinkRide, RideMode rideMode) {
        IUser user = randomFreeUser(pinkRide);
        if (user == null) {
            return false;
        }
        return this.company.provideService(user.getId(), pinkRide, rideMode);
    }

    @Override
    public boolean requestRentalService(RentalVehicleType vehicleType) {
        IUser user = randomFreeUser(false);
        if (user == null) {
            return false;
        }
        return this.company.provideRentalService(user.getId(), vehicleType);
    }

    @Override
    public boolean allVehiclesFree() {
        for (IVehicle vehicle : this.vehicles) {
            if (!vehicle.isFree()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getTotalServices() {
        return this.company.getTotalServices();
    }

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
