package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.NotificationAdapter; // Ensure this import is correct
import com.deshisnap.Notification;     // Ensure this import is correct
import com.deshisnap.Booking_page.BookingsStatusDetails; // Your existing import
import com.deshisnap.cart_page.CartPage; // Assuming this path is correct
import com.deshisnap.ProfileActivity; // Assuming this path is correct
import com.deshisnap.Utils; // Assuming you have a Utils class with applyGradientToText method

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserNotificationPage extends AppCompatActivity {

    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> allNotificationsList; // Stores all fetched notifications
    private DatabaseReference notificationsRef;

    private EditText searchEditText; // The EditText from your searchbar.xml
    private TextView emptyText; // Empty state label

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_page); // Using your specified layout

        // --- Existing UI setup (gradient, cart button, navbar buttons) ---
        TextView notification_page_heading = findViewById(R.id.notification_page_heading);
        // Ensure Utils.java exists and applyGradientToText method is correctly implemented
        // This will apply the gradient to the "Notifications" heading
        Utils.applyGradientToText(notification_page_heading, "#04FDAA", "#01D3F8");

        // Header location text
        TextView headerLocation = findViewById(R.id.textView2);
        FirebaseUser u = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (headerLocation != null) {
            if (u != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("users").child(u.getUid()).child("location")
                        .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                            @Override public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                String loc = snapshot.getValue(String.class);
                                headerLocation.setText((loc == null || loc.trim().isEmpty()) ? "Please share your locality" : loc);
                            }
                            @Override public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                                headerLocation.setText("Please share your locality");
                            }
                        });
            } else {
                headerLocation.setText("Please share your locality");
            }
        }

        // Click listener for the cart image
        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(UserNotificationPage.this, CartPage.class));
        });

        // Navigation Bar Buttons (assuming navbar.xml is included and these IDs are correct)
        // These will handle navigation to other parts of your app
        findViewById(R.id.home_button).setOnClickListener(v -> {
            // If home is your MainActivity or Dashboard, navigate there.
            // For now, it just finishes this activity. Adjust as per your app's flow.
            // startActivity(new Intent(UserNotificationPage.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(UserNotificationPage.this, BookingsStatusDetails.class));
            finish();
        });
        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(UserNotificationPage.this, ProfileActivity.class));
            finish();
        });
        // --- End of existing UI setup ---


        // --- Firebase, RecyclerView, and Search Setup ---
        // Initialize Firebase database reference to ADMIN notifications path
        // Structure: admin/notifications/{notificationId}
        notificationsRef = FirebaseDatabase.getInstance().getReference("admin").child("notifications");

        // Initialize RecyclerView
        notificationRecyclerView = findViewById(R.id.notification_recycler);
        // Use LinearLayoutManager for a vertical scrolling list
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allNotificationsList = new ArrayList<>(); // Initialize the list to hold notifications
        notificationAdapter = new NotificationAdapter(this, allNotificationsList); // Pass context and list to adapter
        notificationRecyclerView.setAdapter(notificationAdapter); // Set the adapter to the RecyclerView

        // Empty state label
        emptyText = findViewById(R.id.empty_text);

        // Initialize Search Bar EditText from the included searchbar.xml
        // We find the <include> tag by its ID and then find the EditText inside it
        View searchBarInclude = findViewById(R.id.search_bar);
        if (searchBarInclude != null) {
            searchEditText = searchBarInclude.findViewById(R.id.searchEditText); // Match the ID from your searchbar.xml
            if (searchEditText != null) {
                setupSearch(); // Set up the text change listener for search
            } else {
                Log.e("UserNotificationPage", "Error: 'searchEditText' ID not found within the 'search_bar' layout. Check searchbar.xml.");
            }
        } else {
            Log.e("UserNotificationPage", "Error: 'search_bar' include ID not found in notification_page.xml. Search functionality will not work.");
        }

        // Fetch notifications from Firebase when the activity starts
        fetchNotifications();
        // --- End of Firebase, RecyclerView, and Search Setup ---
    }

    /**
     * Fetches notifications from Firebase Realtime Database.
     * It listens for real-time updates to the "admin/notifications" path.
     */
    private void fetchNotifications() {
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // --- Check 1: Does Firebase even find data at this path? ---
                Log.d("FirebaseData", "onDataChange triggered. DataSnapshot exists: " + dataSnapshot.exists() + ", Children count: " + dataSnapshot.getChildrenCount());

                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    Toast.makeText(UserNotificationPage.this, "No notifications available yet.", Toast.LENGTH_SHORT).show();
                    // If this Toast shows, it means Firebase found the path but it's either empty or doesn't exist.
                    // Re-check your Firebase database path and if data is actually there.
                    allNotificationsList.clear();
                    notificationAdapter.setNotifications(new ArrayList<>());
                    if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
                } else {
                    allNotificationsList.clear();

                    // Build temp list with numeric timestamps for sorting
                    class Temp { Notification n; long ts; }
                    List<Temp> temps = new ArrayList<>();

                    String firstTitle = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Read basic fields
                        String title = snapshot.child("title").getValue(String.class);
                        String message = snapshot.child("message").getValue(String.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        if (firstTitle == null && title != null) firstTitle = title;

                        // Accept flexible schema: timestamp can be Number or String
                        Object tsObj = snapshot.child("timestamp").getValue();
                        long tsVal = 0L;
                        String tsStrFallback = null;
                        if (tsObj instanceof Number) {
                            tsVal = ((Number) tsObj).longValue();
                        } else if (tsObj instanceof String) {
                            tsStrFallback = (String) tsObj;
                            try {
                                // try millis numeric string first
                                tsVal = Long.parseLong(tsStrFallback);
                            } catch (Exception ignored) {
                                // try formatted date string e.g. "yyyy-MM-dd HH:mm:ss"
                                try {
                                    SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    Date d = f1.parse(tsStrFallback);
                                    if (d != null) tsVal = d.getTime();
                                } catch (Exception ignored2) {
                                    try {
                                        SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                        Date d2 = f2.parse(tsStrFallback);
                                        if (d2 != null) tsVal = d2.getTime();
                                    } catch (Exception ignored3) {}
                                }
                            }
                        }

                        // Format timestamp for display
                        String displayTime = tsVal > 0
                                ? new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date(tsVal))
                                : (tsStrFallback != null ? tsStrFallback : "");

                        Notification n = new Notification(title, message, imageUrl, displayTime);
                        Temp t = new Temp(); t.n = n; t.ts = tsVal; temps.add(t);
                    }

                    // Sort by most recent first
                    Collections.sort(temps, new Comparator<Temp>() {
                        @Override
                        public int compare(Temp o1, Temp o2) {
                            return Long.compare(o2.ts, o1.ts);
                        }
                    });

                    for (Temp t : temps) { allNotificationsList.add(t.n); }
                    notificationAdapter.setNotifications(allNotificationsList);
                    Log.d("FirebaseData", "Notifications fetched and adapter updated. Total: " + allNotificationsList.size());
                    if (emptyText != null) emptyText.setVisibility(allNotificationsList.isEmpty() ? View.VISIBLE : View.GONE);
                    // Removed debug toast for a cleaner user experience
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // --- Check 4: VERY IMPORTANT! This will tell you if there's a permission error ---
                Log.e("FirebaseData", "Failed to load notifications: " + databaseError.getMessage(), databaseError.toException());
                Toast.makeText(UserNotificationPage.this, "Error loading notifications: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                // If this fires and the message is "Permission denied", it's your Firebase rules.
            }
        });
    }

    /**
     * Sets up the TextWatcher for the search bar to filter notifications.
     */
    private void setupSearch() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used in this implementation
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // When text changes, filter the RecyclerView list
                    notificationAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not used in this implementation
                }
            });
            // Your searchbar.xml does not have a separate clear button ImageView,
            // so we don't set a click listener for clearing search here based on your XML.
        }
    }
}



