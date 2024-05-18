package com.example.view.fragments;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.fragments.ImageSliderAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.view.NetworkHandlerThread;

public class RoomDetailsFragment extends Fragment {
    Room room;
    LocalDate checkIn;
    LocalDate checkOut;
    public RoomDetailsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            room = (Room) getArguments().getSerializable("room");
            LocalDate checkIn = (LocalDate) getArguments().getSerializable("cIn");
            LocalDate checkOut = (LocalDate) getArguments().getSerializable("cOut");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomdetails, container, false);

        ViewPager2 viewPagerImages = view.findViewById(R.id.viewPagerImages);
        TextView tvRoomName = view.findViewById(R.id.tvRoomName);
        TextView tvRoomDetails = view.findViewById(R.id.tvRoomDetails);
        tvRoomDetails.setMovementMethod(new ScrollingMovementMethod());


        Button btnBookRoom = view.findViewById(R.id.btnBookRoom);

        // Set up image slider
        List<String> imagePaths = room.getdirRoomImages();
        ImageSliderAdapter adapter = new ImageSliderAdapter(imagePaths);
        viewPagerImages.setAdapter(adapter);
        // Set the room details in the TextViews
        tvRoomName.setText(room.getRoomName());
        String details = "Capacity: " + room.getNoOfPersons() +
                "\nArea: " + room.getArea() +
                "\nStars: " + room.getStars() +
                "\nReviews: " + room.getNoOfReviews() +
                "\nPrice per day: " + room.getPricePerDay()+" Euros";
        tvRoomDetails.setText(details);

        btnBookRoom.setOnClickListener(v ->executeBooking() );

        return view;
    }
    private void executeBooking() {
        ArrayList<Object> bookingData = new ArrayList<>();
        bookingData.add(room.getRoomName());
        bookingData.add(/* user ID */ 1); // TODO Replace with actual user ID
        bookingData.add(checkIn);
        bookingData.add(checkOut);

        NetworkHandlerThread bookingThread = new NetworkHandlerThread(MasterFunction.BOOK_ROOM.getEncoded(), bookingData);
        bookingThread.start();
        try {
            bookingThread.join();
            int result = (int) bookingThread.result;
            if (result == 1) {
                Toast.makeText(getContext(), "Booking successful.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Booking was unsuccessful, days requested were already booked!", Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "An error occurred during booking.", Toast.LENGTH_LONG).show();
        }
    }
}
