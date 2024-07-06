package com.example.view.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import android.app.Dialog;
import com.example.misc.Config;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.recyclerViewAdapters.BookedRoomRVAdapter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
        adapter.setClickListener((View view1, int position) -> {
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

                //clear lists used for adapter
                onlyRoomsList.clear();
                onlyDatesList.clear();

                // wait for master to return rooms
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                bookings = (HashMap<Room, ArrayList<LocalDate>>) in.readObject();

                for (Room room : bookings.keySet()) {
                    ArrayList<LocalDate> allBookedDates = bookings.get(room);

                    allBookedDates.sort(LocalDate::compareTo);
                    System.out.println("All booked dates sorted result: " + allBookedDates);


                    // list that includes min date (check in) and max date (check out)
                    LocalDate[] checkInOutDates = new LocalDate[2]; // 0 - CheckIn, 1 - CheckOut
                    boolean startedBooking = true;
                    for (int i = 0; i < allBookedDates.size(); i++) {
                        if (startedBooking) {
                            checkInOutDates = new LocalDate[2];
                            startedBooking = false;
                        }

                        //check all continuous dates (we consider it one booking)

                        //case where this is the first day of a booking (aka check in)
                        //first date of all dates or previous date isn't continuous
                        if (i == 0 || !allBookedDates.get(i).minusDays(1).isEqual(allBookedDates.get(i - 1))) {
                            checkInOutDates[0] = allBookedDates.get(i);
                            System.out.println("Added check in day: " + checkInOutDates[0]);
                        }
                        //case where this is the last day of a booking (aka check out)
                        //last date of all dates or next date isn't continuous
                        if (i == allBookedDates.size() - 1 || !allBookedDates.get(i).plusDays(1).isEqual(allBookedDates.get(i + 1))) {
                            checkInOutDates[1] = allBookedDates.get(i);
                            System.out.println("Added check out day: " + checkInOutDates[1]);

                            //add room to room only arraylist
                            //this allows us to have the same room with multiple bookings
                            onlyRoomsList.add(room);

                            //add check in and out dates
                            onlyDatesList.add(checkInOutDates);
                            System.out.println("Added a booking record for room: " + room.getRoomName() + " for dates: " + Arrays.toString(checkInOutDates));
                            startedBooking = true;
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }

            //update adapter with the rooms returned
            requireActivity().runOnUiThread(() -> {
                if(onlyRoomsList.isEmpty() || onlyDatesList.isEmpty()){
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
         Dialog dialog = new Dialog(getContext());
         dialog.setContentView(R.layout.dialog_rate_room);
         dialog.setTitle("Rate this room");
         RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
         Button submitButton = dialog.findViewById(R.id.btnSubmitRating);
         dialog.show();
         submitButton.setOnClickListener(v -> {
             double rating = ratingBar.getRating();

             //use a thread to send the network request for rating
             new Thread(() -> {
                 try {
                     Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
                     ArrayList<Object> roomRating = new ArrayList<>();
                     roomRating.add(roomName);
                     roomRating.add(rating);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     out.writeInt(MasterFunction.RATE_ROOM.getEncoded());
                     out.writeObject(roomRating);
                     out.flush();

                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                     int result = (int) in.readObject();

                     // make Toast in UI
                     requireActivity().runOnUiThread(() -> {
                         if (result == 1) Toast.makeText(getContext(),"This room was rated successfully!", Toast.LENGTH_SHORT).show();
                         else Toast.makeText(getContext(),"An error occurred while rating this room.", Toast.LENGTH_SHORT).show();
                     });
                 } catch (IOException | ClassNotFoundException e) {
                     throw new RuntimeException(e);
                 }
             }).start();
         dialog.dismiss();
      });
    }
}