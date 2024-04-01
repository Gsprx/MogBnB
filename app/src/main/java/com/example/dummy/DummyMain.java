package com.example.dummy;

import com.example.misc.JsonConverter;
import com.example.mogbnb.Room;
import com.example.mogbnb.Filter;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DummyMain {
    private static final Scanner scanner = new Scanner(System.in);
    private static List<Room> rooms = JsonConverter.deserializeRooms("app/src/test/java/com/example/mogbnb/exampleInput.json");

    /**
     * Main class for managing tenant and manager operations in a room booking application.
     */
    public static void main(String[] args) {
        String acc_typ;
        //TODO

        // get the account type (master or tenant)
        // if the user does not give valid info then ask again
        do {
            System.out.print("Manager/Tenant[M/t]: ");
            Scanner inp = new Scanner(System.in);
            acc_typ = inp.next().toLowerCase();
            if (!acc_typ.equals("m") && !acc_typ.equals("t")) System.out.println("[-] Invalid option. Please choose Manager or Tenant.");
        } while (!acc_typ.equals("m") && !acc_typ.equals("t"));

        // choose what to run based on account type
        if (acc_typ.equals("m")) Manager.runManager();
        else Tenant.runTenant();
    }
    //private static void runTenant() {
     //   displayOperationOptions();
    //}

    /**
     * Displays a menu of operations (see bookings, search for a room, rate a room, exit) and processes user input to perform the selected action.
     */
    private static void displayOperationOptions() {
        while (true) {
            System.out.println("\nPlease select an operation:");
            System.out.println("1. See my bookings");
            System.out.println("2. Search for a room");
            System.out.println("3. Rate a room");
            System.out.println("4. Exit");
            System.out.print("Your choice: ");

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
                    rateRoom();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please select again.");
            }
        }
    }
    private static void seeBookings() {
        for (Room room : rooms) {
            System.out.println("Room: " + room.getRoomName());
            for (int i = 0; i < room.getAvailableDays(); i++) {
                if (room.getBookingTable()[i]) {
                    System.out.println("Booked on: " + LocalDate.now().plusDays(i));
                }
            }
        }
    }


    private static void searchRoom() {
        System.out.println("Enter search criteria:");

        System.out.print("Enter area (leave blank for no preference): ");
        String area = scanner.nextLine();

        System.out.print("Enter check-in date (YYYY-MM-DD, leave blank for no preference): ");
        LocalDate checkIn = readDate();

        System.out.print("Enter check-out date (YYYY-MM-DD, leave blank for no preference): ");
        LocalDate checkOut = readDate();

        System.out.print("Number of persons (0 for no preference): ");
        int noOfPersons = scanner.nextInt();

        System.out.print("Maximum price (0 for no preference): ");
        double price = scanner.nextDouble();

        System.out.print("Minimum rating (0 for no preference): ");
        double stars = scanner.nextDouble();
        scanner.nextLine();

        Filter filter = new Filter(area, checkIn, checkOut, noOfPersons, price, stars);

        // Now apply this filter to each room and display the ones that match
        System.out.println("Rooms matching your criteria:");
        for (Room room : rooms) {
            if (room.filterAccepted(filter)) {
                System.out.println(room);
            }
        }
    }
    /**
     * Reads a date input from the user, ensuring the format is valid. Allows for blank input to indicate no preference.
     *
     * @return A LocalDate object if a valid date is entered, or null if no date is specified.
     */
    private static LocalDate readDate() {
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
     * Allows the user to rate a room by its name. Prompts for the room name and the desired rating, then updates the room's information accordingly.
     */

    private static void rateRoom() {
        System.out.print("Enter room name to rate: ");
        String roomName = scanner.nextLine();
        System.out.print("Enter rating: ");
        double rating = scanner.nextDouble();
        scanner.nextLine();

        for (Room room : rooms) {
            if (room.getRoomName().equalsIgnoreCase(roomName)) {
                room.addReview(rating);
                System.out.println("Room rated successfully.");
                return;
            }
        }
        System.out.println("Room not found.");
    }
}


