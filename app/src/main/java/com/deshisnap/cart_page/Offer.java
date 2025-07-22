package com.deshisnap.cart_page; // Or your models package

public class Offer {
    private String code;
    private String type; // "percentage" or "fixed"
    private double value; // 20 for 20%, 100 for 100 Rs
    private String description;
    private String offerId; // To store Firebase push key, useful for applying

    // Required for Firebase: public no-argument constructor
    public Offer() {
    }

    // Getters for all fields
    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getOfferId() {
        return offerId;
    }

    // Setter for offerId (useful if you set it after fetching)
    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    // You can add setters for other fields if needed, but Firebase usually populates via getters
}
