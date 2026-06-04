package com.example.staymateapp.fragments.tenant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.staymateapp.EditProfileActivity;
import com.example.staymateapp.LoginActivity;
import com.example.staymateapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    TextView tvName, tvEmail;
    Button btnLogout, btnEdit;
    ImageView profileImage;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView tvPhone, tvCity, tvBudget;

    Uri imageUri;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEdit = view.findViewById(R.id.btnEdit);
        profileImage = view.findViewById(R.id.profileImage);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvBudget = view.findViewById(R.id.tvBudget);
        tvCity = view.findViewById(R.id.tvCity);


        ImageView editIcon = view.findViewById(R.id.editProfileIcon);

        editIcon.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔥 CLOUDINARY INIT (same as property)
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dxr18qf7s");
        config.put("api_key", "857531648886898");
        config.put("api_secret", "Pnufuc4oq_ahUTVMNqt-6cLtwJ8");

        try {
            MediaManager.init(requireContext(), config);
        } catch (Exception e) {
            // already initialized
        }

        loadUserData();

        // Logout
        btnLogout.setOnClickListener(v -> {

            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")

                    .setPositiveButton("Logout", (dialog, which) -> {

                        mAuth.signOut();

                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                        requireActivity().finish();
                    })

                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })

                    .show();
        });        // Edit Profile
        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        // Pick image
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        return view;
    }

    // 🔥 Refresh on return
    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    // 🔥 Load data
    private void loadUserData() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String imageUrl = documentSnapshot.getString("profileImage");
                        String phone = documentSnapshot.getString("phone");
                        String city = documentSnapshot.getString("city");
                        String budget = documentSnapshot.getString("budget");

                        tvName.setText(name != null ? name : "No Name");
                        tvEmail.setText(email != null ? email : "No Email");
                        tvPhone.setText(phone != null ? "Phone: " + phone : "Phone: Not added");
                        tvCity.setText(city != null ? "City: " + city : "City: Not set");
                        tvBudget.setText(budget != null ? "Budget: " + budget : "Budget: Not set");

                        // 🔥 SAFE IMAGE LOAD


                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }

    // 🔥 Handle image pick
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri); // instant preview
            uploadImage(imageUri);
        }
    }

    // 🔥 CLOUDINARY UPLOAD (SDK)
    private void uploadImage(Uri uri) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || uri == null) return;

        MediaManager.get().upload(uri)
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String imageUrl = resultData.get("secure_url").toString();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("profileImage", imageUrl);

                        db.collection("users")
                                .document(user.getUid())
                                .set(map, com.google.firebase.firestore.SetOptions.merge());

                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Image Saved 🔥", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onError(String requestId,
                                        com.cloudinary.android.callback.ErrorInfo error) {

                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(),
                                        "Upload failed: " + error.getDescription(),
                                        Toast.LENGTH_LONG).show()
                        );
                    }

                    @Override
                    public void onReschedule(String requestId,
                                             com.cloudinary.android.callback.ErrorInfo error) {}
                })
                .dispatch();
    }
}