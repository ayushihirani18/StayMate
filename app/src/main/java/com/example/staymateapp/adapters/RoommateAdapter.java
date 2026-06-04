package com.example.staymateapp.adapters;

import android.content.Intent;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staymateapp.ChatActivity;
import com.example.staymateapp.R;
import com.example.staymateapp.RoommateDetailsActivity;
import com.example.staymateapp.models.Roommate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RoommateAdapter extends RecyclerView.Adapter<RoommateAdapter.ViewHolder> {

    List<Roommate> list;

    public RoommateAdapter(List<Roommate> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvLocation, tvBudget, tvPreference, tvOwnerTag;
        Button btnChat, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvPreference = itemView.findViewById(R.id.tvPreference);
            tvOwnerTag = itemView.findViewById(R.id.tvOwnerTag);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_roommate, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Roommate r = list.get(position);

        holder.tvTitle.setText(r.getTitle());
        holder.tvLocation.setText("📍 " + r.getCity() + ", " + r.getArea());
        holder.tvBudget.setText("💰 ₹" + r.getBudget() + "/month");
        holder.tvPreference.setText("👤 " + r.getDescription());

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (r.getUserId().equals(currentUserId)) {
            holder.btnChat.setVisibility(View.GONE);
        } else {
            holder.btnChat.setVisibility(View.VISIBLE);
        }

        if (r.getUserId().equals(currentUserId)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
        if (r.getUserId().equals(currentUserId)) {
            holder.tvOwnerTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvOwnerTag.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("roommates")
                    .document(r.getId())
                    .delete()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(v.getContext(), "Deleted", Toast.LENGTH_SHORT).show()
                    );
        });

// 🔥 OPEN DETAILS ON CARD CLICK
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), RoommateDetailsActivity.class);

            intent.putExtra("title", r.getTitle());
            intent.putExtra("location", r.getCity() + ", " + r.getArea());
            intent.putExtra("budget", r.getBudget());
            intent.putExtra("description", r.getDescription());
            intent.putExtra("preference", r.getDescription());
            intent.putExtra("date", r.getDate());
            intent.putExtra("userId", r.getUserId()); // 🔥 VERY IMPORTANT

            v.getContext().startActivity(intent);
        });
        holder.btnChat.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), ChatActivity.class);

            intent.putExtra("userId", r.getUserId()); // 🔥 important

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}