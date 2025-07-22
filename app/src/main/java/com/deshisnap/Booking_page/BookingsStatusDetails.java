package com.deshisnap.Booking_page;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.BookingAdapter;
import com.deshisnap.cart_page.CartPage;
import com.deshisnap.NotificationPage;
import com.deshisnap.Profile_page;
import com.deshisnap.R;
import com.deshisnap.Utils;


import java.util.ArrayList;
import java.util.List;

public class BookingsStatusDetails extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookings_status_details);

        // Find the TextView
        TextView booking_page_heading = findViewById(R.id.booking_page_heading);
        Utils.applyGradientToText(booking_page_heading, "#04FDAA", "#01D3F8");

        List<BookingItem> bookings = new ArrayList<>();
        bookings.add(new BookingItem("12345", "Flight", "2025-06-12 10:00", "Pending", R.drawable.whatsapp));

        RecyclerView recyclerView = findViewById(R.id.booking_recycler);
        BookingAdapter adapter = new BookingAdapter(bookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(BookingsStatusDetails.this, CartPage.class));
        });

        findViewById(R.id.home_button).setOnClickListener(v -> {
            finish(); // Closes BookingActivity and returns to MainActivity
        });
        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(BookingsStatusDetails.this, NotificationPage.class));
            finish();
        });

        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(BookingsStatusDetails.this, Profile_page.class));
            finish();
        });



    }
}

