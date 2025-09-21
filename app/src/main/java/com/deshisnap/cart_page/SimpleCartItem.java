package com.deshisnap.cart_page;

import java.io.Serializable; // Add Serializable if you still pass this via Intent

public class SimpleCartItem implements Serializable { // Keep Serializable if used elsewhere
    private String serviceName;
    private String servicePrice;
    private long timestamp;
    private String cartItemId; // <-- NEW: To store the Firebase key
    // Pricing metadata
    private String pricingType; // per_sqft, flat, custom_quote
    private String displayPrice; // original display string
    private double squareFeet; // for per_sqft
    private double pricePerSqFt; // for per_sqft
    private double grandTotal; // computed total cost if known
    private double advanceAmount; // 10% or fixed 500 for custom


    public SimpleCartItem() {
    }

    public SimpleCartItem(String serviceName, String servicePrice) {
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters ---
    public String getServiceName() { return serviceName; }
    public String getServicePrice() { return servicePrice; }
    public long getTimestamp() { return timestamp; }
    public String getCartItemId() { return cartItemId; } // <-- NEW Getter
    public String getPricingType() { return pricingType; }
    public String getDisplayPrice() { return displayPrice; }
    public double getSquareFeet() { return squareFeet; }
    public double getPricePerSqFt() { return pricePerSqFt; }
    public double getGrandTotal() { return grandTotal; }
    public double getAdvanceAmount() { return advanceAmount; }

    // --- Setters (needed for Firebase to populate and for setting cartItemId) ---
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setServicePrice(String servicePrice) { this.servicePrice = servicePrice; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; } // <-- NEW Setter
    public void setPricingType(String pricingType) { this.pricingType = pricingType; }
    public void setDisplayPrice(String displayPrice) { this.displayPrice = displayPrice; }
    public void setSquareFeet(double squareFeet) { this.squareFeet = squareFeet; }
    public void setPricePerSqFt(double pricePerSqFt) { this.pricePerSqFt = pricePerSqFt; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }
    public void setAdvanceAmount(double advanceAmount) { this.advanceAmount = advanceAmount; }
}
