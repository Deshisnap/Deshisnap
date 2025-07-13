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

public class ServiceDetailActivity extends AppCompatActivity {

    // Declare a member variable to hold the current Service object
    private Service currentService;

    // Declare UI elements as member variables if they are used outside onCreate
    // For this example, we will keep them local where they are initialized,
    // but currentService is essential for the button logic.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

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
        if (intent != null && intent.hasExtra("SERVICE_OBJECT")) { // Check if extra exists
            // Retrieve the Service object and store it in the member variable
            currentService = (Service) intent.getSerializableExtra("SERVICE_OBJECT");

            if (currentService != null) {
                // Populate the views using the 'currentService' object
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
                    // Fallback image if resource ID is 0 or invalid
                    serviceImage.setImageResource(R.drawable.homeservice); // Make sure you have this drawable
                }
            } else {
                // This block will execute if 'currentService' is null (object wasn't passed correctly)
                Toast.makeText(this, "Service details could not be loaded.", Toast.LENGTH_LONG).show();
                // Optionally, set default/error text and finish activity if data is critical
                toolbarTitle.setText("Error Loading Details");
                serviceTitle.setText("Service Not Found");
                // finish(); // Consider finishing the activity here if service data is essential
            }
        } else {
            // This block executes if the Intent itself is null or "SERVICE_OBJECT" extra is missing
            Toast.makeText(this, "No service data received.", Toast.LENGTH_LONG).show();
            toolbarTitle.setText("Error Loading Details");
            serviceTitle.setText("No Service Selected");
            // finish(); // Consider finishing the activity here if service data is essential
        }

        // --- Set Button Click Listeners ---
        addToCartButton.setOnClickListener(v -> {
            Toast.makeText(ServiceDetailActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
            // TODO: Add logic to add currentService to a cart (e.g., a list in a Singleton or ViewModel)
        });

        // Add logic to the bookNowButton
        bookNowButton.setOnClickListener(v -> {
            // Check if currentService is not null before proceeding
            if (currentService != null) {
                Toast.makeText(ServiceDetailActivity.this, "Booking " + currentService.getName() + "!", Toast.LENGTH_SHORT).show();

                // Create an Intent to start BookingTimePage
                Intent bookingIntent = new Intent(ServiceDetailActivity.this, BookingTimePage.class);

                // Pass the currentService object to the BookingTimePage
                bookingIntent.putExtra("SERVICE_OBJECT", currentService);

                // Start the BookingTimePage Activity
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