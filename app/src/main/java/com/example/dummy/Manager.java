package com.example.dummy;

import static com.example.mogbnb.Master.TEMP_ROOM_DAO;

import com.example.misc.Config;
import com.example.misc.TypeChecking;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.Room;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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

            // option 1: show all registered rooms
            if (option.equals("1")) managerShowRooms();
                // option 2: add a new room (will not write to JSON)
            else if (option.equals("2")) managerAddRoom();
                // option 3: manage bookings
            else managerManageBookings();
        }
    }

    /**
     * Show all registered rooms.
     * If there are no registered rooms yet, do nothing.
     */
    private static void managerShowRooms() {
        // waiting input stream -> its going to be a list with all the registered rooms
        ObjectInputStream in = null;
        // a code so that the master server knows what function to operate
        OutputStream out = null;
        Socket socket = null;

        try {
            socket = new Socket("localhost", Config.PORT);
            in = new ObjectInputStream(socket.getInputStream());
            out = socket.getOutputStream();

            // out the show rooms function
            out.write(MasterFunction.SHOW_ROOMS.getEncoded());
            out.flush();
            List<Room> result = (List<Room>) in.readObject();

            System.out.println("|Registered rooms|");
            if (result != null) {
                for (Room r : result) {
                    System.out.println(r);
                }
            } else {
                System.out.println("Empty list...Real quiet...");
            }
            System.out.println("---------------------------------------------\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a new room.
     */
    private static void managerAddRoom() {
        // room attributes
        String rName;
        int noOfPeople;
        int availDays;
        String area;
        double price;
        String roomImg;

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

        inp = new Scanner(System.in);       // image
        System.out.print("[Image path]: ");
        roomImg = inp.nextLine();
        if (roomImg.equals("exit")) {System.out.println("Canceling...\n"); return;} // if input == exit, then return

        // after completing all the necessary info, give option to ignore new room and exit
        System.out.println("\nProceeding to add room: \nname: " + rName + "\nmax-people: " + noOfPeople + "\n" +
                "calendar-days: " + availDays + "\narea: " + area + "\nprice: " + price + "\nimg-path: " + roomImg);
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
            Room r = new Room(rName, noOfPeople, availDays, area, 0, 0, roomImg, price);
            TEMP_ROOM_DAO.add(r);
            System.out.println();
        }
    }

    /**
     * Manage bookings.
     * Options: 1) Show the bookings for each room
     *          2) Show the bookings in an area, given a "<start_date> - <end_date>"
     */
    private static void managerManageBookings() {
        boolean running = true;
        // manager chooses option
        while (running) {
            System.out.println("\n|Manage Bookings|");
            System.out.println("1) Show all\n2) Areas\n");
            String option;
            // if the user gives invalid info then ask again
            do {
                System.out.print("> ");
                Scanner inp = new Scanner(System.in);
                option = inp.nextLine().toLowerCase();
                if (option.equals("exit")) {
                    running = false;
                    break;
                } else if (!option.equals("1") && !option.equals("2"))
                    System.out.println("[-] Option " + option + " not found.");
            } while (!option.equals("1") && !option.equals("2"));
        }
    }

}
