package com.deshisnap.Booking_page;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.deshisnap.MainActivity;
import com.deshisnap.R;
import com.deshisnap.service_related_work.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID; // For unique file names

public class BookingSummaryActivity extends AppCompatActivity {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;

    private Service bookedService;
    private int year, month, day; // Correct variables for year, month, day
    private String timeSlot;
    private Uri selectedImageUri; // URI of the selected screenshot
    
    // New fields for square foot calculation
    private double squareFeet = 0.0;
    private double pricePerSqFt = 0.0;
    private double grandTotal = 0.0;
    private double advanceAmount = 0.0;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    // UI elements
    private TextView summaryTitle, summaryDate, summaryTime, summaryPrice;
    private Button uploadScreenshotButton, confirmBookingButton;
    private ImageView screenshotPreviewImage;
    private ProgressBar uploadProgressBar;

    // Activity Result Launcher for picking image from gallery
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_summary);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.booking_summary_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize UI elements
        summaryTitle = findViewById(R.id.summary_service_title);
        summaryDate = findViewById(R.id.summary_date);
        summaryTime = findViewById(R.id.summary_time);
        summaryPrice = findViewById(R.id.summary_price);
        uploadScreenshotButton = findViewById(R.id.upload_screenshot_button);
        confirmBookingButton = findViewById(R.id.confirm_booking_button);
        screenshotPreviewImage = findViewById(R.id.screenshot_preview_image);
        uploadProgressBar = findViewById(R.id.upload_progress_bar);

        // Register ActivityResultLauncher for picking image from gallery
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        screenshotPreviewImage.setImageURI(selectedImageUri);
                        screenshotPreviewImage.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
                        screenshotPreviewImage.setVisibility(View.GONE);
                        selectedImageUri = null; // Clear URI if no image selected
                    }
                });

        // Retrieve booking details from Intent
        Intent intent = getIntent();
        if (intent != null) {
            bookedService = (Service) intent.getSerializableExtra("SERVICE_OBJECT");
            year = intent.getIntExtra("SELECTED_YEAR", -1);
            month = intent.getIntExtra("SELECTED_MONTH", -1);
            day = intent.getIntExtra("SELECTED_DAY", -1);
            timeSlot = intent.getStringExtra("SELECTED_TIME_SLOT");
            
            // Get square foot calculation data
            squareFeet = intent.getDoubleExtra("SQUARE_FEET", 0.0);
            pricePerSqFt = intent.getDoubleExtra("PRICE_PER_SQFT", 0.0);
            grandTotal = intent.getDoubleExtra("GRAND_TOTAL", 0.0);
            advanceAmount = intent.getDoubleExtra("ADVANCE_AMOUNT", 0.0);
            String pricingType = intent.getStringExtra("PRICING_TYPE") != null ? intent.getStringExtra("PRICING_TYPE") : "";
            String displayPrice = intent.getStringExtra("DISPLAY_PRICE") != null ? intent.getStringExtra("DISPLAY_PRICE") : "";

            if (bookedService != null && year != -1 && month != -1 && day != -1 && timeSlot != null) {
                populateSummaryDetails(pricingType, displayPrice);
            } else {
                Toast.makeText(this, "Booking details incomplete. Please try again.", Toast.LENGTH_LONG).show();
                finish(); // Go back if data is missing
            }
        } else {
            Toast.makeText(this, "No booking data received. Please try again.", Toast.LENGTH_LONG).show();
            finish();
        }

        // Set up click listeners
        uploadScreenshotButton.setOnClickListener(v -> checkPermissionAndPickImage());
        confirmBookingButton.setOnClickListener(v -> submitBooking());

        // First get the included footer layout
        View footerView = findViewById(R.id.footer_include);

// Then find buttons from that included layout
        footerView.findViewById(R.id.home_button).setOnClickListener(v -> {
            startActivity(new Intent(BookingSummaryActivity.this, MainActivity.class));
            finish();
        });



        footerView.findViewById(R.id.booking_button).setOnClickListener(v -> {
            Toast.makeText(this, "You are currently reviewing your booking.", Toast.LENGTH_SHORT).show();
        });

        footerView.findViewById(R.id.inbox_button).setOnClickListener(v -> {
            Toast.makeText(this, "Inbox Clicked!", Toast.LENGTH_SHORT).show();
        });

        footerView.findViewById(R.id.profile_button).setOnClickListener(v -> {
            Toast.makeText(this, "Profile Clicked!", Toast.LENGTH_SHORT).show();
        });
    }

    private void populateSummaryDetails(String pricingType, String displayPrice) {
        summaryTitle.setText(bookedService.getName());

        Calendar selectedCalendar = Calendar.getInstance();
        // Use 'year', 'month', 'day' to set the calendar instance
        selectedCalendar.set(year, month, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        summaryDate.setText(dateFormat.format(selectedCalendar.getTime()));

        summaryTime.setText(timeSlot);

        // Update price to show detailed calculation based on pricingType
        if ("per_sqft".equals(pricingType) && grandTotal > 0) {
            String priceDetails = String.format("%.1f sq ft × ₹%.0f = ₹%.2f\nAdvance (10%%): ₹%.2f",
                    squareFeet, pricePerSqFt, grandTotal, advanceAmount);
            summaryPrice.setText(priceDetails);
        } else if ("flat".equals(pricingType) && grandTotal > 0) {
            String priceDetails = String.format("Flat Price: %s\nAdvance (10%%): ₹%.2f",
                    bookedService.getPrice(), advanceAmount);
            summaryPrice.setText(priceDetails);
        } else if ("custom_quote".equals(pricingType)) {
            String priceDetails = String.format("Price: %s\nBooking Advance: ₹%.2f",
                    displayPrice.isEmpty() ? bookedService.getPrice() : displayPrice, advanceAmount);
            summaryPrice.setText(priceDetails);
        } else {
            // Fallback to original price string
            summaryPrice.setText(bookedService.getPrice());
        }
    }

    private void checkPermissionAndPickImage() {
        // For Android 13 (API 33) and above, READ_EXTERNAL_STORAGE is not needed for MediaStore.ACTION_PICK_IMAGES
        // However, for older Android versions, or if using ACTION_GET_CONTENT, it's still good to check.
        // For simplicity, we'll check for READ_EXTERNAL_STORAGE for all versions below API 33.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            // No need for READ_EXTERNAL_STORAGE permission for picking images
            pickImageFromGallery();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                pickImageFromGallery();
            }
        }
    }

    private void pickImageFromGallery() {
        // Use "image/*" to filter for all image types
        pickImageLauncher.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied to read external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitBooking() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to submit a booking.", Toast.LENGTH_SHORT).show();
            // Optionally, redirect to login page
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please upload the payment screenshot.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable buttons and show progress
        confirmBookingButton.setEnabled(false);
        uploadScreenshotButton.setEnabled(false);
        uploadProgressBar.setVisibility(View.VISIBLE);

        // 1. Upload screenshot to Firebase Storage
        String fileName = "screenshot_" + UUID.randomUUID().toString() + ".jpg"; // Unique file name
        StorageReference imageRef = mStorageRef.child("screenshots").child(currentUser.getUid()).child(fileName);

        UploadTask uploadTask = imageRef.putFile(selectedImageUri);

        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            uploadProgressBar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully, now get its download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                // 2. Save booking details with image URL to Realtime Database
                saveBookingToDatabase(currentUser.getUid(), imageUrl);
            }).addOnFailureListener(e -> {
                Toast.makeText(BookingSummaryActivity.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                resetUIForFailure();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(BookingSummaryActivity.this, "Screenshot upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resetUIForFailure();
        });
    }

    private void saveBookingToDatabase(String userId, String screenshotUrl) {
        // Create a unique booking ID
        String bookingId = mDatabase.child("users").child(userId).child("bookings").push().getKey();

        // Prepare booking data
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("serviceName", bookedService.getName());
        // *** FIX STARTS HERE ***
        // Use 'year', 'month', 'day' variables which are correctly populated from the Intent
        // 'month + 1' is used because Calendar.MONTH (and Intent data) is 0-indexed (Jan=0),
        // but typically you want 1-indexed for YYYY-MM-DD string representation.
        bookingData.put("date", String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day));
        // *** FIX ENDS HERE ***
        bookingData.put("timeSlot", timeSlot);
        bookingData.put("price", bookedService.getPrice());
        
        // Always store advance amount
        bookingData.put("advanceAmount", advanceAmount);
        // Add square foot calculation data when applicable
        if (grandTotal > 0) {
            bookingData.put("squareFeet", squareFeet);
            bookingData.put("pricePerSqFt", pricePerSqFt);
            bookingData.put("grandTotal", grandTotal);
            bookingData.put("calculatedPrice", String.format("%.1f sq ft × ₹%.0f = ₹%.2f", 
                    squareFeet, pricePerSqFt, grandTotal));
        }
        // Save pricing meta
        bookingData.put("pricingType", getIntent().getStringExtra("PRICING_TYPE"));
        bookingData.put("displayPrice", getIntent().getStringExtra("DISPLAY_PRICE"));
        
        bookingData.put("screenshotUrl", screenshotUrl);
        bookingData.put("timestamp", System.currentTimeMillis()); // For ordering/tracking

        // Save to Firebase Realtime Database
        mDatabase.child("users").child(userId).child("bookings").child(bookingId).setValue(bookingData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookingSummaryActivity.this, "Booking submitted successfully!", Toast.LENGTH_LONG).show();
                    // Navigate to a confirmation page or back to MainActivity
                    Intent mainIntent = new Intent(BookingSummaryActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears activity stack
                    startActivity(mainIntent);
                    finish(); // Finish current activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingSummaryActivity.this, "Failed to save booking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetUIForFailure();
                });
    }

    private void resetUIForFailure() {
        confirmBookingButton.setEnabled(true);
        uploadScreenshotButton.setEnabled(true);
        uploadProgressBar.setVisibility(View.GONE);
        uploadProgressBar.setProgress(0);
    }
}