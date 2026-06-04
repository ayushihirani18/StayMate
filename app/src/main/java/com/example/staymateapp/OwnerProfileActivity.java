package com.example.staymateapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OwnerProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvPhone, tvCity, tvTotalProperties, tvTotalBookings,tvInitials;
    TextView tvBookingRequests;
    ImageView profileImage, editProfileIcon;
    MaterialButton btnLogout, btnEdit;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);

        // 🔗 Bind Views
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvCity = findViewById(R.id.tvCity);
        tvTotalProperties = findViewById(R.id.tvTotalProperties);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvBookingRequests = findViewById(R.id.tvBookingRequests);
        tvInitials = findViewById(R.id.tvInitials);



        profileImage = findViewById(R.id.profileImage);
        editProfileIcon = findViewById(R.id.editProfileIcon);

        btnLogout = findViewById(R.id.btnLogout);
        btnEdit = findViewById(R.id.btnEdit);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔥 CLOUDINARY INIT (same as tenant)
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");

        try {
            MediaManager.init(this, config);
        } catch (Exception e) {
            // already initialized
        }

        loadOwnerData();
        countOwnerProperties();
        countOwnerBookings();
        countBookingRequests();

        // 🔓 Logout
        btnLogout.setOnClickListener(v -> {

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setIcon(android.R.drawable.ic_dialog_alert)

                    .setPositiveButton("Logout", (dialog, which) -> {

                        mAuth.signOut();

                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })

                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())

                    .show();
        });
        // ✏️ Edit Profile
        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("role", "owner"); // ✅ ADD THIS
            startActivity(intent);
        });

        editProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("role", "owner"); // ✅ ADD THIS
            startActivity(intent);
        });
    }
    private String getInitials(String name) {

        if (name == null || name.isEmpty()) return "";

        String[] parts = name.trim().split(" ");
        String initials = "";

        for (int i = 0; i < parts.length && i < 2; i++) {
            initials += parts[i].charAt(0);
        }

        return initials.toUpperCase();
    }

    private void countOwnerProperties() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("properties")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int count = queryDocumentSnapshots.size();
                    tvTotalProperties.setText("Total Properties: " + count);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOwnerData();
        countOwnerProperties();
        countOwnerBookings();
        countBookingRequests();// 🔥 refresh
    }

    // 🔥 Load Owner Data
    private void loadOwnerData() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String imageUrl = documentSnapshot.getString("profileImage");
                        String phone = documentSnapshot.getString("phone");
                        String city = documentSnapshot.getString("city");
                        String initials = getInitials(name);
                        tvInitials.setText(initials);



                        tvName.setText(name != null ? name : "Owner");
                        tvEmail.setText(email != null ? email : "");
                        tvPhone.setText(phone != null ? "Phone: " + phone : "Phone: Not added");
                        tvCity.setText(city != null ? "City: " + city : "City: Not set");



                        // 🔥 IMAGE LOAD
                        if (imageUrl != null && !imageUrl.isEmpty()) {

                            profileImage.setVisibility(View.VISIBLE);
                            tvInitials.setVisibility(View.GONE);

                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(profileImage);

                        } else {

                            profileImage.setVisibility(View.GONE);
                            tvInitials.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void countBookingRequests() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("bookings")
                .whereEqualTo("ownerId", user.getUid())
                .whereEqualTo("status", "Pending") // 🔥 IMPORTANT
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int count = queryDocumentSnapshots.size();
                    tvBookingRequests.setText("Booking Requests: " + count);
                });
    }
    private void countOwnerBookings() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("bookings")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int count = queryDocumentSnapshots.size();
                    tvTotalBookings.setText("Total Bookings: " + count);
                });
    }

    // 🔥 IMAGE PICK RESULT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImage(imageUri);
        }
    }

    // 🔥 CLOUDINARY UPLOAD
    private void uploadImage(Uri uri) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || uri == null) return;

        MediaManager.get().upload(uri)
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String imageUrl = resultData.get("secure_url").toString();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("profileImage", imageUrl);

                        db.collection("users")
                                .document(user.getUid())
                                .set(map, com.google.firebase.firestore.SetOptions.merge());

                        runOnUiThread(() ->
                                Toast.makeText(OwnerProfileActivity.this, "Image Saved 🔥", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onError(String requestId,
                                        com.cloudinary.android.callback.ErrorInfo error) {

                        runOnUiThread(() ->
                                Toast.makeText(OwnerProfileActivity.this,
                                        "Upload failed: " + error.getDescription(),
                                        Toast.LENGTH_LONG).show()
                        );
                    }

                    @Override
                    public void onReschedule(String requestId,
                                             com.cloudinary.android.callback.ErrorInfo error) {}
                })
                .dispatch();
    }
}
