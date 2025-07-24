package com.deshisnap; // Keep this package as per your file structure

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

// Make sure these imports are correct based on your project structure
// import com.deshisnap.R;
// import com.deshisnap.User; // Your provided User model
// import com.deshisnap.UserDisplayModel; // Our new wrapper model

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// If you use Glide or Picasso for image loading, uncomment and import them:
// import com.bumptech.glide.Glide;
// import com.squareup.picasso.Picasso;

public class user_for_admin_adapter extends RecyclerView.Adapter<user_for_admin_adapter.UserViewHolder> {

    private Context context;
    private List<UserDisplayModel> userList; // Holds all UserDisplayModels (the full, unfiltered list)
    private List<UserDisplayModel> filteredUserList; // Users currently shown in RecyclerView
    private DatabaseReference usersRef;

    // Interface for click events to communicate back to the Activity
    public interface OnUserActionListener {
        void onBlockUnblock(String uid, boolean blockStatus);
        void onDeleteUser(String uid);
    }

    private OnUserActionListener userActionListener;
    private String currentSearchText = ""; // To maintain search state across updates

    public user_for_admin_adapter(Context context, List<UserDisplayModel> userList, OnUserActionListener listener) {
        this.context = context;
        // IMPORTANT: Initialize userList as an empty ArrayList first.
        // The data will be added via setUsers() later.
        this.userList = new ArrayList<>();
        this.filteredUserList = new ArrayList<>(); // Initialize filtered list as empty too
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
        this.userActionListener = listener;

        // If you pass an initial list, populate both lists.
        // This constructor might be called with an empty list from the Activity.
        if (userList != null && !userList.isEmpty()) {
            this.userList.addAll(userList);
            this.filteredUserList.addAll(userList); // Initially, filtered list is the same as full list
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_details_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Ensure position is valid
        if (position < 0 || position >= filteredUserList.size()) {
            Log.e("UserAdapter", "Invalid position in onBindViewHolder: " + position);
            return;
        }

        UserDisplayModel userDisplay = filteredUserList.get(position);
        User user = userDisplay.getUser();

        // Display User Name
        String fullName = "";
        if (user.getFirstName() != null) {
            fullName += user.getFirstName();
        }
        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
            if (!fullName.isEmpty()) fullName += " ";
            fullName += user.getLastName();
        }
        holder.tvName.setText(fullName.isEmpty() ? "N/A" : fullName);

        // Display Phone Number
        holder.tvPhoneNumber.setText("Phone No : " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A"));

        // Display Email
        holder.tvEmail.setText("Email       : " + (user.getEmail() != null ? user.getEmail() : "N/A"));

        // Display Role (from UserDisplayModel)
        holder.tvRole.setText("Role           : " + (userDisplay.getRole() != null ? userDisplay.getRole() : "User"));

        // Load Profile Image (from UserDisplayModel)
        if (!TextUtils.isEmpty(userDisplay.getProfileImageUrl())) {
            // Glide.with(context).load(userDisplay.getProfileImageUrl()).placeholder(R.drawable.electrician).error(R.drawable.electrician).into(holder.ivProfilePic);
            holder.ivProfilePic.setImageResource(R.drawable.electrician); // Use your default placeholder
        } else {
            holder.ivProfilePic.setImageResource(R.drawable.electrician);
        }

        // Handle Block/Unblock Button Visibility and Text (from UserDisplayModel)
        if (userDisplay.getIsBlocked()) {
            holder.blockButton.setVisibility(View.INVISIBLE); // User is blocked, so show Unblock
            holder.unblockButton.setVisibility(View.VISIBLE);
        } else {
            holder.blockButton.setVisibility(View.VISIBLE); // User is not blocked, so show Block
            holder.unblockButton.setVisibility(View.INVISIBLE);
        }

        // Set Click Listeners for buttons
        holder.blockButton.setOnClickListener(v -> userActionListener.onBlockUnblock(userDisplay.getUid(), true));
        holder.unblockButton.setOnClickListener(v -> userActionListener.onBlockUnblock(userDisplay.getUid(), false));
        holder.deleteButton.setOnClickListener(v -> userActionListener.onDeleteUser(userDisplay.getUid()));
    }

    @Override
    public int getItemCount() {
        return filteredUserList.size();
    }

    /**
     * ViewHolder class for user_details_card.xml.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvPhoneNumber, tvEmail;
        ImageView ivProfilePic;
        CardView unblockButton, blockButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.user_name);
            tvRole = itemView.findViewById(R.id.transition_id);
            tvPhoneNumber = itemView.findViewById(R.id.booking_type);
            tvEmail = itemView.findViewById(R.id.email);
            ivProfilePic = itemView.findViewById(R.id.dp_img);

            unblockButton = itemView.findViewById(R.id.unblock_button);
            blockButton = itemView.findViewById(R.id.block_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    /**
     * Filters the list of users based on the provided search text.
     * Searches against User fields and UserDisplayModel fields (role).
     * @param searchText The text entered in the search bar.
     */
    public void filter(String searchText) {
        currentSearchText = searchText; // Store the current search text
        filteredUserList.clear(); // Clear the list being displayed

        if (searchText.isEmpty()) {
            filteredUserList.addAll(userList); // If search is empty, show all users from the original list
        } else {
            searchText = searchText.toLowerCase(Locale.getDefault());
            for (UserDisplayModel userDisplay : userList) { // Iterate over the full userList
                User user = userDisplay.getUser();
                boolean matches = false;

                // Search basic User fields
                if (user.getFirstName() != null && user.getFirstName().toLowerCase(Locale.getDefault()).contains(searchText)) matches = true;
                if (user.getLastName() != null && user.getLastName().toLowerCase(Locale.getDefault()).contains(searchText)) matches = true;
                if (user.getPhoneNumber() != null && user.getPhoneNumber().toLowerCase(Locale.getDefault()).contains(searchText)) matches = true;
                if (user.getEmail() != null && user.getEmail().toLowerCase(Locale.getDefault()).contains(searchText)) matches = true;

                // Search UserDisplayModel-specific fields (like role)
                if (userDisplay.getRole() != null && userDisplay.getRole().toLowerCase(Locale.getDefault()).contains(searchText)) matches = true;

                if (matches) {
                    filteredUserList.add(userDisplay);
                }
            }
        }
        Log.d("UserAdapter", "Filter applied. Filtered list size: " + filteredUserList.size()); // Add a log here
        notifyDataSetChanged(); // Notify the RecyclerView to update
    }

    /**
     * Updates the adapter's primary list of users and re-applies the current filter.
     * This is the method called from ManageUsersActivity after fetching data.
     * @param newUserDisplayModels The complete new list of UserDisplayModel objects.
     */
    public void setUsers(List<UserDisplayModel> newUserDisplayModels) {
        // Clear and update the *original, unfiltered* list (userList)
        this.userList.clear();
        this.userList.addAll(newUserDisplayModels);
        Log.d("UserAdapter", "setUsers called. userList size: " + this.userList.size());

        // Then, re-apply the filter based on the last search text
        // This call will then populate filteredUserList and call notifyDataSetChanged()
        filter(currentSearchText);
    }
}