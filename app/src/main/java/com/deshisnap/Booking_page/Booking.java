package com.deshisnap.Booking_page;

import java.util.List;

public class Booking {
    private String bookingId;
    private String userId;
    private List<String> serviceNames;
    private double grandTotal;
    private String bookingDate;
    private String timeSlot;
    private String paymentScreenshotUrl;
    private String status; // e.g., "Pending", "Confirmed", "Completed", "Cancelled"
    private long timestamp; // To store when the booking was made

    public Booking() {
        // Default constructor required for calls to DataSnapshot.getValue(Booking.class)
    }

    public Booking(String bookingId, String userId, List<String> serviceNames, double grandTotal,
                   String bookingDate, String timeSlot, String paymentScreenshotUrl, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.serviceNames = serviceNames;
        this.grandTotal = grandTotal;
        this.bookingDate = bookingDate;
        this.timeSlot = timeSlot;
        this.paymentScreenshotUrl = paymentScreenshotUrl;
        this.status = status;
        this.timestamp = System.currentTimeMillis(); // Set current time
    }

    // Getters and Setters (Important for Firebase to work correctly)
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getPaymentScreenshotUrl() {
        return paymentScreenshotUrl;
    }

    public void setPaymentScreenshotUrl(String paymentScreenshotUrl) {
        this.paymentScreenshotUrl = paymentScreenshotUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
