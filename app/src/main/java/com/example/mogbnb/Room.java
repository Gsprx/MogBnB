package com.example.mogbnb;

public class Room {
    private String roomName;
    private int noOfPersons;
    private final int availableDays;
    private String area;
    private double stars;
    private int noOfReviews;
    private float pricePerDay;

    //Path to url of the image
    private String roomImage;

    public Room(String roomName, int noOfPersons, int availableDays, String area, double stars, int noOfReviews, String roomImage, float pricePerDay) {
        this.roomName = roomName;
        this.noOfPersons = noOfPersons;
        this.availableDays = availableDays;
        this.area = area;
        this.stars = stars;
        this.noOfReviews = noOfReviews;
        this.roomImage = roomImage;
        this.pricePerDay = pricePerDay;
    }

    /**
     * Used to add a new review to a specific room
     * @param stars the double number of stars for the review
     */
    public void addReview(double stars){
        this.noOfReviews++;
        this.stars = (this.stars + stars)/2;
    }

    //
    // setter functions
    //


    public void setPricePerDay(float pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setNoOfPersons(int noOfPersons) {
        this.noOfPersons = noOfPersons;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setNoOfReviews(int noOfReviews) {
        this.noOfReviews = noOfReviews;
    }

    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
    }

    //
    // getter functions
    //


    public float getPricePerDay() {
        return pricePerDay;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getNoOfPersons() {
        return noOfPersons;
    }

    public int getAvailableDays() {
        return availableDays;
    }

    public String getArea() {
        return area;
    }

    public double getStars() {
        return stars;
    }

    public int getNoOfReviews() {
        return noOfReviews;
    }

    public String getRoomImage() {
        return roomImage;
    }
}
