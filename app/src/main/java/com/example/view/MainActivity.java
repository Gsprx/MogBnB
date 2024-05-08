package com.example.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.mogbnb.R;
import com.example.view.fragments.BookingsFragment;
import com.example.view.fragments.ProfileFragment;
import com.example.view.fragments.SearachFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navbar;
    SearachFragment search;
    BookingsFragment bookings;
    ProfileFragment profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create search fragment
        search = new SearachFragment();
        // create bookings fragment
        bookings = new BookingsFragment();
        // create profile fragment
        profile = new ProfileFragment();
        // // when the main appears set the frame layout to the search fragment
        replaceFragment(search);

        // get navbar
        navbar = findViewById(R.id.main_bottomNavView);
        // manage navbar fragments
        navbar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_search) replaceFragment(search);
            else if (item.getItemId() == R.id.nav_bookings) replaceFragment(bookings);
            else replaceFragment(profile);
            return true;
        });
    }

    /**
     * Αλλαγή του fragment
     * @param fragment Το fragment στο οποίο θέλουμε να αλλάξουμε
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frameLayout, fragment);
        fragmentTransaction.commit();
    }
}