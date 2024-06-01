package com.example.view.recyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogbnb.R;
import com.example.mogbnb.Room;

import java.time.LocalDate;
import java.util.ArrayList;

public class BookedRoomRVAdapter extends RecyclerView.Adapter<BookedRoomRVAdapter.BookedRoomsViewHolder> {
    private ArrayList<Room> rooms;
    private ArrayList<LocalDate[]> dates;
    private Context context;
    private ItemClickListener clickListener;

    public BookedRoomRVAdapter(Fragment fragment, ArrayList<Room> rooms, ArrayList<LocalDate[]> dates) {
        this.rooms = rooms;
        this.dates = dates;
        if (fragment.getContext() != null) {
            this.context = fragment.getContext();
        } else {
            throw new IllegalStateException("Bookings Fragment is not attached to context.");
        }
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

    public void setDates(ArrayList<LocalDate[]> dates) {
        this.dates = dates;
    }

    //catch click events with a click listener that we set
    public void setClickListener(ItemClickListener listener){
        this.clickListener = listener;
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

        // set the roomName textView
        holder.roomNameTV.setText(rooms.get(position).getRoomName());
        // set the imageview
        holder.imageView.setImageResource(R.drawable.child_po);
        System.out.println(rooms.get(position).getRoomName());
        System.out.println(dates.get(position)[0] + " - "  + dates.get(position)[1]);

        // set check in textView
        holder.checkInTV.setText(dates.get(position)[0].toString());
        // set check out textView
        holder.checkOutTV.setText(dates.get(position)[1].toString());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    //method of this interface must be implemented by parent activity
    //parent activity is responsible for what to do when a room is clicked
    public interface ItemClickListener{
        void onItemClick(View view,int position);
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
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }
//    private Room getRoomAtPosition(int position) {
//        Set<Room> roomsSet = bookings.keySet();
//        List<Room> rooms = new ArrayList<>(roomsSet.size());
//        rooms.addAll(roomsSet);
//        return rooms.get(position);
//    }
//    private void showRatingDialog(Room room) {
////        Dialog dialog = new Dialog(context);
////        dialog.setContentView(R.layout.dialog_rate_room);
////        dialog.setTitle("Rate this room");
////
////        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
////        Button submitButton = dialog.findViewById(R.id.btnSubmitRating);
////
////        submitButton.setOnClickListener(v -> {
////            double rating = ratingBar.getRating();
////            rateRoom(room.getRoomName(), rating);
////            dialog.dismiss();
////        });
////
////        dialog.show();
////    }
//
//    private void rateRoom(String roomName, double rating) {
//        ArrayList<Object> roomRatingData = new ArrayList<>();
//        roomRatingData.add(roomName);
//        roomRatingData.add(rating);
//
//        NetworkHandlerThread rateRoomThread = new NetworkHandlerThread(MasterFunction.RATE_ROOM.getEncoded(), roomRatingData);
//        rateRoomThread.start();
//        try {
//            rateRoomThread.join();
//            String result = (String) rateRoomThread.result;
//            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            Toast.makeText(context, "An error occurred during rating.", Toast.LENGTH_LONG).show();
//        }
//    }
}
