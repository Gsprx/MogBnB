package com.example.dummy;

import com.example.misc.Config;
import com.example.misc.Misc;
import com.example.misc.TypeChecking;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.Room;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Manager {

    public static void runManager() {
        System.out.println("\n-------------------Welcome Boss-------------------");
        boolean running = true;
        // manager chooses option
        while (running) {
            System.out.println("1) Show Rooms\n2) Add Room\n3) Bookings\n");
            String option;
            // if the user gives invalid info then ask again
            do {
                System.out.print("> ");
                Scanner inp = new Scanner(System.in);
                option = inp.nextLine().toLowerCase();
                if (option.equals("exit")) {
                    running = false;
                }
                else if (!option.equals("1") && !option.equals("2") && !option.equals("3")) System.out.println("[-] Option " + option + " not found.");
            } while (!option.equals("1") && !option.equals("2") && !option.equals("3") && !option.equals("exit"));

            if (!running) continue;

            switch (option) {
                // option 1: show all registered rooms
                case "1":
                    managerShowRooms();
                    break;
                // option 2: add a new room (will not write to JSON)
                case "2":
                    managerAddRoom();
                    break;
                // option 3: manage bookings
                default:
                    managerManageBookings();
                    System.out.println();
                    break;
            }
        }
    }

    /**
     * Show all registered rooms.
     * If there are no registered rooms yet, do nothing.
     */
    private static void managerShowRooms() {
        // waiting input stream -> its going to be a list with all the registered rooms
        ObjectInputStream in = null;
        // holds a code for the function that needs to be executed and an object argument
        ObjectOutputStream out = null;
        Socket socket = null;

        try {
            socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());

            // out the show rooms function
            out.writeInt(MasterFunction.SHOW_ROOMS.getEncoded());
            out.writeObject(null);
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());
            ArrayList<Room> resultObj = (ArrayList<Room>) in.readObject();

            System.out.println("|Registered rooms|");
            if (resultObj != null) {
                for (Room r : resultObj) {
                    System.out.println(r);
                }
            } else {
                System.out.println("Empty list...Real quiet...");
            }
            System.out.println("---------------------------------------------\n");

            out.close();
            in.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("Something went wrong fetching the data. Error: " + e + "\n");
        }
    }

    /**
     * Add a new room.
     */
    private static void managerAddRoom() {
        // holds a code for the function that needs to be executed and an object argument
        ObjectOutputStream out = null;
        Socket socket = null;

        // room attributes
        String rName;
        int noOfPeople;
        int availDays;
        String area;
        double price;
        String roomImg = null;
        String description;
        List<String> amenities = new ArrayList<>();

        String temp_inp = ""; // we will use this for the values that need to be transformed from string to int/double

        System.out.println("|Add room|");

        Scanner inp = new Scanner(System.in); // room name
        System.out.print("[Room name]: ");
        rName = inp.nextLine();
        if (rName.equals("exit")) {System.out.println("Canceling...\n"); return;} // if input == exit, then return

        do {                                // number of people
            inp = new Scanner(System.in);
            System.out.print("[No. of people]: ");
            temp_inp = inp.nextLine();
            if (temp_inp.equals("exit")) {System.out.println("Canceling...\n"); return;}
            else if (!TypeChecking.isInteger(temp_inp)) System.out.println("[-] Invalid input.");
            else if (TypeChecking.isInteger(temp_inp) && Integer.parseInt(temp_inp) <= 0) System.out.println("[-] Invalid input. Number must be grater than 0.");
        } while (!TypeChecking.isInteger(temp_inp) || (TypeChecking.isInteger(temp_inp) && Integer.parseInt(temp_inp) <= 0));
        noOfPeople = Integer.parseInt(temp_inp);

        do {                                // available days
            inp = new Scanner(System.in);
            System.out.print("[Calendar days]: ");
            temp_inp = inp.nextLine();
            if (temp_inp.equals("exit")) {System.out.println("Canceling...\n"); return;}
            else if (!TypeChecking.isInteger(temp_inp)) System.out.println("[-] Invalid input.");
            else if (TypeChecking.isInteger(temp_inp) && Integer.parseInt(temp_inp) <= 13) System.out.println("[-] Invalid input. Available days must be at least 14.");
        } while (!TypeChecking.isInteger(temp_inp) || (TypeChecking.isInteger(temp_inp) && Integer.parseInt(temp_inp) <= 13));
        availDays = Integer.parseInt(temp_inp);

        inp = new Scanner(System.in);       // area
        System.out.print("[Area]: ");
        area = inp.nextLine();
        if (area.equals("exit")) {System.out.println("Canceling...\n"); return;} // if input == exit, then return

        do {                                // price
            inp = new Scanner(System.in);
            System.out.print("[Price]: ");
            temp_inp = inp.nextLine();
            if (temp_inp.equals("exit")) {System.out.println("Canceling...\n"); return;}
            else if (!TypeChecking.isDouble(temp_inp)) System.out.println("[-] Invalid input.");
            else if (TypeChecking.isDouble(temp_inp) && Double.parseDouble(temp_inp) <= 0) System.out.println("[-] Invalid input. Price must be grater than 0.");
        } while (!TypeChecking.isDouble(temp_inp) || (TypeChecking.isDouble(temp_inp) && Double.parseDouble(temp_inp) <= 0));
        price = Double.parseDouble(temp_inp);

        inp = new Scanner(System.in);
        System.out.print("[Description]: ");
        description = inp.nextLine();
        if (description.equals("exit")) {
            System.out.println("Canceling...\n");
            return;
        }

        System.out.println("[Amenities] (type 'done' when finished):");
        while (true) {
            System.out.print("> ");
            String amenity = inp.nextLine();
            if (amenity.equalsIgnoreCase("done")) {
                break;
            } else if (amenity.equalsIgnoreCase("exit")) {
                System.out.println("Canceling...\n");
                return;
            } else {
                amenities.add(amenity);
            }
        }
        inp = new Scanner(System.in);
        System.out.print("[Image directory name]: ");
        roomImg = inp.nextLine();
        // Create directory if it doesn't exist
        String dirPath = Config.ASSETSPATH + roomImg;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directory created: " + dirPath);
            } else {
                System.out.println("Failed to create directory: " + dirPath);
                return;
            }
        }
        if (roomImg.equals("exit")) { System.out.println("Canceling...\n"); return; }

        System.out.println("\nProceeding to add room: \nname: " + rName + "\nmax-people: " + noOfPeople + "\n" +
                "calendar-days: " + availDays + "\narea: " + area + "\nprice: " + price + "\nimg-path: " + dirPath +
                "\ndescription: " + description + "\nAmenities: " + String.join(", ", amenities));
        System.out.println("\nAre you sure?[Y/n]");
        String ans;
        do {
            System.out.print("> ");
            inp = new Scanner(System.in);
            ans = inp.nextLine().toLowerCase();
            if (!ans.equals("y") && !ans.equals("n")) System.out.println("[-] Invalid option. Please choose Yes or No");
        } while (!ans.equals("y") && !ans.equals("n"));

        if (ans.equals("n")) {System.out.println("Canceling...\n");}
        else {
            try {
                socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());

                Room r = new Room(rName, noOfPeople, availDays, area, 0, 0, roomImg, price,amenities,description);

                // out the add room function
                out.writeInt(MasterFunction.ADD_ROOM.getEncoded());
                out.writeObject(r);
                out.flush();

                System.out.println();

                out.close();
                socket.close();

            } catch (IOException e) {
                System.out.println("Something went wrong. Error: " + e + "\nCanceling...\n");
            }
        }
    }

    /**
     * Manage bookings.
     * Options: 1) Show the bookings for a chosen room
     *          2) Show the bookings in an area, given a "<start_date> - <end_date>"
     */
    private static void managerManageBookings() {
        boolean running = true;
        // manager chooses option
        while (running) {
            System.out.println("\n|Manage Bookings|");
            System.out.println("1) Show for room\n2) Areas\nType -exit- to return to main menu\n");
            String option;
            // if the user gives invalid info then ask again
            do {
                System.out.print("> ");
                Scanner inp = new Scanner(System.in);
                option = inp.nextLine().toLowerCase();
                if (option.equals("exit")) {
                    return;
                } else if (!option.equals("1") && !option.equals("2"))
                    System.out.println("[-] Option " + option + " not found.");
            } while (!option.equals("1") && !option.equals("2"));

            // option 1: show all bookings
            if (option.equals("1")) managerShowBookingsForRoom();
            else managerShowBookingsPerArea();
        }
    }

    private static void managerShowBookingsForRoom() {
        // first show all rooms
        managerShowRooms();

        // then ask to choose a specific room
        // waiting input stream -> its going to be a list with all the registered rooms
        ObjectInputStream in = null;
        // holds a code for the function that needs to be executed and an object argument
        ObjectOutputStream out = null;
        Socket socket = null;

        // get room name
        System.out.println("|Get bookings of room|");
        System.out.print("Name of room: ");
        Scanner inp = new Scanner(System.in);
        String input = inp.nextLine();

        try {
            socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);

            out = new ObjectOutputStream(socket.getOutputStream());

            // write
            out.writeInt(MasterFunction.SHOW_BOOKINGS_OF_ROOM.getEncoded());
            out.writeObject(input);
            out.flush();

            // wait for input
            in = new ObjectInputStream(socket.getInputStream());
            ArrayList<String> bookings = (ArrayList<String>) in.readObject();

            if (bookings != null) {
                System.out.println();
                for (String b : bookings) {
                    System.out.println(b);
                    System.out.println("-----------------------------");
                }
            } else {
                System.out.println("Empty list...Real quiet...");
            }
            System.out.println();

            out.close();
            in.close();
            socket.close();

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void managerShowBookingsPerArea() {
        // waiting input stream -> its going to be a list with all the bookings
        ObjectInputStream in;
        // holds a code for the function that needs to be executed and an object argument
        ObjectOutputStream out;
        Socket socket;

        LocalDate start;
        LocalDate end;
        System.out.println("\n|Bookings per area|");
        while(true) {
            System.out.print("Start date (YYYY-MM-DD): ");
            start = Misc.readDate();
            System.out.print("End date (YYYY-MM-DD): ");
            end = Misc.readDate();
            if(start.isAfter(end)){
                System.out.print("[-]Invalid dates, start date must be the same or before the end date!\n");
            }
            else {
                break;
            }
        }

        // create an array with start and end date
        ArrayList<LocalDate> dates = new ArrayList<>();
        dates.add(start); dates.add(end);

        try {
            socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);

            out = new ObjectOutputStream(socket.getOutputStream());

            // write the filter
            out.writeInt(MasterFunction.BOOKINGS_PER_AREA.getEncoded());
            out.writeObject(dates);
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());
            HashMap<String, Integer> areas = (HashMap<String, Integer>) in.readObject();

            // Iterating HashMap
            if (areas != null) {
                for (Map.Entry<String, Integer> set : areas.entrySet()) {
                    System.out.println(set.getKey() + ": " + set.getValue());
                }
            } else {
                System.out.println("Empty list...Real quiet...");
            }
            System.out.println();

            out.close();
            in.close();
            socket.close();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        Manager.runManager();
    }
}
