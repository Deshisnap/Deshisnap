package com.deshisnap;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.Booking_page.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminViewBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingAdminAdapter adapter;
    private final List<Booking> allBookings = new ArrayList<>();
    private final List<Booking> filteredBookings = new ArrayList<>();
    private DatabaseReference usersRef;
    private Spinner statusFilterSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_booking_status);

        recyclerView = findViewById(R.id.booking_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdminAdapter(filteredBookings);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        setupStatusFilter();
        loadBookings();
    }

    private void setupStatusFilter() {
        statusFilterSpinner = findViewById(R.id.status_filter_spinner);
        if (statusFilterSpinner != null) {
            List<String> options = new ArrayList<>();
            options.add("All");
            options.add("Pending");
            options.add("Confirmed");
            options.add("Rejected");
            ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusFilterSpinner.setAdapter(a);
            statusFilterSpinner.setSelection(0);
            statusFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    applyFilter(options.get(position));
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    applyFilter("All");
                }
            });
        }
    }

    private void applyFilter(String status) {
        filteredBookings.clear();
        if ("All".equals(status)) {
            filteredBookings.addAll(allBookings);
        } else {
            for (Booking b : allBookings) {
                if (status.equalsIgnoreCase(b.getStatus())) {
                    filteredBookings.add(b);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadBookings() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBookings.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    DataSnapshot bookingsNode = userSnap.child("bookings");
                    for (DataSnapshot bookingSnap : bookingsNode.getChildren()) {
                        Booking b = bookingSnap.getValue(Booking.class);
                        if (b != null) {
                            // Ensure bookingId is set from key if missing
                            if (TextUtils.isEmpty(b.getBookingId())) {
                                b.setBookingId(bookingSnap.getKey());
                            }
                            // Ensure userId is set from parent if missing
                            if (TextUtils.isEmpty(b.getUserId())) {
                                b.setUserId(userSnap.getKey());
                            }
                            // Fallback: if serviceNames list is empty, try single field 'serviceName'
                            if ((b.getServiceNames() == null || b.getServiceNames().isEmpty())) {
                                String singleService = bookingSnap.child("serviceName").getValue(String.class);
                                if (!TextUtils.isEmpty(singleService)) {
                                    List<String> one = new ArrayList<>();
                                    one.add(singleService);
                                    b.setServiceNames(one);
                                }
                            }
                            // Pull advanceAmount from node if present
                            Double adv = bookingSnap.child("advanceAmount").getValue(Double.class);
                            if (adv != null) {
                                b.setAdvancePaid(adv);
                            }
                            allBookings.add(b);
                        }
                    }
                }
                // Sort most recent first by timestamp
                Collections.sort(allBookings, new Comparator<Booking>() {
                    @Override
                    public int compare(Booking o1, Booking o2) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    }
                });
                Toast.makeText(AdminViewBookingsActivity.this, "Loaded " + allBookings.size() + " bookings", Toast.LENGTH_SHORT).show();
                applyFilter(statusFilterSpinner != null && statusFilterSpinner.getSelectedItem() != null
                        ? statusFilterSpinner.getSelectedItem().toString() : "All");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminViewBookingsActivity.this, "Failed to load bookings: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private class BookingAdminAdapter extends RecyclerView.Adapter<BookingAdminAdapter.VH> {
        private final List<Booking> data;

        BookingAdminAdapter(List<Booking> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_status_card, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Booking b = data.get(position);

            // Hide Transaction ID row
            h.transitionId.setVisibility(View.GONE);

            // Status chip
            h.statusText.setText(TextUtils.isEmpty(b.getStatus()) ? "Pending" : b.getStatus());

            // Booking type -> show service names joined; if still N/A, show "N/A"
            String services = (b.getServiceNames() != null && !b.getServiceNames().isEmpty()) ? TextUtils.join(", ", b.getServiceNames()) : "N/A";
            h.bookingType.setText("Services: " + services);

            // Date from timestamp if bookingDate is null, plus time slot and remaining
            String displayDate = b.getBookingDate();
            if (TextUtils.isEmpty(displayDate)) {
                long ts = b.getTimestamp();
                if (ts > 0) {
                    displayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date(ts));
                } else {
                    displayDate = "N/A";
                }
            }
            String timeSlot = !TextUtils.isEmpty(b.getTimeSlot()) ? b.getTimeSlot() : "N/A";
            String dt = String.format(Locale.getDefault(), "Date & Time: %s | %s\nAdvance: Rs. %.2f",
                    displayDate, timeSlot, b.getAdvancePaid());
            h.dateTime.setText(dt);

            // Card click -> details dialog
            h.itemView.setOnClickListener(v -> showDetailsDialog(b));

            // Color-code status card
            colorStatusCard(h.statusCard, h.statusText.getText().toString());

            // Status change menu on status card
            h.statusCard.setOnClickListener(v -> showStatusMenu(v, b));

            // Know more button
            h.knowMore.setOnClickListener(v -> showDetailsDialog(b));

            // Open screenshot button
            if (!TextUtils.isEmpty(b.getPaymentScreenshotUrl())) {
                h.openScreenshot.setVisibility(View.VISIBLE);
                h.openScreenshot.setText("See screenshot");
                h.openScreenshot.setOnClickListener(v -> {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(b.getPaymentScreenshotUrl()));
                    startActivity(i);
                });
                h.openScreenshot.setOnLongClickListener(v -> {
                    ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cb != null) {
                        cb.setPrimaryClip(ClipData.newPlainText("screenshot_url", b.getPaymentScreenshotUrl()));
                        Toast.makeText(AdminViewBookingsActivity.this, "Screenshot URL copied", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            } else {
                h.openScreenshot.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView statusText, transitionId, bookingType, dateTime;
            View statusCard;
            ImageView bookedImg;
            TextView knowMore, openScreenshot;
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
    }

    private void colorStatusCard(View statusCard, String status) {
        int color = 0xFFF9FD04; // Pending default yellow
        if ("Confirmed".equalsIgnoreCase(status)) {
            color = 0xFF32EC38; // green
        } else if ("Rejected".equalsIgnoreCase(status)) {
            color = 0xFFFF5252; // red
        }
        if (statusCard instanceof CardView) {
            ((CardView) statusCard).setCardBackgroundColor(color);
        } else {
            statusCard.setBackgroundColor(color);
        }
    }

    private void showStatusMenu(View anchor, Booking b) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Pending");
        menu.getMenu().add("Confirmed");
        menu.getMenu().add("Rejected");
        menu.setOnMenuItemClickListener(item -> {
            updateStatus(b, item);
            return true;
        });
        menu.show();
    }

    private void updateStatus(Booking b, MenuItem item) {
        String newStatus = item.getTitle().toString();
        DatabaseReference global = FirebaseDatabase.getInstance().getReference("bookings").child(b.getBookingId()).child("status");
        DatabaseReference user = FirebaseDatabase.getInstance().getReference("users").child(b.getUserId()).child("bookings").child(b.getBookingId()).child("status");
        global.setValue(newStatus);
        user.setValue(newStatus);
        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private void showDetailsDialog(Booking b) {
        // Fetch user details first
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(b.getUserId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String phone = snapshot.child("phoneNumber").getValue(String.class);

                String name = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
                if (TextUtils.isEmpty(name)) name = "N/A";
                if (TextUtils.isEmpty(email)) email = "N/A";
                if (TextUtils.isEmpty(phone)) phone = "N/A";

                final String fName = name;
                final String fEmail = email;
                final String fPhone = phone;

                // Now fetch full booking node to show all key-values exactly
                DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(b.getUserId()).child("bookings").child(b.getBookingId());
                bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot bookingSnap) {
                        // Build a simple table-like layout programmatically
                        ScrollView scroll = new ScrollView(AdminViewBookingsActivity.this);
                        LinearLayout root = new LinearLayout(AdminViewBookingsActivity.this);
                        root.setOrientation(LinearLayout.VERTICAL);
                        root.setPadding(24, 24, 24, 24);
                        scroll.addView(root);

                        // Header: User info
                        TextView header = new TextView(AdminViewBookingsActivity.this);
                        header.setText("User Details");
                        header.setTextSize(18);
                        header.setPadding(0, 0, 0, 16);
                        root.addView(header);

                        addRow(root, "Name", fName);
                        addRow(root, "Email", fEmail);
                        addRow(root, "Phone", fPhone);

                        TextView sep = new TextView(AdminViewBookingsActivity.this);
                        sep.setText("\nBooking Node Data");
                        sep.setTextSize(16);
                        sep.setPadding(0, 16, 0, 8);
                        root.addView(sep);

                        for (DataSnapshot field : bookingSnap.getChildren()) {
                            String key = field.getKey();
                            Object val = field.getValue();
                            String displayVal;
                            if ("timestamp".equals(key) && val instanceof Long) {
                                displayVal = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                                        .format(new Date((Long) val));
                                addRow(root, key, displayVal);
                            } else if ("serviceNames".equals(key) && field.getValue() instanceof List) {
                                displayVal = TextUtils.join(", ", (List<?>) field.getValue());
                                addRow(root, key, displayVal);
                            } else if ("paymentScreenshotUrl".equals(key) || "screenshotUrl".equals(key)) {
                                String url = String.valueOf(val);
                                if (!TextUtils.isEmpty(url)) {
                                    LinearLayout row = new LinearLayout(AdminViewBookingsActivity.this);
                                    row.setOrientation(LinearLayout.HORIZONTAL);
                                    row.setPadding(0, 8, 0, 8);

                                    TextView k2 = new TextView(AdminViewBookingsActivity.this);
                                    k2.setText(key + ": ");
                                    k2.setTypeface(null, Typeface.BOLD);
                                    k2.setPadding(0, 0, 12, 0);

                                    TextView link = new TextView(AdminViewBookingsActivity.this);
                                    link.setText("Open screenshot");
                                    link.setTextColor(0xFF03A9F4);
                                    link.setOnClickListener(v -> {
                                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        startActivity(i);
                                    });
                                    link.setOnLongClickListener(v -> {
                                        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        if (cb != null) {
                                            cb.setPrimaryClip(ClipData.newPlainText("screenshot_url", url));
                                            Toast.makeText(AdminViewBookingsActivity.this, "Screenshot URL copied", Toast.LENGTH_SHORT).show();
                                        }
                                        return true;
                                    });

                                    row.addView(k2);
                                    row.addView(link);
                                    root.addView(row);
                                } else {
                                    addRow(root, key, "");
                                }
                            } else {
                                displayVal = String.valueOf(val);
                                addRow(root, key, displayVal);
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(AdminViewBookingsActivity.this)
                                .setTitle("Booking Details")
                                .setView(scroll)
                                .setPositiveButton("Close", (d, w) -> d.dismiss());

                        if (!TextUtils.isEmpty(b.getPaymentScreenshotUrl())) {
                            builder.setNeutralButton("Open Screenshot", (d, w) -> {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(b.getPaymentScreenshotUrl()));
                                startActivity(i);
                            });
                        }

                        builder.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminViewBookingsActivity.this, "Failed to load booking node: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminViewBookingsActivity.this, "Failed to load user: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addRow(LinearLayout root, String key, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView k = new TextView(this);
        k.setText(key + ": ");
        k.setTypeface(null, Typeface.BOLD);
        k.setPadding(0, 0, 12, 0);

        TextView v = new TextView(this);
        v.setText(value == null ? "" : value);

        row.addView(k);
        row.addView(v);
        root.addView(row);
    }
}
