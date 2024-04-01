package com.example.mogbnb;

import com.example.misc.Config;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Reducer extends Thread {
    /* Socket for receiving requests */
    Socket socket = null;

    /* Socket for handling the connection */
    ServerSocket server;

    public void run() {
        // TODO: EDW PREPEI NA KANEIS REDUCER THREAD
        try {
            //output list from the combined inputs of the workers
            ArrayList<Room> outputRoomList;
            server = new ServerSocket(Config.WORKER_REDUCER_PORT);

            while (true) {
                // accept the connection
                socket = server.accept();

                try {
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    int mapID = in.readInt();

                    Socket replyToMaster = new Socket("localhost", Config.REDUCER_MASTER_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(replyToMaster.getOutputStream());

                    if (mapID == MasterFunction.FIND_ROOM_BY_NAME.getEncoded()) {
                        out.writeInt(mapID);
                        out.writeObject(in.readObject());
                        out.flush();
                    }

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
