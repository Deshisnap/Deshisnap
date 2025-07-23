package com.deshisnap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest; // Not directly used for name, but for display name if needed
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    private EditText nameInput, emailInput, phoneInput, locationInput; // Added locationInput
    private Button saveButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Assuming this is your settings layout

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        nameInput = findViewById(R.id.settings_name_input);
        emailInput = findViewById(R.id.settings_email_input);
        phoneInput = findViewById(R.id.settings_phone_input);
        // IMPORTANT: You need to add an EditText for location in your activity_settings.xml
        // For example:
        // <EditText android:id="@+id/settings_location_input" android:layout_width="match_parent" android:layout_height="wrap_content" android:hint="Address/Location" />
        locationInput = findViewById(R.id.settings_location_input); // Assuming you add this ID to your XML
        saveButton = findViewById(R.id.save_settings_button);

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to access settings.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        loadCurrentProfileData();

        saveButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadCurrentProfileData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        // Populate name (combine first and last)
                        String fullName = "";
                        if (user.getFirstName() != null) {
                            fullName += user.getFirstName();
                        }
                        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                            if (!fullName.isEmpty()) fullName += " ";
                            fullName += user.getLastName();
                        }
                        nameInput.setText(fullName);

                        // Populate email
                        if (user.getEmail() != null) {
                            emailInput.setText(user.getEmail());
                        } else if (currentUser.getEmail() != null) { // Fallback to Auth email if not in DB
                            emailInput.setText(currentUser.getEmail());
                        }

                        // Populate phone
                        if (user.getPhoneNumber() != null) {
                            phoneInput.setText(user.getPhoneNumber());
                        }

                        // Populate location
                        if (locationInput != null) { // Check if EditText exists
                            if (user.getLocation() != null) {
                                locationInput.setText(user.getLocation());
                            }
                        }
                    }
                } else {
                    Toast.makeText(SettingsActivity.this, "No profile data found in database.", Toast.LENGTH_SHORT).show();
                    // You can pre-fill email from Firebase Auth if no DB data
                    if (currentUser.getEmail() != null) {
                        emailInput.setText(currentUser.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load settings data: " + databaseError.getMessage());
                Toast.makeText(SettingsActivity.this, "Failed to load current data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileChanges() {
        String fullName = nameInput.getText().toString().trim();
        String newEmail = emailInput.getText().toString().trim();
        String newPhone = phoneInput.getText().toString().trim();
        String newLocation = locationInput != null ? locationInput.getText().toString().trim() : ""; // Get location

        if (fullName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Name, Email, and Phone are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Split full name into first and last (simple split, adjust if more complex logic needed)
        String firstName;
        String lastName = "";
        int spaceIndex = fullName.lastIndexOf(' ');
        if (spaceIndex != -1) {
            firstName = fullName.substring(0, spaceIndex);
            lastName = fullName.substring(spaceIndex + 1);
        } else {
            firstName = fullName; // If no space, whole name is first name
        }

        // 1. Update Firebase Authentication (for email)
        if (!newEmail.equals(currentUser.getEmail())) { // Only update if email changed
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated in Auth.");
                            Toast.makeText(SettingsActivity.this, "Email updated in Auth.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to update email in Auth: " + task.getException().getMessage());
                            Toast.makeText(SettingsActivity.this, "Failed to update email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            // If email update fails, you might want to stop here or handle it.
                            // For simplicity, we'll continue with DB update.
                        }
                    });
        }

        // 2. Update Firebase Realtime Database
        User updatedUser = new User(firstName, lastName, newEmail, newLocation, newPhone);

        userRef.setValue(updatedUser) // Use setValue to overwrite the user object
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SettingsActivity.this, "Profile details saved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to ProfileActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SettingsActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to save profile to RTDB", e);
                });
    }
}