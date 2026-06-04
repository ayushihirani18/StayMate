package com.example.staymateapp.fragments.owner;

import android.os.Bundle;
import android.view.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.staymateapp.R;
import com.example.staymateapp.adapters.BookingAdapter;
import com.example.staymateapp.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class BookingRequestsFragment extends Fragment {

    RecyclerView recyclerView;
    List<Booking> bookingList;
    BookingAdapter adapter;

    public BookingRequestsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_bookings, container, false);

        recyclerView = view.findViewById(R.id.recyclerBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();

        // ✅ OWNER MODE (no click)
        adapter = new BookingAdapter(bookingList, true);

        recyclerView.setAdapter(adapter);

        loadBookings();

        return view;
    }

    private void loadBookings() {

        String ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(query -> {

                    bookingList.clear();

                    for (var doc : query) {

                        Booking booking = new Booking(
                                doc.getString("propertyName"),
                                doc.getString("rent"),
                                doc.getString("status")
                        );

                        // 📅 timestamp safe
                        booking.setTimestamp(
                                doc.getLong("timestamp") != null
                                        ? doc.getLong("timestamp")
                                        : 0
                        );

                        booking.setUserName(doc.getString("name"));
                        booking.setId(doc.getId());

                        // 🖼️ SAFE IMAGE LIST
                        List<String> images = (List<String>) doc.get("imageUrls");
                        if (images == null) {
                            images = new ArrayList<>();
                        }
                        booking.setImageUrls(images);

                        bookingList.add(booking);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}