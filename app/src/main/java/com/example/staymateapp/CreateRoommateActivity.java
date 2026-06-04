package com.example.staymateapp;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateRoommateActivity extends AppCompatActivity {

    EditText etTitle, etDescription, etCity, etArea, etBudget, etDate;
    Button btnPost;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_roommate_post);

        // 🔥 INIT FIRESTORE (IMPORTANT)
        db = FirebaseFirestore.getInstance();

        // 🔥 INIT VIEWS
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etCity = findViewById(R.id.etCity);
        etArea = findViewById(R.id.etArea);
        etBudget = findViewById(R.id.etBudget);
        etDate = findViewById(R.id.etDate);
        btnPost = findViewById(R.id.btnPost);

        btnPost.setOnClickListener(v -> {
            Toast.makeText(this, "Clicked ✅", Toast.LENGTH_SHORT).show();
            postRoommate();
        });
    }

    private void postRoommate() {

        Toast.makeText(this, "Inside postRoommate()", Toast.LENGTH_SHORT).show();

        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String budget = etBudget.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        // ❗ VALIDATION
        if (title.isEmpty()) {
            etTitle.setError("Enter title");
            return;
        }

        if (desc.isEmpty()) {
            etDescription.setError("Enter description");
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("Enter city");
            return;
        }

        if (budget.isEmpty()) {
            etBudget.setError("Enter budget");
            return;
        }

        // 🔥 CHECK LOGIN (only once)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first ❌", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔥 CREATE DATA MAP
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", desc);
        map.put("city", city);
        map.put("area", area);
        map.put("budget", budget);
        map.put("date", date);
        map.put("userId", userId);
        map.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        Toast.makeText(this, "Saving to Firestore...", Toast.LENGTH_SHORT).show();

        // 🔥 SAVE TO FIRESTORE
        db.collection("roommates")
                .add(map)
                .addOnSuccessListener(doc -> {
                    String id = doc.getId();

                    android.util.Log.d("ROOMMATE_DEBUG", "SUCCESS ID: " + id);
                    Toast.makeText(this, "Posted successfully 🔥", Toast.LENGTH_LONG).show();

                    finish(); // 🔥 go back
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ROOMMATE_DEBUG", "ERROR", e);
                    Toast.makeText(this, "FAILED ❌ " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}