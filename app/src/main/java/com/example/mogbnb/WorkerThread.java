package com.example.mogbnb;

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
     * (NOTE) mapIDs: 2 - searchRooms
     *                3 -
     */
    public void run(){
        switch (mapID){
            case 2: {
                searchRooms();
                break;
            }
            case 3:{
                break;
            }
            default:{
                throw new RuntimeException("Error! Invalid MapID");
            }
        }

    }

    //  ------------------------      WORK FUNCTIONS      ----------------------------------


    // mapID: 2
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
            Socket outputSocket = new Socket("localhost", 6000);

            //get the output stream to send results
            ObjectOutputStream out = new ObjectOutputStream(outputSocket.getOutputStream());

            //flush mapID
            out.writeInt(mapID);
            out.flush();
            //flush rooms array list
            out.writeObject(resultRooms);
            out.flush();


        } catch (IOException e) {
           e.printStackTrace();
        }
    }
}
