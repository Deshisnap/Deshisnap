package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen); // Your splash screen layout

        TextView title = findViewById(R.id.title);
        Utils.applyGradientToText(title, "#04FDAA", "#01D3F8");

        mAuth = FirebaseAuth.getInstance();

        // Delay for 3 seconds
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                // Reload the user to ensure account deletion is reflected
                currentUser.reload().addOnCompleteListener(task -> {
                    FirebaseUser updatedUser = mAuth.getCurrentUser();

                    if (updatedUser != null) {
                        // User still exists, go to MainActivity
                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        // User was deleted, redirect to LoginPage
                        Intent intent = new Intent(SplashScreenActivity.this, LoginPage.class);
                        startActivity(intent);
                    }
                    finish(); // Close splash screen
                });
            } else {
                // No user logged in, go to LoginPage
                Intent intent = new Intent(SplashScreenActivity.this, LoginPage.class);
                startActivity(intent);
                finish();
            }
        }, 3000); // 3 seconds delay
    }
}

