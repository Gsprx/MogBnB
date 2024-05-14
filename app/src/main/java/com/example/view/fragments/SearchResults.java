package com.example.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.recyclerViewAdapters.SelectRoomRVAdapter;

import java.util.ArrayList;


public class SearchResults extends Fragment {

    public SearchResults() {
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
        //get rooms passed by the filter fragment
        ArrayList<Room> rooms = getArguments()!=null ? (ArrayList<Room>) getArguments().getSerializable("rooms") : new ArrayList<>();

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
    }

    //method is called when a room is clicked in the list
    private void showRoomDetails(Room room) {
        //TODO: Start room details fragment

    }
}