package com.example.staymateapp.fragments.tenant;

import android.content.Intent;
import android.os.Bundle;

import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.staymateapp.PropertyDetailsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.staymateapp.R;
import com.example.staymateapp.adapters.PropertyAdapter;
import com.example.staymateapp.models.Property;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BrowsePropertiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private PropertyAdapter adapter;
    private List<Property> propertyList;
    private List<Property> allProperties;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration propertyListener;
    private ImageButton btnFilter;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    RadioGroup genderGroup;

    TextWatcher simpleWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            filterProperties(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    public BrowsePropertiesFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse_properties, container, false);

        recyclerView = view.findViewById(R.id.recyclerBrowseProperties);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch = view.findViewById(R.id.etSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        progressBar = view.findViewById(R.id.progressBar);

        propertyList = new ArrayList<>();
        allProperties = new ArrayList<>();

        // Pass NULL listener so Edit/Delete buttons disappear
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

                        // 🔥 PASS FULL IMAGE LIST
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

        db = FirebaseFirestore.getInstance();

        btnFilter.setOnClickListener(v -> {

            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
            View sheetView = getLayoutInflater().inflate(
                    R.layout.bottom_sheet_filters, null);

            dialog.setContentView(sheetView);

            // Get views
            Chip wifi = sheetView.findViewById(R.id.filterWifi);
            Chip ac = sheetView.findViewById(R.id.filterAC);
            Chip parking = sheetView.findViewById(R.id.filterParking);

            EditText minRent = sheetView.findViewById(R.id.etMinRent);
            EditText maxRent = sheetView.findViewById(R.id.etMaxRent);

            RadioGroup genderGroup = sheetView.findViewById(R.id.radioGender); // ✅ CORRECT PLACE
            RadioGroup typeGroup = sheetView.findViewById(R.id.radioType);

            Button apply = sheetView.findViewById(R.id.btnApplyFilters);

            apply.setOnClickListener(v1 -> {

                // 🔥 GET SELECTED GENDER
                String selectedGender = "Any";

                int selectedId = genderGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.rbMale) {
                    selectedGender = "Male";
                } else if (selectedId == R.id.rbFemale) {
                    selectedGender = "Female";
                } else if (selectedId == R.id.rbCoed) {
                    selectedGender = "Co-ed";
                }



                String selectedType = "All";

                int typeId = typeGroup.getCheckedRadioButtonId();

                if (typeId == R.id.rbPG) {
                    selectedType = "PG";
                } else if (typeId == R.id.rbFlat) {
                    selectedType = "Flat";
                } else if (typeId == R.id.rbHostel) {
                    selectedType = "Hostel";
                }

                // 🔥 CALL FILTER WITH GENDER
                applyFilters(
                        wifi.isChecked(),
                        ac.isChecked(),
                        parking.isChecked(),
                        minRent.getText().toString(),
                        maxRent.getText().toString(),
                        selectedGender,
                        selectedType  // ✅ NEW
                );

                dialog.dismiss();
            });

            dialog.show();
        });

        loadProperties();

        etSearch.addTextChangedListener(simpleWatcher);

        return view;
    }

    private void loadProperties() {

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        propertyListener = db.collection("properties")
                .addSnapshotListener((value, error) -> {

                    progressBar.setVisibility(View.GONE);

                    if (error != null || value == null) {
                        return;
                    }

                    propertyList.clear();
                    allProperties.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        Property property = doc.toObject(Property.class);

                        if (property == null) continue;

                        property.setId(doc.getId());
                        propertyList.add(property);
                        allProperties.add(property);

                    }

                    adapter.notifyDataSetChanged();

                    if (propertyList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
                });
    }
    private void filterProperties(String text) {

        propertyList.clear();

        if (text.isEmpty()) {
            propertyList.addAll(allProperties);
        } else {

            text = text.toLowerCase();

            for (Property property : allProperties) {

                if (
                        (property.getLocation() != null &&
                                property.getLocation().toLowerCase().contains(text)) ||

                                (property.getName() != null &&
                                        property.getName().toLowerCase().contains(text))
                ) {
                    propertyList.add(property);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (propertyList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void applyFilters(
            boolean wifi,
            boolean ac,
            boolean parking,
            String minRentStr,
            String maxRentStr,
            String gender,
            String type// ✅ NEW
    ) {

        int minRent = 0;
        int maxRent = Integer.MAX_VALUE;

        try {
            if (!minRentStr.isEmpty()) {
                minRent = Integer.parseInt(minRentStr);
            }

            if (!maxRentStr.isEmpty()) {
                maxRent = Integer.parseInt(maxRentStr);
            }
        } catch (NumberFormatException e) {
            minRent = 0;
            maxRent = Integer.MAX_VALUE;
        }

        propertyList.clear();

        for (Property property : allProperties) {

            boolean match = true;

            if (wifi && !property.hasWifi()) match = false;
            if (ac && !property.hasAc()) match = false;
            if (parking && !property.hasParking()) match = false;

            if (property.getMaxRent() < minRent || property.getMinRent() > maxRent) {
                match = false;
            }

            // 🔥 GENDER FILTER
            if (!gender.equals("Any")) {

                String propertyGender = property.getGender();

                if (propertyGender == null) {
                    match = false;
                } else {

                    propertyGender = propertyGender.trim();

                    // 🔥 LOGIC
                    if (gender.equals("Male") || gender.equals("Female")) {

                        // Allow both specific + co-ed
                        if (!propertyGender.equalsIgnoreCase(gender) &&
                                !propertyGender.equalsIgnoreCase("Co-ed")) {
                            match = false;
                        }

                    } else if (gender.equals("Co-ed")) {

                        if (!propertyGender.equalsIgnoreCase("Co-ed")) {
                            match = false;
                        }
                    }
                }
            }
            // 🔥 PROPERTY TYPE FILTER
            if (!type.equals("All")) {
                if (property.getPropertyType() == null ||
                        !property.getPropertyType().equalsIgnoreCase(type)) {
                    match = false;
                }
            }

            if (match) {
                propertyList.add(property);
            }
        }

        if (propertyList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }


        adapter.notifyDataSetChanged();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (propertyListener != null) {
            propertyListener.remove(); // ✅ stop Firebase listener
            propertyListener = null;
        }
    }
}