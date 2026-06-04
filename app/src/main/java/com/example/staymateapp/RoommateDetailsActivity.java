package com.example.staymateapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class RoommateDetailsActivity extends AppCompatActivity {

    TextView tvTitle, tvLocation, tvBudget, tvDescription, tvDate, tvPreference;
    Button btnChat;

    String userId; // 🔥 important for chat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roommate_details);

        // 🔥 INIT
        tvTitle = findViewById(R.id.tvTitle);
        tvLocation = findViewById(R.id.tvLocation);
        tvBudget = findViewById(R.id.tvBudget);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        btnChat = findViewById(R.id.btnChat);
        tvPreference = findViewById(R.id.tvPreference);


        // 🔥 GET DATA
        Intent intent = getIntent();

        tvTitle.setText(intent.getStringExtra("title"));
        tvLocation.setText(intent.getStringExtra("location"));
        tvBudget.setText("💰 ₹" + intent.getStringExtra("budget"));
        tvDescription.setText(intent.getStringExtra("description"));
        tvDate.setText(intent.getStringExtra("date"));
        tvPreference.setText(getIntent().getStringExtra("preference"));

        userId = intent.getStringExtra("userId");

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId != null && userId.equals(currentUserId)) {
            btnChat.setVisibility(View.GONE); // 🔥 hide if own post
        } else {
            btnChat.setVisibility(View.VISIBLE);
        }

        // 🔥 CHAT BUTTON
        btnChat.setOnClickListener(v -> {

            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("userId", userId);
            startActivity(chatIntent);
        });
    }
}