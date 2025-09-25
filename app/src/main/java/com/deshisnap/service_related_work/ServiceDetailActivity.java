package com.deshisnap.service_related_work;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

// Firebase Imports
import com.deshisnap.Booking_page.BookingTimePage;
import com.deshisnap.R;
import com.deshisnap.Utils;
import com.deshisnap.cart_page.SimpleCartItem;
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

        TextView toolbar_title = findViewById(R.id.toolbar_title);
        Utils.applyGradientToText(toolbar_title, "#04FDAA", "#01D3F8");

        TextView text_detail_service_title = findViewById(R.id.text_detail_service_title);
        Utils.applyGradientToText(text_detail_service_title, "#04FDAA", "#01D3F8");

        TextView text_detail_service_price = findViewById(R.id.text_detail_service_price);
        Utils.applyGradientToText(text_detail_service_price, "#04FDAA", "#01D3F8");

        TextView service_included = findViewById(R.id.service_include);
        Utils.applyGradientToText(service_included, "#04FDAA", "#01D3F8");


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
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(ServiceDetailActivity.this, "Please log in to add items to cart.", Toast.LENGTH_LONG).show();
                    return;
                }

                String priceStr = currentService.getPrice() != null ? currentService.getPrice() : "";
                if (isPerSqFtPrice(priceStr)) {
                    // Ask for sq ft, then add to cart with computed totals
                    showSquareFootDialogForCart();
                } else if (isCustomQuote(priceStr)) {
                    // Custom quote: fixed ₹500 advance
                    addToCartWithValues("custom_quote", 0.0, 0.0, 0.0, 500.0);
                } else {
                    // Flat price: 10% advance
                    double flatPrice = extractPriceFromString(priceStr);
                    if (flatPrice <= 0) {
                        Toast.makeText(ServiceDetailActivity.this, "Invalid price for this service.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double advance = flatPrice * 0.10;
                    addToCartWithValues("flat", 0.0, 0.0, flatPrice, advance);
                }
            } else {
                Toast.makeText(ServiceDetailActivity.this, "Cannot add to cart: Service details missing.", Toast.LENGTH_SHORT).show();
            }
        });

        bookNowButton.setOnClickListener(v -> {
            if (currentService != null) {
                String priceStr = currentService.getPrice() != null ? currentService.getPrice() : "";
                if (isPerSqFtPrice(priceStr)) {
                    // Show dialog to collect square feet for per sq ft pricing
                    showSquareFootDialog();
                } else {
                    // Handle flat price or custom quote without sq ft dialog
                    handleFlatOrCustomBooking(priceStr);
                }
            } else {
                Toast.makeText(ServiceDetailActivity.this, "Cannot book: Service details missing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Variant: dialog that adds to cart instead of navigating
    private void showSquareFootDialogForCart() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_square_foot_input, null);

        EditText editSquareFoot = dialogView.findViewById(R.id.edit_square_foot);
        TextView textPriceCalculation = dialogView.findViewById(R.id.text_price_calculation);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        double pricePerSqFt = extractPriceFromString(currentService.getPrice());
        textPriceCalculation.setText("Price per sq ft: ₹" + (int)pricePerSqFt + "\nTotal: ₹0");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        editSquareFoot.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double sqFt = s.toString().trim().isEmpty() ? 0.0 : Double.parseDouble(s.toString().trim());
                    double total = sqFt * pricePerSqFt;
                    textPriceCalculation.setText("Price per sq ft: ₹" + (int)pricePerSqFt + "\nTotal: ₹" + (int)total);
                } catch (NumberFormatException e) {
                    textPriceCalculation.setText("Price per sq ft: ₹" + (int)pricePerSqFt + "\nTotal: ₹0");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String sqFtText = editSquareFoot.getText().toString().trim();
            if (sqFtText.isEmpty()) {
                Toast.makeText(ServiceDetailActivity.this, "Please enter square footage", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double squareFeet = Double.parseDouble(sqFtText);
                if (squareFeet <= 0) {
                    Toast.makeText(ServiceDetailActivity.this, "Please enter a valid square footage", Toast.LENGTH_SHORT).show();
                    return;
                }
                double grandTotal = squareFeet * pricePerSqFt;
                double advance = grandTotal * 0.10;
                addToCartWithValues("per_sqft", squareFeet, pricePerSqFt, grandTotal, advance);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(ServiceDetailActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void addToCartWithValues(String pricingType, double squareFeet, double pricePerSqFt, double grandTotal, double advanceAmount) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to add items to cart.", Toast.LENGTH_LONG).show();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        SimpleCartItem cartItem = new SimpleCartItem(currentService.getName(), currentService.getPrice());
        cartItem.setPricingType(pricingType);
        cartItem.setDisplayPrice(currentService.getPrice());
        cartItem.setSquareFeet(squareFeet);
        cartItem.setPricePerSqFt(pricePerSqFt);
        cartItem.setGrandTotal(grandTotal);
        cartItem.setAdvanceAmount(advanceAmount);

        String cartItemId = cartDatabaseRef.child(userId).push().getKey();
        if (cartItemId == null) {
            Toast.makeText(this, "Error: Could not generate cart item ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        cartItem.setCartItemId(cartItemId);
        cartDatabaseRef.child(userId).child(cartItemId).setValue(cartItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(ServiceDetailActivity.this, currentService.getName() + " added to cart!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ServiceDetailActivity.this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Handle booking flow for non sq-ft based services:
     * - If Custom Quote: set fixed advance ₹500
     * - Else Flat price: compute 10% advance from parsed price
     */
    private void handleFlatOrCustomBooking(String priceStr) {
        boolean custom = isCustomQuote(priceStr);
        double grandTotal;
        double advanceAmount;

        if (custom) {
            grandTotal = 0.0; // unknown now
            advanceAmount = 500.0; // fixed minimum advance
        } else {
            // Flat price path
            grandTotal = extractPriceFromString(priceStr);
            if (grandTotal <= 0) {
                Toast.makeText(this, "Invalid price for this service.", Toast.LENGTH_SHORT).show();
                return;
            }
            advanceAmount = grandTotal * 0.10; // 10%
        }

        Intent bookingIntent = new Intent(ServiceDetailActivity.this, BookingTimePage.class);
        bookingIntent.putExtra("SERVICE_OBJECT", currentService);
        bookingIntent.putExtra("PRICING_TYPE", custom ? "custom_quote" : "flat");
        bookingIntent.putExtra("DISPLAY_PRICE", priceStr);
        bookingIntent.putExtra("SQUARE_FEET", 0.0);
        bookingIntent.putExtra("PRICE_PER_SQFT", 0.0);
        bookingIntent.putExtra("GRAND_TOTAL", grandTotal);
        bookingIntent.putExtra("ADVANCE_AMOUNT", advanceAmount);
        startActivity(bookingIntent);
    }

    private boolean isPerSqFtPrice(String price) {
        if (price == null) return false;
        String p = price.toLowerCase();
        return p.contains("sq ft") || p.contains("sqft") || p.contains("per sq ft") || p.contains("/sq ft") || p.contains("/sqft");
    }

    private boolean isCustomQuote(String price) {
        if (price == null) return false;
        String p = price.toLowerCase().trim();
        return p.contains("custom quote") || p.equals("custom") || p.equals("quote");
    }

    private void showSquareFootDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_square_foot_input, null);

        // Get references to dialog views
        EditText editSquareFoot = dialogView.findViewById(R.id.edit_square_foot);
        TextView textPriceCalculation = dialogView.findViewById(R.id.text_price_calculation);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // Extract price per square foot from service price
        double pricePerSqFt = extractPriceFromString(currentService.getPrice());
        
        // Update price calculation text initially
        textPriceCalculation.setText("Price per sq ft: ₹" + (int)pricePerSqFt + "\nTotal: ₹0");

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Add text watcher to calculate total in real time
        editSquareFoot.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    try {
                        double sqFt = Double.parseDouble(s.toString().trim());
                        double total = sqFt * pricePerSqFt;
                        textPriceCalculation.setText("Price per sq ft: ₹" + (int)pricePerSqFt + 
                                "\nTotal: ₹" + (int)total);
                    } catch (NumberFormatException e) {
                        textPriceCalculation.setText("Price per sq ft: ₹" + (int)pricePerSqFt + 
                                "\nTotal: ₹0");
                    }
                } else {
                    textPriceCalculation.setText("Price per sq ft: ₹" + (int)pricePerSqFt + 
                            "\nTotal: ₹0");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String sqFtText = editSquareFoot.getText().toString().trim();
            if (sqFtText.isEmpty()) {
                Toast.makeText(ServiceDetailActivity.this, "Please enter square footage", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double squareFeet = Double.parseDouble(sqFtText);
                if (squareFeet <= 0) {
                    Toast.makeText(ServiceDetailActivity.this, "Please enter a valid square footage", Toast.LENGTH_SHORT).show();
                    return;
                }

                double grandTotal = squareFeet * pricePerSqFt;
                double advanceAmount = grandTotal * 0.10; // 10% advance

                // Navigate to BookingTimePage with the calculated values
                Intent bookingIntent = new Intent(ServiceDetailActivity.this, BookingTimePage.class);
                bookingIntent.putExtra("SERVICE_OBJECT", currentService);
                bookingIntent.putExtra("PRICING_TYPE", "per_sqft");
                bookingIntent.putExtra("DISPLAY_PRICE", currentService.getPrice());
                bookingIntent.putExtra("SQUARE_FEET", squareFeet);
                bookingIntent.putExtra("PRICE_PER_SQFT", pricePerSqFt);
                bookingIntent.putExtra("GRAND_TOTAL", grandTotal);
                bookingIntent.putExtra("ADVANCE_AMOUNT", advanceAmount);
                startActivity(bookingIntent);
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(ServiceDetailActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private double extractPriceFromString(String priceString) {
        // Extract numeric value from price string like "₹ 500 onwards" or "₹500"
        if (priceString == null) return 150.0; // Default price
        
        // Remove currency symbols, spaces, and text
        String numericPart = priceString.replaceAll("[^0-9.]", "");
        
        try {
            return Double.parseDouble(numericPart);
        } catch (NumberFormatException e) {
            return 150.0; // Default price if parsing fails
        }
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
