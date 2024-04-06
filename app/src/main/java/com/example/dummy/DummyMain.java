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
    /**
     * Main class for managing tenant and manager operations in a room booking application.
     */
    public static void main(String[] args) {
        String acc_typ;

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
        else {
            Tenant tenant = new Tenant(1);
            tenant.runTenant();
        }
    }


    /**
     * Reads a date input from the user, ensuring the format is valid. Allows for blank input to indicate no preference.
     * @return A LocalDate object if a valid date is entered, or null if no date is specified.
     */
    public static LocalDate readDate() {
        LocalDate date = null;
        while (true) {
            Scanner inp = new Scanner(System.in);
            String input = inp.nextLine().trim();
            if (input.isEmpty()) {
                // No date entered, return null to indicate no preference
                return null;
            } else {
                try {
                    date = LocalDate.parse(input); // Try to parse the input
                    break; // Break the loop if parsing is successful
                } catch (DateTimeParseException e) {
                    System.out.print("[-]Invalid date format. Please enter a date in YYYY-MM-DD format or leave blank for no preference: ");
                }
            }
        }
        return date;
    }
}



