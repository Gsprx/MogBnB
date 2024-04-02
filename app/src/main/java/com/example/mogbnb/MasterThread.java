package com.example.mogbnb;

import com.example.dummy.Manager;
import com.example.misc.Config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MasterThread extends Thread {
    ObjectOutputStream out;
    ObjectInputStream in;
    int inputID;
    Object inputValue;

    public MasterThread(Socket socket) throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void run() {
        // handle
        try {
            inputID = in.readInt();
            inputValue = in.readObject();

            switch (inputID) {
                case 1:
                    showRooms();
                    break;
                case 2:
                    break;
                default:
                    break;
            }

            // case 2: add room
            if (inputID == MasterFunction.ADD_ROOM.getEncoded()) {
                Room r = (Room) in.readObject();
                Master.TEMP_ROOM_DAO.add(r);
                out.writeInt(MasterFunction.ADD_ROOM.getEncoded());
                out.writeObject(null);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //  ------------------------      WORK FUNCTIONS      ----------------------------------

    // mapID: 1
    private void showRooms() {
        synchronized (Master.INPUT_IDs) {
            String mapID = "manager_show" + Master.INPUT_IDs.get(MasterFunction.SHOW_ROOMS.getEncoded());
            // increment
            Master.INPUT_IDs.merge(MasterFunction.SHOW_ROOMS.getEncoded(), 1, Integer::sum);

            for (int i = 1; i <= Config.NUM_OF_WORKERS; i++) {
                sendRequest(mapID, null, i);
            }
        }
    }

    // mapID: 2
    private void addRoom(){

    }

    /**
     * Function used to send request of the work needed to be done by the workers.
     */
    private void sendRequest(String mapID, Object mapValue, int workerIndex) {
        try {
            Socket workerSocket = new Socket("localhost", Config.INIT_WORKER_PORT + workerIndex);
            ObjectOutputStream workerOut = new ObjectOutputStream(workerSocket.getOutputStream());

            // send mapID
            workerOut.writeObject(mapID);
            // send data
            workerOut.writeObject(mapValue);
            workerOut.flush();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

