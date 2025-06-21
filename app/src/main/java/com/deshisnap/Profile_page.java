package com.foodcafe.myapplication;





import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Profile_page extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);


        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(Profile_page.this, CartPage.class));
        });

        findViewById(R.id.home_button).setOnClickListener(v -> {
            finish(); // Closes BookingActivity and returns to MainActivity
        });
        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(Profile_page.this, BookingActivity.class));
            finish();

        });

        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(Profile_page.this, NotificationPage.class));
            finish();
        });




    }
}



