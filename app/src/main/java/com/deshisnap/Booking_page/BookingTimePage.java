package com.deshisnap.Booking_page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Add this import
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.deshisnap.MainActivity;
import com.deshisnap.R;
import com.deshisnap.service_related_work.Service;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingTimePage extends AppCompatActivity {

    private static final String TAG = "BookingTimePage"; // Use a TAG for logging

    private Service bookedService;
    private RelativeLayout currentSelectedDateContainer = null;
    private View currentSelectedDateOverlay = null;
    private TextView currentSelectedTimeSlot = null;
    private int selectedYear, selectedMonth, selectedDayOfMonth;
    private String selectedTimeSlot = "";
    
    // Pricing type handling
    private String pricingType = ""; // values: per_sqft (implicit), flat, custom_quote
    private String displayPrice = "";

    // New fields for square foot calculation
    private double squareFeet = 0.0;
    private double pricePerSqFt = 0.0;
    private double grandTotal = 0.0;
    private double advanceAmount = 0.0;

    private TextView[] weekdays = new TextView[7];
    private TextView[] dates = new TextView[7];
    private TextView[] months = new TextView[7];
    private RelativeLayout[] dateContainers = new RelativeLayout[7];
    private View[] overlays = new View[7];

    private TextView[] timeSlots = new TextView[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.booking_time_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.booking_time_page_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SERVICE_OBJECT")) {
            bookedService = (Service) intent.getSerializableExtra("SERVICE_OBJECT");
            
            // Get square foot calculation data
            squareFeet = intent.getDoubleExtra("SQUARE_FEET", 0.0);
            pricePerSqFt = intent.getDoubleExtra("PRICE_PER_SQFT", 0.0);
            grandTotal = intent.getDoubleExtra("GRAND_TOTAL", 0.0);
            advanceAmount = intent.getDoubleExtra("ADVANCE_AMOUNT", 0.0);
            pricingType = intent.getStringExtra("PRICING_TYPE") != null ? intent.getStringExtra("PRICING_TYPE") : "";
            displayPrice = intent.getStringExtra("DISPLAY_PRICE") != null ? intent.getStringExtra("DISPLAY_PRICE") : "";
            
            TextView headingText = findViewById(R.id.heading_text);
            if (bookedService != null) {
                headingText.setText(bookedService.getName() + " Booking");
            } else {
                headingText.setText("Service Booking");
            }
            
            // Update the advance amount display
            updateAdvanceAmountDisplay();
        } else {
            Toast.makeText(this, "Error: Service details not received.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Initialize and populate date selection UI
        initializeDateViews(); // This is where the problem likely is
        populateDates();
        setupDateClickListeners();

        // 3. Initialize and setup time slot click listeners
        initializeTimeSlotViews();
        setupTimeSlotClickListeners();

        // 4. Set up "Next" Button Click Listener
        findViewById(R.id.next).setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked.");
            Log.d(TAG, "currentSelectedDateContainer is null: " + (currentSelectedDateContainer == null));
            Log.d(TAG, "currentSelectedTimeSlot is null: " + (currentSelectedTimeSlot == null));

            if (currentSelectedDateContainer == null) {
                Toast.makeText(BookingTimePage.this, "Please select a booking date.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentSelectedTimeSlot == null) {
                Toast.makeText(BookingTimePage.this, "Please select a booking time slot.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "All selections made. Proceeding to BookingSummaryActivity.");
            Intent summaryIntent = new Intent(BookingTimePage.this, BookingSummaryActivity.class);
            summaryIntent.putExtra("SERVICE_OBJECT", bookedService);
            summaryIntent.putExtra("SELECTED_YEAR", selectedYear);
            summaryIntent.putExtra("SELECTED_MONTH", selectedMonth);
            summaryIntent.putExtra("SELECTED_DAY", selectedDayOfMonth);
            summaryIntent.putExtra("SELECTED_TIME_SLOT", selectedTimeSlot);
            
            // Pass square foot calculation data
            summaryIntent.putExtra("SQUARE_FEET", squareFeet);
            summaryIntent.putExtra("PRICE_PER_SQFT", pricePerSqFt);
            summaryIntent.putExtra("GRAND_TOTAL", grandTotal);
            summaryIntent.putExtra("ADVANCE_AMOUNT", advanceAmount);
            // Pass pricing meta
            summaryIntent.putExtra("PRICING_TYPE", pricingType);
            summaryIntent.putExtra("DISPLAY_PRICE", displayPrice);

            startActivity(summaryIntent);
        });

        // 5. Setup other UI elements' click listeners (e.g., header, footer)
        findViewById(R.id.cart_img).setOnClickListener(v -> {
            Toast.makeText(this, "Cart Clicked!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.offer_numbers).setOnClickListener(v -> {
            Toast.makeText(this, "Need Help? Clicked!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.home_button).setOnClickListener(v -> {
            startActivity(new Intent(BookingTimePage.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.booking_button).setOnClickListener(v -> {
            Toast.makeText(BookingTimePage.this, "You are currently in the booking process.", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            Toast.makeText(this, "Inbox Clicked!", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            Toast.makeText(this, "Profile Clicked!", Toast.LENGTH_SHORT).show();
        });

        // Load Admin QR into ImageView if present
        ImageView qrImg = findViewById(R.id.qr_code_img);
        if (qrImg != null) {
            loadAdminQrInto(qrImg);
        }
    }

    private void initializeDateViews() {
        for (int i = 0; i < 7; i++) {
            int weekdayId = getResources().getIdentifier("weekday" + (i + 1), "id", getPackageName());
            int dateId = getResources().getIdentifier("date" + (i + 1), "id", getPackageName());
            int monthId = getResources().getIdentifier("month" + (i + 1), "id", getPackageName());
            int containerId = getResources().getIdentifier("date_slot_container_" + (i + 1), "id", getPackageName()); // ✅ FIXED spelling
            int overlayId = getResources().getIdentifier("overlay" + (i + 1), "id", getPackageName());

            weekdays[i] = findViewById(weekdayId);
            if (weekdays[i] == null) Log.e(TAG, "Error: Weekday TextView " + (i + 1) + " NOT FOUND!");

            dates[i] = findViewById(dateId);
            if (dates[i] == null) Log.e(TAG, "Error: Date TextView " + (i + 1) + " NOT FOUND!");

            months[i] = findViewById(monthId);
            if (months[i] == null) Log.e(TAG, "Error: Month TextView " + (i + 1) + " NOT FOUND!");

            dateContainers[i] = findViewById(containerId);  // ✅ FIXED index here
            if (dateContainers[i] == null) {
                Log.e(TAG, "CRITICAL ERROR: Date Container RelativeLayout " + (i + 1) + " NOT FOUND!");
            } else {
                Log.d(TAG, "Found dateContainer " + (i + 1));
            }

            overlays[i] = findViewById(overlayId);  // ✅ FIXED index here
            if (overlays[i] == null) {
                Log.e(TAG, "Error: Overlay View " + (i + 1) + " NOT FOUND!");
            } else {
                Log.d(TAG, "Found overlay " + (i + 1));
            }
        }
        Log.d(TAG, "initializeDateViews completed.");
    }

    private void populateDates() {
        // ... (rest of your populateDates method, no changes needed here)
        Calendar calendar = Calendar.getInstance(); // Gets current date and time
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // e.g., "THU"
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault()); // e.g., "11"
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault()); // e.g., "JUL"

        for (int i = 0; i < 7; i++) {
            // Set text for each date slot
            if (weekdays[i] != null) weekdays[i].setText(dayFormat.format(calendar.getTime()).toUpperCase());
            if (dates[i] != null) dates[i].setText(dateFormat.format(calendar.getTime()));
            if (months[i] != null) months[i].setText(monthFormat.format(calendar.getTime()).toUpperCase());

            // Set the first day as initially selected
            if (i == 0) {
                if (dateContainers[0] != null) { // CRITICAL CHECK: If dateContainers[0] is null, this won't run
                    currentSelectedDateContainer = dateContainers[0];
                    currentSelectedDateContainer.setBackgroundResource(R.drawable.time_slot_selected_bg); // Apply selected background
                    currentSelectedDateOverlay = overlays[0]; // Store the overlay
                    if (currentSelectedDateOverlay != null) {
                        currentSelectedDateOverlay.setVisibility(View.VISIBLE); // Show the overlay
                    }

                    // Store the initial selected date values
                    selectedYear = calendar.get(Calendar.YEAR);
                    selectedMonth = calendar.get(Calendar.MONTH);
                    selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    Log.d(TAG, "Initial date selected: " + selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear);
                } else {
                    Log.e(TAG, "CRITICAL: dateContainers[0] is null in populateDates, initial selection failed!");
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to the next day for the next iteration
        }
    }

    private void setupDateClickListeners() {
        for (int i = 0; i < dateContainers.length; i++) {
            final int index = i;
            if (dateContainers[i] != null) { // Ensure the view exists
                dateContainers[i].setOnClickListener(v -> {
                    Log.d(TAG, "Date slot " + (index + 1) + " clicked.");
                    // ... (rest of your click listener logic for dates)
                    if (currentSelectedDateContainer != null) {
                        currentSelectedDateContainer.setBackgroundResource(R.drawable.time_slot_bg);
                        if (currentSelectedDateOverlay != null) {
                            currentSelectedDateOverlay.setVisibility(View.GONE);
                        }
                    }

                    currentSelectedDateContainer = (RelativeLayout) v;
                    currentSelectedDateContainer.setBackgroundResource(R.drawable.time_slot_selected_bg);
                    currentSelectedDateOverlay = overlays[index];
                    if (currentSelectedDateOverlay != null) {
                        currentSelectedDateOverlay.setVisibility(View.VISIBLE);
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, index);
                    selectedYear = cal.get(Calendar.YEAR);
                    selectedMonth = cal.get(Calendar.MONTH);
                    selectedDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                    Toast.makeText(BookingTimePage.this, "Selected Date: " +
                            weekdays[index].getText().toString() + ", " +
                            dates[index].getText().toString() + " " +
                            months[index].getText().toString(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.e(TAG, "ERROR: Cannot set click listener for dateContainer " + (i + 1) + " because it's null.");
            }
        }
    }

    private void initializeTimeSlotViews() {
        // ... (rest of your initializeTimeSlotViews method)
        timeSlots[0] = findViewById(R.id.time_7am_8am);
        if (timeSlots[0] == null) Log.e(TAG, "Time slot 7am-8am (ID: time_7am_8am) NOT FOUND!");
        timeSlots[1] = findViewById(R.id.time_8am_9am);
        if (timeSlots[1] == null) Log.e(TAG, "Time slot 8am-9am (ID: time_8am_9am) NOT FOUND!");
        timeSlots[2] = findViewById(R.id.time_9am_10am);
        if (timeSlots[2] == null) Log.e(TAG, "Time slot 9am-10am (ID: time_9am_10am) NOT FOUND!");
        timeSlots[3] = findViewById(R.id.time_10am_11am);
        if (timeSlots[3] == null) Log.e(TAG, "Time slot 10am-11am (ID: time_10am_11am) NOT FOUND!");
        timeSlots[4] = findViewById(R.id.time_11am_12pm);
        if (timeSlots[4] == null) Log.e(TAG, "Time slot 11am-12pm (ID: time_11am_12pm) NOT FOUND!");
        timeSlots[5] = findViewById(R.id.time_12pm_1pm);
        if (timeSlots[5] == null) Log.e(TAG, "Time slot 12pm-1pm (ID: time_12pm_1pm) NOT FOUND!");
        timeSlots[6] = findViewById(R.id.time_3pm_4pm);
        if (timeSlots[6] == null) Log.e(TAG, "Time slot 3pm-4pm (ID: time_3pm_4pm) NOT FOUND!");
        timeSlots[7] = findViewById(R.id.time_4pm_5pm);
        if (timeSlots[7] == null) Log.e(TAG, "Time slot 4pm-5pm (ID: time_4pm_5pm) NOT FOUND!");
        timeSlots[8] = findViewById(R.id.time_5pm_6pm);
        if (timeSlots[8] == null) Log.e(TAG, "Time slot 5pm-6pm (ID: time_5pm_6pm) NOT FOUND!");
        Log.d(TAG, "initializeTimeSlotViews completed.");
    }

    private void setupTimeSlotClickListeners() {
        for (TextView timeSlot : timeSlots) {
            if (timeSlot != null) {
                timeSlot.setOnClickListener(v -> {
                    Log.d(TAG, "Time slot " + ((TextView)v).getText() + " clicked.");
                    if (currentSelectedTimeSlot != null) {
                        currentSelectedTimeSlot.setBackgroundResource(R.drawable.time_slot_bg);
                    }

                    currentSelectedTimeSlot = (TextView) v;
                    currentSelectedTimeSlot.setBackgroundResource(R.drawable.time_slot_selected_bg);
                    selectedTimeSlot = currentSelectedTimeSlot.getText().toString();

                    Toast.makeText(BookingTimePage.this, "Selected Time: " + selectedTimeSlot, Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.e(TAG, "ERROR: Cannot set click listener for a null time slot.");
            }
        }
    }

    private void updateAdvanceAmountDisplay() {
        TextView grandTotalToPayText = findViewById(R.id.grand_total_to_pay_text);
        if (grandTotalToPayText == null) return;

        if ("custom_quote".equals(pricingType)) {
            // Fixed minimum advance for custom quotes
            grandTotalToPayText.setText("You have to pay this: ₹" + String.format("%.2f", advanceAmount) + " (Booking advance)");
        } else if (advanceAmount > 0 && grandTotal > 0) {
            // Per-sqft or flat total with known grandTotal
            grandTotalToPayText.setText("You have to pay this: ₹" + String.format("%.2f", advanceAmount) +
                    " (10% advance of ₹" + String.format("%.2f", grandTotal) + ")");
        } else if (advanceAmount > 0) {
            // Fallback – show just the advance
            grandTotalToPayText.setText("You have to pay this: ₹" + String.format("%.2f", advanceAmount));
        } else {
            // Nothing to show
            grandTotalToPayText.setText("You have to pay this: ₹0.00");
        }
    }

    private void loadAdminQrInto(final ImageView target) {
        final long MAX_DOWNLOAD_BYTES = 2 * 1024 * 1024; // 2MB
        final View progress = findViewById(R.id.qr_progress);

        // 1) Try cache first
        try {
            java.io.File cacheFile = new java.io.File(getFilesDir(), "admin_qr_cache.jpg");
            if (cacheFile.exists()) {
                android.graphics.Bitmap cached = android.graphics.BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                if (cached != null) {
                    target.setImageBitmap(cached);
                }
            }
        } catch (Exception ignored) {}

        if (progress != null) progress.setVisibility(View.VISIBLE);
        DatabaseReference urlRef = FirebaseDatabase.getInstance().getReference("admin").child("qr").child("url");
        urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String url = snapshot.getValue(String.class);
                if (url == null || url.isEmpty()) {
                    Log.d(TAG, "No admin QR URL set.");
                    if (progress != null) progress.setVisibility(View.GONE);
                    return;
                }
                // If we already have a cached file AND URL hasn't changed, skip network
                try {
                    java.io.File cacheFile = new java.io.File(getFilesDir(), "admin_qr_cache.jpg");
                    String lastUrl = getSharedPreferences("cache", MODE_PRIVATE)
                            .getString("admin_qr_url", null);
                    if (cacheFile.exists() && url.equals(lastUrl)) {
                        if (progress != null) progress.setVisibility(View.GONE);
                        return; // already set from cache above
                    }
                } catch (Exception ignored) {}
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("admin/qr/qr.jpg");
                storageRef.getBytes(MAX_DOWNLOAD_BYTES)
                        .addOnSuccessListener(bytes -> {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            target.setImageBitmap(bmp);
                            // 2) Save to cache for next loads
                            try {
                                java.io.File cacheFile = new java.io.File(getFilesDir(), "admin_qr_cache.jpg");
                                java.io.FileOutputStream fos = new java.io.FileOutputStream(cacheFile);
                                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                fos.flush();
                                fos.close();
                                getSharedPreferences("cache", MODE_PRIVATE)
                                        .edit()
                                        .putString("admin_qr_url", url)
                                        .apply();
                            } catch (Exception e) {
                                Log.w(TAG, "Failed to cache admin QR: " + e.getMessage());
                            }
                            if (progress != null) progress.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to load admin QR image: " + e.getMessage());
                            if (progress != null) progress.setVisibility(View.GONE);
                        });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read admin QR URL: " + error.getMessage());
                if (progress != null) progress.setVisibility(View.GONE);
            }
        });
    }
}