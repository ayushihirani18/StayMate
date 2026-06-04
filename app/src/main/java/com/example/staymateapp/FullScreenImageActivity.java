package com.example.staymateapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.staymateapp.adapters.ImageSliderAdapter;

import java.util.ArrayList;

public class FullScreenImageActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    ArrayList<String> imageUrls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        viewPager = findViewById(R.id.viewPagerFull);

        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        int position = getIntent().getIntExtra("position", 0);

        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }

        // 🔥 SET ADAPTER (FULLSCREEN MODE)
        ImageSliderAdapter adapter = new ImageSliderAdapter(imageUrls, true);
        viewPager.setAdapter(adapter);

        // 🔥 OPEN AT CLICKED IMAGE
        viewPager.setCurrentItem(position, false);

        // 🔥 CLOSE BUTTON
        ImageView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        // 🔥 SWIPE DOWN TO CLOSE (FIXED WARNING)
        viewPager.setOnTouchListener(new View.OnTouchListener() {

            float startY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        float endY = event.getY();

                        // 🔥 SWIPE DOWN DETECT
                        if (endY - startY > 300) {
                            finish();
                        }

                        v.performClick(); // ✅ FIX WARNING
                        break;
                }

                return false; // allow normal swipe also
            }
        });
    }
}