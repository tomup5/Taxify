package com.taxify.vehicle;

import com.taxify.company.ITaxiCompany;
import com.taxify.map.ILocation;
import com.taxify.people.IDriver;
import com.taxify.people.Person;

public final class Shuttle extends Vehicle {

    public Shuttle(int id, ILocation location, ITaxiCompany company, IDriver driver) {
        super(id, location, (Person) driver);
        this.setCompany(company);
    }

    
    @Override
    public double calculateCost() {
        return super.calculateCost() * 1.5;
    }
}
