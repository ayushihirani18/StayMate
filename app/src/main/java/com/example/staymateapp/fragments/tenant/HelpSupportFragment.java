package com.example.staymateapp.fragments.tenant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.staymateapp.R;

public class HelpSupportFragment extends Fragment {

    public HelpSupportFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help_support, container, false);

        // 📧 EMAIL SUPPORT
        view.findViewById(R.id.btnEmail).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:staymate.support@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Help & Support - StayMate");
            startActivity(intent);
        });

        // 📞 CALL SUPPORT
        view.findViewById(R.id.btnCall).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+917861973936"));
            startActivity(intent);
        });

        // ❓ FAQ POPUP
        view.findViewById(R.id.btnFAQ).setOnClickListener(v -> {

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("FAQs")
                    .setMessage(
                            "1. How to book a PG?\n" +
                                    "→ Open property → Click Book\n\n" +

                                    "2. How to contact owner?\n" +
                                    "→ Use Call / Chat button\n\n" +

                                    "3. How to save property?\n" +
                                    "→ Tap ❤️ icon"
                    )
                    .setPositiveButton("OK", null)
                    .show();
        });

        // 🐞 REPORT ISSUE
        view.findViewById(R.id.btnReport).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:staymate.support@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report - StayMate");
            startActivity(intent);
        });

        return view;
    }
}