package com.example.view;

import com.example.misc.Config;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.Room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class NetworkHandlerThread extends Thread {
    int function;
    Object data;
    public Object result;

    public NetworkHandlerThread(int function, Object data) {
        this.function = function;
        this.data = data;
        this.result = null;
    }

    @Override
    public void run() {
        switch (function) {
            case 5:
                searchRoom();
                break;
            default:
                break;
        }
    }

    private void searchRoom() {
        try {
            Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            out.writeInt(MasterFunction.SEARCH_ROOM.getEncoded());
            out.writeObject(data);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            this.result = in.readObject();

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}
