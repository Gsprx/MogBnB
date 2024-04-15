package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


// Master-Reducer Server handles replies from Reducer Node
public class MasterReducer extends Thread{
    public void run(){
        ServerSocket reducerServer;
        try {
            reducerServer = new ServerSocket(Config.REDUCER_MASTER_PORT);

            while(true){
                Socket reducerSocket = reducerServer.accept();
                System.out.println("[Master-Reducer] accepted socket: " + reducerSocket.toString());
                Thread t = new MasterReducerThread(reducerSocket);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
