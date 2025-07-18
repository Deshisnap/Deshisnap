package com.deshisnap; // Or your models package

public class SimpleCartItem {
    private String serviceName;
    private String servicePrice;
    private long timestamp; // Optional: to know when it was added

    // Required for Firebase: public no-argument constructor
    public SimpleCartItem() {
    }

    // Constructor with parameters
    public SimpleCartItem(String serviceName, String servicePrice) {
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.timestamp = System.currentTimeMillis(); // Set current time
    }

    // Getters for all fields (required by Firebase for reading data)
    public String getServiceName() {
        return serviceName;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // You can add setters if you need to modify these fields later,
    // but for simple "add to cart", getters are sufficient for Firebase.
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setServicePrice(String servicePrice) {
        this.servicePrice = servicePrice;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
