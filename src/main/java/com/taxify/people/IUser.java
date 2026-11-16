package com.taxify.people;

import com.taxify.company.IService;
import com.taxify.company.ITaxiCompany;
import com.taxify.company.RideMode;
import com.taxify.vehicle.RentalVehicleType;

public interface IUser {

    public int getId();
    
    public boolean getService();
    
    public void setService(boolean service);
    
    public void setCompany(ITaxiCompany company);
    
    public void requestService(boolean pinkRide, RideMode rideMode);

    public void requestRentalService(RentalVehicleType vehicleType);
    
    public void rateService(IService service);
    
    public String toString();

    public boolean acceptSharedRide();

    public boolean isEligibleForPinkRide();
}
