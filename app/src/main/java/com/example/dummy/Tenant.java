package com.example.dummy;

import com.example.misc.Config;
import com.example.misc.TypeChecking;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.Room;
import com.example.mogbnb.Filter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Tenant implements Serializable{

    private int id;

    public Tenant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void runTenant() {
        displayOperationOptions(this);
    }

    /**
     * Displays a menu of operations (see bookings, search for a room, rate a room, exit) and processes user input to perform the selected action.
     */
    private void displayOperationOptions(Tenant tenant){
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
                    seeBookings(tenant);
                    break;
                case 2:
                    searchRoom();
                    break;
                case 3:
                    makeReservation(tenant);
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

    private static void seeBookings(Tenant tenant) {
        try (Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(MasterFunction.SHOW_BOOKINGS.getEncoded());
            out.writeObject(tenant);
            out.flush();

            List<Room> bookings = (List<Room>) in.readObject();

            if (bookings.isEmpty()) {
                System.out.println("No bookings found.");
            } else {
                bookings.forEach(System.out::println);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void searchRoom() {
        System.out.print("Enter area (leave blank for no preference): ");
        Scanner scanner = new Scanner(System.in);
        String area = scanner.nextLine();
        if (area.equals("")) area = null;

        System.out.print("Enter check-in date (YYYY-MM-DD, leave blank for no preference): ");
        LocalDate checkIn = readDate();

        System.out.print("Enter check-out date (YYYY-MM-DD, leave blank for no preference): ");
        LocalDate checkOut = readDate();

        System.out.print("Number of persons: ");
        scanner = new Scanner(System.in);
        String noOfPersonsStr = scanner.nextLine();
        int noOfPersons;
        if (noOfPersonsStr.equals("") || !TypeChecking.isInteger(noOfPersonsStr)) noOfPersons = -1;
        else noOfPersons = Integer.parseInt(noOfPersonsStr);

        System.out.print("Enter the maximum price: ");
        scanner = new Scanner(System.in);
        String priceStr = scanner.nextLine();
        double price;
        if (priceStr.equals("") || !TypeChecking.isDouble(priceStr)) price = -1;
        else price = Double.parseDouble(priceStr);

        System.out.print("Minimum number of stars: ");
        scanner = new Scanner(System.in);
        String starsStr = scanner.nextLine();
        double stars;
        if (starsStr.equals("") || !TypeChecking.isDouble(starsStr)) stars = -1;
        else stars = Double.parseDouble(starsStr);

        Filter filter = new Filter(area, checkIn, checkOut, noOfPersons, price, stars);

        try (Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(MasterFunction.SEARCH_ROOM.getEncoded());
            out.writeObject(filter);
            out.flush();

            ArrayList<Room> rooms = (ArrayList<Room>) in.readObject();
            rooms.forEach(System.out::println);
            System.out.println("------------------------------------------------\n");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a date input from the user, ensuring the format is valid. Allows for blank input to indicate no preference.
     *
     * @return A LocalDate object if a valid date is entered, or null if no date is specified.
     */
    private static LocalDate readDate() {
        Scanner scanner = new Scanner(System.in);

        LocalDate date = null;
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                // No date entered, return null to indicate no preference
                return null;
            } else {
                try {
                    date = LocalDate.parse(input); // Try to parse the input
                    break; // Break the loop if parsing is successful
                } catch (DateTimeParseException e) {

                    System.out.print("Invalid date format. Please enter a date in YYYY-MM-DD format or leave blank for no preference: ");
                }
            }
        }
        return date;
    }

    /**
     * Make a reservation.
     */
    private static void makeReservation(Tenant tenant) {
        System.out.println("\nEnter the name of the room you want to book:");
        Scanner scanner = new Scanner(System.in);
        String roomName = scanner.nextLine();

        try {
            Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
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

            search_out.close();
            search_in.close();
            socket.close();

            socket = new Socket("localhost", Config.USER_MASTER_PORT);
            ObjectOutputStream book_out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream book_in = new ObjectInputStream(socket.getInputStream());

            // read check-in, check-out
            System.out.println("Enter checkIn (YYYY-MM-DD): ");
            LocalDate checkIn = readDate();
            System.out.println("Enter checkOut (YYYY-MM-DD): ");
            LocalDate checkOut = readDate();

            // Now, send the booking along with room information to the server
            book_out.writeInt(MasterFunction.BOOK_ROOM.getEncoded());
            ArrayList<Object> roomBooking = new ArrayList<>();
            roomBooking.add(roomName); roomBooking.add(tenant.getId()); roomBooking.add(checkIn); roomBooking.add(checkOut);
            book_out.writeObject(roomBooking);
            book_out.flush();

            // Await confirmation from the server
            String response = (String) book_in.readObject();
            System.out.println(response);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows the user to rate a room by its name. Prompts for the room name and the desired rating, then updates the room's information accordingly.
     */
    private static void rateRoom() {
        System.out.println("\nEnter the name of the room you want to rate:");
        Scanner scanner = new Scanner(System.in);
        String roomName = scanner.nextLine();

        try {
            Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
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

            socket = new Socket("localhost", Config.USER_MASTER_PORT);
            ObjectOutputStream rate_out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream rate_in = new ObjectInputStream(socket.getInputStream());

            while (!validInput) {
                try {
                    System.out.println("Enter your rating (0.0 to 5.0):");
                    scanner = new Scanner(System.in);
                    rating = Double.parseDouble(scanner.nextLine());
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
            Map.Entry<String, Double> roomRating = new AbstractMap.SimpleEntry<>(roomName, rating);
            rate_out.writeObject(roomRating);
            rate_out.flush();

            // Await confirmation from the server
            String response = (String) rate_in.readObject();
            System.out.println(response);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred while communicating with the server: " + e.getMessage());
        }
    }


     public static void main(String[] args) {
        Tenant tenant = new Tenant(Integer.parseInt(args[0]));
        tenant.runTenant();
     }
}


