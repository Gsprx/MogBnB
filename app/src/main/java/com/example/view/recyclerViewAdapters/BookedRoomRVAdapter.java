package com.example.view.recyclerViewAdapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

import java.io.IOException;
import java.io.InputStream;
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
        System.out.println("RV ADAPTER ROOM LIST:" + this.rooms);
        holder.roomNameTV.setText(rooms.get(position).getRoomName());
        // set the imageview
        holder.imageView.setImageResource(R.drawable.child_po);
        System.out.println(rooms.get(position).getRoomName());
        System.out.println(dates.get(position)[0] + " - "  + dates.get(position)[1]);

        // set check in textView
        holder.checkInTV.setText(dates.get(position)[0].toString());
        // set check out textView
        holder.checkOutTV.setText(dates.get(position)[1].toString());

        try {
            String imagePath = rooms.get(position).getRoomImage();
            InputStream inputStream = context.getAssets().open(imagePath + "/" + context.getAssets().list(imagePath)[0]);
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            holder.imageView.setImageDrawable(drawable);
            inputStream.close();
        } catch (IOException e) {
            holder.imageView.setImageResource(R.drawable.child_po);
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    //method of this interface must be implemented by parent activity
    //parent activity is responsible for what to do when a room is clicked
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class BookedRoomsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView roomNameTV;
        TextView checkInTV;
        TextView checkOutTV;
        ImageView imageView;

        public BookedRoomsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

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
}
