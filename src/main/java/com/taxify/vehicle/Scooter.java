package com.taxify.vehicle;

import com.taxify.company.ITaxiCompany;
import com.taxify.map.ILocation;
import com.taxify.people.Person;

public final class Scooter extends MicroVehicle {
    private double battery = 100;  // Battery level from 0 to 100 (percentage), starts fully charged
    private RentalVehicleType rentalType = RentalVehicleType.SCOOTER;

    public Scooter(int id, ILocation location, Person driver, ITaxiCompany company) {
        super(id, location, driver);
        this.setCompany(company);
    }

    public double getBattery() {
        return battery;
    }

    @Override
    public double calculateCost() {
        return (super.calculateCost() - 1) * .15 + 1; // Scooters have a base cost of 1, and then charge 15% of the distance cost above that
    }

    @Override
    public void startService() {
        // set destination to the service drop-off location, 
        // and status to "service"
        if(this.battery <= 25) {
            this.getCompany().notifyObserver("Scooter " + this.getId() + " has insufficient battery to start service. Current battery: " + this.battery + "%");
            this.chargeBattery();
        }
        super.startService();
    }

    @Override
    public void endService(int riders) {
        this.battery -= 25;
        this.getCompany().notifyObserver("Scooter " + this.getId() + " current battery: " + this.battery + "%");
        super.endService(riders);
    }

    public void chargeBattery() {
        this.getCompany().notifyObserver("Charging scooter " + this.getId() + " from " + this.battery + "% to 100%.");
        this.battery = 100;
    }

    @Override
    public RentalVehicleType getRentalType() {
        return this.rentalType;
    }
}
