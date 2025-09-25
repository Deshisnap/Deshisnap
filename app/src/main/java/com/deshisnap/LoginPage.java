package com.deshisnap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query; // Import Query
import com.google.firebase.database.ValueEventListener; // Import ValueEventListener
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.concurrent.TimeUnit;

public class LoginPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText phoneNumberInput;
    private TextView sendOtpButton, googleLoginButton, whatsappLoginButton,notAuser;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar loginProgress;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        TextView login_page_heading = findViewById(R.id.login_page_heading);
        Utils.applyGradientToText(login_page_heading, "#FFFFFF", "#FFFFFF");

        notAuser = findViewById(R.id.not_a_user);
        notAuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to registration page
                Intent intent = new Intent(LoginPage.this, Registration_page.class);
                startActivity(intent);
            }
        });


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        phoneNumberInput = findViewById(R.id.phone_number_input);
        sendOtpButton = findViewById(R.id.send_otp);
        googleLoginButton = findViewById(R.id.google_text);
        whatsappLoginButton = findViewById(R.id.whatsapp_text);
        loginProgress = findViewById(R.id.login_progress);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        sendOtpButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber)) {
                phoneNumberInput.setError("Phone number required");
                return;
            }

            // Special admin flow: if phone matches 9348976663, open Admin email/password login
            String digits = phoneNumber.replaceAll("[^0-9]", "");
            if (digits.length() >= 10) {
                String last10 = digits.substring(digits.length() - 10);
                if ("8144524544".equals(last10)) {
                    Intent adminIntent = new Intent(LoginPage.this, AdminLoginActivity.class);
                    startActivity(adminIntent);
                    return;
                }
            }

            // Start the phone verification process
            setLoading(true);
            sendOtp(phoneNumber);
        });

        googleLoginButton.setOnClickListener(v -> {
            setLoading(true);
            googleSignIn();
        });

        whatsappLoginButton.setOnClickListener(v -> {
            Toast.makeText(LoginPage.this, "WhatsApp login not implemented yet.", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "IN");

            if (!phoneNumberUtil.isValidNumber(number)) {
                Toast.makeText(LoginPage.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
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
                            // Auto-verification, sign in and then handle user data persistence
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Log.e("LoginPage", "Phone verification failed: " + e.getMessage(), e);
                            Toast.makeText(LoginPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            setLoading(false);
                        }

                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                            // Pass the formatted phone number and details for potential data saving in OtpPage
                            Intent otpIntent = new Intent(LoginPage.this, OtpPage.class);
                            otpIntent.putExtra("verificationId", verificationId);
                            otpIntent.putExtra("resendToken", token);
                            otpIntent.putExtra("phoneNumber", formattedPhoneNumber);
                            // IMPORTANT: For phone LOGIN, we don't have first/last name, email, location from input fields.
                            // If this OTP is for an existing user logging in, OtpPage needs to handle fetching their data.
                            // If this is a new phone number trying to register via login (not ideal, Registration_page should be for that),
                            // then OtpPage would need to collect that info.
                            // For this "Login Page", we assume data is already registered or will be minimal.
                            // We won't pass firstName, lastName, email, location to OtpPage from here for phone login.
                            startActivity(otpIntent);
                            setLoading(false);
                        }
                    });
        } catch (Exception e) {
            Log.e("LoginPage", "Error processing phone number: " + e.getMessage(), e);
            Toast.makeText(LoginPage.this, "Error processing phone number", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = task.getResult().getUser();
                // For phone login, save/update user data.
                // We only have the phone number from Firebase Auth.
                // If a user with this phone number already exists, update their UID.
                saveOrUpdateUserData(user, null, null, null, user.getPhoneNumber()); // Pass null for other fields
            } else {
                Toast.makeText(LoginPage.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static final int RC_SIGN_IN = 9001;
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e("LoginPage", "Google sign-in failed: " + e.getMessage(), e);
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                // For Google login, extract available details and save/update user data.
                String firstName = (acct.getGivenName() != null) ? acct.getGivenName() : "";
                String lastName = (acct.getFamilyName() != null) ? acct.getFamilyName() : "";
                String email = user.getEmail();
                String phoneNumber = user.getPhoneNumber(); // Will be null initially for Google sign-in

                // Try to parse display name if first/last are missing
                if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && acct.getDisplayName() != null) {
                    String[] parts = acct.getDisplayName().split(" ", 2);
                    if (parts.length > 0) firstName = parts[0];
                    if (parts.length > 1) lastName = parts[1];
                }

                saveOrUpdateUserData(user, firstName, lastName, email, phoneNumber);

            } else {
                Log.e("LoginPage", "Firebase Auth with Google failed: " + task.getException().getMessage(), task.getException());
                Toast.makeText(LoginPage.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
            setLoading(false);
        });
    }

    // --- NEW CORE LOGIC FOR CONSOLIDATING USER DATA ---
    private void saveOrUpdateUserData(FirebaseUser currentUser, String newFirstName, String newLastName, String newEmail, String newPhoneNumber) {
        String currentUid = currentUser.getUid();

        // Check if an existing user entry (by email or phone) exists in the database
        Query queryByEmail = null;
        if (newEmail != null && !TextUtils.isEmpty(newEmail)) {
            queryByEmail = mDatabase.child("users").orderByChild("email").equalTo(newEmail);
        }

        Query queryByPhoneNumber = null;
        if (newPhoneNumber != null && !TextUtils.isEmpty(newPhoneNumber)) {
            queryByPhoneNumber = mDatabase.child("users").orderByChild("phoneNumber").equalTo(newPhoneNumber);
        }

        // Use a flag to track if we've found an existing user
        final boolean[] userFound = {false};

        // First, check by email if available
        if (queryByEmail != null) {
            Query finalQueryByPhoneNumber = queryByPhoneNumber;
            queryByEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userFound[0] = true;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Found an existing user by email
                            String existingUid = snapshot.getKey();
                            Log.d("LoginPage", "Found existing user by email: " + existingUid);
                            updateUserEntry(existingUid, currentUser, newFirstName, newLastName, newEmail, newPhoneNumber);
                            return; // Assuming one unique email per user
                        }
                    } else {
                        // If not found by email, try by phone number
                        if (finalQueryByPhoneNumber != null) {
                            finalQueryByPhoneNumber.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        userFound[0] = true;
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            // Found an existing user by phone number
                                            String existingUid = snapshot.getKey();
                                            Log.d("LoginPage", "Found existing user by phone: " + existingUid);
                                            updateUserEntry(existingUid, currentUser, newFirstName, newLastName, newEmail, newPhoneNumber);
                                            return; // Assuming one unique phone number per user
                                        }
                                    }

                                    // If still not found after checking both email and phone
                                    if (!userFound[0]) {
                                        Log.d("LoginPage", "No existing user found. Creating new entry for UID: " + currentUid);
                                        createNewUserEntry(currentUser, newFirstName, newLastName, newEmail, newPhoneNumber);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("LoginPage", "Database error checking by phone: " + databaseError.getMessage());
                                    handleUserDataSaveFailure(databaseError.getMessage());
                                }
                            });
                        } else {
                            // No email query, and no phone query, so must be a truly new user
                            Log.d("LoginPage", "No email/phone provided. Creating new entry for UID: " + currentUid);
                            createNewUserEntry(currentUser, newFirstName, newLastName, newEmail, newPhoneNumber);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("LoginPage", "Database error checking by email: " + databaseError.getMessage());
                    handleUserDataSaveFailure(databaseError.getMessage());
                }
            });
        } else if (queryByPhoneNumber != null) {
            // Only phone number available, check only by phone
            queryByPhoneNumber.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userFound[0] = true;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String existingUid = snapshot.getKey();
                            Log.d("LoginPage", "Found existing user by phone (only phone check): " + existingUid);
                            updateUserEntry(existingUid, currentUser, newFirstName, newLastName, newEmail, newPhoneNumber);
                            return;
                        }
                    }
                    // If not found by phone
                    if (!userFound[0]) {
                        Log.d("LoginPage", "No existing user found (only phone check). Creating new entry for UID: " + currentUid);
                        createNewUserEntry(currentUser, newFirstName, newLastName, newEmail, newPhoneNumber);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("LoginPage", "Database error checking by phone (only phone check): " + databaseError.getMessage());
                    handleUserDataSaveFailure(databaseError.getMessage());
                }
            });
        } else {
            // Neither email nor phone provided/extracted (rare for login methods)
            // Default to creating a new entry under the current UID
            Log.d("LoginPage", "Neither email nor phone available. Creating new entry for UID: " + currentUid);
            createNewUserEntry(currentUser, newFirstName, newLastName, newEmail, newPhoneNumber);
        }
    }


    // Logic to update an existing user entry (merging new data)
    private void updateUserEntry(String existingUid, FirebaseUser currentUser, String newFirstName, String newLastName, String newEmail, String newPhoneNumber) {
        // Fetch existing data to merge, then update.
        // This is crucial to not lose existing info like location or an old phone number if Google login doesn't provide it.
        mDatabase.child("users").child(existingUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User existingUserData = dataSnapshot.getValue(User.class);
                if (existingUserData == null) {
                    existingUserData = new User(); // Should not happen if dataSnapshot.exists() was true
                }

                // Update fields with new data if available and not empty
                if (newFirstName != null && !TextUtils.isEmpty(newFirstName)) {
                    existingUserData.setFirstName(newFirstName);
                }
                if (newLastName != null && !TextUtils.isEmpty(newLastName)) {
                    existingUserData.setLastName(newLastName);
                }
                if (newEmail != null && !TextUtils.isEmpty(newEmail)) {
                    existingUserData.setEmail(newEmail);
                }
                if (newPhoneNumber != null && !TextUtils.isEmpty(newPhoneNumber)) {
                    existingUserData.setPhoneNumber(newPhoneNumber);
                }

                // Push the updated data to the CURRENT auth UID
                mDatabase.child("users").child(currentUser.getUid()).setValue(existingUserData)
                        .addOnSuccessListener(aVoid -> {
                            if (!existingUid.equals(currentUser.getUid())) {
                                mDatabase.child("users").child(existingUid).removeValue()
                                        .addOnSuccessListener(v -> Log.d("LoginPage", "Removed old UID entry: " + existingUid))
                                        .addOnFailureListener(e -> Log.e("LoginPage", "Failed to remove old UID entry: " + e.getMessage()));
                            }
                            Log.d("LoginPage", "User data updated for UID: " + currentUser.getUid());
                            Toast.makeText(LoginPage.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            redirectAfterLogin();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LoginPage", "Error updating user data: " + e.getMessage(), e);
                            handleUserDataSaveFailure(e.getMessage());
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LoginPage", "Database error fetching existing user data for update: " + databaseError.getMessage());
                handleUserDataSaveFailure(databaseError.getMessage());
            }
        });
    }

    // Logic to create a brand new user entry
    private void createNewUserEntry(FirebaseUser currentUser, String newFirstName, String newLastName, String newEmail, String newPhoneNumber) {
        String userId = currentUser.getUid();
        User userData = new User(newFirstName, newLastName, newEmail, null, newPhoneNumber);
        mDatabase.child("users").child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoginPage", "New user entry created for UID: " + userId);
                    Toast.makeText(LoginPage.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                    redirectAfterLogin();
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginPage", "Error creating new user data: " + e.getMessage(), e);
                    handleUserDataSaveFailure(e.getMessage());
                });
    }

    private void handleUserDataSaveFailure(String errorMessage) {
        Toast.makeText(LoginPage.this, "Error processing login: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean loading) {
        if (loginProgress != null) loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (sendOtpButton != null) sendOtpButton.setEnabled(!loading);
        if (googleLoginButton != null) googleLoginButton.setEnabled(!loading);
        if (whatsappLoginButton != null) whatsappLoginButton.setEnabled(!loading);
    }

    private void redirectAfterLogin() {
        FirebaseUser cu = FirebaseAuth.getInstance().getCurrentUser();
        String email = cu != null ? cu.getEmail() : null;
        String phone = cu != null ? cu.getPhoneNumber() : null; // likely "+91..." for India

        boolean isAdminEmail = email != null && email.equalsIgnoreCase("deshisnap@gmail.com");

        // Compare last 10 digits of phone with admin number
        String adminPhone = "9348976663";
        boolean isAdminPhone = false;
        if (phone != null) {
            String digits = phone.replaceAll("[^0-9]", "");
            if (digits.length() >= 10) {
                String last10 = digits.substring(digits.length() - 10);
                isAdminPhone = adminPhone.equals(last10);
            }
        }

        Intent intent = new Intent(LoginPage.this, (isAdminEmail || isAdminPhone) ? admin_dashboard.class : MainActivity.class);
        startActivity(intent);
        finish();
    }
}