package com.deshisnap.cart_page;

import java.io.Serializable; // Add Serializable if you still pass this via Intent

public class SimpleCartItem implements Serializable { // Keep Serializable if used elsewhere
    private String serviceName;
    private String servicePrice;
    private long timestamp;
    private String cartItemId; // <-- NEW: To store the Firebase key


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

    // --- Setters (needed for Firebase to populate and for setting cartItemId) ---
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setServicePrice(String servicePrice) { this.servicePrice = servicePrice; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; } // <-- NEW Setter
}
