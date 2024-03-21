package com.example.mogbnb;

public class Room {
    private String roomName;
    private int noOfPersons;
    private final int availableDays;
    private String area;
    private float stars;
    private int noOfReviews;

    //Path to url of the image
    private String roomImage;

    public Room(String roomName, int noOfPersons, int availableDays, String area, float stars, int noOfReviews, String roomImage) {
        this.roomName = roomName;
        this.noOfPersons = noOfPersons;
        this.availableDays = availableDays;
        this.area = area;
        this.stars = stars;
        this.noOfReviews = noOfReviews;
        this.roomImage = roomImage;
    }

    /**
     * Used to add a new review to a specific room
     * @param stars the integer number of stars for the review
     */
    public void addReview(int stars){
        this.noOfReviews++;
        this.stars = (this.stars + stars)/2;
    }

    //
    // setter functions
    //

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

    public float getStars() {
        return stars;
    }

    public int getNoOfReviews() {
        return noOfReviews;
    }

    public String getRoomImage() {
        return roomImage;
    }
}
