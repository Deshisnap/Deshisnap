package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.concurrent.TimeUnit;

public class Registration_page extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText phoneNumberInput, firstNameInput, lastNameInput, emailAddressInput, locationInput;
    private TextView registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);

        // Find the TextView
        TextView login_page_heading = findViewById(R.id.registration_page_heading);
        Utils.applyGradientToText(login_page_heading, "#FFFFFF", "#FFFFFF");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();  // Get a reference to the Realtime Database
        phoneNumberInput = findViewById(R.id.phone_number_input);
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailAddressInput = findViewById(R.id.email_address_input);
        locationInput = findViewById(R.id.Location_input);
        registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim(); // Trim whitespace
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String email = emailAddressInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();

            // Validate all inputs before sending OTP
            if (TextUtils.isEmpty(phoneNumber)) {
                phoneNumberInput.setError("Phone number required");
                return;
            }
            if (TextUtils.isEmpty(firstName)) {
                firstNameInput.setError("First name required");
                return;
            }
            if (TextUtils.isEmpty(lastName)) {
                lastNameInput.setError("Last name required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailAddressInput.setError("Email required");
                return;
            }
            if (TextUtils.isEmpty(location)) {
                locationInput.setError("Location required");
                return;
            }
            // You might want to add more robust email and phone number validation here

            sendOtp(phoneNumber, firstName, lastName, email, location); // Pass all data
        });
    }

    // Modify sendOtp to accept user details
    private void sendOtp(String phoneNumber, String firstName, String lastName, String email, String location) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "IN");

            if (!phoneNumberUtil.isValidNumber(number)) {
                Toast.makeText(Registration_page.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedPhoneNumber = phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    formattedPhoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            // This case handles auto-verification. If it happens, save data here.
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Toast.makeText(Registration_page.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                            Intent otpIntent = new Intent(Registration_page.this, OtpPage.class);
                            otpIntent.putExtra("verificationId", verificationId);
                            otpIntent.putExtra("resendToken", token);
                            otpIntent.putExtra("phoneNumber", phoneNumber); // Pass original phone number for resend
                            otpIntent.putExtra("firstName", firstName);     // Pass first name
                            otpIntent.putExtra("lastName", lastName);       // Pass last name
                            otpIntent.putExtra("email", email);             // Pass email
                            otpIntent.putExtra("location", location);       // Pass location
                            startActivity(otpIntent);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Registration_page.this, "Error processing phone number", Toast.LENGTH_SHORT).show();
        }
    }

    // Keep signInWithPhoneAuthCredential and saveUserData in Registration_page for auto-verification case
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        // Only save user data if auto-verification happens on this page
                        saveUserData(user);
                    } else {
                        Toast.makeText(Registration_page.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user) {
        // This method is only for the auto-verification case in Registration_page
        String userId = user.getUid();
        String firstName = firstNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();
        String email = emailAddressInput.getText().toString();
        String location = locationInput.getText().toString();

        User userData = new User(firstName, lastName, email, location, user.getPhoneNumber());

        mDatabase.child("users").child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Registration_page.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Registration_page.this, MainActivity.class));
                    finish(); // Finish Registration_page to prevent going back
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Registration_page.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
