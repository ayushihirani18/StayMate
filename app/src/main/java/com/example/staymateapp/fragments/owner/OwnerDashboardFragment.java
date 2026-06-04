package com.example.staymateapp.fragments.owner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.staymateapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerDashboardFragment extends Fragment {

    public OwnerDashboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_owner_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTotalProperties = view.findViewById(R.id.tvTotalProperties);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            tvTotalProperties.setText("0");
            return;
        }

        String ownerId = mAuth.getCurrentUser().getUid();

        db.collection("properties")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int count = queryDocumentSnapshots.size();
                    tvTotalProperties.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {

                    tvTotalProperties.setText("0");
                });
    }
}