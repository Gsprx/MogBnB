package com.example.dummy;

import com.example.misc.Config;
import com.example.misc.Misc;
import com.example.misc.TypeChecking;
import com.example.mogbnb.Filter;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.Room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class Tenant implements Serializable{

    private int id;

    public Tenant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void runTenant() {
        displayOperationOptions();
    }

    /**
     * Displays a menu of operations (see bookings, search for a room, rate a room, exit) and processes user input to perform the selected action.
     */
    private void displayOperationOptions(){
        while (true) {
            System.out.println("\nPlease select an operation:");
            System.out.println("1. See my bookings");
            System.out.println("2. Search for a room");
            System.out.println("3. Make reservation");
            System.out.println("4. Rate a room");
            System.out.println("5. Exit");
            System.out.print("Your choice: ");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    seeBookings();
                    break;
                case 2:
                    searchRoom();
                    break;
                case 3:
                    makeReservation();
                    break;
                case 4:
                    rateRoom();
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please select again.");
                    break;
            }
        }
    }

    private void seeBookings() {
        try (Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(MasterFunction.SHOW_BOOKINGS.getEncoded());
            out.writeObject(id);
            out.flush();

            HashMap<String,ArrayList<LocalDate>> bookings = (HashMap<String,ArrayList<LocalDate>>) in.readObject();

            if (bookings.isEmpty()) {
                System.out.println("No bookings found.");
            } else {
                Set<String> roomNames = bookings.keySet();
                for (String name : roomNames){
                    System.out.println("Room: " + name + "\nDays booked: " + bookings.get(name).toString());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void searchRoom() {
        System.out.print("Enter area (leave blank for no preference): ");
        Scanner scanner = new Scanner(System.in);
        String area = scanner.nextLine();
        if (area.equals("")) area = null;
        if (area != null && area.equalsIgnoreCase("exit")) return;

        LocalDate checkIn;
        LocalDate checkOut;

        while(true) {
            System.out.print("Start date (YYYY-MM-DD): ");
            checkIn= Misc.readDate();
            if(checkIn==null)return;
            System.out.print("End date (YYYY-MM-DD): ");
            checkOut= Misc.readDate();
            if(checkOut==null)return;
            if(checkIn.isAfter(checkOut)){
                System.out.print("[-]Invalid dates, start date must be the same or before the end date!\n");
            }
            else {
                break;
            }
        }

        System.out.print("Number of persons (leave blank for no preference): ");
        scanner = new Scanner(System.in);
        String noOfPersonsStr = scanner.nextLine();
        int noOfPersons;
        if (noOfPersonsStr.equals("") || !TypeChecking.isInteger(noOfPersonsStr)) noOfPersons = -1;
        else if (noOfPersonsStr.equalsIgnoreCase("exit")) {
            return;
        } else noOfPersons = Integer.parseInt(noOfPersonsStr);

        System.out.print("Enter the maximum price (leave blank for no preference): ");
        scanner = new Scanner(System.in);
        String priceStr = scanner.nextLine();
        double price;
        if (priceStr.equals("") || !TypeChecking.isDouble(priceStr)) price = -1;
        else if (priceStr.equalsIgnoreCase("exit")) {
            return;
        } else price = Double.parseDouble(priceStr);

        System.out.print("Minimum number of stars (leave blank for no preference): ");
        scanner = new Scanner(System.in);
        String starsStr = scanner.nextLine();
        double stars;
        if (starsStr.equals("") || !TypeChecking.isDouble(starsStr)) stars = -1;
        else if (starsStr.equalsIgnoreCase("exit")) {
            return;
        } else stars = Double.parseDouble(starsStr);

        Filter filter = new Filter(area, checkIn, checkOut, noOfPersons, price, stars);

        try (Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(MasterFunction.SEARCH_ROOM.getEncoded());
            out.writeObject(filter);
            out.flush();

            ArrayList<Room> rooms = (ArrayList<Room>) in.readObject();
            System.out.println("------------------ROOMS FOUND-------------------");
            rooms.forEach(System.out::println);
            System.out.println("------------------------------------------------\n");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make a reservation.
     */
    private void makeReservation() {
        System.out.println("\nEnter the name of the room you want to book:");
        Scanner scanner = new Scanner(System.in);
        String roomName = scanner.nextLine();

        if(roomName.equalsIgnoreCase("exit")) return;

        try {
            Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
            ObjectOutputStream search_out = new ObjectOutputStream(socket.getOutputStream());

            search_out.writeInt(MasterFunction.FIND_ROOM_BY_NAME.getEncoded());
            // Send the room name for searching
            search_out.writeObject(roomName);
            search_out.flush();

            // Receive the room information from the server
            ObjectInputStream search_in = new ObjectInputStream(socket.getInputStream());
            Room room = ((ArrayList<Room>) search_in.readObject()).get(0);

            if (room == null) {
                System.out.println("Room not found.");
                return;
            }
            Room.setCurrentDate();

            // show booking table
            System.out.print("========== Room calendar ==========");
            for (int i=0; i<room.getBookingTable().length; i++) {
                if (i % 7 == 0) {
                    System.out.println();
                }

                // if booking in i != 0, then room is unavailable
                LocalDate dateOf_i = room.getCurrentDate().plusDays(i);
                if (room.getBookingTable()[i] != 0)
                    System.out.print(dateOf_i + ": Unavailable | ");
                else
                    System.out.print(dateOf_i + ": Available | ");
            }
            System.out.println();

            search_out.close();
            search_in.close();
            socket.close();

            socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
            ObjectOutputStream book_out = new ObjectOutputStream(socket.getOutputStream());

            // read check-in, check-out
            LocalDate checkIn;
            LocalDate checkOut;
            while(true) {
                System.out.print("\nStart date (YYYY-MM-DD): ");
                checkIn = Misc.readDate();
                if(checkIn==null)return;

                System.out.print("End date (YYYY-MM-DD): ");
                checkOut = Misc.readDate();
                if(checkOut==null)return;

                if(checkIn.isAfter(checkOut)){
                    System.out.print("[-]Invalid dates, start date must be the same or before the end date!\n");
                }
                else {
                    break;
                }
            }

            // Now, send the booking along with room information to the server
            book_out.writeInt(MasterFunction.BOOK_ROOM.getEncoded());
            ArrayList<Object> roomBooking = new ArrayList<>();
            roomBooking.add(roomName); roomBooking.add(id); roomBooking.add(checkIn); roomBooking.add(checkOut);
            book_out.writeObject(roomBooking);
            book_out.flush();

            // Await confirmation from the server
            ObjectInputStream book_in = new ObjectInputStream(socket.getInputStream());
            int result = (int) book_in.readObject();
            if (result == 1) System.out.println("Booking successful.");
            else System.out.println("Booking was unsuccessful, days requested were already booked!");

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows the user to rate a room by its name. Prompts for the room name and the desired rating, then updates the room's information accordingly.
     */
    private void rateRoom() {
        System.out.println("\nEnter the name of the room you want to rate:");
        Scanner scanner = new Scanner(System.in);
        String roomName = scanner.nextLine();

        if(roomName.equalsIgnoreCase("exit")) return;

        try {
            Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
            ObjectOutputStream search_out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream search_in = new ObjectInputStream(socket.getInputStream());

            search_out.writeInt(MasterFunction.FIND_ROOM_BY_NAME.getEncoded());
            // Send the room name for searching
            search_out.writeObject(roomName);
            search_out.flush();

            // Receive the room information from the server
            Room room = (Room) search_in.readObject();
            if (room == null) {
                System.out.println("Room not found.");
                return;
            }

            double rating = 0;
            boolean validInput = false;

            search_out.close();
            search_in.close();
            socket.close();

            socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
            ObjectOutputStream rate_out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream rate_in = new ObjectInputStream(socket.getInputStream());

            while (!validInput) {
                try {
                    System.out.println("Enter your rating (0.0 to 5.0):");
                    scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    if(input.equalsIgnoreCase("exit")) return;
                    rating = Double.parseDouble(input);
                    if (rating < 0 || rating > 5) {
                        System.out.println("Rating must be between 0.0 and 5.0.");
                    } else {
                        validInput = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
            }

            // Now, send the rating along with room information to the server
            rate_out.writeInt(MasterFunction.RATE_ROOM.getEncoded());
            ArrayList<Object> roomRating = new ArrayList<>();
            roomRating.add(roomName);
            roomRating.add(rating);
            rate_out.writeObject(roomRating);
            rate_out.flush();

            // Await confirmation from the server
            int result = (int) rate_in.readObject();
            if (result == 1) System.out.println("Rating updated successfully.");
            else System.out.println("An error occurred while rating this room.");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred while communicating with the server: " + e.getMessage());
        }
    }


     public static void main(String[] args) {
        Tenant tenant = new Tenant(Integer.parseInt(args[0]));
        tenant.runTenant();
     }
}
