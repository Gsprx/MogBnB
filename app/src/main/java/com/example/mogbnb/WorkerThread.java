package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Threads used by workers to handle multithreaded workloads
 * The worker thread is mewing and looksmaxxing even after being called for labor
 */
public class WorkerThread extends Thread{
    private Socket socket = null;
    private Object mapValue;
    private int mapID;
    private final ArrayList<Room> rooms;

    public WorkerThread(Socket socket, ArrayList<Room> rooms) {
        this.socket = socket;
        this.rooms = rooms;
    }

    /**
     * Used by threads to run their different functions based on mapID
     * (NOTE) mapIDs: 1 - return all rooms
     *                2 - add a room
     *                3 - search rooms
     *                4 - return specific room based on room's name
     */
    public void run(){
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            mapID = in.readInt();
            mapValue = in.readObject();
            switch (mapID){
                case 1: {
                    showRooms();
                    break;
                }
                case 2:{
                    addRoom();
                    break;
                }
                case 3: {
                    searchRooms();
                    break;
                }
                case 4:{
                    findRoomByName();
                    break;
                }
                default:{
                    throw new RuntimeException("Error! Invalid MapID");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    //  ------------------------      WORK FUNCTIONS      ----------------------------------

    // mapID: 1
    // expected mapValue is null
    private void showRooms(){
        ArrayList<Room> result = new ArrayList<>(rooms);
        sendResults(result);
    }


    // mapID: 2
    // expected mapValue is a Room Object
    private void addRoom(){
        synchronized (rooms) {
            this.rooms.add((Room) mapValue);
        }
    };


    // mapID: 3
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

    //mapID: 4
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

    /**
     * Function used to send the output of the work completed to the reducer node.
     */
    private void sendResults(ArrayList<Room> resultRooms){
        try {
            //socket used to send results to reducer
            Socket outputSocket = new Socket("localhost", Config.WORKER_REDUCER_PORT);

            //get the output stream to send results
            ObjectOutputStream out = new ObjectOutputStream(outputSocket.getOutputStream());

            //write mapID
            out.writeInt(mapID);
            //write rooms array list
            out.writeObject(resultRooms);
            out.flush();


        } catch (IOException e) {
           e.printStackTrace();
        }
    }
}
