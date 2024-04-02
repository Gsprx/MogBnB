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
    private final int id;

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

        try {
            //start a server socket to receive calls from master
            serverSocket = new ServerSocket(Config.INIT_WORKER_PORT + id);

            //working loop
            while(true){
                receiverSocket = serverSocket.accept();

                Thread workThread = new WorkerThread(receiverSocket, this.roomData);
                workThread.start();
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

}
