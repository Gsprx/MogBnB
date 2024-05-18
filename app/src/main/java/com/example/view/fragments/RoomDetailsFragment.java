package com.example.view.fragments;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.misc.Config;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.fragments.ImageSliderAdapter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDetailsFragment extends Fragment {
    Room room;
    LocalDate checkIn;
    LocalDate checkOut;
    int userID;
    public RoomDetailsFragment(int userID) {
        this.userID = userID;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            room = (Room) getArguments().getSerializable("room");
            checkIn = (LocalDate) getArguments().getSerializable("cIn");
            checkOut = (LocalDate) getArguments().getSerializable("cOut");
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
        String details = "Description: " + room.getDescription() + "\n\n" +
                "Capacity: " + room.getNoOfPersons() +
                "\nArea: " + room.getArea() +
                "\nStars: " + room.getStars() +
                "\nReviews: " + room.getNoOfReviews() +
                "\nPrice per day: " + room.getPricePerDay() + " Euros" +
                "\nAmenities:\n" + String.join("\n", room.getAmenities());



                tvRoomDetails.setText(details);

        btnBookRoom.setOnClickListener(v ->executeBooking() );

        return view;
    }
    private void executeBooking() {
        ArrayList<Object> bookingData = new ArrayList<>();
        bookingData.add(room.getRoomName());
        bookingData.add(this.userID);
        bookingData.add(checkIn);
        bookingData.add(checkOut);

        new Thread(() -> {
            int result;
            try {
                Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeInt(MasterFunction.BOOK_ROOM.getEncoded());
                out.writeObject(bookingData);
                out.flush();

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                result = (int) in.readObject();

                // update adapter with the rooms returned
                requireActivity().runOnUiThread(() -> {
                    if (result == 1) {
                        Toast.makeText(getContext(), "Booking successful!", Toast.LENGTH_LONG).show();
                    } else if (result == 0) {
                        Toast.makeText(getContext(), "Booking was unsuccessful, days requested were already booked!", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
