package com.example.mogbnb;

import com.example.dummy.Manager;
import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Worker extends Thread {
    private ArrayList<Room> roomData;
    private static int classID;
    private int id;

    public Worker(){
        roomData = new ArrayList<>(Master.TEMP_ROOM_DAO);
        classID++;
        id = classID;
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
            serverSocket = new ServerSocket(Config.INIT_WORKER_PORT + id);


            //working loop
            while(true){
                receiverSocket = serverSocket.accept();

                // TODO: EDW LOCKAREI KAI DEN PREPEI (TSEKARE ERGASTHRIA KWDIKA)
                in = new ObjectInputStream(receiverSocket.getInputStream());

                //
                // Reads the mapID to determine whether to use local functions or push the workload to threads
                //
                int mapID = in.readInt();

                // Function 2: add room
                if (mapID==MasterFunction.ADD_ROOM.getEncoded()) {
                    addRoom((Room) in.readObject());
                }

                Thread workThread = new WorkerThread(mapID, (Room) in.readObject(), this.roomData);
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
