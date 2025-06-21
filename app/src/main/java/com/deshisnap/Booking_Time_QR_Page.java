package com.deshisnap;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Booking_Time_QR_Page extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_time_page);

        // Find the TextView
        TextView heading_text= findViewById(R.id.heading_text);
        Utils.applyGradientToText(heading_text, "#04FDAA", "#01D3F8");

        TextView book_a_slot_text= findViewById(R.id.book_a_slot_text);
        Utils.applyGradientToText(book_a_slot_text, "#04FDAA", "#01D3F8");

        TextView scan_qrcode_text= findViewById(R.id.scan_qrcode_text);
        Utils.applyGradientToText(scan_qrcode_text, "#04FDAA", "#01D3F8");


        RelativeLayout slot = findViewById(R.id.saturday_button);
        View overlay = slot.findViewById(R.id.overlay);

        slot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (overlay.getVisibility() == View.VISIBLE) {
                    overlay.setVisibility(View.GONE);
                } else {
                    overlay.setVisibility(View.VISIBLE);
                    overlay.bringToFront(); // ensures it overlays correctly
                }
            }
        });





        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(Booking_Time_QR_Page.this, CartPage.class));
        });

        findViewById(R.id.home_button).setOnClickListener(v -> {
            finish(); // Closes BookingActivity and returns to MainActivity
        });
        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(Booking_Time_QR_Page.this, BookingActivity.class));
            finish();

        });

        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(Booking_Time_QR_Page.this, NotificationPage.class));
            finish();
        });





    }
}


