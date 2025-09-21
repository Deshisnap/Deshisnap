package com.deshisnap;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserBookingsAdapter extends RecyclerView.Adapter<UserBookingsAdapter.VH> {

    public static class Row {
        public List<String> serviceNames = new ArrayList<>();
        public String serviceName;
        public String bookingDate;
        public String timeSlot;
        public double advanceAmount;
        public double calculatedPrice;
        public double grandTotal;
        public double advancePaid; // fallback
        public String status;
        public long timestamp;
    }

    private final List<Row> data;

    public UserBookingsAdapter(List<Row> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_status_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Row r = data.get(position);

        // Hide not-used views
        h.transitionId.setVisibility(View.GONE);
        h.knowMore.setVisibility(View.GONE);
        h.openScreenshot.setVisibility(View.GONE);

        // Status
        String status = TextUtils.isEmpty(r.status) ? "Pending" : r.status;
        h.statusText.setText(status);
        colorStatusCard(h.statusCard, status);

        // Services
        String services;
        if (r.serviceNames != null && !r.serviceNames.isEmpty()) {
            services = TextUtils.join(", ", r.serviceNames);
        } else if (!TextUtils.isEmpty(r.serviceName)) {
            services = r.serviceName;
        } else {
            services = "N/A";
        }
        h.bookingType.setText("Services: " + services);

        // Date
        String displayDate = r.bookingDate;
        if (TextUtils.isEmpty(displayDate) && r.timestamp > 0) {
            displayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date(r.timestamp));
        }
        if (TextUtils.isEmpty(displayDate)) displayDate = "N/A";
        String slot = TextUtils.isEmpty(r.timeSlot) ? "N/A" : r.timeSlot;

        // Remaining = calculatedPrice - advanceAmount (fallbacks to grandTotal/advancePaid)
        double total = (r.calculatedPrice > 0) ? r.calculatedPrice : r.grandTotal;
        double adv = (r.advanceAmount > 0) ? r.advanceAmount : r.advancePaid;
        double remaining = Math.max(0.0, total - adv);

        String dt = String.format(Locale.getDefault(), "Date & Time: %s | %s\nAdvance: Rs. %.2f   Remaining: Rs. %.2f", displayDate, slot, adv, remaining);
        h.dateTime.setText(dt);
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView statusText, transitionId, bookingType, dateTime, knowMore, openScreenshot;
        View statusCard;
        ImageView bookedImg;
        VH(@NonNull View itemView) {
            super(itemView);
            statusText = itemView.findViewById(R.id.status_text);
            transitionId = itemView.findViewById(R.id.transition_id);
            bookingType = itemView.findViewById(R.id.booking_type);
            dateTime = itemView.findViewById(R.id.date_time);
            statusCard = itemView.findViewById(R.id.status_card);
            bookedImg = itemView.findViewById(R.id.booked_img);
            knowMore = itemView.findViewById(R.id.know_more_button);
            openScreenshot = itemView.findViewById(R.id.open_screenshot_button);
        }
    }

    private void colorStatusCard(View statusCard, String status) {
        int color = 0xFFF9FD04; // Pending default yellow
        if ("Confirmed".equalsIgnoreCase(status)) color = 0xFF32EC38;
        else if ("Rejected".equalsIgnoreCase(status)) color = 0xFFFF5252;
        if (statusCard instanceof CardView) {
            ((CardView) statusCard).setCardBackgroundColor(color);
        } else {
            statusCard.setBackgroundColor(color);
        }
    }
}
