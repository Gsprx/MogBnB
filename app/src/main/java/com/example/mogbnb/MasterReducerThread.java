package com.example.mogbnb;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MasterReducerThread extends Thread{
    private Socket reducerSocket;
    public MasterReducerThread(Socket reducerSocket){
        this.reducerSocket = reducerSocket;
    }
    public void run(){
        try{
            ObjectInputStream in = new ObjectInputStream(reducerSocket.getInputStream());
            String mapID = (String) in.readObject();

            //get user socket using unique map id to send output to user
            synchronized (Master.userSockets){
                Socket replySocket = Master.userSockets.get(mapID);
                ObjectOutputStream out = new ObjectOutputStream(replySocket.getOutputStream());
                out.writeObject(in.readObject());
            }

        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
