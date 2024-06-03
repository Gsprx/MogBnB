package com.example.view.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mogbnb.R;
import com.example.view.MainActivity;
import com.example.view.TenantEnterActivity;

public class ProfileFragment extends Fragment {
    TextView usernameTV;
    TextView idTV;
    String username;
    String id;
    Button exitBtn;

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

        usernameTV.setText(username);
        idTV.setText(id);

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