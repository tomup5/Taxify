package com.taxify.people;

import java.time.LocalDate;

public class Driver extends Person implements IDriver {

    // name, gender and birth date are inherited from Person
    private int yearsOfExperience;
    private double rating;

    public Driver() {
        super("Miss", "Driver", 'F', LocalDate.now());
        this.yearsOfExperience = 0;
        this.rating = 0.0;
    }

    public Driver(String name, char gender, LocalDate birthDate,
                  int yearsOfExperience, double rating) {
        super(name.split(" ")[0], name.split(" ")[1], gender, birthDate);
        this.yearsOfExperience = yearsOfExperience;
        this.rating = rating;
    }

    @Override
    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    @Override
    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    @Override
    public double getRating() {
        return rating;
    }

    @Override
    public void setRating(double rating) {
        this.rating = rating;
    }
}
