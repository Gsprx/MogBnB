package com.example.view.fragments;

import static com.example.misc.Config.defaultZoneId;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.misc.Misc;
import com.example.mogbnb.Filter;
import com.example.mogbnb.R;
import com.google.android.material.slider.Slider;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class SearchFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    EditText area;
    EditText noOfPeople;
    Slider maxPrice;
    RatingBar minRating;
    TextView checkInShow;
    TextView checkOutShow;
    Button checkInBtn;
    Button checkOutBtn;
    Button confirmBtn;

    Date checkIn;
    Date checkOut;
    // used as holder to know if we pressed the checkIn or the checkOut button
    // we will use this for setting the available days in the calendar DBD
    boolean checkInPressed;

    int userID;

    public SearchFragment(int userID) {
        checkIn = null;
        checkOut = null;
        checkInPressed = false;
        this.userID = userID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);


        // get the components of the view
        area = view.findViewById(R.id.search_area_entry);
        noOfPeople = view.findViewById(R.id.search_numOfPeople_entry);
        maxPrice = view.findViewById(R.id.search_price_slider);
        minRating = view.findViewById(R.id.search_ratingBar);
        checkInShow = view.findViewById(R.id.search_checkIn_span);
        checkOutShow = view.findViewById(R.id.search_checkOut_span);
        checkInBtn = view.findViewById(R.id.search_add_checkIn);
        checkOutBtn = view.findViewById(R.id.search_add_checkOut);
        confirmBtn = view.findViewById(R.id.search_confirm_btn);


        // get previous filter if returned from search results
        if(getArguments()!=null) {
            Filter filter = (Filter) getArguments().getSerializable("filter");
            if (filter != null) {
                area.setText(filter.getArea());
                noOfPeople.setText(filter.getNoOfPersons());
                maxPrice.setValue((float) filter.getPrice());
                minRating.setRating((float) filter.getStars());
                checkInShow.setText(filter.getCheckIn().toString());
                checkOutShow.setText(filter.getCheckOut().toString());
                checkIn = Date.from(filter.getCheckIn().atStartOfDay().atZone(defaultZoneId.systemDefault()).toInstant());
                checkOut = Date.from(filter.getCheckOut().atStartOfDay().atZone(defaultZoneId.systemDefault()).toInstant());
            }
        }

        // when checkIn button is pressed
        checkInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInPressed = true;
                DatePickerDialog dpd = createDPD();
                setAvailableInCalendar(dpd);
                dpd.show(getActivity().getSupportFragmentManager(), "CheckIn");
            }
        });

        // when checkOut button is pressed
        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = createDPD();
                setAvailableInCalendar(dpd);
                dpd.show(getActivity().getSupportFragmentManager(), "CheckOut");
            }
        });

        // when confirm button is clicked
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if checkIn or checkOut are not selected
                if (checkInShow.getText().equals(getResources().getString(R.string.search_empty_checkIn_checkOut))
                || checkOutShow.getText().equals(getResources().getString(R.string.search_empty_checkIn_checkOut))) {
                    Toast.makeText(getActivity(), "Must select dates to checkIn-checkOut",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Filter filter = new Filter(
                        area.getText().toString().equals("") ? null : area.getText().toString(),
                        checkIn.toInstant()
                        .atZone(defaultZoneId)
                        .toLocalDate(),
                        checkOut.toInstant()
                        .atZone(defaultZoneId)
                        .toLocalDate(),
                        noOfPeople.getText().toString().equals("") ? -1 : Integer.parseInt(noOfPeople.getText().toString()), maxPrice.getValue(), minRating.getRating());


                // change fragment to search results
                int containerViewId = R.id.main_frameLayout;
                Fragment searchResultsFragment = new SearchResultsFragment(userID);
                Bundle args = new Bundle();
                args.putSerializable("filter", filter);
                searchResultsFragment.setArguments(args);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(containerViewId, searchResultsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    /**
     * Create the DatePickerDialog.
     * @return The DatePickerDialog
     */
    public DatePickerDialog createDPD() {
        Calendar c = Calendar.getInstance();
        return DatePickerDialog.newInstance(
                (DatePickerDialog.OnDateSetListener)this,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
    }

    /**
     * Set all the available dates in the DatePickerDialog.
     * @param dpd The DatePickerDialog
     */
    public void setAvailableInCalendar(DatePickerDialog dpd) {
        // disable past dates
        dpd.setMinDate(java.util.Calendar.getInstance());

        // if in check out disable all past date of check in
        java.util.Calendar c = java.util.Calendar.getInstance();
        if (this.checkIn != null && !checkInPressed) {
            c.setTime(this.checkIn);
            dpd.setMinDate(c);
        } else if (this.checkOut != null && checkInPressed) {
            c.setTime(this.checkOut);
            dpd.setMaxDate(c);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = DateFormat.getDateInstance().format(c.getTime());
        if (checkInPressed) {
            checkIn = Misc.parseDate(date);
            checkInShow.setText("" + checkIn.toInstant()
                    .atZone(defaultZoneId)
                    .toLocalDate());
            checkInPressed = false;
        } else {
            checkOut = Misc.parseDate(date);
            checkOutShow.setText("" + checkOut.toInstant()
                    .atZone(defaultZoneId)
                    .toLocalDate());
        }
    }
}