package com.example.staymateapp.fragments.owner;

import android.os.Bundle;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staymateapp.R;
import com.example.staymateapp.adapters.PropertyAdapter;
import com.example.staymateapp.models.Property;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MyPropertiesFragment extends Fragment {

    private PropertyAdapter adapter;
    private List<Property> propertyList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ListenerRegistration propertyListener;

    public MyPropertiesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_properties, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerProperties);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        propertyList = new ArrayList<>();

        adapter = new PropertyAdapter(propertyList,
                new PropertyAdapter.OnPropertyActionListener() {

                    @Override
                    public void onEdit(Property property) {

                        Bundle bundle = new Bundle();
                        bundle.putString("propertyId", property.getId());

                        AddPropertyFragment fragment = new AddPropertyFragment();
                        fragment.setArguments(bundle);

                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onDelete(Property property) {

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Delete Property")
                                .setMessage("Are you sure you want to delete this property?")
                                .setIcon(android.R.drawable.ic_dialog_alert)

                                .setPositiveButton("Delete", (dialog, which) -> {

                                    db.collection("properties")
                                            .document(property.getId())
                                            .delete()
                                            .addOnSuccessListener(unused -> {

                                                Toast.makeText(getContext(),
                                                        "Property Deleted",
                                                        Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(),
                                                            "Delete failed",
                                                            Toast.LENGTH_SHORT).show());
                                })

                                .setNegativeButton("Cancel",
                                        (dialog, which) -> dialog.dismiss())
                                .show();
                    }

                    @Override
                    public void onView(Property property) {

                        if (property == null || property.getId() == null) {
                            Toast.makeText(getContext(), "Property ID missing", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 🔍 Debug (remove later if you want)
                        android.util.Log.d("OWNER_DEBUG", "Clicked ID: " + property.getId());

                        Bundle bundle = new Bundle();
                        bundle.putString("propertyId", property.getId());

                        PropertyDetailsFragment fragment = new PropertyDetailsFragment();
                        fragment.setArguments(bundle);

                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                },
                true // ✅ VERY IMPORTANT → OWNER
        );

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        listenForProperties();
    }

    private void listenForProperties() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(),
                    "User not logged in",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = mAuth.getCurrentUser().getUid();

        propertyListener = db.collection("properties")
                .whereEqualTo("ownerId", ownerId)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {

                    if (error != null) {

                        Toast.makeText(getContext(),
                                "Error loading properties",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    propertyList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {

                        Toast.makeText(getContext(),
                                "No properties found for this owner",
                                Toast.LENGTH_SHORT).show();
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots) {

                        Property property = document.toObject(Property.class);

                        if (property != null) {
                            property.setId(document.getId());
                            propertyList.add(property);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (propertyListener != null) {
            propertyListener.remove();
        }
    }
}