package com.example.mogbnb;

import androidx.annotation.NonNull;

import com.example.misc.JsonConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Filter {
    private static final Scanner scanner = new Scanner(System.in);

    //TODO
    public static List<Room> rooms = JsonConverter.deserializeRooms("app/src/test/java/com/example/mogbnb/exampleInput.json");

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
    public static void displayOperationOptions() {
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
        System.out.print("Enter area to search (leave blank for no preference): ");
        String area = scanner.nextLine();
        System.out.print("Enter minimum rating (0 for no preference): ");
        double rating = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        System.out.println("Searching for rooms...");
        for (Room room : rooms) {
            if ((area.isEmpty() || room.getArea().equalsIgnoreCase(area)) && room.getStars() >= rating) {
                System.out.println(room);
            }
        }
    }
    private static void rateRoom() {
        System.out.print("Enter room name to rate: ");
        String roomName = scanner.nextLine();
        System.out.print("Enter rating: ");
        double rating = scanner.nextDouble();

        for (Room room : rooms) {
            if (room.getRoomName().equals(roomName)) {
                room.addReview(rating);
                System.out.println("Room rated successfully.");
                return;
            }
        }
        System.out.println("Room not found.");
    }
}

