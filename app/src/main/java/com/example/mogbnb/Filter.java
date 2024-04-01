package com.example.mogbnb;

import androidx.annotation.NonNull;

import com.example.misc.JsonConverter;

import java.time.LocalDate;

import java.util.Scanner;

public class Filter {


    private final String area;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int noOfPersons;
    private final double price;
    private final double stars;

    public Filter(String area, LocalDate checkIn, LocalDate checkOut, int noOfPersons, double price, double stars) {
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

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
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

