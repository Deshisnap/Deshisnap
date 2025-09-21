package com.deshisnap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.Booking_page.Booking;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.VH> {
    private final List<Booking> bookings = new ArrayList<>();
    private final Context context;

    public AdminBookingAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Booking> list) {
        bookings.clear();
        if (list != null) bookings.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_booking_status_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Booking b = bookings.get(position);
        holder.transactionId.setText("Transaction ID : " + (b.getBookingId() == null ? "-" : b.getBookingId()));
        holder.bookingType.setText("Booking Type : " + joinServiceNames(b));
        holder.dateTime.setText("Date & Time : " + (b.getBookingDate() + " " + b.getTimeSlot()));
        holder.bookedImg.setImageResource(R.drawable.homeservice);

        // Uploaded file open
        holder.uploadedFileButton.setOnClickListener(v -> {
            if (b.getPaymentScreenshotUrl() != null && !b.getPaymentScreenshotUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(b.getPaymentScreenshotUrl()));
                context.startActivity(intent);
            }
        });

        // Status dropdown
        String[] statuses = new String[]{"Pending", "Confirmed", "Rejected"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, statuses);
        holder.statusDropdown.setAdapter(adapter);
        holder.statusDropdown.setText(b.getStatus(), false);

        holder.statusDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            String sel = statuses[pos];
            updateStatus(b, sel);
        });
    }

    private void updateStatus(Booking b, String status) {
        b.setStatus(status);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        if (b.getBookingId() == null || b.getUserId() == null) return;
        root.child("bookings").child(b.getBookingId()).child("status").setValue(status);
        root.child("users").child(b.getUserId()).child("bookings").child(b.getBookingId()).child("status").setValue(status);
        notifyDataSetChanged();
    }

    private String joinServiceNames(Booking b) {
        if (b.getServiceNames() == null || b.getServiceNames().isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.getServiceNames().size(); i++) {
            sb.append(b.getServiceNames().get(i));
            if (i < b.getServiceNames().size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView bookedImg;
        TextView transactionId;
        TextView bookingType;
        TextView dateTime;
        CardView uploadedFileButton;
        AutoCompleteTextView statusDropdown;

        public VH(@NonNull View itemView) {
            super(itemView);
            bookedImg = itemView.findViewById(R.id.booked_img);
            transactionId = itemView.findViewById(R.id.transition_id);
            bookingType = itemView.findViewById(R.id.booking_type);
            dateTime = itemView.findViewById(R.id.date_time);
            uploadedFileButton = itemView.findViewById(R.id.uploaded_file_button);
            statusDropdown = itemView.findViewById(R.id.statusDropdown);
        }
    }
}
