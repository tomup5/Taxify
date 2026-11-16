package com.taxify.vehicle;

import com.taxify.company.IService;
import com.taxify.company.IStatistics;
import com.taxify.company.ITaxiCompany;
import com.taxify.map.ILocation;
import com.taxify.people.Person;

public interface IVehicle extends IMovable {

    public int getId();

    public ILocation getLocation();

    public ILocation getDestination();

    public IService getService();

    public IStatistics getStatistics();

    public void setDriver(Person driver);

    public Person getDriver();

    public ITaxiCompany getCompany();

    public void setCompany(ITaxiCompany company);

    public void pickService(IService service);

    public void startService();

    public void endService(int riders);

    public void notifyArrivalAtPickupLocation();

    public void notifyArrivalAtDropoffLocation();

    public boolean isFree();

    public double calculateCost();

    @Override
    public String toString();
    
}
