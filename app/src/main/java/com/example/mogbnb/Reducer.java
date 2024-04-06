package com.example.mogbnb;

import com.example.misc.Config;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Reducer extends Thread {
    /* Socket for receiving requests */
    Socket socket = null;

    /* Socket for handling the connection */
    ServerSocket server;

    HashMap<String, Integer> mapIDCounter;
    HashMap<String, ArrayList<Room>> roomListBuffer; //used to hold room values from workers until all workers are finished. key = mapID
    HashMap<String, HashMap<String,Integer>> areaBookingsBuffer; //used to hold hashmaps of area-days_booked tuples until all workers are finished. key = mapID
    private int numOfWorkers;


    public Reducer(){
        mapIDCounter = new HashMap<>();
        roomListBuffer = new HashMap<>();
        areaBookingsBuffer = new HashMap<>();
    }


    public static void main(String[] args){
        Reducer reducer = new Reducer();
        reducer.start();
    }


    public void run() {
        // Get number of workers once
        if(numOfWorkers == 0) {
            synchronized ((Integer) numOfWorkers) {
                try {
                    ServerSocket initSocketServer = new ServerSocket(Config.MASTER_REDUCER_PORT);
                    Socket initSocket = initSocketServer.accept();

                    ObjectInputStream initInputStream = new ObjectInputStream(initSocket.getInputStream());

                    numOfWorkers = initInputStream.readInt();

                    initInputStream.close();
                    initSocket.close();
                    initSocketServer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //setup the server and start while true loop
        try {
            server = new ServerSocket(Config.WORKER_REDUCER_PORT);

            while (true) {
                socket = server.accept();
                Thread t = new ReducerThread(socket, mapIDCounter, numOfWorkers, roomListBuffer, areaBookingsBuffer);
                t.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
