package com.taxify.company;

import com.taxify.map.ILocation;
import com.taxify.people.IUser;

import java.util.ArrayList;

public class Service implements IService {
    private ArrayList<IUser> users;
    private ILocation pickup;
    private ILocation dropoff;
    private int stars;
    private boolean isPink;
    private RideMode rideMode;

    public Service(IUser user, ILocation pickup, ILocation dropoff, boolean pinkRide, RideMode rideMode) {
        this.users = new ArrayList<>();
        this.users.add(user);
        this.pickup = pickup;
        this.dropoff = dropoff; 
        this.stars = 0;
        this.isPink = pinkRide;
        this.rideMode = rideMode;
    }
    
    @Override
    public boolean isPinkRide() {
        return this.isPink;
    }

    @Override
    public RideMode getRideMode() {
        return this.rideMode;
    }

    @Override
    public ArrayList<IUser> getUsers() {
        return this.users;
    }
    
    @Override
    public ILocation getPickupLocation() {
        return this.pickup;
    }
    
    @Override
    public ILocation getDropoffLocation() {
        return this.dropoff;
    }
    
    @Override
    public int getStars() {
        return this.stars;
    }
    
    @Override
    public void setStars(int stars) {
        this.stars = stars;
    }
    
    @Override
    public int calculateDistance() {
        return Math.abs(this.pickup.getX() - this.dropoff.getX()) + Math.abs(this.pickup.getY() - this.dropoff.getY());
    }
    
    @Override
    public String toString() {
        return this.getPickupLocation().toString() + " to " + this.getDropoffLocation().toString();
    }

    @Override
    public void addUser(IUser user) {
        this.users.add(user);
    }
}
