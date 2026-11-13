package com.taxify.people;

import com.taxify.company.IService;
import com.taxify.company.ITaxiCompany;
import com.taxify.company.RideMode;
import com.taxify.vehicle.RentalVehicleType;

/**
 * Interface representing a user.
 * Provides methods to access and modify user information, manage services, and interact with the taxi company.
 */
public interface IUser {

    /**
     * Gets the unique identifier of the user.
     * @return the user ID as an integer
     */
    public int getId();
    
    /**
     * Checks if the user is currently in service.
     * @return true if the user is in service, false otherwise
     */
    public boolean getService();
    
    /**
     * Sets the service status of the user.
     * @param service true to set the user in service, false otherwise
     */
    public void setService(boolean service);
    
    /**
     * Sets the taxi company associated with this user.
     * @param company the ITaxiCompany to associate with the user
     */
    public void setCompany(ITaxiCompany company);
    
    /**
     * Requests a service from the associated taxi company.
     */
    public void requestService(boolean pinkRide, RideMode rideMode);

    /**
     * Requests a rental service from the associated taxi company.
     */    
    public void requestRentalService(RentalVehicleType vehicleType);
    
    /**
     * Rates a completed service.
     * @param service the IService to rate
     */
    public void rateService(IService service);
    
    /**
     * Returns a string representation of the user.
     * @return a string describing the user's details
     */
    public String toString();

    /**
     * Randomly returns acceptance of a shared ride offer
     * @return True/accept, or false/reject
     */
    public boolean acceptSharedRide();

    /**
     * Whether the user may request a pink ride (women and children under 18).
     * @return true if eligible
     */
    public boolean isEligibleForPinkRide();
}
