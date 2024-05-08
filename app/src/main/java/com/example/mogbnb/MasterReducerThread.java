package com.example.mogbnb;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
// focuses on putting together all the data that comes in related to one task.
public class MasterReducerThread extends Thread{
    private Socket reducerSocket;
    public MasterReducerThread(Socket reducerSocket){
        this.reducerSocket = reducerSocket;
    }
    public void run(){
        try {
            ObjectInputStream in = new ObjectInputStream(reducerSocket.getInputStream());
            String mapID = (String) in.readObject();

            //get user socket using unique map id to send output to user
            Socket replySocket;
            synchronized (Master.userSockets) {
                replySocket = Master.userSockets.remove(mapID);
            }
            System.out.println("[Master-Reducer] obtained socket " + replySocket.toString() + " to reply for map id:" + mapID);
            ObjectOutputStream out = new ObjectOutputStream(replySocket.getOutputStream());
            out.writeObject(in.readObject());

        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
