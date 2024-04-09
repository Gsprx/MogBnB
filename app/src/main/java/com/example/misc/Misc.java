package com.example.misc;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Misc {

    /**
     * Reads a date input from the user, ensuring the format is valid. Allows for blank input to indicate no preference.
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

}