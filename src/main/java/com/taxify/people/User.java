package com.taxify.people;

import com.taxify.company.IService;
import com.taxify.company.ITaxiCompany;
import com.taxify.company.RideMode;
import com.taxify.simulation.ApplicationLibrary;
import com.taxify.vehicle.RentalVehicleType;

import java.time.LocalDate;
import java.time.Period;

/**
 * Represents a user in the taxi system with personal information and service management capabilities.
 * Implements the IUser interface to provide user-related functionality.
 */
public class User extends Person implements IUser {
    /** The unique identifier of the user */
    private int id;
    /** The taxi company associated with this user */
    private ITaxiCompany company;
    /** Indicates if the user is currently in service */
    private boolean service;
    
    /**
     * Constructs a User with the specified details.
     * @param id the unique identifier
     * @param firstName the first name
     * @param lastName the last name
     * @param gender the gender ('M' or 'F')
     * @param birthDate the birth date
     */
    public User(int id, String firstName, String lastName, char gender, LocalDate birthDate) {
        super(firstName, lastName, gender, birthDate);
        this.id = id;
        this.service = false;
    }
    
    /**
     * Gets the unique identifier of the user.
     * @return the user ID as an integer
     */
    @Override
    public int getId() {
        return this.id;
    }

    /**
     * Checks if the user is currently in service.
     * @return true if the user is in service, false otherwise
     */
    @Override
    public boolean getService() {
        return this.service;
    }
    
    /**
     * Sets the service status of the user.
     * @param service true to set the user in service, false otherwise
     */
    @Override
    public void setService(boolean service) {
        this.service = service;
    }
    
    /**
     * Sets the taxi company associated with this user.
     * @param company the ITaxiCompany to associate with the user
     */
    @Override
    public void setCompany(ITaxiCompany company) {
        this.company = company;
    }
    
    /**
     * Requests a service from the associated taxi company.
     * @param pinkRide requests a ride by, and for, women
     * @param rideMode "Silent" or "Standard" noise levels for the ride
     */
    @Override
    public void requestService(boolean pinkRide, RideMode rideMode) {
        if (pinkRide && !isEligibleForPinkRide()) {
            throw new IllegalArgumentException("Pink rides are only available to women and children.");
        }

        this.company.provideService(this.id, pinkRide, rideMode);
    }

    /**
     * Requests a rental service from the associated taxi company.
     * @param vehicleType the type of rental vehicle requested
     */
    @Override
    public void requestRentalService(RentalVehicleType vehicleType) {
        this.company.provideRentalService(this.id, vehicleType);
    }
    
    /**
     * Rates a completed service.
     * @param service the IService to rate
     */
    @Override
    public void rateService(IService service) {
        // users rate around 50% of the services (1 to 5 stars)
        
        if (ApplicationLibrary.rand() % 2 == 0) {
            service.setStars(ApplicationLibrary.rand(5) + 1);
        }
    }
    
    /**
     * Returns a string representation of the user.
     * @return a string describing the user's details
     */
    @Override
    public String toString() {
        return this.getId() + " " + String.format("%-20s",this.getFirstName() + " " + this.getLastName());
    }

    /**
     * Randomly returns acceptance of a shared ride offer
     * @return True/accept, or false/reject
     */
    @Override
    public boolean acceptSharedRide() {
        return ApplicationLibrary.chance() > 0.5;
    }

    /**
     * Pink rides (female driver) are offered to women and to children under 18.
     * @return true if this user may request a pink ride
     */
    @Override
    public boolean isEligibleForPinkRide() {
        int age = Period.between(this.getBirthDate(), LocalDate.now()).getYears();
        return this.getGender() == 'F' || age < 18;
    }
}
