package com.deshisnap;



import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_page);

        // Find the TextView
        TextView notification_page_heading= findViewById(R.id.notification_page_heading);
        Utils.applyGradientToText(notification_page_heading, "#04FDAA", "#01D3F8");


        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(NotificationPage.this, CartPage.class));
        });

        findViewById(R.id.home_button).setOnClickListener(v -> {
            finish(); // Closes BookingActivity and returns to MainActivity
        });
        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(NotificationPage.this, BookingActivity.class));
            finish();
        });
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(NotificationPage.this, Profile_page.class));
            finish();
        });



    }
}


