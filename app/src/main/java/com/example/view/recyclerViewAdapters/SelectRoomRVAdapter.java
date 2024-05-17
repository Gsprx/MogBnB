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

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SelectRoomRVAdapter extends RecyclerView.Adapter<SelectRoomRVAdapter.SelectRoomViewHolder> {
    private ArrayList<Room> rooms;
    private Context context;
    private ItemClickListener clickListener;


    public SelectRoomRVAdapter(Fragment fragment, ArrayList<Room> rooms){
        this.rooms = rooms;
        if (fragment.getContext()!=null){
            this.context = fragment.getContext();
        }
        else{
            throw new IllegalStateException("Search Results Fragment is not attached to context.");
        }
    }
    @NonNull
    @Override
    //Creates the look for the rows in the UI recycler view
    public SelectRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_room_item, parent, false);
        return new SelectRoomViewHolder(view);
    }

    @Override
    //Assign(bind) the data to use for each row
    //changes the data on the recycler view based on the position of the recycler
    public void onBindViewHolder(@NonNull SelectRoomViewHolder holder, int position) {
        //set each holder's member fields to match the data of the Room data found on the position given in the list of filtered rooms
        holder.roomName.setText(rooms.get(position).getRoomName());

        //we use a decimal formatter to clean up the result of the doubles in the room class such as price (if needed) and rating.
        DecimalFormat df = new DecimalFormat("#.##");

        String formattedPrice = df.format(rooms.get(position).getPricePerDay());
        holder.roomPrice.setText(formattedPrice);

        String formattedRating = df.format(rooms.get(position).getStars());
        holder.roomRating.setText(formattedRating);

        holder.roomNumOfReviews.setText(String.valueOf(rooms.get(position).getNoOfReviews()));

        //TODO: set image later
        holder.roomImage.setImageResource(R.drawable.child_po);
    }


    //catch click events with a click listener that we set
    public void setClickListener(ItemClickListener listener){
        this.clickListener = listener;
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



    //this class holds the View objects in the recycler view room item layout file
    //additionally handles the click listening
    public class SelectRoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView roomName;
        TextView roomPrice;
        TextView roomRating;
        TextView roomNumOfReviews;
        ImageView roomImage;

        public SelectRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            roomName = itemView.findViewById(R.id.textRoomItemName);
            roomPrice = itemView.findViewById(R.id.textRoomItemPrice);
            roomRating = itemView.findViewById(R.id.textRoomItemRating);
            roomNumOfReviews = itemView.findViewById(R.id.textRoomItemNumReviews);
            roomImage = itemView.findViewById(R.id.imageRoomItem);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }

    }

}


