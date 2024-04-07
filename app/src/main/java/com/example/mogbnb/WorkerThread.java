package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Threads used by workers to handle multithreaded workloads
 * The worker thread is mewing and looksmaxxing even after being called for labor
 */
public class WorkerThread extends Thread {
    ObjectInputStream in;
    private Object mapValue;
    private String mapID;
    private ArrayList<Room> rooms;
    private int workerID;

    public WorkerThread(Socket socket, ArrayList<Room> rooms, int id) {
        this.workerID = id;
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
     * (NOTE) mapIDs: manager_show_x - return all rooms
     *                manager_add_x - add a room
     *                manager_bookings_of_room_x - return all bookings of a room
     *                manager_area_bookings_x - return bookings per area for a time period
     *                tenant_search_x - search rooms
     *                tenant_rate_x - rate a specific room
     *                tenant_book_x - make a reservation for a specific room
     *                find_x - return specific room based on room's name
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
        } else if(mapID.contains("tenant_rate")){
            rateRoom();
        } else if (mapID.contains("tenant_book")) {
            bookRoom();
        } else if (mapID.contains("manager_bookings_of_room")) {
            showBookingsOfRoom();
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
            Room room = (Room) mapValue;
            for (Room r : rooms) {
                if (r.equals(room)) {
                    System.out.println(workerID + ": " + "Room \"" + r.getRoomName() + "\" already exists");
                    return;
                }
            }
            this.rooms.add(room);
            System.out.println(workerID + ": " + (Room) mapValue);
        }
    };


    // expected mapValue is a Filter object
    private void searchRooms(){
        ArrayList<Room> result = new ArrayList<>();
        Filter filter = (Filter) mapValue;
        for (Room room : rooms){
            if (room.filterAccepted(filter)){
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

    // expected mapValue is an ArrayList [start, end] local dates
    private void areaBookings() {
        HashMap<String,Integer> areaResults = new HashMap<>();
        ArrayList<LocalDate> dates = (ArrayList<LocalDate>) mapValue;
        LocalDate start = dates.get(0);
        LocalDate end = dates.get(1);

        for(Room r : rooms){
            areaResults.merge(r.getArea(), r.totalDaysBooked(start, end), Integer::sum);
        }

        System.out.println(areaResults.size());
        sendResults(areaResults);
    }

    // expected mapValue is a String (roomName)
    private void showBookingsOfRoom() {
        String roomName = (String) mapValue;
        ArrayList<String> bookings = new ArrayList<>();
        for (Room r : rooms) {
            if (r.getRoomName().equals(roomName)) {
                // iterate through the bookingTable
                int u_id = 0; // user id
                LocalDate checkInH = null; // checkIn holder
                LocalDate checkOutH = null; // checkOut holder
                for (int i=0; i<r.getAvailableDays(); i++) {
                    // if we find != user_id, that means we found the beginning of a reservation
                    if (r.getBookingTable()[i] != u_id) {
                        // if the current bookingTable value is not 0 or the index is not 0, meaning its not the first day or the first day after the room was not occupied
                        if (u_id != 0 && i > 0) {
                            checkOutH = r.getCurrentDate().plusDays(i);
                            bookings.add(u_id + ": " + checkInH + " - " + checkOutH);
                        }
                        u_id = r.getBookingTable()[i];
                        checkInH = r.getCurrentDate().plusDays(i);
                    }
                }
                // if the room is booked until the last day it is available, the last reservation will not be accounted because we will not get a checkOutH
                // so when the for loop breaks we also make sure that if it was booked for a <checkIn>-<checkOut> (where the checkOut is at the last day of the room),
                // it will be added to the bookings list
                if (r.getBookingTable()[r.getAvailableDays() - 1] != 0) {
                    checkOutH = r.getCurrentDate().plusDays(r.getAvailableDays());
                    bookings.add(r.getBookingTable()[r.getAvailableDays() - 1] + ": " + checkInH + " - " + checkOutH);
                }
            }
        }

        sendResults(bookings);
    }

    //expected mapValue is an array [room_name, rating]
    private void rateRoom() {
        ArrayList<Object> rateInfo = (ArrayList<Object>) mapValue; // [String roomName, double rating]
        String roomName = (String) rateInfo.get(0);
        double rating = (double) rateInfo.get(1);

        int foundRoom = 0;
        for (Room r : rooms) {
            if (r.getRoomName().equalsIgnoreCase(roomName)) {
                synchronized (r) {
                    r.addReview(rating);
                    foundRoom = 1;
                }
            }
        }

        //return a verification message back to master
        sendResults(foundRoom);
    }

    //expected mapValue is an ArrayList of : [roomName, userID, checkIn, checkOut]
    private void bookRoom() {
        ArrayList<Object> data = (ArrayList<Object>) mapValue;
        String roomName = (String) data.get(0);
        int userID = (int) data.get(1);
        LocalDate checkIn = (LocalDate) data.get(2);
        LocalDate checkOut = (LocalDate) data.get(3);

        int result = 0;
        for (Room r : rooms) {
            if (r.getRoomName().equals(roomName)) {
                if (r.bookRoom(checkIn, checkOut, userID)) result = 1;
            }
        }

        //return a verification message back to master
        sendResults(result);
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
