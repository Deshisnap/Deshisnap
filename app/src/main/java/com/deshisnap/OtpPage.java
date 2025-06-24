package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference; // Import DatabaseReference
import com.google.firebase.database.FirebaseDatabase; // Import FirebaseDatabase

import java.util.concurrent.TimeUnit;

public class OtpPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase; // Add DatabaseReference
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6;
    private TextView resendOtpButton;

    // Variables to hold user data passed from Registration_page
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_page);

        TextView notification_page_heading = findViewById(R.id.textView4);
        Utils.applyGradientToText(notification_page_heading, "#04FDAA", "#01D3F8");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Initialize DatabaseReference

        // Retrieve data from the Intent
        verificationId = getIntent().getStringExtra("verificationId");
        mResendToken = getIntent().getParcelableExtra("resendToken");
        phoneNumber = getIntent().getStringExtra("phoneNumber"); // Get phone number for resend
        firstName = getIntent().getStringExtra("firstName");     // Get first name
        lastName = getIntent().getStringExtra("lastName");       // Get last name
        email = getIntent().getStringExtra("email");             // Get email
        location = getIntent().getStringExtra("location");       // Get location


        otpDigit1 = findViewById(R.id.otp_digit_1);
        otpDigit2 = findViewById(R.id.otp_digit_2);
        otpDigit3 = findViewById(R.id.otp_digit_3);
        otpDigit4 = findViewById(R.id.otp_digit_4);
        otpDigit5 = findViewById(R.id.otp_digit_5);
        otpDigit6 = findViewById(R.id.otp_digit_6);

        TextView verifyButton = findViewById(R.id.verify_button);
        verifyButton.setOnClickListener(v -> {
            String otp = otpDigit1.getText().toString() + otpDigit2.getText().toString() +
                    otpDigit3.getText().toString() + otpDigit4.getText().toString() +
                    otpDigit5.getText().toString() + otpDigit6.getText().toString();

            if (otp.length() == 6) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                signInWithPhoneAuthCredential(credential);
            } else {
                Toast.makeText(OtpPage.this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        resendOtpButton = findViewById(R.id.resend_Otp);
        resendOtpButton.setOnClickListener(v -> resendOtp());
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        // OTP verified, now save user data
                        saveUserData(user);
                    } else {
                        Toast.makeText(OtpPage.this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // New method to save user data on OtpPage
    private void saveUserData(FirebaseUser user) {
        String userId = user.getUid();

        // Use the user data retrieved from the Intent
        User userData = new User(firstName, lastName, email, location, user.getPhoneNumber());

        mDatabase.child("users").child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OtpPage.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OtpPage.this, MainActivity.class));
                    finish(); // Finish OtpPage after successful registration
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OtpPage.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Optionally, you might want to log this error to Firebase Crashlytics
                });
    }

    // Function to resend OTP
    private void resendOtp() {
        if (phoneNumber != null && mResendToken != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Toast.makeText(OtpPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                            OtpPage.this.verificationId = verificationId;
                            mResendToken = token;
                            Toast.makeText(OtpPage.this, "OTP resent", Toast.LENGTH_SHORT).show();
                        }
                    },
                    mResendToken
            );
        } else {
            Toast.makeText(this, "Phone number or Resend token is missing", Toast.LENGTH_SHORT).show();
        }
    }
}