package com.example.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.misc.Config;
import com.example.mogbnb.Filter;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.recyclerViewAdapters.SelectRoomRVAdapter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;


public class SearchResultsFragment extends Fragment {
    LocalDate checkIn;
    LocalDate checkOut;
    public SearchResultsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        //get filters passed
        Filter filter = (Filter) getArguments().getSerializable("filter");
        checkIn = filter.getCheckIn();
        checkOut = filter.getCheckOut();


        //create array list of shown rooms
        ArrayList<Room> rooms = new ArrayList<>();
        //get RV reference
        RecyclerView recyclerView = view.findViewById(R.id.rvSearchResults);
        //create RV adapter
        SelectRoomRVAdapter adapter = new SelectRoomRVAdapter(this, rooms);

        //set adapter's click listener
        adapter.setClickListener((view1, position) -> {
            //set code to run when a room is clicked
            showRoomDetails(rooms.get(position));
        });

        //bind adapter to RV
        recyclerView.setAdapter(adapter);

        //set layout for RV
        //TODO: Check if this code works (green fn implementation).
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        //run background thread to acquire filtered rooms and update rv adapter
        new Thread(() -> {
            //connect to master and send filter request
            ArrayList<Room> result;
            try {
                Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeInt(MasterFunction.SEARCH_ROOM.getEncoded());
                out.writeObject(filter);
                out.flush();

                //wait for master to return rooms
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                result =  (ArrayList<Room>) in.readObject();


            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }

            //update adapter with the rooms returned
            requireActivity().runOnUiThread(() -> {
                adapter.setRooms(result);
                adapter.notifyDataSetChanged();
            });
        }).start();



    }

    //method is called when a room is clicked in the list
    private void showRoomDetails(Room room) {
        int containerViewId = R.id.main_frameLayout;

        Fragment roomDetails = new RoomDetailsFragment();

        Bundle args = new Bundle();
        args.putSerializable("room", room);
        args.putSerializable("cIn", checkIn);
        args.putSerializable("cOut", checkOut);

        roomDetails.setArguments(args);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(containerViewId, roomDetails);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}