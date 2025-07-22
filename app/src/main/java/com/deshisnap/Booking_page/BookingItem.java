package com.deshisnap.Booking_page;

public class BookingItem {
    private String transactionId;
    private String bookingType;
    private String dateTime;
    private String status; // e.g., "Pending"
    private int iconResId; // e.g., R.drawable.whatsapp

    public BookingItem(String transactionId, String bookingType, String dateTime, String status, int iconResId) {
        this.transactionId = transactionId;
        this.bookingType = bookingType;
        this.dateTime = dateTime;
        this.status = status;
        this.iconResId = iconResId;
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getBookingType() { return bookingType; }
    public String getDateTime() { return dateTime; }
    public String getStatus() { return status; }
    public int getIconResId() { return iconResId; }
}

