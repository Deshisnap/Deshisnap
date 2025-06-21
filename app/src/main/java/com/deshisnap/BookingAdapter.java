package com.deshisnap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingViewHolder> {
    private List<BookingItem> bookingItems;

    public BookingAdapter(List<BookingItem> bookingItems) {
        this.bookingItems = bookingItems;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.booking_status_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingItem item = bookingItems.get(position);
        holder.icon.setImageResource(item.getIconResId());
        holder.statusText.setText(item.getStatus());
        holder.transactionIdText.setText("Transaction ID : " + item.getTransactionId());
        holder.bookingTypeText.setText("Booking Type : " + item.getBookingType());
        holder.dateTimeText.setText("Date & Time : " + item.getDateTime());
    }

    @Override
    public int getItemCount() {
        return bookingItems.size();
    }


}

