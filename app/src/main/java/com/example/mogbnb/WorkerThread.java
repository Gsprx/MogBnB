package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Threads used by workers to handle multithreaded workloads
 * The worker thread is mewing and looksmaxxing even after being called for labor
 */
public class WorkerThread extends Thread{
    private final int mapID;
    private final Object mapValue;
    private final ArrayList<Room> rooms;

    public WorkerThread(int mapID, Object mapValue, ArrayList<Room> rooms) {
        this.mapID = mapID;
        this.mapValue = mapValue;
        this.rooms = rooms;
    }

    /**
     * Used by threads to run their different functions based on mapID
     * (NOTE) mapIDs: 1 - return all rooms
     *                3 - searchRooms
     *                4 -
     */
    public void run(){
        switch (mapID){
            case 1:
                showRooms();
                break;
            case 3: {
                searchRooms();
                break;
            }
            case 4:{
                break;
            }
            default:{
                throw new RuntimeException("Error! Invalid MapID");
            }
        }

    }

    //  ------------------------      WORK FUNCTIONS      ----------------------------------

    // mapID: 1
    // expected mapValue is null
    private void showRooms(){
        ArrayList<Room> result = new ArrayList<>(rooms);
        sendResults(result);
    }

    // mapID: 3
    // expected mapValue is a Filter object
    private void searchRooms(){
        ArrayList<Room> result = new ArrayList<>();
        for (Room room : rooms){
            if(room.filterAccepted((Filter) mapValue)){
                result.add(room);
            }
        }
        sendResults(result);
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

            //flush mapID
            out.writeInt(mapID);
            //flush rooms array list
            out.writeObject(resultRooms);
            out.flush();


        } catch (IOException e) {
           e.printStackTrace();
        }
    }
}
