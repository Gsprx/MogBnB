package com.example.mogbnb;

import androidx.annotation.NonNull;

import java.util.Date;

public class Filter {
    private final String area;
    private final Date checkIn;
    private final Date checkOut;
    private final int noOfPersons;
    private final double price;
    private final double stars;

    public Filter(String area, Date checkIn, Date checkOut, int noOfPersons, double price, double stars) {
        this.area = area;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.noOfPersons = noOfPersons;
        this.price = price;
        this.stars = stars;
    }

    public String getArea() {
        return area;
    }

    public Date getCheckIn() {
        return checkIn;
    }

    public Date getCheckOut() {
        return checkOut;
    }

    public int getNoOfPersons() {
        return noOfPersons;
    }

    public double getPrice() {
        return price;
    }

    public double getStars() {
        return stars;
    }

    @NonNull
    @Override
    public String toString() {
        String printArea = area == null ? "No Filter" : area;
        String printCheckIn = checkIn == null ? "No Filter" : checkIn.toString();
        String printCheckOut = checkOut == null ? "No Filter" : checkOut.toString();
        String printNoOfPersons = noOfPersons == -1 ? "No Filter" : Integer.toString(noOfPersons);
        String printPrice = price == -1 ? "No Filter" : Double.toString(price);
        String printRating = stars == -1 ? "No Filter" : Double.toString(stars);
        return "New Filter\n------------------\nArea: " + printArea + "\nCheck In: " + printCheckIn
                + "\nCheck Out: " + printCheckOut + "\nPeople: " + printNoOfPersons + "\nPrice: " +
                printPrice + "\nRating: " + printRating;
    }
}
