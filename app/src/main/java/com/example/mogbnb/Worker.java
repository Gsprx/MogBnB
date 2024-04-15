package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Worker extends Thread {
    private ArrayList<Room> roomData;
    private final int id;

    public Worker(int id){
        roomData = new ArrayList<>();
        this.id = id;
    }

    /**
     * Main function after starting a thread, the worker node listens actively for map messages from the Master node,
     * and uses WorkerThreads for the workload given to it.
     *
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

                Thread workThread = new WorkerThread(receiverSocket, this.roomData, this.id);
                workThread.start();
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        Worker worker = new Worker(Integer.parseInt(args[0]));
        worker.start();
    }
}
