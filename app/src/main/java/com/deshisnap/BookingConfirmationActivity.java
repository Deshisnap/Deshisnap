package com.deshisnap;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.deshisnap.Booking_page.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookingConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "BookingConfirm";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView grandTotalTextView;
    private TextView serviceNamesTextView;
    private ImageView qrCodeImageView;
    private Button uploadScreenshotButton;
    private ImageView screenshotPreviewImageView; // To show the uploaded image
    private EditText dateEditText;
    private Spinner timeSlotSpinner;
    private Button submitBookingButton;
    private boolean hasScreenshot = false;
    private String selectedSlotInternal = null;
    private TextView selectedSlotTextView;
    private View uploadProgressBar;

    private double grandTotal;
    private double totalAdvance; // sum of advances from cart
    private ArrayList<String> serviceNames;
    private Uri paymentScreenshotUri; // URI of the selected image
    private String uploadedScreenshotUrl = null; // URL after uploading to Firebase Storage

    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    private FirebaseAuth mAuth;
    private DatabaseReference bookingsRef; // Reference to store bookings
    private StorageReference storageRef; // Reference for Firebase Storage

    // ActivityResultLauncher for picking images
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation); // Make sure this matches your XML

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        storageRef = FirebaseStorage.getInstance().getReference("payment_screenshots");

        // Initialize UI elements
        grandTotalTextView = findViewById(R.id.grand_total_text_view);
        serviceNamesTextView = findViewById(R.id.service_names_text_view);
        qrCodeImageView = findViewById(R.id.qr_code_image_view);
        uploadScreenshotButton = findViewById(R.id.upload_screenshot_button);
        screenshotPreviewImageView = findViewById(R.id.screenshot_preview_image_view);
        dateEditText = findViewById(R.id.date_edit_text);
        timeSlotSpinner = findViewById(R.id.time_slot_spinner);
        submitBookingButton = findViewById(R.id.submit_booking_button);
        submitBookingButton.setEnabled(false); // disabled until all inputs ready
        selectedSlotTextView = findViewById(R.id.selected_slot_text_view);
        uploadProgressBar = findViewById(R.id.upload_progress_bar);

        // Get data from Intent
        grandTotal = getIntent().getDoubleExtra("GRAND_TOTAL", 0.0);
        totalAdvance = getIntent().getDoubleExtra("TOTAL_ADVANCE", 0.0);
        serviceNames = getIntent().getStringArrayListExtra("SERVICE_NAMES");

        // Show Advance to Pay Now (instead of grand total)
        grandTotalTextView.setText(String.format(Locale.getDefault(), "You have to pay now: Rs. %.2f", totalAdvance));
        if (serviceNames != null && !serviceNames.isEmpty()) {
            serviceNamesTextView.setText("Services: " + String.join(", ", serviceNames));
        } else {
            serviceNamesTextView.setText("Services: N/A");
        }

        // --- Date Picker Setup ---
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // --- Time Slot Spinner Setup ---
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_slots, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);
        timeSlotSpinner.setPrompt("Select Time Slot");
        timeSlotSpinner.setSelection(0, false);
        timeSlotSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String val = parent.getItemAtPosition(position).toString();
                selectedSlotInternal = val;
                if (selectedSlotTextView != null) {
                    selectedSlotTextView.setText("Selected Slot: " + val);
                }
                updateSubmitEnabled();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedSlotInternal = null;
                if (selectedSlotTextView != null) {
                    selectedSlotTextView.setText("Selected Slot: ");
                }
                updateSubmitEnabled();
            }
        });

        // --- QR Code Display ---
        qrCodeImageView.setImageResource(R.drawable.payment_qr_code);
        Utils.applyGradientToText(grandTotalTextView, "#04FDAA", "#01D3F8");
        Utils.applyGradientToText(serviceNamesTextView, "#04FDAA", "#01D3F8");

        // --- Initialize ActivityResultLauncher for image picking ---
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                paymentScreenshotUri = uri;
                screenshotPreviewImageView.setImageURI(paymentScreenshotUri);
                screenshotPreviewImageView.setVisibility(View.VISIBLE);
                hasScreenshot = true;
                updateSubmitEnabled();
                Toast.makeText(this, "Image selected. Click Submit Booking to upload.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Upload Screenshot Button Listener ---
        uploadScreenshotButton.setOnClickListener(v -> {
            checkAndRequestPermissions();
        });

        // --- Submit Booking Button Listener ---
        submitBookingButton.setOnClickListener(v -> {
            submitBooking();
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    dateEditText.setText(dateFormatter.format(calendar.getTime()));
                    updateSubmitEnabled();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
        } else {
            openImagePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to read storage. Cannot upload screenshot.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    private void submitBooking() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to book services.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(BookingConfirmationActivity.this, LoginPage.class));
            finish();
            return;
        }

        String selectedDate = dateEditText.getText().toString().trim();
        String selectedTimeSlot = (selectedSlotInternal == null) ? "" : selectedSlotInternal;

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTimeSlot.isEmpty() || selectedTimeSlot.equals("Select Time Slot")) {
            Toast.makeText(this, "Please select a time slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- MANDATORY SCREENSHOT CHECK ---
        if (paymentScreenshotUri == null) {
            Toast.makeText(this, "Payment screenshot is mandatory. Please upload it.", Toast.LENGTH_LONG).show();
            return; // Stop execution if no screenshot is uploaded
        }

        // If screenshot is selected, proceed with upload
        setLoading(true);
        uploadScreenshot(currentUser.getUid(), selectedDate, selectedTimeSlot);
    }

    private void updateSubmitEnabled() {
        boolean hasDate = dateEditText.getText() != null && !dateEditText.getText().toString().trim().isEmpty();
        boolean hasSlot = selectedSlotInternal != null && !"Select Time Slot".equals(selectedSlotInternal);
        boolean enable = hasDate && hasSlot && hasScreenshot;
        if (submitBookingButton != null) {
            submitBookingButton.setEnabled(enable);
        }
    }

    private void uploadScreenshot(String userId, String date, String timeSlot) {
        // paymentScreenshotUri is guaranteed to be non-null at this point due to the check in submitBooking()
        Toast.makeText(this, "Uploading screenshot...", Toast.LENGTH_SHORT).show();

        StorageReference fileRef = storageRef.child(userId + "_" + System.currentTimeMillis() + ".jpg");

        fileRef.putFile(paymentScreenshotUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedScreenshotUrl = uri.toString();
                    Toast.makeText(BookingConfirmationActivity.this, "Screenshot uploaded successfully!", Toast.LENGTH_SHORT).show();
                    saveBookingToDatabase(userId, date, timeSlot, uploadedScreenshotUrl);
                }).addOnFailureListener(e -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to get uploaded file URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to get download URL", e);
                    setLoading(false);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Screenshot upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Screenshot upload failed", e);
                    setLoading(false);
                });
    }

    private void saveBookingToDatabase(String userId, String date, String timeSlot, String screenshotUrl) {
        String bookingId = bookingsRef.push().getKey();

        if (bookingId == null) {
            Toast.makeText(this, "Failed to generate booking ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking newBooking = new Booking(
                bookingId,
                userId,
                serviceNames,
                grandTotal,
                date,
                timeSlot,
                screenshotUrl,
                "Pending" // Initial status of the booking
        );

        // Compute advance using passed TOTAL_ADVANCE (fallback to 10% if not provided)
        double advancePaid = totalAdvance > 0 ? totalAdvance : Math.max(0.0, grandTotal * 0.10);
        double remainingPayment = Math.max(0.0, grandTotal - advancePaid);
        newBooking.setAdvancePaid(advancePaid);
        newBooking.setRemainingPayment(remainingPayment);

        // Save globally under /bookings
        bookingsRef.child(bookingId).setValue(newBooking)
                .addOnSuccessListener(aVoid -> {
                    // Also save under user's bookings
                    DatabaseReference userBookingRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(userId).child("bookings").child(bookingId);
                    userBookingRef.setValue(newBooking)
                            .addOnSuccessListener(v2 -> {
                                Toast.makeText(BookingConfirmationActivity.this, "Booking submitted successfully!", Toast.LENGTH_LONG).show();
                                clearUserCart(userId);
                                // Navigate to a success page
                                Intent successIntent = new Intent(BookingConfirmationActivity.this, MainActivity.class);
                                startActivity(successIntent);
                                finish();
                            })
                            .addOnFailureListener(e2 -> {
                                Toast.makeText(BookingConfirmationActivity.this, "Saved globally but failed under user: " + e2.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Failed to save user booking", e2);
                                setLoading(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to submit booking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to save booking", e);
                    setLoading(false);
                });
    }

    private void setLoading(boolean loading) {
        if (uploadProgressBar != null) {
            uploadProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (submitBookingButton != null) {
            submitBookingButton.setEnabled(!loading);
        }
        if (uploadScreenshotButton != null) {
            uploadScreenshotButton.setEnabled(!loading);
        }
        if (dateEditText != null) {
            dateEditText.setEnabled(!loading);
        }
        if (timeSlotSpinner != null) {
            timeSlotSpinner.setEnabled(!loading);
        }
    }

    private void clearUserCart(String userId) {
        DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
        userCartRef.removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User cart cleared after booking."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to clear user cart: " + e.getMessage()));
    }
}