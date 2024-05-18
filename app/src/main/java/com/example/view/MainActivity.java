package com.example.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.mogbnb.R;
import com.example.view.fragments.BookingsFragment;
import com.example.view.fragments.RoomDetailsFragment;
import com.example.view.fragments.ProfileFragment;
import com.example.view.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navbar;
    SearchFragment search;
    BookingsFragment bookings;
    ProfileFragment profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get intent from welcoming screen
        Intent intent = getIntent();
        // set user's username and id
        String username = intent.getStringExtra("USERNAME");
        String id = intent.getStringExtra("ID");

        // create search fragment
        search = new SearchFragment(Integer.parseInt(id));
        // create bookings fragment
        bookings = new BookingsFragment(id);
        // create profile fragment
        profile = new ProfileFragment(username, id);
        // when the main appears set the frame layout to the search fragment
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