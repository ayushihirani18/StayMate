package com.example.staymateapp.fragments.owner;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staymateapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;

import java.util.*;

public class AddPropertyFragment extends Fragment {

    EditText etName, etLocation, etMinRent, etMaxRent, etContact;
    Spinner spPropertyType, spGenderType;
    CheckBox cbWifi, cbAC, cbParking;
    TextView tvAmenitiesHeader;
    LinearLayout layoutAmenities;
    Button btnAdd;

    RecyclerView rvImages;
    ImageAdapter imageAdapter;

    List<Uri> imageUriList = new ArrayList<>();
    List<String> existingImageUrls = new ArrayList<>();
    List<String> removedImageUrls = new ArrayList<>();

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    private String propertyId = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetMultipleContents(),
                    uris -> {
                        if (uris != null && !uris.isEmpty()) {
                            imageUriList.addAll(uris);
                            imageAdapter.notifyDataSetChanged();
                        }
                    });

    public AddPropertyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_property, container, false);

        initViews(view);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dxr18qf7s");
        config.put("api_key", "857531648886898");
        config.put("api_secret", "Pnufuc4oq_ahUTVMNqt-6cLtwJ8");

        try {
            MediaManager.init(requireContext(), config);
        } catch (Exception e) {
            // already initialized
        }

        if (getArguments() != null) {
            propertyId = getArguments().getString("propertyId");
        }

        setupSpinners();
        setupAmenitiesToggle();
        setupImageRecycler();

        if (propertyId != null) {
            loadPropertyData();
            btnAdd.setText(R.string.update_property);
        }

        btnAdd.setOnClickListener(v -> saveProperty());

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etPropertyName);
        etLocation = view.findViewById(R.id.etLocation);
        etMinRent = view.findViewById(R.id.etMinRent);
        etMaxRent = view.findViewById(R.id.etMaxRent);
        etContact = view.findViewById(R.id.etContact);
        spPropertyType = view.findViewById(R.id.spPropertyType);
        spGenderType = view.findViewById(R.id.spGenderType);
        cbWifi = view.findViewById(R.id.cbWifi);
        cbAC = view.findViewById(R.id.cbAC);
        cbParking = view.findViewById(R.id.cbParking);
        tvAmenitiesHeader = view.findViewById(R.id.tvAmenitiesHeader);
        layoutAmenities = view.findViewById(R.id.layoutAmenities);
        btnAdd = view.findViewById(R.id.btnAddProperty);
        rvImages = view.findViewById(R.id.rvImages);
    }

    private void setupImageRecycler() {
        rvImages.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false));

        imageAdapter = new ImageAdapter(
                getContext(),
                imageUriList,
                () -> imagePickerLauncher.launch("image/*"),
                uri -> {
                    if (uri.toString().startsWith("https")) {
                        markImageRemoved(uri.toString());
                    }
                }
        );
        rvImages.setAdapter(imageAdapter);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.property_types,
                        android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spPropertyType.setAdapter(adapter);

        ArrayAdapter<CharSequence> genderAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.gender_types,
                        android.R.layout.simple_spinner_item);

        genderAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spGenderType.setAdapter(genderAdapter);
    }

    private void setupAmenitiesToggle() {
        tvAmenitiesHeader.setOnClickListener(v -> {
            if (layoutAmenities.getVisibility() == View.GONE) {
                layoutAmenities.setVisibility(View.VISIBLE);
                tvAmenitiesHeader.setText(R.string.amenities_expanded);
            } else {
                layoutAmenities.setVisibility(View.GONE);
                tvAmenitiesHeader.setText(R.string.amenities_collapsed);
            }
        });
    }

    private void loadPropertyData() {
        db.collection("properties")
                .document(propertyId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) return;

                    etName.setText(document.getString("name"));
                    etLocation.setText(document.getString("location"));

                    Map<String, Boolean> amenities = (Map<String, Boolean>) document.get("amenities");

                    if (amenities != null) {

                        cbWifi.setChecked(Boolean.TRUE.equals(amenities.get("wifi")));
                        cbAC.setChecked(Boolean.TRUE.equals(amenities.get("ac")));
                        cbParking.setChecked(Boolean.TRUE.equals(amenities.get("parking")));

                    }

                    Long min = document.getLong("minRent");
                    Long max = document.getLong("maxRent");

                    if (min != null) etMinRent.setText(String.valueOf(min));
                    if (max != null) etMaxRent.setText(String.valueOf(max));

                    String contact = document.getString("contact");
                    if (contact != null)
                        etContact.setText(contact.replace("+91", ""));

                    existingImageUrls.clear();

                    Object urlsObj = document.get("imageUrls");
                    List<String> imageUrls = new ArrayList<>();

                    if (urlsObj instanceof List<?>) {
                        for (Object item : (List<?>) urlsObj) {
                            if (item instanceof String) {
                                imageUrls.add((String) item);
                            }
                        }
                    }

                    if (!imageUrls.isEmpty()) {

                        existingImageUrls.clear();
                        existingImageUrls.addAll(imageUrls);

                        imageUriList.clear();

                        for (String url : imageUrls) {
                            imageUriList.add(Uri.parse(url));
                        }

                        imageAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void saveProperty() {

        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Required");
            return;
        }

        Map<String, Object> property = new HashMap<>();
        property.put("name", name);
        property.put("location", location);

        // 🔥 GENDER
        String gender = spGenderType.getSelectedItem().toString();

        if (gender.equalsIgnoreCase("Select Gender")) {
            Toast.makeText(getContext(), "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        property.put("gender", gender);

        // 🔥 PROPERTY TYPE
        String propertyType = spPropertyType.getSelectedItem().toString();

        if (propertyType.equalsIgnoreCase("Select Type")) {
            Toast.makeText(getContext(), "Please select property type", Toast.LENGTH_SHORT).show();
            return;
        }

        property.put("propertyType", propertyType);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        property.put("ownerId", mAuth.getCurrentUser().getUid());

        String minRentStr = etMinRent.getText().toString().trim();
        String maxRentStr = etMaxRent.getText().toString().trim();

        if (TextUtils.isEmpty(minRentStr)) {
            etMinRent.setError("Required");
            return;
        }

        if (TextUtils.isEmpty(maxRentStr)) {
            etMaxRent.setError("Required");
            return;
        }

        int minRent = Integer.parseInt(minRentStr);
        int maxRent = Integer.parseInt(maxRentStr);

        property.put("minRent", minRent);
        property.put("maxRent", maxRent);

        String contact = etContact.getText().toString().trim();

        if (TextUtils.isEmpty(contact)) {
            etContact.setError("Required");
            return;
        }

        property.put("contact", "+91" + contact);

        Map<String, Boolean> amenities = new HashMap<>();
        amenities.put("wifi", cbWifi.isChecked());
        amenities.put("ac", cbAC.isChecked());
        amenities.put("parking", cbParking.isChecked());

        property.put("amenities", amenities);

        uploadImagesAndSave(property);
    }

    private void uploadImagesAndSave(Map<String, Object> property) {

        List<String> finalUrls = new ArrayList<>(existingImageUrls);
        finalUrls.removeAll(removedImageUrls);

        List<Uri> newImages = new ArrayList<>();

        for (Uri uri : imageUriList) {
            if (!uri.toString().startsWith("https")) {
                newImages.add(uri);
            }
        }

        // If no new images were added
        if (newImages.isEmpty()) {
            property.put("imageUrls", finalUrls);
            commitToFirestore(property);
            return;
        }

        int totalUploads = newImages.size();
        final int[] uploadCount = {0};

        for (Uri uri : newImages) {

            MediaManager.get().upload(uri)
                    .callback(new UploadCallback() {

                        @Override
                        public void onSuccess(String requestId, Map resultData) {

                            String imageUrl = resultData.get("secure_url").toString();

                            finalUrls.add(imageUrl);
                            uploadCount[0]++;

                            if (uploadCount[0] == totalUploads) {

                                property.put("imageUrls", finalUrls);
                                commitToFirestore(property);
                            }
                        }

                        @Override
                        public void onStart(String requestId) {
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onError(String requestId,
                                            com.cloudinary.android.callback.ErrorInfo error) {

                            Toast.makeText(getContext(),
                                    "Upload failed: " + error.getDescription(),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onReschedule(String requestId,
                                                 com.cloudinary.android.callback.ErrorInfo error) {
                        }

                    })
                    .dispatch();
        }
    }

    private void commitToFirestore(Map<String, Object> property) {

        if (propertyId != null) {

            db.collection("properties")
                    .document(propertyId)
                    .update(property)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(),
                                "Updated",
                                Toast.LENGTH_SHORT).show();
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    });

        } else {

            db.collection("properties")
                    .add(property)
                    .addOnSuccessListener(documentReference -> {

                        Toast.makeText(getContext(),
                                "Added",
                                Toast.LENGTH_SHORT).show();

                        // Navigate back to My Properties
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Failed to add property",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void markImageRemoved(String url) {
        removedImageUrls.add(url);
        existingImageUrls.remove(url);
    }
}