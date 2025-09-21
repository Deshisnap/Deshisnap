package com.deshisnap.cart_page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.BookingConfirmationActivity;
import com.deshisnap.Booking_page.BookingsStatusDetails;
import com.deshisnap.LoginPage;
import com.deshisnap.MainActivity;
import com.deshisnap.ProfileActivity;
import com.deshisnap.UserNotificationPage;
import com.deshisnap.R;
import com.deshisnap.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartPage extends AppCompatActivity implements CartAdapter.OnItemDeleteListener, OffersAdapter.OnOfferApplyListener {

    private static final String TAG = "CartPage";

    private FirebaseAuth mAuth;
    private DatabaseReference cartItemsRef;
    private DatabaseReference adminOffersRef;

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<SimpleCartItem> cartList;

    private RecyclerView offersRecyclerView;
    private OffersAdapter offersAdapter;
    private List<Offer> offersList;

    private TextView finalGrandTotalTextView;
    private TextView totalAdvanceTextView;
    private TextView noOffersTextView;

    private double cartTotalPrice = 0.0;
    private double currentDiscount = 0.0;
    private Offer appliedOffer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_page);

        // Apply gradients to text (existing functionality)
        TextView coupon_offer_text = findViewById(R.id.coupon_offer_text);
        if (coupon_offer_text != null) {
            Utils.applyGradientToText(coupon_offer_text, "#04FDAA", "#01D3F8");
        }

        TextView payment_text = findViewById(R.id.payment_text);
        if (payment_text != null) {
            Utils.applyGradientToText(payment_text, "#04FDAA", "#01D3F8");
        }

        // Apply EdgeToEdge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cart_page_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view your cart.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(CartPage.this, LoginPage.class));
            finish();
            return;
        }

        // Initialize Firebase Database References
        cartItemsRef = FirebaseDatabase.getInstance().getReference("carts").child(currentUser.getUid());
        adminOffersRef = FirebaseDatabase.getInstance().getReference("admin_data").child("offers");

        // Initialize RecyclerView for Cart Items
        cartRecyclerView = findViewById(R.id.cart_items_recycler_view);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartList, this);
        cartRecyclerView.setAdapter(cartAdapter);

        // Initialize RecyclerView for Offers
        offersRecyclerView = findViewById(R.id.offers_recycler_view);
        offersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        offersList = new ArrayList<>();
        offersAdapter = new OffersAdapter(offersList, this);
        offersRecyclerView.setAdapter(offersAdapter);

        noOffersTextView = findViewById(R.id.no_offers_text);

        // Initialize total and advance TextViews
        finalGrandTotalTextView = findViewById(R.id.final_grand_total_text_view);
        totalAdvanceTextView = findViewById(R.id.total_advance_text_view);

        // --- MODIFIED: Setup click listener for "Proceed to Pay" button ---
        findViewById(R.id.proceed_to_pay_button).setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(CartPage.this, "Your cart is empty! Add items before proceeding.", Toast.LENGTH_SHORT).show();
            } else {
                // Prepare data to pass to BookingConfirmationActivity
                double finalAmount = cartTotalPrice - currentDiscount;
                ArrayList<String> serviceNames = new ArrayList<>();
                for (SimpleCartItem item : cartList) {
                    serviceNames.add(item.getServiceName());
                }

                // Compute total advance from items
                double advanceSum = 0.0;
                for (SimpleCartItem i : cartList) advanceSum += Math.max(0.0, i.getAdvanceAmount());

                Intent intent = new Intent(CartPage.this, BookingConfirmationActivity.class);
                intent.putExtra("GRAND_TOTAL", finalAmount);
                intent.putExtra("TOTAL_ADVANCE", advanceSum);
                intent.putStringArrayListExtra("SERVICE_NAMES", serviceNames);
                startActivity(intent);
                // Optionally finish this activity if you don't want to come back to cart
                // finish();
            }
        });

        // Load cart items and offers from Firebase
        loadCartItems();
        loadAdminOffers();

        // --- Existing Navigation Buttons (retain their logic) ---
        // --- Bottom Navigation Button Listeners ---


        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(CartPage.this, BookingsStatusDetails.class));
        });
        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(CartPage.this, UserNotificationPage.class));
        });

        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(CartPage.this, ProfileActivity.class));
        });
    }

    private void loadCartItems() {
        cartItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartList.clear();
                cartTotalPrice = 0.0;
                double advanceSum = 0.0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SimpleCartItem item = snapshot.getValue(SimpleCartItem.class);
                    if (item != null) {
                        item.setCartItemId(snapshot.getKey());
                        cartList.add(item);
                        // Prefer computed grandTotal when available; else parse servicePrice
                        double itemTotal;
                        if (item.getGrandTotal() > 0) {
                            itemTotal = item.getGrandTotal();
                        } else {
                            try {
                                String priceStr = item.getServicePrice() == null ? "0" : item.getServicePrice().replaceAll("[^\\d.]", "");
                                itemTotal = Double.parseDouble(priceStr);
                            } catch (NumberFormatException e) {
                                itemTotal = 0.0;
                                Log.e(TAG, "Error parsing price for " + item.getServiceName() + ": " + item.getServicePrice(), e);
                            }
                        }
                        cartTotalPrice += itemTotal;
                        // Sum advances
                        if (item.getAdvanceAmount() > 0) {
                            advanceSum += item.getAdvanceAmount();
                        }
                    }
                }
                cartAdapter.notifyDataSetChanged();
                updatePaymentSummary(advanceSum);

                if (cartList.isEmpty()) {
                    // Handle empty cart state if needed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read cart items.", databaseError.toException());
                Toast.makeText(CartPage.this, "Failed to load cart items: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAdminOffers() {
        adminOffersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                offersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Offer offer = snapshot.getValue(Offer.class);
                    if (offer != null) {
                        offer.setOfferId(snapshot.getKey());
                        offersList.add(offer);
                    }
                }
                if (offersList.isEmpty()) {
                    noOffersTextView.setVisibility(View.VISIBLE);
                    offersRecyclerView.setVisibility(View.GONE);
                } else {
                    noOffersTextView.setVisibility(View.GONE);
                    offersRecyclerView.setVisibility(View.VISIBLE);
                    offersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read offers.", databaseError.toException());
                Toast.makeText(CartPage.this, "Failed to load offers: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePaymentSummary(double advanceSum) {
        applyOfferDiscount(appliedOffer);

        double finalPrice = cartTotalPrice - currentDiscount;

        if (finalGrandTotalTextView != null) {
            finalGrandTotalTextView.setText(String.format(Locale.getDefault(), "Rs. %.2f", finalPrice));
        }
        if (totalAdvanceTextView != null) {
            totalAdvanceTextView.setText(String.format(Locale.getDefault(), "Rs. %.2f", advanceSum));
        }
    }

    @Override
    public void onDeleteClick(int position) {
        SimpleCartItem itemToDelete = cartList.get(position);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && itemToDelete != null && itemToDelete.getCartItemId() != null) {
            cartItemsRef.child(itemToDelete.getCartItemId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CartPage.this, itemToDelete.getServiceName() + " removed.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CartPage.this, "Failed to remove item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to remove cart item: " + itemToDelete.getCartItemId(), e);
                    });
        } else {
            Toast.makeText(CartPage.this, "Error: Cannot delete item. User not logged in or item ID missing.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOfferApply(Offer offer) {
        appliedOffer = offer;
        // Recompute totals by triggering listener implicitly or do quick refresh here
        // We'll just call updatePaymentSummary with current summed advances from list
        double advanceSum = 0.0;
        for (SimpleCartItem i : cartList) advanceSum += Math.max(0.0, i.getAdvanceAmount());
        updatePaymentSummary(advanceSum);
        Toast.makeText(this, "Applied offer: " + offer.getDescription(), Toast.LENGTH_SHORT).show();
    }

    private void applyOfferDiscount(Offer offer) {
        if (offer == null) {
            currentDiscount = 0.0;
            return;
        }

        if (offer.getType() != null && offer.getType().equalsIgnoreCase("percentage")) {
            currentDiscount = cartTotalPrice * (offer.getValue() / 100.0);
        } else if (offer.getType() != null && offer.getType().equalsIgnoreCase("fixed")) {
            currentDiscount = offer.getValue();
            if (currentDiscount > cartTotalPrice) {
                currentDiscount = cartTotalPrice;
            }
        } else {
            currentDiscount = 0.0;
        }
        if (currentDiscount < 0) {
            currentDiscount = 0;
        }
    }
}