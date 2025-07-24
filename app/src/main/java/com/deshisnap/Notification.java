package com.deshisnap;
public class Notification {
    public String title;
    public String message;
    public String imageUrl;
    public String timestamp;

    // REQUIRED: Public no-argument constructor for Firebase deserialization
    public Notification() {
    }

    // Constructor with arguments
    public Notification(String title, String message, String imageUrl, String timestamp) {
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getters (recommended for good practice)
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getImageUrl() { return imageUrl; }
    public String getTimestamp() { return timestamp; }
}