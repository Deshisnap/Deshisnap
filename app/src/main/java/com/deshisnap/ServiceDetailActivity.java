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
        if (intent != null) {
            // VERIFY THIS LINE: Retrieving the Service object
            Service service = (Service) intent.getSerializableExtra("SERVICE_OBJECT"); // Key must be "SERVICE_OBJECT"

            if (service != null) {
                // If the service object is not null, then populate the views
                // These should now use the 'service' object's getters
                toolbarTitle.setText(service.getName() != null ? service.getName() + " Details" : "Service Details");
                serviceTitle.setText(service.getName());
                serviceRating.setText(service.getRating());
                shortDescription.setText(service.getShortDescription());
                longDescription.setText(service.getLongDescription());
                includedServices.setText(service.getIncludedServices());
                priceText.setText(service.getPrice());

                if (service.getImageResId() != 0) {
                    serviceImage.setImageResource(service.getImageResId());
                } else {
                    // Fallback image if resource ID is 0 or invalid
                    serviceImage.setImageResource(R.drawable.homeservice); // Make sure you have this drawable
                }
            } else {
                // This block will execute if 'service' is null, which means the object wasn't passed correctly
                Toast.makeText(this, "Service details could not be loaded.", Toast.LENGTH_LONG).show();
                // Optionally, set default/error text
                toolbarTitle.setText("Error Loading Details");
                serviceTitle.setText("Service Not Found");
                // ... set other TextViews to indicate an error
            }
        } else {
            // This block executes if the Intent itself is null
            Toast.makeText(this, "No intent data received.", Toast.LENGTH_LONG).show();
            toolbarTitle.setText("Error Loading Details");
            serviceTitle.setText("No Service Selected");
        }

        // --- Set Button Click Listeners ---
        addToCartButton.setOnClickListener(v -> {
            Toast.makeText(ServiceDetailActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
        });

        bookNowButton.setOnClickListener(v -> {
            Toast.makeText(ServiceDetailActivity.this, "Booking Service!", Toast.LENGTH_SHORT).show();
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