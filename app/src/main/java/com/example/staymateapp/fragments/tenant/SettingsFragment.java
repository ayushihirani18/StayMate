package com.example.staymateapp.fragments.tenant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import android.content.SharedPreferences;
import android.widget.Switch;

import com.example.staymateapp.ChangePasswordActivity;
import com.example.staymateapp.R;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
        });

        Switch switchNotifications = view.findViewById(R.id.switchNotifications);

        SharedPreferences prefs = requireActivity().getSharedPreferences("settings", 0);

// Load saved state
        boolean isEnabled = prefs.getBoolean("notifications", true);
        switchNotifications.setChecked(isEnabled);

// Save when toggled
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications", isChecked).apply();

            if (isChecked) {
                Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnAbout).setOnClickListener(v ->
                Toast.makeText(getContext(), "StayMate v1.0", Toast.LENGTH_SHORT).show()
        );



        return view;
    }
}