package com.deshisnap;



import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CartPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_page);

        // Find the TextView
        TextView coupon_offer_text= findViewById(R.id.coupon_offer_text);
        Utils.applyGradientToText(coupon_offer_text, "#04FDAA", "#01D3F8");

        TextView payment_text= findViewById(R.id.payment_text);
        Utils.applyGradientToText(payment_text, "#04FDAA", "#01D3F8");


        findViewById(R.id.home_button).setOnClickListener(v -> {
            finish(); // Closes BookingActivity and returns to MainActivity
        });
        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(CartPage.this, BookingActivity.class));
            finish();
        });
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(CartPage.this, LoginPage.class));
            finish();
        });

        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(CartPage.this, NotificationPage.class));
            finish();
        });

        findViewById(R.id.book_a_slot_button).setOnClickListener(v -> {
            startActivity(new Intent(CartPage.this, BookingTimePage.class));
            finish();
        });




    }
}

