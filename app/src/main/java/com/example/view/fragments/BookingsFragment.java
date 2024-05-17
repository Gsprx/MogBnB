package com.example.view.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.NetworkHandlerThread;
import com.example.view.recyclerViewAdapters.BookedRoomRVAdapter;

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

        // get the bookings of this tenant
        // send request to master and wait for response
        NetworkHandlerThread t = new NetworkHandlerThread(MasterFunction.SHOW_BOOKINGS.getEncoded(), this.userID);
        t.start();

        while (true) {
            if (t.result != null) break;
        }
        bookings = (HashMap<Room, ArrayList<LocalDate>>) t.result;

        // build the recycler view
        recyclerView = view.findViewById(R.id.rvBookings);
        BookedRoomRVAdapter adapter = new BookedRoomRVAdapter(this, bookings);

        //bind adapter to RV
        recyclerView.setAdapter(adapter);

        //set layout for RV
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return view;
    }
}