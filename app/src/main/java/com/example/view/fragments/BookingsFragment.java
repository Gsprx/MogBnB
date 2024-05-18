package com.example.view.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.misc.Config;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.NetworkHandlerThread;
import com.example.view.recyclerViewAdapters.BookedRoomRVAdapter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class BookingsFragment extends Fragment {
    int userID;
    RecyclerView recyclerView;
    HashMap<Room, ArrayList<LocalDate>> bookings;
    public BookingsFragment(String id) {
        this.userID = Integer.parseInt(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        // build the recycler view
        recyclerView = view.findViewById(R.id.rvBookings);
        BookedRoomRVAdapter adapter = new BookedRoomRVAdapter(this, bookings);

        //bind adapter to RV
        recyclerView.setAdapter(adapter);

        //set layout for RV
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // run background thread to acquire booked rooms and update rv adapter
        new Thread(() -> {
            // connect to master and send user id
            try {
                Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeInt(MasterFunction.SHOW_BOOKINGS.getEncoded());
                out.writeObject(userID);
                out.flush();

                // wait for master to return rooms
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                bookings = (HashMap<Room,ArrayList<LocalDate>>) in.readObject();
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }

            //update adapter with the rooms returned
            requireActivity().runOnUiThread(() -> {
                adapter.setBookings(bookings);
                adapter.notifyDataSetChanged();
            });
        }).start();

        return view;
    }
}