package com.example.view.recyclerViewAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mogbnb.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {
    private String imagePath;
    private Context context;

    public ImageSliderAdapter(Fragment fragment, String imagePath) {
        this.imagePath = imagePath;
        if (fragment.getContext() != null) {
            this.context = fragment.getContext();
        } else {
            throw new IllegalStateException("Bookings Fragment is not attached to context.");
        }
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_image_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        try {
            System.out.println(context.getAssets().list(imagePath)[position]);
            InputStream inputStream = context.getAssets().open(imagePath + "/" + context.getAssets().list(imagePath)[position]);
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            holder.imageView.setImageDrawable(drawable);
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        try {
            return context.getAssets().list(imagePath).length;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
