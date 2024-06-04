package com.example.view.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mogbnb.R;
import com.example.view.MainActivity;
import com.example.view.TenantEnterActivity;

import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {
    TextView usernameTV;
    TextView idTV;
    String username;
    String id;
    Button exitBtn;
    ImageView profileImage;

    public ProfileFragment(String username, String id) {
        this.username = username;
        this.id = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameTV = view.findViewById(R.id.profile_tenantName_span);
        idTV = view.findViewById(R.id.profile_tenantID_span);
        exitBtn = view.findViewById(R.id.profile_exitAppBtn);
        profileImage = view.findViewById(R.id.profileImage);

        usernameTV.setText(username);
        idTV.setText(id);

        //select a random goated profile pic based on id
        try {
            int imageIndex = Integer.parseInt(id) % getContext().getAssets().list("profiles").length;
            InputStream inputStream = getContext().getAssets().open("profiles/" + getContext().getAssets().list("profiles")[imageIndex]);
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            profileImage.setImageDrawable(drawable);
            inputStream.close();
        } catch (IOException e) {
            profileImage.setImageResource(R.drawable.child_po);
        }


        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TenantEnterActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}