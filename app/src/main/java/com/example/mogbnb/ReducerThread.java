package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ReducerThread extends Thread{
    private ObjectInputStream in;
    private final int numOfWorkers;
    private HashMap<String, Integer> mapIDCounter;
    private HashMap<String, ArrayList<Room>> mapValueBuffer;

    public ReducerThread(Socket socket, HashMap<String, Integer> mapIDBuffer, int numOfWorkers, HashMap<String, ArrayList<Room>> mapValueBuffer){
        try {
            this.numOfWorkers = numOfWorkers;
            this.mapIDCounter = mapIDBuffer;
            this.mapValueBuffer = mapValueBuffer;
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(){
        try {
            //read map code
            String mapID = (String) in.readObject();

            //check if mapID exists already in the counter
            synchronized (mapIDCounter) {
                synchronized (mapValueBuffer) {
                    //if key exists       : increase the value of the counter related to the specific map ID by 1
                    //if key doesn't exist: add a new mapping of mapID,1
                    mapIDCounter.merge(mapID, 1, Integer::sum);

                    //if key exists       : extend the existing arraylist with the the new input
                    //if key doesn't exist: add a new mapping of mapID,inputList
                    mapValueBuffer.merge(mapID, (ArrayList<Room>) in.readObject(), (existingList, newList) -> {
                        existingList.addAll(newList);
                        return existingList;
                    });
                }
            }
            //when the counter reaches num of workers, the reducer thread can output the result of the workers
            if(mapIDCounter.get(mapID) == numOfWorkers){

                //send the final refined list to the master using the reducer -> master port
                Socket masterSocket = new Socket("localhost", Config.REDUCER_MASTER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
                out.writeObject(mapID);
                out.writeObject(mapValueBuffer.get(mapID));
                out.flush();

                //sync and remove the buffered data related to the map request after sending it to master
                synchronized (mapIDCounter){
                    synchronized (mapValueBuffer){
                        mapIDCounter.remove(mapID);
                        mapValueBuffer.remove(mapID);
                    }
                }
            }

    } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
/*
    green fn
 */
