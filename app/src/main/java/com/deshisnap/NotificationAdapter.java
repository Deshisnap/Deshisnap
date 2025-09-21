package com.deshisnap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast; // For optional click feedback

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.R;
import com.deshisnap.Notification; // Ensure this path is correct

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList; // Original unfiltered list
    private List<Notification> filteredNotificationList; // List shown in RecyclerView after filter

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        // Copy into internal list to avoid sharing the same reference as caller
        this.notificationList = new ArrayList<>(notificationList);
        this.filteredNotificationList = new ArrayList<>(this.notificationList); // Initialize with all notifications
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your specific notification_component.xml for each item in the list
        View view = LayoutInflater.from(context).inflate(R.layout.notification_component, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = filteredNotificationList.get(position);

        // Set text for the TextViews using data from the Notification object
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTimestamp.setText(notification.getTimestamp());

        // The notification_component.xml provided does not include an ImageView for 'imageUrl',
        // so no image loading logic (e.g., Glide/Picasso) is included here.

        // Optional: Add an item click listener for each notification
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
            // You can add intent logic here to open a detailed notification page if needed
        });
    }

    @Override
    public int getItemCount() {
        return filteredNotificationList.size();
    }

    /**
     * ViewHolder class to hold references to the views in notification_component.xml.
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTimestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            // Link the TextViews to their IDs in notification_component.xml
            tvTitle = itemView.findViewById(R.id.Notification_title);
            tvMessage = itemView.findViewById(R.id.Notification_message);
            tvTimestamp = itemView.findViewById(R.id.Notification_time);
        }
    }

    /**
     * Filters the list of notifications based on the provided search text.
     * This method is called by the UserNotificationPage when the search bar text changes.
     * @param searchText The text entered in the search bar.
     */
    public void filter(String searchText) {
        filteredNotificationList.clear(); // Clear the currently displayed list
        if (searchText.isEmpty()) {
            filteredNotificationList.addAll(notificationList); // If search is empty, show all notifications
        } else {
            searchText = searchText.toLowerCase(Locale.getDefault()); // Convert search text to lowercase
            for (Notification notification : notificationList) {
                // Check if the title or message contains the search text (case-insensitive)
                if ((notification.getTitle() != null && notification.getTitle().toLowerCase(Locale.getDefault()).contains(searchText)) ||
                        (notification.getMessage() != null && notification.getMessage().toLowerCase(Locale.getDefault()).contains(searchText))) {
                    filteredNotificationList.add(notification); // Add matching notifications to the filtered list
                }
            }
        }
        notifyDataSetChanged(); // Notify the RecyclerView to re-draw with the filtered data
    }

    /**
     * Updates the adapter's primary list of notifications.
     * This is typically called after fetching new data from Firebase.
     * @param newNotifications The complete new list of notifications.
     */
    public void setNotifications(List<Notification> newNotifications) {
        // Replace internal list with a copy to avoid mutating caller's list
        this.notificationList = new ArrayList<>(newNotifications);
        filter(""); // Re-apply the filter (or show all if no filter is active) after updating the main list
    }
}
