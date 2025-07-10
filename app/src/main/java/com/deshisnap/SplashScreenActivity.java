package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Declare FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);  // Set the splash screen layout

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Add a delay for the splash screen to show (e.g., 3 seconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if a user is currently logged in
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    // User is already logged in, go to MainActivity
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // No user logged in, go to LoginPage
                    Intent intent = new Intent(SplashScreenActivity.this, LoginPage.class);
                    startActivity(intent);
                }
                finish();  // Close this activity after starting the next one
            }
        }, 3000);  // Delay in milliseconds (3000 ms = 3 seconds)
    }
}
