package com.example.mogbnb;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.Objects;

public class Room implements Serializable {
    private String roomName;
    private int noOfPersons;
    private final int availableDays;
    private String area;
    private double stars;
    private int noOfReviews;
    private double pricePerDay;
    private String roomImage; //Path to url of the image
    private final int[] bookingTable; // 0 - No booking for the day, else booked by the user with id equal to the int.
    private static LocalDate currentDate;

    public Room(String roomName, int noOfPersons, int availableDays, String area, double stars, int noOfReviews, String roomImage, double pricePerDay) {
        this.roomName = roomName;
        this.noOfPersons = noOfPersons;
        this.availableDays = availableDays;
        this.area = area;
        this.stars = stars;
        this.noOfReviews = noOfReviews;
        this.roomImage = roomImage;
        this.pricePerDay = pricePerDay;
        bookingTable = new int[availableDays];
        //initiate all days as not booked in the booking table
        for(int i = 0; i<availableDays; i++){
            bookingTable[i] = 0;
        }
        if (currentDate == null) {
            currentDate = LocalDate.now();
        }
    }

    /**
     * Method used book a room for the duration of two given dates.
     * @param start Start of booking
     * @param end End of booking
     * @param userID ID of the user attempting to book the room
     * @return True if the room can be booked for the duration of the two given dates, else false.
     * @throws RuntimeException
     */
    public boolean bookRoom(LocalDate start, LocalDate end, int userID) throws RuntimeException{
        int bookingDaysTotal = (int)ChronoUnit.DAYS.between(start, end);

        //error handling for invalid dates
        LocalDate finalAvailableDate = currentDate.plusDays(availableDays-1);
        //check if given dates are out of bounds for the booking table
        if(bookingDaysTotal>availableDays){
            throw new RuntimeException("Error! The requested dates exceed the available days for this room.");
        } else if (start.isAfter(finalAvailableDate) || end.isAfter(finalAvailableDate)) {
            throw new RuntimeException("Error! The requested dates are outside of the available days for this room");
        }

        int indexOfCheckInDate = (int) ChronoUnit.DAYS.between(Room.currentDate, start);

        //lock the booking table
        synchronized (this.bookingTable){
            //check if the room has the dates available
            //case where the room cannot be booked because of unavailable dates in the duration given,
            //caused by someone else booking a day in the duration given, before the current user manages to themselves
            //no changes are made to the booking table of this room
            if (!checkAvailability(indexOfCheckInDate,indexOfCheckInDate + bookingDaysTotal)){
                return false;
            }
            //case where the room can be booked for all the days given, uses the user id as the value of the day
            for(int i = indexOfCheckInDate; i<=indexOfCheckInDate+bookingDaysTotal; i++){
                bookingTable[i] = userID;
            }
            //unlock the booking table
            return true;
        }
    }


    private boolean checkAvailability(int indexForBooking, int end){
        for (int i = indexForBooking; i<=end; i++){
            if (this.bookingTable[i] != 0){
                return false;
            }
        }
        return true;
    }


    /**
     * Method used to determine if a filter is accepted by a room
     * @param filter the filter given to a room
     * @return true if the filter matches the room, false if not.
     */
    public boolean filterAccepted(Filter filter){
        LocalDate checkOutDate = filter.getCheckOut();
        LocalDate checkInDate = filter.getCheckIn();

        long bookingDaysTotal;

        // if checkIn and checkOut not null calculate normally
        if (checkInDate != null && checkOutDate != null) {

            //check if check in/out dates are within the booking table
            LocalDate finalDate = currentDate.plusDays(availableDays - 1);
            if(checkInDate.isAfter(finalDate)){
                return false;
            }
            else if(checkOutDate.isAfter(finalDate)){
                return false;
            }


            int indexOfCheckInDate;
            bookingDaysTotal = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            indexOfCheckInDate = (int) ChronoUnit.DAYS.between(Room.currentDate, checkInDate);

            // if the booking period requested is bigger than the availability of the room return false
            if ((int) ChronoUnit.DAYS.between(Room.currentDate, checkOutDate)>availableDays) {
                return false;
            }

            //check if filtered days are not booked
            for (int i = 0; i <=bookingDaysTotal; i++) {
                if (bookingTable[indexOfCheckInDate + i] != 0) {
                    return false;
                }
            }
        } else {
            // else set bookingDays to 0 (so we avoid for loop below)
            bookingDaysTotal = 0;
        }


        //filtered days are all available from this point onward

        return ((filter.getArea() == null || filter.getArea().equalsIgnoreCase(this.area))&&(filter.getPrice()==-1 || filter.getPrice()>=this.pricePerDay)&&(filter.getStars()==-1 || filter.getStars()<=this.stars)
                &&(filter.getNoOfPersons()==-1 || filter.getNoOfPersons()<=this.noOfPersons)
                &&(bookingDaysTotal<= availableDays));
    }

    /**
     * Used to add a new review to a specific room.
     * @param stars the double number of stars for the review
     */
    public void addReview(double stars) {
        this.stars = (noOfReviews > 0) ? (noOfReviews*this.stars + stars)/(noOfReviews + 1) : stars;
        this.noOfReviews++;
    }


    /**
     * Used to obtain total days booked for a room.
     * @return the integer value of days booked for the room.
     */
    public int totalDaysBooked(){
        int count = 0;

        for (int day : bookingTable){
            if (day != 0){
                count++;
            }
        }
        return count;
    }

    /**
     * Used to obtain total days booked for a room.
     * @param start date
     * @param end date
     * @return total amount of days booked for the given duration
     */
    public int totalDaysBooked(LocalDate start, LocalDate end){

        //check if start/end dates are within the booking table
        LocalDate finalDate = currentDate.plusDays(availableDays - 1);
        if(start.isAfter(finalDate)){
            return 0;
        }
        else if(end.isAfter(finalDate)){
            end = finalDate;
        }

        int count = 0;
        int indexOfCheckInDate = (int) ChronoUnit.DAYS.between(Room.currentDate, start);
        int bookingDaysTotal = (int) ChronoUnit.DAYS.between(start, end);


        for (int i = indexOfCheckInDate; i<=indexOfCheckInDate + bookingDaysTotal; i++){
            if (bookingTable[i]!= 0){
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return Objects.equals(roomName, room.roomName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomName);
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

    public static void setCurrentDate() {
        if (currentDate == null)
            currentDate = LocalDate.now();
    }
    //
    // getter functions
    //

    public double getPricePerDay() {
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

    public LocalDate getCurrentDate() {return currentDate;}
    public int[] getBookingTable(){return bookingTable;}

    @NonNull
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

 /*
@@@@@@@@%%%%%%@@@@#%@@@@@%@#@@@@@@#@%@@@%%%+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@%%%%%%@##%*%*@#%@@@@%@@@@@@@%%@%@@@**#-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@%%%%%@@@@%*#@%=##+#@@@%@@#@@@@#@@%@@**=#+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@%%@@@@@@@%*+@@%@@%#*@@%#%####*%##%%#+##**@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@%%@@@@@@@@@%@*#@%*@%###%@*%@#@*#%@%@#=+%**@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%##%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@=%@#*#@##*@%@%%%@*@%@%=*#%*%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*---=====-+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@#+@*+%@#++@@%%+#@@@#+@**-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%-==+***#%%#+-*@@@@@@@#*%+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@#*#*#*##+@#%+@+@@+%*%=+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%-=+*#%@@@@@@%#=*@@@@@%%+#*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@+#*%%*%%@*+%*@=##@#==@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-++#%%@@@@@@@@@#=@@@@@@#*+*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@++@**=@%+@*+#+%++#=*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@=*#%%@@@@@@@@@@@*@@@+=####=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@%**@+*###+#*@@++*%-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+##%@@@@@@@@@@@%*@@@@##%@+=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@**+#%#*=%#%%=+#%+*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@##%%@@@@@@@@@@@%%@@@@#+@@*=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@=+#%###%%**+@#%=+#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+#@%@@@@@@@@@%@@@@@@-+%@#=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@*+*#%*@%#*#@**+++@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@===-=#%@@@@%#@@@@@@*+%@%**@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@#+##*@@+####*%++=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-:::-##*###@@@@@@@@#=*%@@*-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@+%+%@##@#@*#*%*+=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*:::-=+*=@#@@@@@@@@@@@+*#%%*=:%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@%@##@%*%%#+**%+*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@=:::-=++*#=@@@@@@@@@@@@-+*##@#+:@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:::-=+****+#@@@@@@@@@@@@==+##%@#--@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%:::-+*####*+@@@@@@@@@@@@@%-=*%%@@*:#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@::-+*#@@@%**@@@****+=#@@@@@-=+#%@@#=:@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@::-+#@@@@%#*@@=:-::-=**##@@@%-+*%@@%*-#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#:-*%@@@@@%=::::=%%+::+*=@#@@@:=*#%@@#==@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@=:=#%@@@@%*::-+:=#@@-:::-:-*@@=-+#%%%#+-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:-+#%%@@@#=:-=:-+#%%@####+-=@@*:+#%%%#+=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:-+#%%%%%%+:-:::-*%%%##%%#==@#:-+#%%%#=*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%:=*#%%%%%%*--*#***+###%@%#+=@:-=*#%%%*-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*:=*#%%%%%%#+-**%%%@@@@@@@%*=:-+*#%%%#+=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+:=*##%%%%%%#+:+#%@@@@@@@@%#-=*#%%%%%#=#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@      @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@       .@  .@@@: #@@@@@@@@@@@@@@@@@@@@@@+:=+*##%%%%@@%=:%%@@@@@@@@@%@@@%%%%%#+-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@  @@@@. :@@@@@#@@@@=@@@@@@@=@@@@@@@@+@@@@@@@@  *@@@@@@    @@: #@@@@@@@@@@@@@@@@@@@@@@+:=+*##%%@@@@%+-:*@@@@@@@@@%@@@@%%%#*=*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@  #@@@@@@@@@    @  :@  -@. .@  :@   @%  @@@@@@  .....@@  @  @: #@@@@@@@@@@@@@@@@@@@@@@@:-+**%%@@@@@@#+=:-+#@@@@%%@@@@@%%##+=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@  #@@=    @@ .@@@       @       @  @@@  @@@@@@  #@@@@@@  @@  : #@@@@@@@@@@@@@@@@@@@@@@*::=+*#%%%%@@@%#*=:--+#%@%*%%@@@%##*-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@  @@@@   @@ .@@@  @@@##@  @@@##@  @@@  @@@@@@  #@@@@@@  @@@   #@@@@@@@@@@@@@@@@@@@@@@:::-=++***#%%%%##+--:-==++*+*+#%%**=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@:    @  @@ .@@@@.   .@@@.   .@@  @@@  @@@@@@  #@@@@@@  @@@@  #@@@@@@@@@@@@@@@@@@@@#::::-==+++**######*+--:--=====+=##+=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*:..::--===++***####*+=-:::---=---=+=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#:::..:::---===+++*******+=-::::----::=@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+.:....:::::----===++******++=-:::::::::#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*:....:::::::::----===+++*****+==-::::::::@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#:..:::::::::-------===+++******+==-:::::::@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@=-::::::---------====+++********+==-:::::-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*:=*+---:----====++++****########**+=-::::+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*:+##-=#*=---=++++***####%%%%%%%%%%##*+=-:::#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@---=-*%%+=%%*+==+**####%%@@@@@@@@@@@%%#*+=-::@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:--=--=*@@@#+@@%#**##%%#%%@@@@@@@@@@@@%#*+=-::@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%:-=---###%@@@@@#@@@%%%%@@@@@@@@@@@@@@@@@%#*=-:-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#-----*#%%%%@@@@@@@@%@@@@@@@@@@@@@@@@@@@@%##*+-:+@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*::.=@@@@@@@@@*=::::-:::=-+=#%%%%@@@@@@@@@@@@@%@@@@@@@@@@@@@%%%%%%#*+-:%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@--:::.:=:------=====--=:-*+#%%%@@@@@@@@@@@@@%%@@@@@@@@@@@@@@%%%%%%##*+--@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%:-::::::-*--=--======+++--*@@@@@@@@@@@@@@@@@@@@%%%%@@@@@@@%%@@@@%%#***=-#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+---:=-::-*@+*#*+++****##*==%@@@@@@@@@@@@@@@@@@@@@%###%%%@@@@@@%#%@@#**+==@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-=-:#++*-=*#@#%@%######%%@@@@@@@@@@@@@@@@@@@@@@@@@@@%%######%%%@@@@#*@#+=-@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%:+-=*****#*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%***#######@@@=#=#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-+++*#####*#*=+%@@@@@@@@@@@@@@@@@@@@@@@@@@%@@@%@@@@@@@@@@@%##****++++=%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@+**####**+==-:-=::+%%%%%####%%%%%%%################%%%%%%%%%@@@%#++%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    */
