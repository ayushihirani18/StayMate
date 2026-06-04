package com.example.staymateapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.staymateapp.adapters.RoommateAdapter;
import com.example.staymateapp.models.Roommate;
import com.google.firebase.firestore.*;

import java.util.*;

public class BrowseRoommatesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Roommate> list;
    RoommateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_roommates);

        recyclerView = findViewById(R.id.recyclerRoommates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new RoommateAdapter(list);
        recyclerView.setAdapter(adapter);

        // 🔥 ADD BUTTON CLICK
        findViewById(R.id.btnAddRoommate).setOnClickListener(v -> {
            startActivity(new Intent(this, CreateRoommateActivity.class));
        });

        loadRoommates();
    }

    private void loadRoommates() {

        FirebaseFirestore.getInstance()
                .collection("roommates")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    list.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Roommate r = doc.toObject(Roommate.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            list.add(r);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}