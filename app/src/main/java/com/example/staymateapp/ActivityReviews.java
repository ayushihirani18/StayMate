package com.example.staymateapp;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staymateapp.R;
import com.example.staymateapp.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityReviews extends AppCompatActivity {

    RatingBar ratingBar;
    EditText etReview;
    Button btnSubmit;

    String propertyId;
    RecyclerView recyclerReviews;
    TextView tvOverallRating, tvTotalReviews;

    List<Review> reviewList;
    com.example.staymateapp.adapters.ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        // 🔥 GET PROPERTY ID
        propertyId = getIntent().getStringExtra("propertyId");
        android.util.Log.d("DEBUG", "Review Screen ID = " + propertyId);

        if (propertyId == null) {
            Toast.makeText(this, "Property not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔥 INIT VIEWS
        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        btnSubmit = findViewById(R.id.btnSubmit);
        recyclerReviews = findViewById(R.id.recyclerReviews);
        tvOverallRating = findViewById(R.id.tvOverallRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);

        reviewList = new java.util.ArrayList<>();
        reviewAdapter = new com.example.staymateapp.adapters.ReviewAdapter(reviewList);

        recyclerReviews.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerReviews.setAdapter(reviewAdapter);

        // 🔥 CLICK LISTENER
        btnSubmit.setOnClickListener(v -> submitReview());

        loadReviews();
    }

    private void submitReview() {

        String text = etReview.getText().toString().trim();
        float rating = ratingBar.getRating();

        // ❗ VALIDATION
        if (text.isEmpty()) {
            etReview.setError("Write review");
            return;
        }

        if (rating == 0) {
            Toast.makeText(this, "Please give rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 CHECK LOGIN
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔥 GET USER NAME
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userName = userDoc.getString("name");

                    // 🔥 CREATE REVIEW MAP
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", userId);
                    map.put("propertyId", propertyId.trim());
                    map.put("userName", userName != null ? userName : "User");
                    map.put("reviewText", text);
                    map.put("rating", rating);
                    map.put("timestamp", System.currentTimeMillis());

                    FirebaseFirestore.getInstance()
                            .collection("reviews")
                            .whereEqualTo("propertyId", propertyId)
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(query -> {

                                if (!query.isEmpty()) {
                                    Toast.makeText(this, "You already reviewed this property", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 🔥 SAVE REVIEW ONLY IF NOT EXISTS
                                FirebaseFirestore.getInstance()
                                        .collection("reviews")
                                        .add(map)
                                        .addOnSuccessListener(doc -> {

                                            Toast.makeText(this, "Review Added ⭐", Toast.LENGTH_SHORT).show();

                                            etReview.setText("");
                                            ratingBar.setRating(0);

                                            loadReviews(); // refresh
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                        );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error checking review", Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show()
                );
    }
    private void loadReviews() {

        FirebaseFirestore.getInstance()
                .collection("reviews")
                .whereEqualTo("propertyId", propertyId)
                .get()
                .addOnSuccessListener(query -> {

                    reviewList.clear();
                    reviewList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    float total = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : query) {

                        com.example.staymateapp.models.Review review =
                                doc.toObject(com.example.staymateapp.models.Review.class);

                        if (review != null) {
                            reviewList.add(review);
                            total += review.getRating();
                        }
                    }

                    reviewAdapter = new com.example.staymateapp.adapters.ReviewAdapter(reviewList);
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
}