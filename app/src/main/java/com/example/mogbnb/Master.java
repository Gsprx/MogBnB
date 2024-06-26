package com.example.mogbnb;

import com.example.misc.Config;
import com.example.misc.JsonConverter;
import com.example.misc.Misc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/*    Manages all parts of the system to make sure everything works together. */
public class Master extends Thread {

    // load the existing rooms from json
    private final List<Room> ROOMS_FROM_JSON = JsonConverter.deserializeRooms("app/src/test/java/com/example/mogbnb/exampleInput.json");

    // The master is going to send requests to the workers.
    // It needs to keep track of the requests it sends.
    public static final HashMap<Integer, Integer> INPUT_IDs = new HashMap<>();

    int numOfWorkers;

    public static HashMap<String, Socket> userSockets;

    public Master(int numOfWorkers) {
        userSockets = new HashMap<>();
        this.numOfWorkers = numOfWorkers;
        // add all the inputIds and set them to 0
        INPUT_IDs.put(MasterFunction.SHOW_ROOMS.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.ADD_ROOM.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.BOOKINGS_PER_AREA.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.FIND_ROOM_BY_NAME.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.SEARCH_ROOM.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.SHOW_BOOKINGS.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.RATE_ROOM.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.BOOK_ROOM.getEncoded(), 0);
        INPUT_IDs.put(MasterFunction.SHOW_BOOKINGS_OF_ROOM.getEncoded(), 0);

        userSockets = new HashMap<>();
    }
    public static void main(String[] args) {
        Master master = new Master(Integer.parseInt(args[0]));
        master.start();
    }

    /**
     * Start the master server.
     * Waits for requests and establishes connections.
     */
    public void run() {
        try {

            // load the rooms to the workers
            for (Room r : ROOMS_FROM_JSON) {
                int workerIndex = (int) (Misc.hash(r.getRoomName()) % numOfWorkers) + 1;
                Socket loadToWorker = new Socket(Config.WORKER_IP[workerIndex-1], Config.INIT_WORKER_PORT + workerIndex);
                ObjectOutputStream loadToWorkerOut = new ObjectOutputStream(loadToWorker.getOutputStream());
                loadToWorkerOut.writeObject("manager_add");
                loadToWorkerOut.writeObject(r);
                loadToWorkerOut.flush();
                loadToWorkerOut.close();
                loadToWorker.close();
            }

            Thread clientServer = new MasterClient(numOfWorkers);
            clientServer.start();

            Thread reducerServer = new MasterReducer();
            reducerServer.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumOfWorkers() {
        return numOfWorkers;
    }


}
