package com.example.staymateapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.staymateapp.adapters.ReviewAdapter;
import com.example.staymateapp.models.Review;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;
import com.example.staymateapp.adapters.ImageSliderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.appbar.MaterialToolbar;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PropertyDetailsActivity extends AppCompatActivity {

    TextView txtName, txtLocation, txtRent, txtType, txtGender;
    Button btnBookNow, btnCallOwner, btnWhatsappOwner, btnChatOwner, btnAddReview;
    String propertyType, gender;

    String propertyName, location, rent;
    ArrayList<String> imageUrls;
    int minRent, maxRent;
    String contact, ownerId, propertyId;
    String bookingId = null;
    RecyclerView recyclerReviews;

    TextView tvOverallRating, tvTotalReviews;

    List<Review> reviewList;
    ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        // 🔹 Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // 🔹 Views
        txtName = findViewById(R.id.txtPropertyName);
        txtLocation = findViewById(R.id.txtLocation);
        txtRent = findViewById(R.id.txtRent);
        txtType = findViewById(R.id.txtType);
        txtGender = findViewById(R.id.txtGender);

        btnAddReview = findViewById(R.id.btnAddReview);

        btnBookNow = findViewById(R.id.btnBookNow);
        btnCallOwner = findViewById(R.id.btnCallOwner);
        btnWhatsappOwner = findViewById(R.id.btnWhatsappOwner);
        btnChatOwner = findViewById(R.id.btnChatOwner);
        recyclerReviews = findViewById(R.id.recyclerReviews);
        tvOverallRating = findViewById(R.id.tvOverallRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);


// 🔥 Recycler setup
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(reviewAdapter);
        propertyType = getIntent().getStringExtra("propertyType");
        gender = getIntent().getStringExtra("gender");
        propertyId = getIntent().getStringExtra("propertyId");

        if (propertyId != null) {
            propertyId = propertyId.trim(); // 🔥 IMPORTANT
        }
        android.util.Log.d("DEBUG", "PropertyDetails ID = " + propertyId);

        // 🔹 Get Intent Data
        propertyName = getIntent().getStringExtra("propertyName");
        location = getIntent().getStringExtra("location");
        rent = getIntent().getStringExtra("rent");
        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        ownerId = getIntent().getStringExtra("ownerId");
        contact = getIntent().getStringExtra("contact");
        minRent = getIntent().getIntExtra("minRent", 0);
        maxRent = getIntent().getIntExtra("maxRent", 0);

        // ✅ SAFETY FIX (VERY IMPORTANT)
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }

        // 🔹 Set UI safely
        txtName.setText(propertyName != null ? propertyName : "N/A");
        txtLocation.setText(location != null ? location : "Location not available");

        txtType.setText("Type: " + (propertyType != null ? propertyType : "N/A"));
        txtGender.setText("Gender: " + (gender != null ? gender : "Any"));

        if (minRent > 0 && maxRent > 0) {
            txtRent.setText("₹ " + minRent + " - ₹ " + maxRent);
        } else if (minRent > 0) {
            txtRent.setText("₹ " + minRent);
        } else {
            txtRent.setText("Not available");
        }

        // 🔥 IMAGE SLIDER
        ViewPager2 viewPager = findViewById(R.id.viewPagerImages);
        WormDotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);

        ImageSliderAdapter adapter = new ImageSliderAdapter(imageUrls, false);
        viewPager.setAdapter(adapter);
        dotsIndicator.setViewPager2(viewPager);

        // 🔥 CALL OWNER
        btnCallOwner.setOnClickListener(v -> {
            if (contact == null || contact.isEmpty()) {
                Toast.makeText(this, "Contact not available", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact));
            startActivity(intent);
        });

        // 🔥 WHATSAPP OWNER
        btnWhatsappOwner.setOnClickListener(v -> {
            if (contact == null || contact.isEmpty()) {
                Toast.makeText(this, "Contact not available", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://wa.me/" + contact.replace("+", "");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.whatsapp");

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // 🔥 CHAT OWNER
        btnChatOwner.setOnClickListener(v -> {
            if (ownerId == null || ownerId.isEmpty()) {
                Toast.makeText(this, "Owner not available", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(PropertyDetailsActivity.this, ChatActivity.class);
            intent.putExtra("userId", ownerId);
            startActivity(intent);
        });

        btnAddReview.setOnClickListener(v -> {

            Intent intent = new Intent(this, com.example.staymateapp.ActivityReviews.class);
            intent.putExtra("propertyId", propertyId);
            startActivity(intent);
        });

        // 🔥 BOOK BUTTON
        checkIfAlreadyBooked();

        btnBookNow.setOnClickListener(v -> {

            if (bookingId != null) {

                // 🔥 SHOW CONFIRMATION DIALOG
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Cancel Booking")
                        .setMessage("Are you sure you want to cancel this booking?")
                        .setPositiveButton("Yes", (d, w) -> cancelBooking())
                        .setNegativeButton("No", null)
                        .show();

            } else {
                bookProperty();
            }
        });
        loadReviews();
    }
    private void loadReviews() {

        if (propertyId == null) {
            Toast.makeText(this, "Property ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("reviews")
                .whereEqualTo("propertyId", propertyId)
                .get()
                .addOnSuccessListener(query -> {

                    reviewList.clear();
                    float total = 0;

                    for (DocumentSnapshot doc : query) {

                        Review review = doc.toObject(Review.class);

                        if (review != null) {
                            review.setId(doc.getId());
                            reviewList.add(review);
                            total += review.getRating();
                        }
                    }

                    reviewAdapter = new ReviewAdapter(reviewList);
                    recyclerReviews.setAdapter(reviewAdapter);

                    int count = reviewList.size();

                    if (count > 0) {
                        float avg = total / count;
                        tvOverallRating.setText(String.format("%.1f ⭐", avg));
                        tvTotalReviews.setText("Based on " + count + " reviews");
                    } else {
                        tvOverallRating.setText("0.0 ⭐");
                        tvTotalReviews.setText("No reviews yet");
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIfAlreadyBooked() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("propertyId", propertyId)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(query -> {

                    if (!query.isEmpty()) {

                        // ✅ Get booking document ID
                        bookingId = query.getDocuments().get(0).getId();

                        btnBookNow.setEnabled(true);
                        btnBookNow.setText("Cancel Booking"); // 🔥 CHANGE TEXT
                    }
                });
    }

    private void bookProperty() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .addOnSuccessListener(query -> {

                    if (!query.isEmpty()) {
                        Toast.makeText(this, "Already requested", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(doc -> {

                                String userName = doc.getString("name");

                                Map<String, Object> booking = new HashMap<>();
                                booking.put("userId", userId);
                                booking.put("name", userName);

                                // ✅ FULL DATA SAVE
                                booking.put("propertyId", propertyId);
                                booking.put("propertyName", propertyName);
                                booking.put("location", location);
                                booking.put("rent", rent);
                                booking.put("status", "Pending");

                                booking.put("ownerId", ownerId);
                                booking.put("contact", contact);

                                booking.put("timestamp", System.currentTimeMillis());
                                booking.put("imageUrls", imageUrls);

                                FirebaseFirestore.getInstance()
                                        .collection("bookings")
                                        .add(booking)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Booking Requested", Toast.LENGTH_SHORT).show();
                                            btnBookNow.setEnabled(false);
                                            btnBookNow.setText("Already Requested");
                                        });
                            });
                });
    }
    private void cancelBooking() {

        if (bookingId == null) return;

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .document(bookingId)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Booking Cancelled", Toast.LENGTH_SHORT).show();

                    // 🔄 Reset button
                    bookingId = null;
                    btnBookNow.setText("Book Now");
                    btnBookNow.setEnabled(true);
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadReviews(); // 🔥 refresh automatically
    }
}