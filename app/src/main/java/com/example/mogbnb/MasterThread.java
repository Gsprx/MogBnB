package com.example.mogbnb;

import com.example.dummy.Manager;
import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MasterThread extends Thread {
    ObjectOutputStream out;
    ObjectInputStream in;
    int inputID;
    Object inputValue;
    ServerSocket reducerListener;

    public MasterThread(Socket socket, ServerSocket reducerListener) throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.reducerListener = reducerListener;
    }

    public void run() {
        // handle
        try {
            inputID = in.readInt();
            inputValue = in.readObject();

            switch (inputID) {
                case 1:
                    showRooms();
                    break;
                case 2:
                    addRoom();
                    break;
                case 3:
                    bookingsPerArea();
                    break;
                default:
                    break;
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //  ------------------------      WORK FUNCTIONS      ----------------------------------

    /**
     * Show rooms | inputID : 1
     * - Create mapID
     * - Send request to each worker
     * - Wait for response from reducer
     * - Answer to user
     */
    private void showRooms() {
        // send mapID to workers
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "manager_show_" + Master.INPUT_IDs.get(MasterFunction.SHOW_ROOMS.getEncoded());
            // increment
            Master.INPUT_IDs.merge(MasterFunction.SHOW_ROOMS.getEncoded(), 1, Integer::sum);
        }
        // send to each worker
        for (int i = 1; i <= Config.NUM_OF_WORKERS; i++) {
            sendRequest(mapID, null, i);
        }

        try {
            // listen for reply from reducer
            Socket reducerResultSocket = reducerListener.accept();

            // read from reducer
            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            String mapIdResult = (String) reducer_in.readObject();
            ArrayList<Room> roomsResult = (ArrayList<Room>) reducer_in.readObject();

            // write to user
            out.writeObject(mapIdResult);
            out.writeObject(roomsResult);
            out.flush();

            // close
            reducer_in.close();
            reducerResultSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add room | inputID : 2
     * - Create mapID
     * - Hash input value
     * - Assign the right worker
     * - Send request to worker
     */
    private void addRoom(){
        // send mapID to workers
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "manager_add_" + Master.INPUT_IDs.get(MasterFunction.ADD_ROOM.getEncoded());
            // increment
            Master.INPUT_IDs.merge(MasterFunction.ADD_ROOM.getEncoded(), 1, Integer::sum);
        }

        // get the worker we need to out to using hash function
        Room r = (Room) inputValue;
        int workerIndex = (Master.hash(r.getRoomName()) % Config.NUM_OF_WORKERS) + 1;

        // send to worker
        sendRequest(mapID, r, workerIndex);
    }

    /**
     * Show bookings per area for a given time period | inputID : 3
     * - Create mapID
     * - Send request to each worker
     * - Wait for response from reducer
     * - Answer to user
     */
    private void bookingsPerArea() {
        // send mapID to workers
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "manager_area_bookings_" + Master.INPUT_IDs.get(MasterFunction.BOOKINGS_PER_AREA.getEncoded());
            // increment
            Master.INPUT_IDs.merge(MasterFunction.BOOKINGS_PER_AREA.getEncoded(), 1, Integer::sum);
        }
        // send to each worker
        for (int i = 1; i <= Config.NUM_OF_WORKERS; i++) {
            sendRequest(mapID, inputValue, i);
        }

        try {
            // listen for reply from reducer
            Socket reducerResultSocket = reducerListener.accept();

            // read from reducer
            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            String mapIdResult = (String) reducer_in.readObject();
            HashMap<String, Integer> result = (HashMap<String, Integer>) reducer_in.readObject();

            // write to user
            out.writeObject(mapIdResult);
            out.writeObject(result);
            out.flush();

            //close
            reducer_in.close();
            reducerResultSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function used to send request of the work needed to be done by the workers.
     * @param mapID The mapID value
     * @param mapValue The object passed
     * @param workerIndex The worker we are passing to
     */
    private void sendRequest(String mapID, Object mapValue, int workerIndex) {
        try {
            Socket workerSocket = new Socket("localhost", Config.INIT_WORKER_PORT + workerIndex);
            ObjectOutputStream workerOut = new ObjectOutputStream(workerSocket.getOutputStream());

            // send mapID
            workerOut.writeObject(mapID);
            // send data
            workerOut.writeObject(mapValue);
            workerOut.flush();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

