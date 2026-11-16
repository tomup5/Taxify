package com.taxify.vehicle;

import com.taxify.company.ITaxiCompany;
import com.taxify.map.ILocation;
import com.taxify.people.Person;

public final class Bike extends MicroVehicle {
    private RentalVehicleType rentalType = RentalVehicleType.BIKE;

    public Bike(int id, ILocation location, Person driver, ITaxiCompany company) {
        super(id, location, driver);
        this.setCompany(company);
    }

    @Override
    public double calculateCost() {
        return (super.calculateCost() - 1) * .25 + 1; // Bikes have a base cost of 1, and then charge 25% of the distance cost above that   
    }

    @Override
    public RentalVehicleType getRentalType() {
        return this.rentalType;
    }
}
