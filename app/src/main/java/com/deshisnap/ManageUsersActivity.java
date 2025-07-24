package com.deshisnap; // Adjust package as needed

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.R;
import com.deshisnap.User; // Your provided User model
import com.deshisnap.UserDisplayModel; // Our new wrapper model
import com.deshisnap.Utils; // For gradient text
// IMPORTANT: Change this import to your new adapter name
import com.deshisnap.user_for_admin_adapter; // Your new adapter class

// For navigation (adjust paths as per your project)
import com.deshisnap.Booking_page.BookingsStatusDetails;
import com.deshisnap.cart_page.CartPage;
import com.deshisnap.ProfileActivity;
import com.deshisnap.UserNotificationPage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap; // Make sure this is imported if used elsewhere for claims
import java.util.Map;     // Make sure this is imported if used elsewhere for claims

import com.google.firebase.auth.FirebaseAuth; // For checking authentication
import com.google.firebase.auth.FirebaseUser; // For checking authentication

public class ManageUsersActivity extends AppCompatActivity implements user_for_admin_adapter.OnUserActionListener { // Change adapter interface name

    private RecyclerView userRecyclerView;
    private user_for_admin_adapter userAdapter; // Change adapter type
    private List<UserDisplayModel> allUsersDisplayList;
    private DatabaseReference usersRef;

    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_user);

        TextView booking_page_heading = findViewById(R.id.booking_page_heading);
        Utils.applyGradientToText(booking_page_heading, "#04FDAA", "#01D3F8");

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        userRecyclerView = findViewById(R.id.user_recycler);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allUsersDisplayList = new ArrayList<>();
        // IMPORTANT: Use the new adapter class name here
        userAdapter = new user_for_admin_adapter(this, allUsersDisplayList, this);
        userRecyclerView.setAdapter(userAdapter);

        View searchBarInclude = findViewById(R.id.search_bar);
        if (searchBarInclude != null) {
            searchEditText = searchBarInclude.findViewById(R.id.searchEditText);
            if (searchEditText != null) {
                setupSearch();
            } else {
                Log.e("ManageUsersActivity", "Error: 'searchEditText' ID not found within the 'search_bar' layout. Check searchbar.xml.");
            }
        } else {
            Log.e("ManageUsersActivity", "Error: 'search_bar' include ID not found in activity_manage_users.xml. Search functionality will not work.");
        }

        fetchUsers();

        ImageView cartImg = findViewById(R.id.cart_img);
        if (cartImg != null) {
            cartImg.setOnClickListener(v -> {
                startActivity(new Intent(ManageUsersActivity.this, CartPage.class));
            });
        }


    }

    private void fetchUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUsersDisplayList.clear();

                Log.d("ManageUsersActivity", "onDataChange triggered. Users count: " + dataSnapshot.getChildrenCount());

                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    Toast.makeText(ManageUsersActivity.this, "No users available yet.", Toast.LENGTH_SHORT).show();
                    // Still update adapter to clear any old data
                    userAdapter.setUsers(new ArrayList<>());
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String uid = snapshot.getKey();
                        User user = snapshot.getValue(User.class);

                        if (user != null) {
                            String role = snapshot.child("role").getValue(String.class);
                            Boolean isBlockedBoolean = snapshot.child("isBlocked").getValue(Boolean.class);
                            boolean isBlocked = isBlockedBoolean != null ? isBlockedBoolean : false;

                            String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                            UserDisplayModel userDisplay = new UserDisplayModel(uid, user, role, isBlocked, profileImageUrl);
                            allUsersDisplayList.add(userDisplay);
                            // Log.d("ManageUsersActivity", "Added User to list: " + user.getFirstName() + ". Current list size: " + allUsersDisplayList.size()); // Keep this log for debugging
                            Log.d("ManageUsersActivity", "Parsed User: " + user.getFirstName() + ", Blocked: " + isBlocked + ", Role: " + role + ", UID: " + uid);
                        } else {
                            Log.w("ManageUsersActivity", "Failed to parse User object for UID: " + uid + ". Data might be malformed or User.java mismatch.");
                        }
                    }
                    userAdapter.setUsers(allUsersDisplayList);
                    Log.d("ManageUsersActivity", "Users fetched and adapter updated. Final allUsersDisplayList size: " + allUsersDisplayList.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ManageUsersActivity", "Failed to load users: " + databaseError.getMessage(), databaseError.toException());
                Toast.makeText(ManageUsersActivity.this, "Error loading users: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSearch() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    userAdapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // Implement methods from user_for_admin_adapter.OnUserActionListener interface
    @Override
    public void onBlockUnblock(String uid, boolean blockStatus) {
        new AlertDialog.Builder(this)
                .setTitle(blockStatus ? "Block User" : "Unblock User")
                .setMessage("Are you sure you want to " + (blockStatus ? "block" : "unblock") + " this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    performBlockUnblock(uid, blockStatus);
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onDeleteUser(String uid) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to permanently delete this user's data from the database? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    performDeleteUser(uid);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performBlockUnblock(String uid, boolean blockStatus) {
        usersRef.child(uid).child("isBlocked").setValue(blockStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ManageUsersActivity.this, "User " + (blockStatus ? "blocked" : "unblocked") + " successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManageUsersActivity.this, "Failed to " + (blockStatus ? "block" : "unblock") + " user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ManageUsersActivity", "Error toggling block status for UID " + uid + ": " + e.getMessage());
                });
    }

    private void performDeleteUser(String uid) {
        usersRef.child(uid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ManageUsersActivity.this, "User data deleted from database!", Toast.LENGTH_SHORT).show();
                    Log.d("ManageUsersActivity", "User data deleted for UID: " + uid);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManageUsersActivity.this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ManageUsersActivity", "Error deleting user data for UID " + uid + ": " + e.getMessage());
                });
    }
}