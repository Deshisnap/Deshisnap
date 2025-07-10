package com.deshisnap;

import java.io.Serializable; // Important: Make it Serializable to pass the object easily via Intent

public class Service implements Serializable {
    private int imageResId;
    private String name;
    private String shortDescription;
    private String longDescription;
    private String price;
    private String rating;
    private String includedServices;

    // Constructor to initialize all service details
    public Service(int imageResId, String name, String shortDescription,
                   String longDescription, String price, String rating,
                   String includedServices) {
        this.imageResId = imageResId;
        this.name = name;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.price = price;
        this.rating = rating;
        this.includedServices = includedServices;
    }

    // --- Getters for all fields ---
    public int getImageResId() {
        return imageResId;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getPrice() {
        return price;
    }

    public String getRating() {
        return rating;
    }

    public String getIncludedServices() {
        return includedServices;
    }

    // You can also add setters if you need to modify service data after creation,
    // but for this use case (displaying static data), getters are sufficient.
}
