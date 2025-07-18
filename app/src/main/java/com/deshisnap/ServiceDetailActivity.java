package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// Firebase Imports
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth; // <-- Import FirebaseAuth

public class ServiceDetailActivity extends AppCompatActivity {

    private Service currentService;

    // Firebase Database Reference
    private DatabaseReference cartDatabaseRef;
    private FirebaseAuth mAuth; // <-- Declare FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        // Initialize Firebase Database and Authentication
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        cartDatabaseRef = firebaseDatabase.getReference("carts"); // Root node for all carts
        mAuth = FirebaseAuth.getInstance(); // <-- Initialize FirebaseAuth

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView serviceImage = findViewById(R.id.image_service);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        TextView serviceTitle = findViewById(R.id.text_detail_service_title);
        TextView serviceRating = findViewById(R.id.text_service_rating);
        TextView shortDescription = findViewById(R.id.text_detail_short_description);
        TextView longDescription = findViewById(R.id.text_detail_long_description);
        TextView includedServices = findViewById(R.id.text_included_services);
        TextView priceText = findViewById(R.id.text_detail_service_price);
        Button addToCartButton = findViewById(R.id.button_detail_add_to_cart);
        Button bookNowButton = findViewById(R.id.button_detail_book_now);

        // --- Get data passed from the previous activity ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SERVICE_OBJECT")) {
            currentService = (Service) intent.getSerializableExtra("SERVICE_OBJECT");

            if (currentService != null) {
                toolbarTitle.setText(currentService.getName() != null ? currentService.getName() + " Details" : "Service Details");
                serviceTitle.setText(currentService.getName());
                serviceRating.setText(currentService.getRating());
                shortDescription.setText(currentService.getShortDescription());
                longDescription.setText(currentService.getLongDescription());
                includedServices.setText(currentService.getIncludedServices());
                priceText.setText(currentService.getPrice());

                if (currentService.getImageResId() != 0) {
                    serviceImage.setImageResource(currentService.getImageResId());
                } else {
                    serviceImage.setImageResource(R.drawable.homeservice);
                }
            } else {
                Toast.makeText(this, "Service details could not be loaded.", Toast.LENGTH_LONG).show();
                toolbarTitle.setText("Error Loading Details");
                serviceTitle.setText("Service Not Found");
            }
        } else {
            Toast.makeText(this, "No service data received.", Toast.LENGTH_LONG).show();
            toolbarTitle.setText("Error Loading Details");
            serviceTitle.setText("No Service Selected");
        }

        // --- Set Button Click Listeners ---
        addToCartButton.setOnClickListener(v -> {
            if (currentService != null) {
                // Check if a user is currently logged in
                if (mAuth.getCurrentUser() != null) {
                    String userId = mAuth.getCurrentUser().getUid(); // Get the current user's ID

                    SimpleCartItem cartItem = new SimpleCartItem(
                            currentService.getName(),
                            currentService.getPrice()
                    );

                    // Get a unique key for the new cart item
                    String cartItemId = cartDatabaseRef.child(userId).push().getKey();

                    if (cartItemId != null) {
                        // Save the cart item under the user's ID
                        cartDatabaseRef.child(userId).child(cartItemId).setValue(cartItem)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ServiceDetailActivity.this, currentService.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
                                    // Optional: Update a cart count display
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ServiceDetailActivity.this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Toast.makeText(ServiceDetailActivity.this, "Error: Could not generate cart item ID.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User is not logged in, prompt them to log in
                    Toast.makeText(ServiceDetailActivity.this, "Please log in to add items to cart.", Toast.LENGTH_LONG).show();
                    // Optional: Redirect to login/signup activity
                    // startActivity(new Intent(ServiceDetailActivity.this, LoginActivity.class));
                }
            } else {
                Toast.makeText(ServiceDetailActivity.this, "Cannot add to cart: Service details missing.", Toast.LENGTH_SHORT).show();
            }
        });

        bookNowButton.setOnClickListener(v -> {
            if (currentService != null) {
                Toast.makeText(ServiceDetailActivity.this, "Booking " + currentService.getName() + "!", Toast.LENGTH_SHORT).show();
                Intent bookingIntent = new Intent(ServiceDetailActivity.this, BookingTimePage.class);
                bookingIntent.putExtra("SERVICE_OBJECT", currentService);
                startActivity(bookingIntent);
            } else {
                Toast.makeText(ServiceDetailActivity.this, "Cannot book: Service details missing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}