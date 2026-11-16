package com.taxify.people;

import com.taxify.company.IService;
import com.taxify.company.ITaxiCompany;
import com.taxify.company.RideMode;
import com.taxify.simulation.ApplicationLibrary;
import com.taxify.vehicle.RentalVehicleType;

import java.time.LocalDate;
import java.time.Period;

public class User extends Person implements IUser {
    private int id;
    private ITaxiCompany company;
    private boolean service;
    
    public User(int id, String firstName, String lastName, char gender, LocalDate birthDate) {
        super(firstName, lastName, gender, birthDate);
        this.id = id;
        this.service = false;
    }
    
    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean getService() {
        return this.service;
    }
    
    @Override
    public void setService(boolean service) {
        this.service = service;
    }
    
    @Override
    public void setCompany(ITaxiCompany company) {
        this.company = company;
    }
    
    @Override
    public void requestService(boolean pinkRide, RideMode rideMode) {
        if (pinkRide && !isEligibleForPinkRide()) {
            throw new IllegalArgumentException("Pink rides are only available to women and children.");
        }

        this.company.provideService(this.id, pinkRide, rideMode);
    }

    @Override
    public void requestRentalService(RentalVehicleType vehicleType) {
        this.company.provideRentalService(this.id, vehicleType);
    }
    
    @Override
    public void rateService(IService service) {
        // users rate around 50% of the services (1 to 5 stars)
        
        if (ApplicationLibrary.rand() % 2 == 0) {
            service.setStars(ApplicationLibrary.rand(5) + 1);
        }
    }
    
    @Override
    public String toString() {
        return this.getId() + " " + String.format("%-20s",this.getFirstName() + " " + this.getLastName());
    }

    @Override
    public boolean acceptSharedRide() {
        return ApplicationLibrary.chance() > 0.5;
    }

    @Override
    public boolean isEligibleForPinkRide() {
        int age = Period.between(this.getBirthDate(), LocalDate.now()).getYears();
        return this.getGender() == 'F' || age < 18;
    }
}
