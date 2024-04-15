package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


// Master-Reducer Server handles replies from Reducer Node
public class MasterReducer extends Thread{
    public void run(){
        ServerSocket reducerServer = null;
        try {
            reducerServer = new ServerSocket(Config.REDUCER_MASTER_PORT);

            while(true){
                Socket reducerSocket = reducerServer.accept();
                Thread t = new MasterReducerThread(reducerSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
