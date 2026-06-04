package com.example.staymateapp.fragments.tenant;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.staymateapp.R;
import com.example.staymateapp.adapters.BookingAdapter;
import com.example.staymateapp.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class MyBookingsFragment extends Fragment {

    RecyclerView recyclerView;
    List<Booking> bookingList;
    BookingAdapter adapter;
    TextView tvNoBookings;

    public MyBookingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_bookings, container, false);

        recyclerView = view.findViewById(R.id.recyclerBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();

        // ✅ NO CLICK (SAFE)
        adapter = new BookingAdapter(bookingList, false);
        tvNoBookings = view.findViewById(R.id.tvNoBookings);

        recyclerView.setAdapter(adapter);

        loadBookings();

        return view;
    }

    private void loadBookings() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            tvNoBookings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {

                    bookingList.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : query) {

                        Booking booking = new Booking(
                                doc.getString("propertyName"),
                                doc.getString("rent"),
                                doc.getString("status")
                        );

                        booking.setTimestamp(
                                doc.getLong("timestamp") != null
                                        ? doc.getLong("timestamp")
                                        : 0
                        );

                        booking.setUserName(doc.getString("name"));
                        booking.setId(doc.getId());

                        List<String> images = (List<String>) doc.get("imageUrls");
                        if (images == null) images = new ArrayList<>();

                        booking.setImageUrls(images);

                        bookingList.add(booking);
                    }

                    adapter.notifyDataSetChanged();

                    // 🔥 IMPORTANT FIX
                    if (bookingList.isEmpty()) {
                        tvNoBookings.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvNoBookings.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    tvNoBookings.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
    }
}