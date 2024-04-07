package com.example.mogbnb;

import com.example.dummy.Manager;
import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                case 4:
                    findRoomByName();
                    break;
                case 5:
                    searchRoom();
                    break;
                case 6:
                    rateRoom();
                    break;
                case 7:
                    showBookings();
                    break;
//                case 8:
//                    assign_user_id();
//                    break;
                case 9:
                    bookRoom();
                    break;
                case 10:
                    showBookingsOfRoom();
                    break;
                default:
                    System.out.println("Function not identified!!");
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
            HashMap<String, Integer> result = (HashMap<String, Integer>) reducer_in.readObject();

            // write to user
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

    private void findRoomByName() {
        // send mapID to workers
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "find_" + Master.INPUT_IDs.get(MasterFunction.FIND_ROOM_BY_NAME.getEncoded());
            // increment
            Master.INPUT_IDs.merge(MasterFunction.FIND_ROOM_BY_NAME.getEncoded(), 1, Integer::sum);
        }

        // get the worker we need to out to using hash function
        String roomName = (String) inputValue;
        int workerIndex = (Master.hash(roomName) % Config.NUM_OF_WORKERS) + 1;

        // send to worker
        sendRequest(mapID, roomName, workerIndex);

        try {
            // listen for reply from reducer
            Socket reducerResultSocket = reducerListener.accept();

            // read from reducer
            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            ArrayList<Room> result = (ArrayList<Room>) reducer_in.readObject();
            Room r = null;
            if (result != null) r = result.get(0);

            // write to user
            out.writeObject(r);
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

    /**
     * Tenant functionality 5
     */
    private void searchRoom(){
        // a Filter object with search criteria
        Filter searchCriteria = (Filter) inputValue;
        //
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "tenant_search_" + Master.INPUT_IDs.get(MasterFunction.SEARCH_ROOM.getEncoded());
            Master.INPUT_IDs.merge(MasterFunction.SEARCH_ROOM.getEncoded(), 1, Integer::sum);
        }
        // send to each worker
        for (int i = 1; i <= Config.NUM_OF_WORKERS; i++) {
            sendRequest(mapID, searchCriteria, i);
        }
        try {
            // Listen for a reply from the reducer
            Socket reducerResultSocket = reducerListener.accept();

            // Read from reducer
            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            ArrayList<Room> roomsResult = (ArrayList<Room>) reducer_in.readObject();

            // Write to user
            out.writeObject(roomsResult);
            out.flush();

            reducer_in.close();
            reducerResultSocket.close();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void showBookings() {
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "tenant_show_bookings_" + Master.INPUT_IDs.get(MasterFunction.SHOW_BOOKINGS.getEncoded());
            Master.INPUT_IDs.merge(MasterFunction.SHOW_BOOKINGS.getEncoded(), 1, Integer::sum);
        }

        // Requests booking information from all worker nodes.

        for (int i = 1; i <= Config.NUM_OF_WORKERS; i++) {
            sendRequest(mapID, inputValue, i);
        }

        // Wait for responses from all workers
        try {

            Socket reducerResultSocket = reducerListener.accept();

            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            HashMap<String, ArrayList<LocalDate>> bookingsResult = (HashMap<String, ArrayList<LocalDate>>) reducer_in.readObject();

            out.writeObject(bookingsResult);
            out.flush();

            reducer_in.close();
            reducerResultSocket.close();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void rateRoom() {
        ArrayList<Object> rateInfo = (ArrayList<Object>) inputValue; // [String roomName, double rating]
        String roomName = (String) rateInfo.get(0);

        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "tenant_rate_" + Master.INPUT_IDs.get(MasterFunction.RATE_ROOM.getEncoded());
            Master.INPUT_IDs.merge(MasterFunction.RATE_ROOM.getEncoded(), 1, Integer::sum);
        }

        // Hash the room name to find the correct worker
        int workerIndex = (Master.hash(roomName) % Config.NUM_OF_WORKERS) + 1;

        sendRequest(mapID, rateInfo, workerIndex);

        try {
            Socket reducerResultSocket = reducerListener.accept();

            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            int result = reducer_in.readInt();

            if (result == 1) out.writeObject("Rating updated successfully.");
            else out.writeObject("An error occured.");
            out.flush();

            reducer_in.close();
            reducerResultSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void assign_user_id() throws IOException {
//        out.writeInt(Master.USER_IDS);
//        out.flush();
//        Master.USER_IDS++;
//    }

    /**
     * Make a reservation for a room | inputID : 9
     * - Create mapID
     * - Hash input value
     * - Assign the right worker
     * - Send request
     * - Wait for response from reducer
     * - Answer to user
     */
    private void bookRoom() throws IOException, ClassNotFoundException {
        // send mapID to workers
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "tenant_book_" + Master.INPUT_IDs.get(MasterFunction.BOOK_ROOM.getEncoded());
            // increment
            Master.INPUT_IDs.merge(MasterFunction.BOOK_ROOM.getEncoded(), 1, Integer::sum);
        }

        // get the worker we need to out to using hash function
        ArrayList<Object> request = (ArrayList<Object>) inputValue;
        String roomName = (String) request.get(0);
        int workerIndex = (Master.hash(roomName) % Config.NUM_OF_WORKERS) + 1;

        // send to worker
        sendRequest(mapID, request, workerIndex);

        // wait for response
        try {
            Socket reducerResultSocket = reducerListener.accept();

            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            int result = reducer_in.readInt();

            if (result == 1) out.writeObject("Booking successful.");
            else out.writeObject("An error occured.");
            out.flush();

            reducer_in.close();
            reducerResultSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show reservations of a room | inputID : 10
     * - Create mapID
     * - Hash room name
     * - Get right worker
     * - Send request
     */
    private void showBookingsOfRoom() {
        // send mapID to workers
        String mapID;
        synchronized (Master.INPUT_IDs) {
            mapID = "manager_bookings_of_room_" + Master.INPUT_IDs.get(MasterFunction.SHOW_BOOKINGS_OF_ROOM.getEncoded());
            // increment
            Master.INPUT_IDs.merge(MasterFunction.SHOW_BOOKINGS_OF_ROOM.getEncoded(), 1, Integer::sum);
        }

        // get the worker we need to out to using hash function
        String roomName = (String) inputValue;
        int workerIndex = (Master.hash(roomName) % Config.NUM_OF_WORKERS) + 1;

        // send to worker
        sendRequest(mapID, roomName, workerIndex);

        // wait for response
        try {
            Socket reducerResultSocket = reducerListener.accept();

            ObjectInputStream reducer_in = new ObjectInputStream(reducerResultSocket.getInputStream());
            ArrayList<String> result = (ArrayList<String>) reducer_in.readObject();

            out.writeObject(result);
            out.flush();

            reducer_in.close();
            reducerResultSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

