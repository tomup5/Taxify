package com.taxify.company;

import com.taxify.map.ILocation;
import com.taxify.people.IUser;

import java.util.ArrayList;

public interface IService {

    public ArrayList<IUser> getUsers();
    
    public ILocation getPickupLocation();
    
    public ILocation getDropoffLocation();
    
    public int getStars();
    
    public void setStars(int stars);
    
    public int calculateDistance();
    
    public String toString();

    public void addUser(IUser user);

    public boolean isPinkRide();

    public RideMode getRideMode();
}
