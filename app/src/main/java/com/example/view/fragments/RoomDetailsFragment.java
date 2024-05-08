package com.example.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.mogbnb.R;
import com.example.mogbnb.Room;

import java.util.Arrays;
import java.util.List;

public class RoomDetailsFragment extends Fragment {

    // TODO TEST OBJECT TO SEE IF THIS WORKS
    private Room room = new Room("SKIBIDDI", 2, 365, "GYATTLAND", 4.5, 150, "url_to_image", 299.99);

    public RoomDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomdetails, container, false);

        ViewPager2 viewPagerImages = view.findViewById(R.id.viewPagerImages);
        TextView tvRoomName = view.findViewById(R.id.tvRoomName);
        TextView tvRoomDetails = view.findViewById(R.id.tvRoomDetails);
        Button btnBookRoom = view.findViewById(R.id.btnBookRoom);

        // TODO replace with actual images and do the bitmap image converter
        //List<Integer> imageList = Arrays.asList(R.drawable.image1, R.drawable.image2, R.drawable.image3);
      //  ImageSliderAdapter adapter = new ImageSliderAdapter(imageList);
      //  viewPagerImages.setAdapter(adapter);

        // Set the room details in the TextViews
        tvRoomName.setText(room.getRoomName());
        String details = "Capacity: " + room.getNoOfPersons() +
                "\nArea: " + room.getArea() +
                "\nStars: " + room.getStars() +
                "\nReviews: " + room.getNoOfReviews() +
                "\nPrice per day: " + room.getPricePerDay()+" Euros";
        tvRoomDetails.setText(details);

        btnBookRoom.setOnClickListener(v -> {
            // TODO implement the booking procedure
            Toast.makeText(getContext(), "Booking initiated!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
/*
    public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {
        private List<Integer> images;

        public ImageSliderAdapter(List<Integer> images) {
            this.images = images;
        }

        @NonNull
       // @Override
        //public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
           // return new SliderViewHolder(view);
       // }

        @Override
        public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
            holder.imageView.setImageResource(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class SliderViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public SliderViewHolder(@NonNull View itemView) {
                super(itemView);
              //  imageView = itemView.findViewById(R.id.imageViewItem);
            }
        }
    }
    */

}
