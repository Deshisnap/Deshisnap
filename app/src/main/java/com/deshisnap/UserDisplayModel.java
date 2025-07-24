package com.deshisnap;

// This class wraps your basic User object and adds display-specific fields
public class UserDisplayModel {
    private String uid; // We will store the actual Firebase UID here
    private User user; // Your basic User object (firstName, lastName, etc.)
    private String role;
    private boolean isBlocked;
    private String profileImageUrl;

    public UserDisplayModel(String uid, User user, String role, boolean isBlocked, String profileImageUrl) {
        this.uid = uid;
        this.user = user;
        this.role = role;
        this.isBlocked = isBlocked;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters for all fields
    public String getUid() {
        return uid;
    }

    public User getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public boolean getIsBlocked() {
        return isBlocked;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // You might need setters if you want to modify these within the model directly
    // For isBlocked, we'll directly update Firebase, and the UI will refresh.
    public void setIsBlocked(boolean blocked) {
        isBlocked = blocked;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
