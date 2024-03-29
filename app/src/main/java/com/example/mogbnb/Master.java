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

public class Master {

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
    }

    /**
     * Start the master server.
     * Waits for requests and establishes connections.
     */
    public void openServer() {
        try {
            // create the server socket
            server = new ServerSocket(Config.PORT);

            while (true) {
                // accept the connection
                socket = server.accept();

                // handle
                try {
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    TCPObjectHolder input = (TCPObjectHolder) in.readObject();

                    // case 0: show rooms
                    if (input.code == MasterFunction.SHOW_ROOMS.getEncoded()) {
                        out.writeObject(new TCPObjectHolder(MasterFunction.SHOW_ROOMS.getEncoded(), TEMP_ROOM_DAO));
                    }
                    // case 1: add room
                    else if (input.code == MasterFunction.ADD_ROOM.getEncoded()) {
                        Room r = (Room) input.obj;
                        TEMP_ROOM_DAO.add(r);
                        out.writeObject(new TCPObjectHolder(MasterFunction.ADD_ROOM.getEncoded(), null));
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
        Master master = new Master(Integer.parseInt(args[0]));
        master.openServer();
    }
}
