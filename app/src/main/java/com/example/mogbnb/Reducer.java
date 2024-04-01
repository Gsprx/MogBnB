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
    HashMap<String, ArrayList<Room>> mapValueBuffer;
    private int numOfWorkers;


    public Reducer(){
        mapIDCounter = new HashMap<>();
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
                    Socket initSocket = new Socket("localhost", Config.MASTER_REDUCER_PORT);
                    ObjectInputStream initInputStream = new ObjectInputStream(initSocket.getInputStream());

                    numOfWorkers = initInputStream.readInt();

                    initInputStream.close();
                    initSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //setup the server and start while true loop
        while(true) {
            try {
                server = new ServerSocket(Config.WORKER_REDUCER_PORT);
                socket = server.accept();
                Thread t = new ReducerThread(socket, mapIDCounter, numOfWorkers, mapValueBuffer);
                t.start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
