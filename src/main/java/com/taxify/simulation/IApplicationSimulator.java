package com.taxify.simulation;

import com.taxify.company.RideMode;
import com.taxify.vehicle.RentalVehicleType;

public interface IApplicationSimulator {
    public void show();

    public void showStatistics();

    public void update();

    public boolean requestService(boolean pinkRide, RideMode rideMode);

    public boolean requestRentalService(RentalVehicleType vehicleType);

    public boolean allVehiclesFree();

    public int getTotalServices();
}
