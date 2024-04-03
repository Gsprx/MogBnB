package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ReducerThread extends Thread {
    private ObjectInputStream in;
    private final int numOfWorkers;
    private HashMap<String, Integer> mapIDCounter;
    private HashMap<String, ArrayList<Room>> roomListBuffer;
    private HashMap<String, HashMap<String,Integer>> areaBookingsBuffer;

    public ReducerThread(Socket socket, HashMap<String, Integer> mapIDBuffer, int numOfWorkers, HashMap<String, ArrayList<Room>> mapValueBuffer, HashMap<String, HashMap<String,Integer>> areaBookings) {
        try {
            this.areaBookingsBuffer = areaBookings;
            this.numOfWorkers = numOfWorkers;
            this.mapIDCounter = mapIDBuffer;
            this.roomListBuffer = mapValueBuffer;
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            //read map code
            String mapID = (String) in.readObject();
            if (mapID.contains("manager_area_bookings")){
                areaBookingReduce(mapID);

            }else if(mapID.contains("tenant_rate")){
                messageReduce();
            }
            else{
                roomReduce(mapID);
            }

    }catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    //the main reducing call, output is an array list of rooms sent to the master
    private void roomReduce(String mapID){
        try {
            //check if mapID exists already in the counter
            synchronized (mapIDCounter) {
                synchronized (roomListBuffer) {
                    //if key exists       : increase the value of the counter related to the specific map ID by 1
                    //if key doesn't exist: add a new mapping of mapID,1
                    mapIDCounter.merge(mapID, 1, Integer::sum);

                    //if key exists       : extend the existing arraylist with the the new input
                    //if key doesn't exist: add a new mapping of mapID,inputList
                    roomListBuffer.merge(mapID, (ArrayList<Room>) in.readObject(), (existingList, newList) -> {
                        existingList.addAll(newList);
                        return existingList;
                    });
                }
            }
            //when the counter reaches num of workers, the reducer thread can output the result of the workers
            if (mapIDCounter.get(mapID) == numOfWorkers) {
                //send the final refined list to the master using the reducer -> master port
                Socket masterSocket = new Socket("localhost", Config.REDUCER_MASTER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
                out.writeObject(mapID);
                out.writeObject(roomListBuffer.get(mapID));
                out.flush();

                //sync and remove the buffered data related to the map request after sending it to master
                synchronized (mapIDCounter) {
                    synchronized (roomListBuffer) {
                        mapIDCounter.remove(mapID);
                        roomListBuffer.remove(mapID);
                    }
                }
            }
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //used to reduce queries related to per area bookings reducing since the output is different from room related reducing
    private void areaBookingReduce(String mapID) {
        try{
            synchronized (mapIDCounter){
                synchronized (areaBookingsBuffer){
                    //if key exists       : increase the value of the counter related to the specific map ID by 1
                    //if key doesn't exist: add a new mapping of mapID,1
                    mapIDCounter.merge(mapID, 1, Integer::sum);

                    HashMap<String,Integer> inputHMap = (HashMap<String,Integer>)in.readObject();
                    HashMap<String,Integer> existingHMap = areaBookingsBuffer.get(mapID);

                    //merge existing area values with new input
                    //if existing values don't exist, initiate them with the input
                    if(existingHMap!= null) {
                        for (String area : inputHMap.keySet()) {
                            existingHMap.merge(area, inputHMap.get(area), Integer::sum);
                        }
                    }
                    else{
                        areaBookingsBuffer.put(mapID,inputHMap);
                    }
                }
            }

            //when the counter reaches num of workers, the reducer thread can output the result of the workers
            if (mapIDCounter.get(mapID) == numOfWorkers) {
                //send the final refined list to the master using the reducer -> master port
                Socket masterSocket = new Socket("localhost", Config.REDUCER_MASTER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
                out.writeObject(mapID);
                out.writeObject(areaBookingsBuffer.get(mapID));
                out.flush();

                //sync and remove the buffered data related to the map request after sending it to master
                synchronized (mapIDCounter) {
                    synchronized (areaBookingsBuffer) {
                        mapIDCounter.remove(mapID);
                        areaBookingsBuffer.remove(mapID);
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //used to send a simple true or false message back to the master as the reduce result
    private void messageReduce(){
        try {
            Socket masterSocket = new Socket("localhost", Config.REDUCER_MASTER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
            out.writeBoolean(in.readBoolean());
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

 /*
    green fn
 */
