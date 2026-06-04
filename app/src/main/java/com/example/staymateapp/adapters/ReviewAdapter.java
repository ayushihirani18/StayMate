package com.example.staymateapp.adapters;

import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staymateapp.R;
import com.example.staymateapp.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    List<Review> list;

    public ReviewAdapter(List<Review> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvReview, tvInitials;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvReviewerName);
            tvReview = itemView.findViewById(R.id.tvReviewText);
            ratingBar = itemView.findViewById(R.id.ratingBarItem);
            tvInitials = itemView.findViewById(R.id.tvInitials);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Review r = list.get(position);

        holder.tvName.setText(r.getUserName());
        holder.tvReview.setText(r.getReviewText());
        holder.ratingBar.setRating(r.getRating());

        // 🔥 INITIALS LOGIC
        String name = r.getUserName();

        if (name != null && !name.isEmpty()) {
            String[] parts = name.trim().split(" ");
            String initials = "";

            for (String part : parts) {
                if (!part.isEmpty()) {
                    initials += part.charAt(0);
                }
            }

            holder.tvInitials.setText(initials.toUpperCase());
        } else {
            holder.tvInitials.setText("U");
        }
        holder.itemView.setOnLongClickListener(v -> {

            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (!r.getUserId().equals(currentUserId)) {
                Toast.makeText(v.getContext(), "You can delete only your review", Toast.LENGTH_SHORT).show();
                return true;
            }

            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Review Options")
                    .setMessage("Do you want to delete this review?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("reviews")
                                .document(r.getId())
                                .delete()
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(v.getContext(), "Deleted", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}