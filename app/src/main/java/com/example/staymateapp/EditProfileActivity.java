package com.example.staymateapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName, etPhone;
    EditText etCity, etBudget;
    ImageView profileImage;
    Button btnSave;
    ProgressBar progressBar;
    String role;

    FirebaseAuth auth;
    FirebaseFirestore db;
    Uri imageUri;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        profileImage = findViewById(R.id.profileImage);
        etCity = findViewById(R.id.etCity);
        etBudget = findViewById(R.id.etBudget);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        role = getIntent().getStringExtra("role");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if ("owner".equals(role)) {
            etBudget.setVisibility(View.GONE); // ✅ HIDE FOR OWNER
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            loadUserData();
        }

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        btnSave.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.97f);
                    v.setScaleY(0.97f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    break;
            }
            return false;
        });

        btnSave.setOnClickListener(v -> updateProfile());

    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);

                    if (doc.exists()) {
                        etName.setText(doc.getString("name"));
                        etPhone.setText(doc.getString("phone"));
                        etBudget.setText(doc.getString("budget"));
                        etCity.setText(doc.getString("city"));


                        String imageUrl = doc.getString("profileImage");

                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(profileImage);
                        }
                    }
                });
    }

    private void updateProfile() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String budget = etBudget.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", phone);
        map.put("city", city);
        if (!TextUtils.isEmpty(budget)) {
            map.put("budget", budget);
        }


        db.collection("users").document(userId)
                .set(map, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Updated Successfully 🔥", Toast.LENGTH_SHORT).show();

                    progressBar.postDelayed(() -> {
                        progressBar.setVisibility(View.GONE);
                        finish(); // 🔥 now closes properly
                    }, 300); // small delay for UI refresh
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE); // 🔥 IMPORTANT
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            uploadImage();
        }
    }

    private void uploadImage() {

        if (imageUri == null) return;

        progressBar.setVisibility(View.VISIBLE);

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profile_images/" + userId);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {

                            db.collection("users")
                                    .document(userId)
                                    .update("profileImage", uri.toString());

                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Image Updated!", Toast.LENGTH_SHORT).show();
                        })
                )
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}