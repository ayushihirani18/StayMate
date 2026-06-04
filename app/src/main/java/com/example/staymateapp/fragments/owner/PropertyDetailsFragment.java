package com.example.staymateapp.fragments.owner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.staymateapp.R;
import com.example.staymateapp.adapters.ImageSliderAdapter;
import com.example.staymateapp.models.Property;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class PropertyDetailsFragment extends Fragment {

    private ViewPager2 viewPagerImages;
    private TextView textTitle, textRent, textLocation, textAmenities;
    private WormDotsIndicator dotsIndicator;

    private FirebaseFirestore db;

    public PropertyDetailsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_property_details,
                container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerImages = view.findViewById(R.id.viewPagerImages);
        textTitle = view.findViewById(R.id.textTitle);
        textRent = view.findViewById(R.id.textRent);
        textLocation = view.findViewById(R.id.textLocation);
        textAmenities = view.findViewById(R.id.textAmenities);
        dotsIndicator = view.findViewById(R.id.dotsIndicator);

        db = FirebaseFirestore.getInstance();

        // ✅ STRONG VALIDATION
        if (getArguments() == null) {
            Toast.makeText(getContext(), "Property ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String propertyId = getArguments().getString("propertyId");

        if (propertyId == null || propertyId.isEmpty()) {
            Toast.makeText(getContext(), "Property ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔍 Debug log
        android.util.Log.d("DETAIL_DEBUG", "Received ID: " + propertyId);

        loadProperty(propertyId);
    }

    private void loadProperty(String propertyId) {

        db.collection("properties")
                .document(propertyId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(getContext(),
                                "Property not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ SAFE PROPERTY FETCH
                    Property property = document.toObject(Property.class);

                    if (property == null) {
                        Toast.makeText(getContext(),
                                "Error loading property",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    property.setId(document.getId());

                    // 🔹 Title
                    textTitle.setText(property.getName());

                    // 🔹 Rent
                    String rent = "₹ " + property.getMinRent()
                            + " - ₹ " + property.getMaxRent();
                    textRent.setText(rent);

                    // 🔹 Location
                    textLocation.setText(property.getLocation());

                    // 🔹 Amenities
                    StringBuilder amenities = new StringBuilder();

                    if (property.hasWifi()) amenities.append("📶 WiFi  ");
                    if (property.hasAc()) amenities.append("❄ AC  ");
                    if (property.hasParking()) amenities.append("🚗 Parking  ");

                    if (amenities.length() == 0) {
                        amenities.append("No Amenities");
                    }

                    textAmenities.setText(amenities.toString());

                    // 🔥 IMAGE SLIDER
                    List<String> imageUrls = property.getImageUrls();

                    if (imageUrls == null) {
                        imageUrls = new ArrayList<>();
                    }

                    ImageSliderAdapter adapter = new ImageSliderAdapter(imageUrls, false);
                    viewPagerImages.setAdapter(adapter);

                    // 🔥 CONNECT DOTS
                    dotsIndicator.setViewPager2(viewPagerImages);


                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load property",
                                Toast.LENGTH_SHORT).show());
    }
}