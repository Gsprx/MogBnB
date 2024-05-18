package com.example.view.recyclerViewAdapters;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogbnb.MasterFunction;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;
import com.example.view.NetworkHandlerThread;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class BookedRoomRVAdapter extends RecyclerView.Adapter<BookedRoomRVAdapter.BookedRoomsViewHolder> {
    private HashMap<Room, ArrayList<LocalDate>> bookings;
    private Context context;

    public BookedRoomRVAdapter(Fragment fragment, HashMap<Room, ArrayList<LocalDate>> bookings) {
        this.bookings = bookings;
        if (fragment.getContext() != null) {
            this.context = fragment.getContext();
        } else {
            throw new IllegalStateException("Bookings Fragment is not attached to context.");
        }
    }

    public void setBookings(HashMap<Room, ArrayList<LocalDate>> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookedRoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_bookings_item, parent, false);
        return new BookedRoomsViewHolder(view);
    }

    @Override
    //Assign(bind) the data to use for each row
    //changes the data on the recycler view based on the position of the recycler
    public void onBindViewHolder(@NonNull BookedRoomsViewHolder holder, int position) {
        // get the set of rooms the tenant has made a booking for
        Set<Room> roomsSet = bookings.keySet();
        // convert this set to a List
        List<Room> rooms = new ArrayList<>(roomsSet.size());
        rooms.addAll(roomsSet);

        // set the roomName textView
        holder.roomNameTV.setText(rooms.get(position).getRoomName());
        // set the imageview
        holder.imageView.setImageResource(R.drawable.child_po);
        // set check in textView
        holder.checkInTV.setText(bookings.get(rooms.get(position)).get(0).toString());
        // set check out textView
        int daysBooked = bookings.get(rooms.get(position)).size() - 1;
        holder.checkOutTV.setText(bookings.get(rooms.get(position)).get(daysBooked).toString());
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public class BookedRoomsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView roomNameTV;
        TextView checkInTV;
        TextView checkOutTV;
        ImageView imageView;

        public BookedRoomsViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameTV = itemView.findViewById(R.id.bookedRoomName);
            checkInTV = itemView.findViewById(R.id.bookedRoomCheckIn);
            checkOutTV = itemView.findViewById(R.id.bookedRoomCheckOut);
            imageView = itemView.findViewById(R.id.bookedRoomImage);
        }

        @Override
        public void onClick(View v) {
            Room room = getRoomAtPosition(getAdapterPosition());
            if (room != null) {
                showRatingDialog(room);
            }
        }
    }
    private Room getRoomAtPosition(int position) {
        Set<Room> roomsSet = bookings.keySet();
        List<Room> rooms = new ArrayList<>(roomsSet.size());
        rooms.addAll(roomsSet);
        return rooms.get(position);
    }
    private void showRatingDialog(Room room) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_rate_room);
        dialog.setTitle("Rate this room");

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        Button submitButton = dialog.findViewById(R.id.btnSubmitRating);

        submitButton.setOnClickListener(v -> {
            double rating = ratingBar.getRating();
            rateRoom(room.getRoomName(), rating);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void rateRoom(String roomName, double rating) {
        ArrayList<Object> roomRatingData = new ArrayList<>();
        roomRatingData.add(roomName);
        roomRatingData.add(rating);

        NetworkHandlerThread rateRoomThread = new NetworkHandlerThread(MasterFunction.RATE_ROOM.getEncoded(), roomRatingData);
        rateRoomThread.start();
        try {
            rateRoomThread.join();
            String result = (String) rateRoomThread.result;
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(context, "An error occurred during rating.", Toast.LENGTH_LONG).show();
        }
    }
}
