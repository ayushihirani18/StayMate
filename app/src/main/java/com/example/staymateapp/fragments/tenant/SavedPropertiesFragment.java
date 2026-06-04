package com.example.staymateapp.fragments.tenant;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.staymateapp.PropertyDetailsActivity;
import com.example.staymateapp.R;
import com.example.staymateapp.adapters.PropertyAdapter;
import com.example.staymateapp.models.Property;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SavedPropertiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<Property> propertyList;
    TextView tvEmptySaved;
    Button btnExplore;
    LinearLayout layoutEmpty;

    public SavedPropertiesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_saved_properties,
                container,
                false
        );

        tvEmptySaved = view.findViewById(R.id.tvEmptySaved);
        btnExplore = view.findViewById(R.id.btnExplore);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);


        recyclerView = view.findViewById(R.id.recyclerSavedProperties);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        propertyList = new ArrayList<>();

        btnExplore.setOnClickListener(v -> {

            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNav);

            bottomNav.setSelectedItemId(R.id.nav_browse);
        });

        adapter = new PropertyAdapter(propertyList,
                new PropertyAdapter.OnPropertyActionListener() {

                    @Override
                    public void onEdit(Property property) {
                        // Not needed for tenant
                    }

                    @Override
                    public void onDelete(Property property) {
                        // Not needed for tenant
                    }
                    @Override
                    public void onView(Property property) {

                        Intent intent = new Intent(requireContext(), PropertyDetailsActivity.class);

                        intent.putExtra("propertyName", property.getName());
                        intent.putExtra("location", property.getLocation());
                        intent.putExtra("minRent", property.getMinRent());
                        intent.putExtra("maxRent", property.getMaxRent());
                        intent.putExtra("ownerId", property.getOwnerId());
                        intent.putExtra("contact", property.getContact());  // 🔥 ADD THIS
                        intent.putExtra("propertyId", property.getId());
                        intent.putExtra("propertyType", property.getPropertyType());
                        intent.putExtra("gender", property.getGender());

                        // ✅ FIX: send ALL images
                        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
                            intent.putStringArrayListExtra(
                                    "imageUrls",
                                    new ArrayList<>(property.getImageUrls())
                            );
                        }

                        startActivity(intent);
                    }
                },
                false // ✅ VERY IMPORTANT → tenant
        );

        recyclerView.setAdapter(adapter);

        loadSavedProperties();


        return view;
    }

    private void loadSavedProperties() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("savedProperties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    propertyList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        Property property = doc.toObject(Property.class);

                        if (property != null) {
                            property.setId(doc.getId());
                            propertyList.add(property);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (propertyList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }
}