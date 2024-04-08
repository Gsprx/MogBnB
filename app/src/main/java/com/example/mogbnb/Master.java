package com.example.mogbnb;

import com.example.misc.Config;
import com.example.misc.JsonConverter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class Master extends Thread {

    // load the existing rooms from json
    private final List<Room> ROOMS_FROM_JSON = JsonConverter.deserializeRooms("app/src/test/java/com/example/mogbnb/exampleInput.json");

    // The master is going to send requests to the workers.
    // It needs to keep track of the requests it sends.
    public static final HashMap<Integer, Integer> INPUT_IDs = new HashMap<>();

    int numOfWorkers;

    /* Socket for receiving requests */
    Socket socket = null;

    /* Socket for handling the connection */
    ServerSocket server;
    ServerSocket reducerListener;

    public Master(int numOfWorkers) {
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
            // create the server socket
            server = new ServerSocket(Config.USER_MASTER_PORT);

            // create the reducer listener server socket
            reducerListener = new ServerSocket(Config.REDUCER_MASTER_PORT);

            // load the rooms to the workers
            for (Room r : ROOMS_FROM_JSON) {
                int workerIndex = (int) (Master.hash(r.getRoomName()) % numOfWorkers) + 1;
                Socket loadToWorker = new Socket(Config.WORKER_IP[workerIndex-1], Config.INIT_WORKER_PORT + workerIndex);
                ObjectOutputStream loadToWorkerOut = new ObjectOutputStream(loadToWorker.getOutputStream());
                loadToWorkerOut.writeObject("manager_add");
                loadToWorkerOut.writeObject(r);
                loadToWorkerOut.flush();
                loadToWorkerOut.close();
                loadToWorker.close();
            }

            // listen for requests from user
            while (true) {
                // accept the connection for user-master
                socket = server.accept();

                Thread t = new MasterThread(socket, reducerListener, numOfWorkers);
                t.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int getNumOfWorkers() {
        return numOfWorkers;
    }

    /**
     * Hash function.
     * @param s String to hash
     * @return Result of hashing
     */
    public static long hash(String s) {
        long hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*11 + s.charAt(i);
        }
        System.out.println(hash);
        return hash;
    }


}
