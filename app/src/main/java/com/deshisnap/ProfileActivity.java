package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.deshisnap.Booking_page.BookingsStatusDetails;
import com.deshisnap.cart_page.CartPage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private TextView nameTextView; // Will display first + last name
    private TextView phoneNumberTextView;
    private TextView addressTextView; // For displaying the 'location' from User object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        nameTextView = findViewById(R.id.Name);
        phoneNumberTextView = findViewById(R.id.phone_number);
        // IMPORTANT: You need to add a TextView in your profile_page.xml or profile_components.xml
        // if you want to display the address/location. For example:
        // <TextView android:id="@+id/address_text_view" android:layout_width="..." android:layout_height="..." android:text="Address: Not set" />
        // Make sure to add this TextView in your layout, otherwise this line will cause a NullPointerException.

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view your profile.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ProfileActivity.this, LoginPage.class));
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        loadUserProfileData();
       // Consolidating your existing listeners
        setupProfileComponentListeners();
    }

    private void loadUserProfileData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch the entire User object
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        // Update Name TextView
                        String fullName = "";
                        if (user.getFirstName() != null) {
                            fullName += user.getFirstName();
                        }
                        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                            if (!fullName.isEmpty()) fullName += " ";
                            fullName += user.getLastName();
                        }
                        if (fullName.isEmpty()) fullName = "Name: N/A";
                        nameTextView.setText(fullName);

                        // Update Phone Number TextView
                        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                            phoneNumberTextView.setText(user.getPhoneNumber());
                        } else {
                            phoneNumberTextView.setText("Phone: N/A");
                        }

                        // Update Address/Location TextView
                        if (addressTextView != null) { // Check if TextView exists in layout
                            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                                addressTextView.setText("Address: " + user.getLocation());
                            } else {
                                addressTextView.setText("Address: Not Set");
                            }
                            Utils.applyGradientToText(addressTextView, "#04FDAA", "#01D3F8");
                        }


                        // Apply gradients (ensure Utils class and method are correct)
                        Utils.applyGradientToText(nameTextView, "#04FDAA", "#01D3F8");
                        Utils.applyGradientToText(phoneNumberTextView, "#04FDAA", "#01D3F8");

                    } else {
                        Toast.makeText(ProfileActivity.this, "User data is empty.", Toast.LENGTH_SHORT).show();
                        nameTextView.setText("Name: N/A");
                        phoneNumberTextView.setText("Phone: N/A");
                        if (addressTextView != null) addressTextView.setText("Address: Not Set");
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User profile data not found in database.", Toast.LENGTH_SHORT).show();
                    nameTextView.setText("Name: N/A");
                    phoneNumberTextView.setText("Phone: N/A");
                    if (addressTextView != null) addressTextView.setText("Address: Not Set");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load user profile: " + databaseError.getMessage());
                Toast.makeText(ProfileActivity.this, "Error loading profile: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, CartPage.class));
        });

        findViewById(R.id.home_button).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, BookingsStatusDetails.class));
            finish();
        });
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "You are already on the Profile Page!", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, UserNotificationPage.class));
            finish();
        });


    }
    private void setupProfileComponentListeners() {
        findViewById(R.id.demoprofilecomponent).findViewById(R.id.manage_address_button).setOnClickListener(v -> {
            showManageAddressDialog();
        });

        findViewById(R.id.demoprofilecomponent).findViewById(R.id.setting_button).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.demoprofilecomponent).findViewById(R.id.costomer_service_button).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ContactUsActivity.class));
        });

        findViewById(R.id.demoprofilecomponent).findViewById(R.id.about_us_button).setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "About Us Clicked!", Toast.LENGTH_SHORT).show();
        });

        // Logout: sign out and redirect to LoginPage
        findViewById(R.id.demoprofilecomponent).findViewById(R.id.logout_button).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(ProfileActivity.this, LoginPage.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }


    // Part 2: "Manage Address" Custom Dialog - Updated to use 'location'
    private void showManageAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set New Address (Location)");

        final EditText addressInput = new EditText(this);
        addressInput.setHint("Enter your new address/location");
        addressInput.setPadding(40, 40, 40, 40);
        addressInput.setTextColor(getResources().getColor(android.R.color.black));
        addressInput.setBackgroundResource(android.R.color.white);
        addressInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(addressInput);
        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newLocation = addressInput.getText().toString().trim();
            if (newLocation.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Address/Location cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveLocationToFirebase(newLocation);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveLocationToFirebase(String newLocation) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to save address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Using a Map to update only the 'location' field
        Map<String, Object> updates = new HashMap<>();
        updates.put("location", newLocation);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Address/Location updated successfully!", Toast.LENGTH_SHORT).show();
                    // Optional: Update the TextView immediately if it exists
                    if (addressTextView != null) addressTextView.setText("Address: " + newLocation);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to update address/location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to update location", e);
                });



    }
}



