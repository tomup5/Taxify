package com.taxify.vehicle;

import com.taxify.company.IService;
import com.taxify.company.IStatistics;
import com.taxify.company.ITaxiCompany;
import com.taxify.company.Statistics;
import com.taxify.map.ILocation;
import com.taxify.map.IRoute;
import com.taxify.map.Route;
import com.taxify.people.IUser;
import com.taxify.people.Person;
import com.taxify.simulation.ApplicationLibrary;

public class MicroVehicle implements IVehicle {
    private int id;
    private ITaxiCompany company;
    private IService service;
    private MicroVehicleStatus status;
    private ILocation location;
    private ILocation destination;
    private IStatistics statistics;
    private IRoute route;
    private Person driver;

    public MicroVehicle(int id, ILocation location, Person driver) {
        this.id = id;
        this.service = null;
        this.status = MicroVehicleStatus.FREE;
        this.location = location;        
        this.destination = ApplicationLibrary.randomLocation(this.location);
        this.statistics = new Statistics();
        this.route = new Route(this.location, this.destination);
        this.driver = driver;
    }

    @Override
    public void setDriver(Person driver) {
        this.driver = driver;
    };

    @Override
    public Person getDriver() {
        return this.driver;
    };

    public MicroVehicleStatus getStatus() {
        return this.status;
    }

    @Override
    public int getId() {
        return this.id;
    }
 
    @Override
    public ILocation getLocation() {
        return this.location; 
    }

    @Override
    public ILocation getDestination() {
        return this.destination;
    }
    
    @Override
    public IService getService() {
        return this.service;
    }
    
    @Override
    public IStatistics getStatistics() {
        return this.statistics; 
    }

    @Override
    public ITaxiCompany getCompany() {
        return this.company;
    }

    @Override
    public void setCompany(ITaxiCompany company) {
        this.company = company;
    }
    
    @Override
    public void pickService(IService service) {
        // pick a service, set destination to the service pickup location, and status to "booked"
        
        this.service = service;
        this.destination = service.getPickupLocation();
        this.route = new Route(this.location, this.destination);        
        this.status = MicroVehicleStatus.BOOKED;
    }

    @Override
    public void startService() {
        // set destination to the service drop-off location, 
        // and status to "service"
        this.destination = this.service.getDropoffLocation();
        this.route = new Route(this.location, this.destination);
        this.status = MicroVehicleStatus.RIDING;
    }

    @Override
    public void endService(int riders) {
        // update vehicle statistics
        double discountPrice = 0.6;
        if(riders == 1) {
            this.statistics.updateBilling(this.calculateCost());
        } else {
            this.statistics.updateBilling(this.calculateCost() * discountPrice * riders);
        }
        
        this.statistics.updateDistance(this.service.calculateDistance());
        this.statistics.updateServices();
        
        // if the service is rated by the user, update statistics
        
        if (this.service.getStars() != 0) {
            this.statistics.updateStars(this.service.getStars());
            this.statistics.updateReviews();
        }
        
        // set service to null, and status to "free"
        
        this.service = null;
        this.destination = ApplicationLibrary.randomLocation(this.location);
        this.route = new Route(this.location, this.destination);
        this.status = MicroVehicleStatus.FREE;
    }

    @Override
    public void notifyArrivalAtPickupLocation() {
        // notify the company that the vehicle is at the pickup location
        if(this.location.getX() == this.service.getPickupLocation().getX() && 
            this.location.getY() == this.service.getPickupLocation().getY()) {
            this.company.arrivedAtPickupLocation(this);
        }
    }
        
    @Override
    public void notifyArrivalAtDropoffLocation() {
        // notify the company that the vehicle is at the dropoff location
        if(this.location.getX() == this.service.getDropoffLocation().getX() && 
            this.location.getY() == this.service.getDropoffLocation().getY()) {
            this.company.arrivedAtDropoffLocation(this);
        }
     }
        
    @Override
    public boolean isFree() {
        if(this.status == MicroVehicleStatus.FREE) {
            return true;
        }
        return false;
    }   
    
    @Override
    public void move() {
        // check if the route has locations to move to
        if (this.route.hasLocations()) {
            // get the next location from the driving route
            this.location = this.route.getNextLocation();
        }
        
        // if the route has no more locations, the vehicle has arrived at a destination
        if (!this.route.hasLocations()) {
            if (this.service == null) {
                // the vehicle continues its random route
                this.destination = ApplicationLibrary.randomLocation(this.location);
                this.route = new Route(this.location, this.destination);
            }
            else {
                // check if the vehicle has arrived to a pickup or drop off location
                ILocation origin = this.service.getPickupLocation();
                ILocation destination = this.service.getDropoffLocation();

                if (this.location.getX() == origin.getX() && this.location.getY() == origin.getY()) {
                    notifyArrivalAtPickupLocation();
                } else if (this.location.getX() == destination.getX() && this.location.getY() == destination.getY()) {
                    notifyArrivalAtDropoffLocation();
                }        
            }
        }
    }

    @Override
    public double calculateCost() {
        return 1 + this.service.calculateDistance();
    }

    @Override
    public String toString() {
        String users = "";
        if (this.service != null) {
            for (IUser user : this.service.getUsers()) {
                users += (users.isEmpty() ? "" : ", ") + user.getId();
            }
        }

        String result = this.id + " at " + this.location + " with destination: " + this.destination +
                ((this.status == MicroVehicleStatus.FREE) ? " is free with path " + this.route.toString(): ((this.status == MicroVehicleStatus.BOOKED) ?
                " is booked by " + users : " in service "));
        
        return result;
    }

    public RentalVehicleType getRentalType() {
        return null;
    };
}
