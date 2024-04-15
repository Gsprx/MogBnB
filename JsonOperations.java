import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;

/**
 * A utility class to read a JSON file and convert its contents into an array of Room objects.
 */
public class JsonFileToArrayConverter {

    public static void main(String[] args) {
        String filePath = "example.json"; // Path to your JSON file
        try {
            Room[] rooms = convertJsonFileToArray(filePath);
            for (Room room : rooms) {
                System.out.println(room);
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
    }

    /**
     * Reads a JSON file and converts its contents into an array of Room objects.
     * @param filePath The path to the JSON file.
     * @return The array of Room objects parsed from the JSON file.
     * @throws IOException If an I/O error occurs while reading the JSON file.
     */
    public static Room[] convertJsonFileToArray(String filePath) throws IOException {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(filePath));
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        Room[] rooms = new Room[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            rooms[i] = gson.fromJson(jsonArray.get(i), Room.class);
        }

        return rooms;
    }
}

/**
 * A class representing a room object.
 */
class Room {
    private String roomName;
    private int noOfPersons;
    private String area;
    private int stars;
    private int noOfReviews;
    private String roomImage;

    // Getters and setters (You can generate them using your IDE or write them manually)

    @Override
    public String toString() {
        return "Room{" +
                "roomName='" + roomName + '\'' +
                ", noOfPersons=" + noOfPersons +
                ", area='" + area + '\'' +
                ", stars=" + stars +
                ", noOfReviews=" + noOfReviews +
                ", roomImage='" + roomImage + '\'' +
                '}';
    }
}
