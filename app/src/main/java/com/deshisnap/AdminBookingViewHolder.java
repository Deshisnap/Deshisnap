package com.deshisnap;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class AdminBookingViewHolder extends RecyclerView.ViewHolder {
    ImageView bookedImg;
    TextView transactionId;
    TextView bookingType;
    TextView dateTime;
    CardView uploadedFileButton;
    TextView statusDropdown; // Will be AutoCompleteTextView in layout; we treat as TextView for setText

    public AdminBookingViewHolder(@NonNull View itemView) {
        super(itemView);
        bookedImg = itemView.findViewById(R.id.booked_img);
        transactionId = itemView.findViewById(R.id.transition_id);
        bookingType = itemView.findViewById(R.id.booking_type);
        dateTime = itemView.findViewById(R.id.date_time);
        uploadedFileButton = itemView.findViewById(R.id.uploaded_file_button);
        statusDropdown = itemView.findViewById(R.id.statusDropdown);
    }
}
