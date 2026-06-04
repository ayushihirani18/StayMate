package com.example.staymateapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import com.example.staymateapp.fragments.tenant.BrowsePropertiesFragment;
import com.example.staymateapp.fragments.tenant.ChatListFragment;
import com.example.staymateapp.fragments.tenant.HelpSupportFragment;
import com.example.staymateapp.fragments.tenant.MyBookingsFragment;
import com.example.staymateapp.fragments.tenant.SavedPropertiesFragment;
import com.example.staymateapp.fragments.tenant.ProfileFragment;

import com.example.staymateapp.fragments.tenant.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import android.view.View;

import java.util.HashMap;

public class TenantDashboardActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    MaterialToolbar toolbar;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tenant_dashboard);

        // Initialize views
        bottomNav = findViewById(R.id.bottomNav);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        Menu menu = navigationView.getMenu();
        MenuItem logoutItem = menu.findItem(R.id.nav_logout);

        if (logoutItem != null && logoutItem.getIcon() != null) {
            logoutItem.getIcon().setTint(getResources().getColor(R.color.status_rejected));
        }
        // Set default selected item
        navigationView.setCheckedItem(R.id.nav_dashboard);
        toolbar = findViewById(R.id.toolbar);

        // Set toolbar
        setSupportActionBar(toolbar);

        // Drawer toggle (hamburger icon)
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new BrowsePropertiesFragment())
                    .commit();

            bottomNav.setSelectedItemId(R.id.nav_browse);
        }

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_browse) {
                selectedFragment = new BrowsePropertiesFragment();
            }
            else if (item.getItemId() == R.id.nav_chat) {
                selectedFragment = new ChatListFragment();
            }

            else if (item.getItemId() == R.id.nav_saved) {
                selectedFragment = new SavedPropertiesFragment();
            }

            else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Drawer menu clicks
        navigationView.getHeaderView(0).setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();

            drawerLayout.closeDrawers();
        });

        navigationView.setNavigationItemSelectedListener(menuItem -> {

            int id = menuItem.getItemId();


            if (id == R.id.nav_my_bookings) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MyBookingsFragment())
                        .commit();

            } else if (id == R.id.nav_roommates) {

                Intent intent = new Intent(this, BrowseRoommatesActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_settings) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .commit();

            } else if (id == R.id.nav_help) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new   HelpSupportFragment())
                        .commit();
            }

            else if (id == R.id.nav_logout) {

                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            FirebaseAuth.getInstance().signOut();

                            Intent intent = new Intent(TenantDashboardActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                            finish();

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
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
        loadTenantData();

    }

    private void loadTenantData() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View headerView = navigationView.getHeaderView(0);

        if (headerView == null) return;

        TextView tvName = headerView.findViewById(R.id.tvOwnerName);
        TextView tvEmail = headerView.findViewById(R.id.tvOwnerEmail);
        TextView tvRole = headerView.findViewById(R.id.tvRole);

        ImageView profileImage = headerView.findViewById(R.id.profileImage);
        TextView tvDrawerInitials = headerView.findViewById(R.id.tvDrawerInitials);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String role = documentSnapshot.getString("role");
                        String imageUrl = documentSnapshot.getString("profileImage");

                        Log.d("PROFILE_DEBUG", "Image URL: " + imageUrl);

                        tvName.setText(name != null ? name : "User");
                        tvEmail.setText(email != null ? email : "");

                        // 🔥 ROLE
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

                            Glide.with(profileImage.getContext())
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
    @Override
    protected void onResume() {
        super.onResume();
        loadTenantData(); // refresh drawer when coming back
    }
}