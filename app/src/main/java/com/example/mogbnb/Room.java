package com.example.mogbnb;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;


public class Room {
    private String roomName;
    private int noOfPersons;
    private final int availableDays;
    private String area;
    private double stars;
    private int noOfReviews;
    private float pricePerDay;
    private String roomImage; //Path to url of the image
    private boolean[] bookingTable;
    private static LocalDate currentDate;

    public Room(String roomName, int noOfPersons, int availableDays, String area, double stars, int noOfReviews, String roomImage, float pricePerDay) {
        this.roomName = roomName;
        this.noOfPersons = noOfPersons;
        this.availableDays = availableDays;
        this.area = area;
        this.stars = stars;
        this.noOfReviews = noOfReviews;
        this.roomImage = roomImage;
        this.pricePerDay = pricePerDay;
        bookingTable = new boolean[availableDays];
        //initiate all days as not booked in the booking table
        for(int i = 0; i<availableDays; i++){
            bookingTable[i] = false;
        }
        if(currentDate == null){
            currentDate = LocalDate.now();
        }
    }


    /**
     * Method used to determine if a filter is accepted by a room
     * @param filter the filter given to a room
     * @return true if the filter matches the room, false if not.
     */
    public boolean filterAccepted(Filter filter){
        LocalDate checkOutDate = filter.getCheckOut();
        LocalDate checkInDate = filter.getCheckIn();

        long bookingDaysTotal = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        int indexOfCheckInDate = (int) ChronoUnit.DAYS.between(Room.currentDate, checkInDate);

        //check if filtered days are not booked
        for (int i = 0; i<bookingDaysTotal; i++){
            if(bookingTable[indexOfCheckInDate + i]){
                return false;
            }
        }
        //filtered days are all available from this point onward

        return (filter.getArea() == null || filter.getArea().equalsIgnoreCase(this.area))&&(filter.getPrice()==0 || filter.getPrice()>=this.pricePerDay)&&(filter.getStars()<=this.stars)
                &&(filter.getNoOfPersons()==0 || filter.getNoOfPersons()==this.noOfPersons)
                &&(bookingDaysTotal<= availableDays)&&((int) ChronoUnit.DAYS.between(Room.currentDate, checkOutDate)<=availableDays);
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

    @Override
    public String toString() {
        return "Room{" +
                "roomName='" + roomName + '\'' +
                ", noOfPersons=" + noOfPersons +
                ", availableDays=" + availableDays +
                ", area='" + area + '\'' +
                ", stars=" + stars +
                ", noOfReviews=" + noOfReviews +
                ", pricePerDay=" + pricePerDay +
                ", roomImage='" + roomImage + '\'' +
                '}';
    }

}
