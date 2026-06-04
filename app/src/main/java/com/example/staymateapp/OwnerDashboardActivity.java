package com.example.staymateapp;

import com.example.staymateapp.fragments.owner.BookingRequestsFragment;
import com.example.staymateapp.fragments.owner.OwnerChatListFragment;
import com.example.staymateapp.fragments.owner.OwnerDashboardFragment;
import com.example.staymateapp.fragments.owner.AddPropertyFragment;
import com.example.staymateapp.fragments.owner.MyPropertiesFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class OwnerDashboardActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("online", true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            HashMap<String, Object> map = new HashMap<>();
            map.put("online", false);
            map.put("lastSeen", System.currentTimeMillis());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update(map);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load default fragment
        if (savedInstanceState == null) {

            navigationView.setCheckedItem(R.id.nav_dashboard);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OwnerDashboardFragment())
                    .commit();

            setTitle("Dashboard");
        }

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();
            Fragment fragment = null;
            String title = "";

            if (id == R.id.nav_dashboard) {
                fragment = new OwnerDashboardFragment();
                title = "Dashboard";
            }
            else if (item.getItemId() == R.id.nav_chat) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new OwnerChatListFragment())
                        .commit();
            }
            else if (id == R.id.nav_add_property) {
                fragment = new AddPropertyFragment();
                title = "Add Property";
            }
            else if (id == R.id.nav_my_properties) {
                fragment = new MyPropertiesFragment();
                title = "My Properties";
            }
            else if (id == R.id.nav_bookings) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new BookingRequestsFragment())
                        .commit();
            }
            else if (id == R.id.nav_owner_profile) {
                startActivity(new Intent(OwnerDashboardActivity.this, OwnerProfileActivity.class));
            }
            else if (id == R.id.nav_logout) {

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setIcon(android.R.drawable.ic_dialog_alert)

                        .setPositiveButton("Logout", (dialog, which) -> {

                            mAuth.signOut();

                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                            finish();
                        })

                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())

                        .show();

                drawerLayout.closeDrawers(); // optional but nice UX
                return true;
            }+

            if (fragment != null) {
                loadFragment(fragment, title);
                navigationView.setCheckedItem(id);
            }

            drawerLayout.closeDrawers();
            return true;
        });
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {

                    if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .set(new HashMap<String, Object>() {{
                                put("fcmToken", token);
                            }}, SetOptions.merge());
                });

        loadOwnerData();
    }

    private void loadFragment(Fragment fragment, String title) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        setTitle(title);
    }

    private void loadOwnerData() {

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String role = documentSnapshot.getString("role");
                        String imageUrl = documentSnapshot.getString("profileImage");

                        // 🔥 HEADER VIEW
                        android.view.View headerView = navigationView.getHeaderView(0);

                        TextView tvName = headerView.findViewById(R.id.tvOwnerName);
                        TextView tvEmail = headerView.findViewById(R.id.tvOwnerEmail);
                        TextView tvRole = headerView.findViewById(R.id.tvRole);

                        ImageView profileImage = headerView.findViewById(R.id.profileImage);
                        TextView tvDrawerInitials = headerView.findViewById(R.id.tvDrawerInitials);

                        // 🔥 SET NAME & EMAIL
                        tvName.setText(name != null ? name : "User");
                        tvEmail.setText(email != null ? email : "");

                        // 🔥 SET ROLE
                        if ("owner".equalsIgnoreCase(role)) {
                            tvRole.setText("Owner");
                        } else {
                            tvRole.setText("Tenant");
                        }

                        // 🔥 INITIALS
                        String initials = getInitials(name);
                        tvDrawerInitials.setText(initials);

                        // 🔥 IMAGE / INITIALS SWITCH
                        if (imageUrl != null && !imageUrl.isEmpty()) {

                            profileImage.setVisibility(View.VISIBLE);
                            tvDrawerInitials.setVisibility(View.GONE);

                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(profileImage);

                        } else {

                            profileImage.setVisibility(View.GONE);
                            tvDrawerInitials.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private String getInitials(String name) {

        if (name == null || name.isEmpty()) return "";

        String[] parts = name.trim().split(" ");
        String initials = "";

        for (int i = 0; i < parts.length && i < 2; i++) {
            initials += parts[i].charAt(0);
        }

        return initials.toUpperCase();
    }

}