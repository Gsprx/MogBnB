package com.example.mogbnb;

import com.example.dummy.Manager;
import com.example.misc.Config;
import com.example.misc.JsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Master extends Thread {

    // load the existing rooms from json
    public static List<Room> TEMP_ROOM_DAO = JsonConverter.deserializeRooms("app/src/test/java/com/example/mogbnb/exampleInput.json");

    // The master is going to send requests to the workers.
    // It needs to keep track of the requests it sends.
    public static final HashMap<Integer, Integer> INPUT_IDs = new HashMap<>();

    int numOfWorkers;
    ArrayList<Worker> workers;

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

        workers = new ArrayList<>();
        for (int i=0; i<numOfWorkers; i++) {
            workers.add(new Worker());
        }
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

            // start the workers
            for (Worker w : workers)
                w.start();

            // start the reducer
            Reducer reducer = new Reducer();
            reducer.start();
            // pass the num of workers to the reducer
            Socket initToReducer = new Socket("localhost", Config.MASTER_REDUCER_PORT);
            ObjectOutputStream outNoOfWorkers = new ObjectOutputStream(initToReducer.getOutputStream());
            outNoOfWorkers.writeInt(numOfWorkers);
            outNoOfWorkers.flush();
            outNoOfWorkers.close();
            initToReducer.close();

            while (true) {
                // accept the connection for user-master
                socket = server.accept();

                Thread t = new MasterThread(socket, reducerListener);
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

    public ArrayList<Worker> getWorkers() {
        return workers;
    }

    public int getNumOfWorkers() {
        return numOfWorkers;
    }


    public static void main(String[] args) {
        Master master = new Master(Config.NUM_OF_WORKERS);
        master.start();
    }
}
