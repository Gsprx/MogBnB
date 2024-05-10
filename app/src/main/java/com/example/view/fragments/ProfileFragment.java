package com.example.view.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mogbnb.R;

public class ProfileFragment extends Fragment {
    TextView usernameTV;
    TextView idTV;
    String username;
    String id;
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

        usernameTV.setText(username);
        idTV.setText(id);

        return view;
    }
}