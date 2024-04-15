package com.example.mogbnb;

import com.example.misc.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Master-Client Server handles requests from Clients-Users
public class MasterClient extends Thread {
    private int numOfWorkers;
    public MasterClient(int numOfWorkers) {
        this.numOfWorkers = numOfWorkers;
    }
    public void run(){
        ServerSocket clientServer = null;
        try {
            clientServer = new ServerSocket(Config.USER_MASTER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            try {
                Socket socket = clientServer.accept();
                Thread t = new MasterClientThread(socket, numOfWorkers);
                t.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
