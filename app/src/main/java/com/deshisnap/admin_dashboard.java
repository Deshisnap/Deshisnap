package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class admin_dashboard extends AppCompatActivity {
    private TextView tvViewBookings;
    private TextView tvManageUsers;
    private TextView tvManageServices;
    private TextView tvSendNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard_page);

        // Initialize TextViews
        tvViewBookings = findViewById(R.id.tv_view_bookings);
        tvManageUsers = findViewById(R.id.tv_manage_users);
        tvManageServices = findViewById(R.id.tv_manage_services);
        tvSendNotification = findViewById(R.id.tv_send_notification);

        // Set OnClickListeners to directly launch respective activities
        tvViewBookings.setOnClickListener(v -> {
            startActivity(new Intent(admin_dashboard.this, ViewBookingsActivity.class));
        });

        tvManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(admin_dashboard.this, ManageUsersActivity.class));
        });

        tvManageServices.setOnClickListener(v -> {
            startActivity(new Intent(admin_dashboard.this, ManageServicesActivity.class));
        });

        tvSendNotification.setOnClickListener(v -> {
            startActivity(new Intent(admin_dashboard.this, SendNotificationActivity.class));
        });
    }
}
