package com.example.view.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import java.util.Iterator;

public class BookingsFragment extends Fragment {
    int userID;
    RecyclerView recyclerView;
    HashMap<Room, ArrayList<LocalDate>> bookings;
    ArrayList<Room> onlyRoomsList;
    ArrayList<LocalDate[]> onlyDatesList;
    public BookingsFragment(String id) {
        this.userID = Integer.parseInt(id);
        onlyRoomsList = new ArrayList<>();
        onlyDatesList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        // build the recycler view
        recyclerView = view.findViewById(R.id.rvBookings);
        BookedRoomRVAdapter adapter = new BookedRoomRVAdapter(this, onlyRoomsList, onlyDatesList);
        adapter.setClickListener((view1, position) -> {
            //set code to run when a room is clicked
            rateRoom(onlyRoomsList.get(position).getRoomName());
        });

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
                for (Iterator<Room> it = bookings.keySet().iterator(); it.hasNext(); ) {
                    Room room = it.next();

                    ArrayList<LocalDate> allBookedDates = bookings.get(room);

                    allBookedDates.sort(LocalDate::compareTo);

                    for(int i = 0; i<allBookedDates.size(); i++){
                        //list that includes min date (check in) and max date (check out)
                        LocalDate[] checkInOutDates = new LocalDate[2]; // 0 - CheckIn, 1 - CheckOut

                        //check all continuous dates (we consider it one booking)

                        //case where this is the first day of a booking (aka check in)
                        //first date of all dates or previous date isn't continuous
                        if(i==0 || !allBookedDates.get(i).minusDays(1).isEqual(allBookedDates.get(i-1))){
                            checkInOutDates[0] = allBookedDates.get(i);
                        }
                        //case where this is the last day of a booking (aka check out)
                        //last date of all dates or next date isn't continuous
                        if(i == allBookedDates.size()-1 || allBookedDates.get(i).plusDays(1).isEqual(allBookedDates.get(i+1))){
                            checkInOutDates[1] = allBookedDates.get(i);


                            //add room to room only arraylist
                            //this allows us to have the same room with multiple bookings
                            onlyRoomsList.add(room);

                            //add check in and out dates
                            onlyDatesList.add(checkInOutDates);
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }

            //update adapter with the rooms returned
            requireActivity().runOnUiThread(() -> {
                if(onlyRoomsList.size()==0){
                    Toast.makeText(getContext(),"No bookings found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.setRooms(onlyRoomsList);
                adapter.setDates(onlyDatesList);
                adapter.notifyDataSetChanged();
            });
        }).start();

        return view;
    }

    private void rateRoom(String roomName) {
    }
}