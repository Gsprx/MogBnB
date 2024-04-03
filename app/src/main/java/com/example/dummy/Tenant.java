package com.example.dummy;

//
// TODO: Implement tenant class for dummy app
//XRHSIMOPOIHSE TO USERMASTER PORT
//h lista dao anti na einai hardcoded px pare to room , sthn ousia twra prepei na anoigw tcp object me ton master kai na dinei to request
import com.example.misc.Config;
import com.example.misc.TypeChecking;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.Room;
import com.example.mogbnb.Filter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Tenant {
    private static final Scanner scanner = new Scanner(System.in);

    public static void runTenant() {displayOperationOptions();}
        /**
         * Displays a menu of operations (see bookings, search for a room, rate a room, exit) and processes user input to perform the selected action.
         */
        private static void displayOperationOptions(){
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
        try (Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(MasterFunction.SHOW_BOOKINGS.getEncoded());
            // out.writeObject(null);
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
        String area = scanner.nextLine();

        System.out.print("Enter check-in date (YYYY-MM-DD, leave blank for no preference): ");
        LocalDate checkIn = readDate();

        System.out.print("Enter check-out date (YYYY-MM-DD, leave blank for no preference): ");
        LocalDate checkOut = readDate();

        System.out.print("Number of persons (1 for minimum ): ");
        int noOfPersons = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter the minimum price  (0 for no preference): ");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.print("Minimum number of stars (0 for lowest rating): ");
        int stars = Integer.parseInt(scanner.nextLine());

        Filter filter = new Filter(area, checkIn, checkOut, noOfPersons, price, stars);

        try (Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(MasterFunction.SEARCH_ROOM.getEncoded());
            out.writeObject(filter);
            out.flush();

            List<Room> rooms = (List<Room>) in.readObject();
            System.out.println("Results");
            rooms.forEach(System.out::println);
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
        System.out.println("\nEnter the name of the room you want to rate:");
        String roomName = scanner.nextLine();
        double rating = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("Enter your rating (0.0 to 5.0):");
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

        try (Socket socket = new Socket("localhost", Config.USER_MASTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send the action identifier for rating a room
            out.writeInt(MasterFunction.RATE_ROOM.getEncoded());
            // Send the room name and rating encapsulated in a serializable object or as separate data
            out.writeObject(new Object[]{roomName, rating});
            out.flush();

            // Await confirmation from the server
            String response = (String) in.readObject();
            System.out.println(response);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred while communicating with the server: " + e.getMessage());
        }

    }
}


