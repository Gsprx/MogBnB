package com.example.misc;

import com.example.mogbnb.Room;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonConverter {

    /**
     * This method takes the path to a JSON file containing room data and deserializes it into
     * a list of Room objects. Instead of directly deserializing the JSON into Room objects,
     * it first deserializes into a list of maps to manually instantiate Room objects. This ensures
     * that the Room constructor is used, preserving any logic contained within.
     *
     * @param jsonFilePath The file path of the JSON file to deserialize.
     * @return A list of Room objects deserialized from the JSON data, or null if an error occurs.
     */
    public static List<Room> deserializeRooms(String jsonFilePath) {
        Gson gson = new Gson();
        List<Room> rooms = new ArrayList<>();
        try (FileReader reader = new FileReader(jsonFilePath)) {
            // Deserialize JSON to a generic structure
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            // Deserialize the JSON file into a List of Maps. Each map corresponds to a Room object,
            // with keys representing property names and values representing property values.
            List<Map<String, Object>> roomMaps = gson.fromJson(reader, listType);

            // Iterate over each map in the list, extracting properties and using them
            // to manually instantiate Room objects via the Room constructor.
            for (Map<String, Object> roomMap : roomMaps) {
                String roomName = (String) roomMap.get("roomName");
                int noOfPersons = ((Double) roomMap.get("noOfPersons")).intValue();
                int availableDays = ((Double) roomMap.get("availableDays")).intValue();
                String area = (String) roomMap.get("area");
                double stars = (Double) roomMap.get("stars");
                int noOfReviews = ((Double) roomMap.get("noOfReviews")).intValue();
                String roomImage = (String) roomMap.get("roomImage");
                double pricePerDay = (Double) roomMap.get("pricePerDay");
                List<String> amenities = (List<String>) roomMap.get("amenities");
                String description = (String) roomMap.get("description");

                // Create a new Room object using the extracted properties and add it to the list of rooms.
                Room room = new Room(roomName, noOfPersons, availableDays, area, stars, noOfReviews, roomImage, pricePerDay, amenities, description);
                rooms.add(room);
            }
            return rooms;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Main method to demonstrate deserialization of JSON data into Room objects.
     * Prints information about each room to the console.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDir);
        List<Room> rooms = deserializeRooms("app/src/test/java/com/example/mogbnb/exampleInput.json");
        if (rooms != null) {
            for (Room room : rooms) {
                System.out.println(room.toString());
                System.out.println(room.getBookingTable()[3]);

                System.out.println("Length " + room.getBookingTable().length);
            }
        }
    }
}
