package com.deshisnap;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class BookingViewHolder extends RecyclerView.ViewHolder {
    ImageView icon;
    TextView statusText;
    TextView transactionIdText;
    TextView bookingTypeText;
    TextView dateTimeText;

    public BookingViewHolder(View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.booked_img); // NOTE: You should set an id for ImageView in XML, e.g., android:id="@+id/icon"
        statusText = itemView.findViewById(R.id.status_text);
        transactionIdText = itemView.findViewById(R.id.transition_id);
        bookingTypeText = itemView.findViewById(R.id.booking_type);
        dateTimeText = itemView.findViewById(R.id.date_time);
    }
}

