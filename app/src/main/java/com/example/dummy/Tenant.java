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
        try (Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
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

        LocalDate checkIn;
        LocalDate checkOut;
        System.out.println("\n|Bookings per area|");
        while(true) {
            System.out.print("Start date (YYYY-MM-DD): ");
            checkIn= DummyMain.readDate();
            System.out.print("End date (YYYY-MM-DD): ");
            checkOut= DummyMain.readDate();
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
        else noOfPersons = Integer.parseInt(noOfPersonsStr);

        System.out.print("Enter the maximum price (leave blank for no preference): ");
        scanner = new Scanner(System.in);
        String priceStr = scanner.nextLine();
        double price;
        if (priceStr.equals("") || !TypeChecking.isDouble(priceStr)) price = -1;
        else price = Double.parseDouble(priceStr);

        System.out.print("Minimum number of stars (leave blank for no preference): ");
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
            System.out.println("------------------ROOMS FOUND-------------------\n");
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
    public static LocalDate readDate() {
        LocalDate date;
        while (true) {
            Scanner inp = new Scanner(System.in);
            String input = inp.nextLine().trim();
            if (!input.isEmpty()) {
                try {
                    date = LocalDate.parse(input); // Try to parse the input
                    if (date.isBefore(LocalDate.now())){
                        System.out.print("[-]Invalid date, must be a date from today onward (" + LocalDate.now().toString() +  ") : ");
                        continue;
                    }
                    break; // Break the loop if parsing is successful
                } catch (DateTimeParseException e) {
                    System.out.print("[-]Invalid date format. Please enter a date in YYYY-MM-DD format: ");
                }
            }
        }
        return date;
    }

    /**
     * Make a reservation.
     */
    private void makeReservation() {
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
            LocalDate checkIn;
            LocalDate checkOut;
            System.out.println("\n|Bookings per area|");
            while(true) {
                System.out.print("Start date (YYYY-MM-DD): ");
                checkIn = DummyMain.readDate();
                System.out.print("End date (YYYY-MM-DD): ");
                checkOut = DummyMain.readDate();
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
            String response = (String) book_in.readObject();
            System.out.println(response);

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
            ArrayList<Object> roomRating = new ArrayList<>();
            roomRating.add(roomName);
            roomRating.add(roomRating);
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


