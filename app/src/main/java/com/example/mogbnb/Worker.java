package com.example.mogbnb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Worker extends Thread{
    private ArrayList<Room> roomData;
    private static int classID;

    public Worker(){
        roomData = new ArrayList<>();
        classID++;
    }

    /**
     * Main function after starting a thread, the worker node listens actively for map messages from the Master node,
     * and uses WorkerThreads for the workload given to it.
     * If Worker gets a mapID of 1 (a.k.a. Add a room) then it does the work itself, otherwise it uses worker threads.
     */
    public void run(){
        //socket used to receive calls from master
        Socket receiverSocket;
        //socket used to handle the tcp connection as a server of master
        ServerSocket serverSocket;


        //object streams
        ObjectInputStream in;
        try {
            //start a server socket to receive calls from master
            serverSocket = new ServerSocket(5000 + classID);


            //working loop
            while(true){
                receiverSocket = serverSocket.accept();

                in = new ObjectInputStream(receiverSocket.getInputStream());

                //
                // Reads the mapID to determine whether to use local functions or push the workload to threads
                //
                int mapID = in.readInt();

                if(mapID==1){
                    addRoom((Room)in.readObject());
                }
                Thread workThread = new WorkerThread(mapID, (Room)in.readObject(), this.roomData);
                workThread.start();
            }

        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            e.printStackTrace();
        }
    }
    private void addRoom(Room room){
        this.roomData.add(room);
    };

}
