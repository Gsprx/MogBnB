package com.example.mogbnb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
/**
 * Utility class for converting JSON data to Java objects using Gson.
 */
public class JsonConverter {
    /**
     * Deserialize JSON data from the specified file path into a list of Room objects.
     *
     * @param jsonFilePath The file path of the JSON file to deserialize.
     * @return A list of Room objects deserialized from the JSON data, or null if an error occurs.
     */
    public static List<Room> deserializeRooms(String jsonFilePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Type roomListType = new TypeToken<List<Room>>() {}.getType();
            return gson.fromJson(reader, roomListType);
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
            }
        }
    }
}
