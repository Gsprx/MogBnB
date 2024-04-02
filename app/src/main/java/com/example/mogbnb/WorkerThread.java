package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Threads used by workers to handle multithreaded workloads
 * The worker thread is mewing and looksmaxxing even after being called for labor
 */
public class WorkerThread extends Thread {
    ObjectInputStream in;
    private final Object mapValue;
    private final String mapID;
    private final ArrayList<Room> rooms;

    public WorkerThread(Socket socket, ArrayList<Room> rooms) {
        this.rooms = rooms;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            mapID = (String) in.readObject();
            mapValue = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used by threads to run their different functions based on mapID.
     * <p>
     * (NOTE) mapIDs: manager_show_x_ - return all rooms
     *                manager_add_x_ - add a room
     *                manager_show_bookings_x - return all bookings
     *                manager_area_bookings_x - return bookings per area for a time period
     *                tenant_search_x_ - search rooms
     *                find_x_ - return specific room based on room's name
     */
    // TODO: function manager_booking_areas
    public void run() {
        if(mapID.contains("manager_show")){
            showRooms();
        } else if (mapID.contains("manager_add")) {
            addRoom();
        } else if (mapID.contains("tenant_search")) {
            searchRooms();
        } else if (mapID.contains("find")) {
            findRoomByName();
        } else if (mapID.contains("manager_area_bookings")) {
            areaBookings();
        }
    }

    //  ------------------------      WORK FUNCTIONS      ----------------------------------

    // expected mapValue is null
    private void showRooms(){
        ArrayList<Room> result = new ArrayList<>(rooms);
        sendResults(result);
    }


    // expected mapValue is a Room Object
    private void addRoom(){
        synchronized (rooms) {
            this.rooms.add((Room) mapValue);
        }
    };


    // expected mapValue is a Filter object
    private void searchRooms(){
        ArrayList<Room> result = new ArrayList<>();
        Filter filter = (Filter) mapValue;
        for (Room room : rooms){
            if(room.filterAccepted(filter)){
                result.add(room);
            }
        }
        sendResults(result);
    }

    // expected mapValue is a String
    private void findRoomByName(){
        ArrayList<Room> result = new ArrayList<>();
        String queryRoomName = (String) mapValue;
        for (Room room : rooms){
            if (room.getRoomName().equalsIgnoreCase(queryRoomName)){
                result.add(room);
                sendResults(result);
            }
        }
    }

    // expected mapValue is a Filter object
    private void areaBookings(){
        HashMap<String,Integer> areaResults = new HashMap<>();
        Filter filter = (Filter) mapValue;
        for(Room r : rooms){
            if(r.filterAccepted(filter)) {
                areaResults.merge(r.getArea(), r.totalDaysBooked(), Integer::sum);
            }
        }
        sendResults(areaResults);

    }

    /**
     * Function used to send the output of the work completed to the reducer node.
     */
    private void sendResults(Object resultForReducer){
        try {
            //socket used to send results to reducer
            Socket outputSocket = new Socket("localhost", Config.WORKER_REDUCER_PORT);

            //get the output stream to send results
            ObjectOutputStream out = new ObjectOutputStream(outputSocket.getOutputStream());

            //write mapID
            out.writeObject(mapID);
            //write rooms array list
            out.writeObject(resultForReducer);
            out.flush();


        } catch (IOException e) {
           e.printStackTrace();
        }
    }
}
