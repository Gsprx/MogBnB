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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.misc.Config;
import com.example.mogbnb.Filter;
import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.recyclerViewAdapters.ImageSliderAdapter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDetailsFragment extends Fragment {
    Room room;
    Filter filter;
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
            filter = (Filter) getArguments().getSerializable("filter");
            checkIn = filter.getCheckIn();
            checkOut = filter.getCheckOut();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomdetails, container, false);

        RecyclerView rvImages = view.findViewById(R.id.rvImagesRoomDetails);
        TextView tvRoomName = view.findViewById(R.id.tvRoomName);
        TextView tvRoomDetails = view.findViewById(R.id.tvRoomDetails);
        tvRoomDetails.setMovementMethod(new ScrollingMovementMethod());


        Button btnBookRoom = view.findViewById(R.id.btnBookRoom);
        Button btnReturnToSearchResults = view.findViewById(R.id.btnRoomDetailsReturn);
        btnReturnToSearchResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //return to search page using the same filters pre applied
                returnToSearchResults(filter);
            }
        });


        // Set up image slider
        String imagePath = room.getRoomImage();
        System.out.println(imagePath);
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, imagePath);
        rvImages.setAdapter(adapter);
        rvImages.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false));
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

    private void returnToSearchResults(Filter filter) {
        int containerViewId = R.id.main_frameLayout;

        Fragment searchResults = new SearchResultsFragment(this.userID);

        Bundle args = new Bundle();
        args.putSerializable("filter", filter);

        searchResults.setArguments(args);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(containerViewId, searchResults);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
                //connect to master
                Socket socket = new Socket(Config.MASTER_IP, Config.USER_MASTER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeInt(MasterFunction.BOOK_ROOM.getEncoded());
                out.writeObject(bookingData);
                out.flush();

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                result = (int) in.readObject();

                //show toast message depending on the result from the server
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
