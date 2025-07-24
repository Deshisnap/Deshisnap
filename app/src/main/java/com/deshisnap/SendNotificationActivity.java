package com.deshisnap;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.deshisnap.R;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SendNotificationActivity extends AppCompatActivity {

    private CardView notificationAddButton;
    private DatabaseReference notificationsRef; // Firebase Database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_notification_panel);

        // --- IMPORTANT CHANGE HERE ---
        // Initialize Firebase to point to "admin/notifications"
        notificationsRef = FirebaseDatabase.getInstance().getReference("admin").child("notifications");
        // This means data will be saved under:
        // {
        //   "admin": {
        //     "notifications": {
        //       "uniqueNotificationId1": { ... },
        //       "uniqueNotificationId2": { ... }
        //     }
        //   }
        // }


        // Initialize views
        notificationAddButton = findViewById(R.id.notification_add_button);

        // Set OnClickListener for the "+ Add More" button
        notificationAddButton.setOnClickListener(v -> {
            showAddNotificationDialog();
        });

        // You'd also initialize your RecyclerView here for displaying existing notifications
        // RecyclerView notificationRecyclerView = findViewById(R.id.notification_recycler);
        // ... setup adapter, layout manager for RecyclerView
    }

    private void showAddNotificationDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_notification);

        EditText etTitle = dialog.findViewById(R.id.et_notification_title);
        EditText etMessage = dialog.findViewById(R.id.et_notification_message);
        EditText etImageUrl = dialog.findViewById(R.id.et_notification_image_url);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_notification);
        Button btnAdd = dialog.findViewById(R.id.btn_add_notification_dialog);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Title is required");
                return;
            }
            if (TextUtils.isEmpty(message)) {
                etMessage.setError("Message is required");
                return;
            }

            if (!TextUtils.isEmpty(imageUrl) && !android.util.Patterns.WEB_URL.matcher(imageUrl).matches()) {
                etImageUrl.setError("Invalid URL format");
                return;
            }

            addNewNotificationToFirebase(title, message, imageUrl);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addNewNotificationToFirebase(String title, String message, String imageUrl) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Notification newNotification = new Notification(title, message, imageUrl, timestamp);

        String notificationId = notificationsRef.push().getKey();
        if (notificationId != null) {
            notificationsRef.child(notificationId).setValue(newNotification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SendNotificationActivity.this, "Notification added successfully!", Toast.LENGTH_SHORT).show();
                        Log.d("SendNotification", "Notification added to admin/notifications: " + title);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SendNotificationActivity.this, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("SendNotification", "Error adding notification", e);
                    });
        } else {
            Toast.makeText(SendNotificationActivity.this, "Failed to generate notification ID.", Toast.LENGTH_SHORT).show();
        }
    }

    // Notification data class (no change needed here)
    public static class Notification {
        public String title;
        public String message;
        public String imageUrl;
        public String timestamp;

        public Notification() {
        }

        public Notification(String title, String message, String imageUrl, String timestamp) {
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
            this.timestamp = timestamp;
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getImageUrl() { return imageUrl; }
        public String getTimestamp() { return timestamp; }
    }
}
