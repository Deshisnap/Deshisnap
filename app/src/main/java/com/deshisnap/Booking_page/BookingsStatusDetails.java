package com.deshisnap.Booking_page;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.UserBookingsAdapter;
import com.deshisnap.UserBookingsAdapter.Row;
import com.deshisnap.cart_page.CartPage;
import com.deshisnap.UserNotificationPage;
import com.deshisnap.ProfileActivity;
import com.deshisnap.R;
import com.deshisnap.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookingsStatusDetails extends AppCompatActivity {

    private final List<Row> rows = new ArrayList<>();
    private UserBookingsAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookings_status_details);

        TextView booking_page_heading = findViewById(R.id.booking_page_heading);
        Utils.applyGradientToText(booking_page_heading, "#04FDAA", "#01D3F8");

        RecyclerView recyclerView = findViewById(R.id.booking_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserBookingsAdapter(rows);
        recyclerView.setAdapter(adapter);
        emptyText = findViewById(R.id.empty_text);

        // Header location text
        TextView headerLocation = findViewById(R.id.textView2);
        FirebaseUser uLoc = FirebaseAuth.getInstance().getCurrentUser();
        if (headerLocation != null) {
            if (uLoc != null) {
                DatabaseReference locRef = FirebaseDatabase.getInstance().getReference("users").child(uLoc.getUid()).child("location");
                locRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String loc = snapshot.getValue(String.class);
                        headerLocation.setText((loc == null || loc.trim().isEmpty()) ? "Please share your locality" : loc);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { headerLocation.setText("Please share your locality"); }
                });
            } else {
                headerLocation.setText("Please share your locality");
            }
        }

        findViewById(R.id.cart_img).setOnClickListener(v -> startActivity(new Intent(BookingsStatusDetails.this, CartPage.class)));
        findViewById(R.id.home_button).setOnClickListener(v -> finish());
        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(BookingsStatusDetails.this, UserNotificationPage.class));
            finish();
        });
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(BookingsStatusDetails.this, ProfileActivity.class));
            finish();
        });

        // Fetch current user's bookings
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("bookings");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    rows.clear();
                    for (DataSnapshot bSnap : snapshot.getChildren()) {
                        Row r = new Row();
                        // serviceNames array
                        List<String> list = new ArrayList<>();
                        for (DataSnapshot s : bSnap.child("serviceNames").getChildren()) {
                            String v = s.getValue(String.class);
                            if (!TextUtils.isEmpty(v)) list.add(v);
                        }
                        r.serviceNames = list;
                        // fallback single serviceName
                        r.serviceName = bSnap.child("serviceName").getValue(String.class);

                        r.bookingDate = bSnap.child("bookingDate").getValue(String.class);
                        r.timeSlot = bSnap.child("timeSlot").getValue(String.class);
                        r.advanceAmount = getDouble(bSnap.child("advanceAmount").getValue());
                        r.calculatedPrice = getDouble(bSnap.child("calculatedPrice").getValue());
                        r.grandTotal = getDouble(bSnap.child("grandTotal").getValue());
                        r.advancePaid = getDouble(bSnap.child("advancePaid").getValue());
                        r.status = bSnap.child("status").getValue(String.class);
                        Long ts = bSnap.child("timestamp").getValue(Long.class);
                        r.timestamp = ts == null ? 0L : ts;

                        rows.add(r);
                    }
                    Collections.sort(rows, new Comparator<Row>() {
                        @Override
                        public int compare(Row o1, Row o2) {
                            return Long.compare(o2.timestamp, o1.timestamp);
                        }
                    });
                    adapter.notifyDataSetChanged();
                    if (emptyText != null) {
                        emptyText.setVisibility(rows.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    }
                    Toast.makeText(BookingsStatusDetails.this, "Loaded " + rows.size() + " bookings", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });


            findViewById(R.id.cart_img).setOnClickListener(v -> {
                startActivity(new Intent(BookingsStatusDetails.this, com.deshisnap.cart_page.CartPage.class));
            });

            findViewById(R.id.booking_button).setOnClickListener(v -> {
                startActivity(new Intent(BookingsStatusDetails.this, BookingsStatusDetails.class));
            });
            findViewById(R.id.inbox_button).setOnClickListener(v -> {
                startActivity(new Intent(BookingsStatusDetails.this, UserNotificationPage.class));
            });

            findViewById(R.id.profile_button).setOnClickListener(v -> {
                startActivity(new Intent(BookingsStatusDetails.this, ProfileActivity.class));
            });

        }
    }

    private double getDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return val == null ? 0.0 : Double.parseDouble(String.valueOf(val)); }
        catch (Exception e) { return 0.0; }
    }
}
