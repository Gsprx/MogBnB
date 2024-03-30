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
import java.util.List;

public class Master extends Thread {

    // load the existing rooms from json
    public static List<Room> TEMP_ROOM_DAO = JsonConverter.deserializeRooms("app/src/test/java/com/example/mogbnb/exampleInput.json");

    int numOfWorkers;
    ArrayList<Worker> workers;

    /* Socket for receiving requests */
    Socket socket = null;

    /* Socket for handling the connection */
    ServerSocket server;

    public Master(int numOfWorkers) {
        this.numOfWorkers = numOfWorkers;
        workers = new ArrayList<>();
        for (int i=0; i<numOfWorkers; i++) {
            workers.add(new Worker());
        }
        for (Worker w : workers)
            w.start();

        Reducer reducer = new Reducer();
        reducer.start();
    }

    /**
     * Start the master server.
     * Waits for requests and establishes connections.
     */
    public void run() {
        try {
            // create the server socket
            server = new ServerSocket(Config.USER_MASTER_PORT);

            while (true) {
                // accept the connection for user-master
                socket = server.accept();

                // handle
                try {
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    int inputID = in.readInt();

                    // case 0: show rooms
                    if (inputID == MasterFunction.SHOW_ROOMS.getEncoded()) {
                        for (int i=1; i<=numOfWorkers; i++) {
                            Socket workerSocket = new Socket("localhost", Config.INIT_WORKER_PORT + i);
                            ObjectOutputStream workerOut = new ObjectOutputStream(workerSocket.getOutputStream());

                            // send mapID
                            workerOut.writeInt(MasterFunction.SHOW_ROOMS.getEncoded());
                            // send data
                            workerOut.writeObject(null);
                            workerOut.flush();
                        }
                        ServerSocket reducer_master = new ServerSocket(Config.REDUCER_MASTER_PORT);
                        while (true) {
                            Socket answerSocket = reducer_master.accept();
                            ObjectInputStream answerIn = new ObjectInputStream(answerSocket.getInputStream());

                            int mapIDofAnswer = answerIn.readInt();
                            System.out.println("Reducer answered for request of " + mapIDofAnswer);

                            out.writeInt(mapIDofAnswer);
                            out.writeObject(answerIn.readObject());
                            out.flush();
                        }
                    }
                    // case 1: add room
                    else if (inputID == MasterFunction.ADD_ROOM.getEncoded()) {
                        Room r = (Room) in.readObject();
                        TEMP_ROOM_DAO.add(r);
                        out.writeInt(MasterFunction.ADD_ROOM.getEncoded());
                        out.writeObject(null);
                    }
                    out.flush();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
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
