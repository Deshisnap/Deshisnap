package com.deshisnap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Admin_Manage_QR extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 1010;
    private static final long MAX_DOWNLOAD_BYTES = 2 * 1024 * 1024; // 2 MB

    private ImageView ivQrPreview;
    private Button btnPrimary;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private DatabaseReference adminQrRef;
    private StorageReference qrStorageRef;

    private String currentQrUrl = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_qr);

        ivQrPreview = findViewById(R.id.iv_qr_preview);
        btnPrimary = findViewById(R.id.btn_primary);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);

        adminQrRef = FirebaseDatabase.getInstance().getReference("admin").child("qr");
        qrStorageRef = FirebaseStorage.getInstance().getReference().child("admin/qr/qr.jpg");

        btnPrimary.setOnClickListener(v -> onPrimaryClick());

        loadExistingQr();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPrimary.setEnabled(!loading);
    }

    private void updateUiForState() {
        if (currentQrUrl == null || currentQrUrl.isEmpty()) {
            tvStatus.setText("No QR uploaded");
            btnPrimary.setText("Add QR");
        } else {
            tvStatus.setText("QR uploaded");
            btnPrimary.setText("Remove QR");
        }
    }

    private void loadExistingQr() {
        setLoading(true);
        adminQrRef.child("url").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentQrUrl = snapshot.getValue(String.class);
                if (currentQrUrl != null && !currentQrUrl.isEmpty()) {
                    // Load image bytes from storage and preview
                    qrStorageRef.getBytes(MAX_DOWNLOAD_BYTES)
                            .addOnSuccessListener(bytes -> {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ivQrPreview.setImageBitmap(bmp);
                                updateUiForState();
                                setLoading(false);
                            })
                            .addOnFailureListener(e -> {
                                updateUiForState();
                                setLoading(false);
                            });
                } else {
                    updateUiForState();
                    setLoading(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_Manage_QR.this, "Failed to load QR info", Toast.LENGTH_SHORT).show();
                setLoading(false);
                updateUiForState();
            }
        });
    }

    private void onPrimaryClick() {
        if (currentQrUrl == null || currentQrUrl.isEmpty()) {
            pickImage();
        } else {
            deleteQr();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select QR Image"), REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                ivQrPreview.setImageURI(uri); // local preview
                uploadQr(uri);
            }
        }
    }

    private void uploadQr(Uri fileUri) {
        setLoading(true);
        qrStorageRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        qrStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                adminQrRef.child("url").setValue(url)
                                        .addOnSuccessListener(unused -> {
                                            currentQrUrl = url;
                                            // Refresh preview from storage to ensure final image shows
                                            qrStorageRef.getBytes(MAX_DOWNLOAD_BYTES)
                                                    .addOnSuccessListener(bytes -> {
                                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                        ivQrPreview.setImageBitmap(bmp);
                                                    });
                                            Toast.makeText(Admin_Manage_QR.this, "QR uploaded", Toast.LENGTH_SHORT).show();
                                            setLoading(false);
                                            updateUiForState();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Admin_Manage_QR.this, "Failed to save QR URL", Toast.LENGTH_SHORT).show();
                                            setLoading(false);
                                            updateUiForState();
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Admin_Manage_QR.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                setLoading(false);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Admin_Manage_QR.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
    }

    private void deleteQr() {
        setLoading(true);
        qrStorageRef.delete()
                .addOnSuccessListener(unused -> adminQrRef.child("url").removeValue()
                        .addOnSuccessListener(unused1 -> {
                            currentQrUrl = null;
                            ivQrPreview.setImageResource(R.drawable.electrician);
                            Toast.makeText(Admin_Manage_QR.this, "QR removed", Toast.LENGTH_SHORT).show();
                            setLoading(false);
                            updateUiForState();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Admin_Manage_QR.this, "Failed to update database", Toast.LENGTH_SHORT).show();
                            setLoading(false);
                        }))
                .addOnFailureListener(e -> {
                    // Even if file not found, clear DB url
                    adminQrRef.child("url").removeValue();
                    currentQrUrl = null;
                    ivQrPreview.setImageResource(R.drawable.electrician);
                    Toast.makeText(Admin_Manage_QR.this, "QR reference cleared", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                    updateUiForState();
                });
    }
}