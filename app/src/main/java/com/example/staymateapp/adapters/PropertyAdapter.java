package com.example.staymateapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;
import com.example.staymateapp.R;
import com.example.staymateapp.models.Property;

import java.util.List;

public class PropertyAdapter
        extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private final List<Property> propertyList;
    private final OnPropertyActionListener listener;
    private final boolean isOwner;


    public interface OnPropertyActionListener {
        void onEdit(Property property);
        void onDelete(Property property);
        void onView(Property property);
    }

    public PropertyAdapter(List<Property> propertyList,
                           OnPropertyActionListener listener,
                           boolean isOwner) {
        this.propertyList = propertyList;
        this.listener = listener;
        this.isOwner = isOwner;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property, parent, false);

        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PropertyViewHolder holder,
            int position) {

        Property property = propertyList.get(position);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        final String userId =
                auth.getCurrentUser() != null
                        ? auth.getCurrentUser().getUid()
                        : null;

        boolean[] isSaved = {false};

        // 🔥 FAVORITE CHECK
        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("savedProperties")
                    .document(property.getId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            isSaved[0] = true;
                            holder.btnFavorite.setImageResource(R.drawable.ic_favorite);
                        } else {
                            isSaved[0] = false;
                            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                        }
                    });


        }


        // 🔹 BASIC INFO
        holder.tvName.setText(property.getName());
        holder.tvLocation.setText(property.getLocation());

        String rentRange = "₹ " +
                property.getMinRent() +
                " - ₹ " +
                property.getMaxRent();

        holder.tvRent.setText(rentRange);

        // 🔥 CLEAN IMAGE LOAD (ONLY imageUrls)
        String imageToLoad = null;

        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            imageToLoad = property.getImageUrls().get(0); // first image as thumbnail
        }

        if (imageToLoad != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageToLoad)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(holder.imgProperty);
        } else {
            holder.imgProperty.setImageResource(R.drawable.ic_home);
        }

        // 🔥 AMENITIES
        StringBuilder amenitiesText = new StringBuilder();

        if (property.hasWifi()) amenitiesText.append("📶 WiFi  ");
        if (property.hasAc()) amenitiesText.append("❄ AC  ");
        if (property.hasParking()) amenitiesText.append("🚗 Parking");

        if (amenitiesText.length() == 0) {
            amenitiesText.append("No Amenities");
        }
        if (property.getPropertyType() != null) {
            holder.txtType.setText(property.getPropertyType());
        } else {
            holder.txtType.setText("PG"); // default
        }

        holder.tvAmenities.setText(amenitiesText.toString());
        // ⭐ LOAD RATING
        FirebaseFirestore.getInstance()
                .collection("reviews")
                .whereEqualTo("propertyId", property.getId())
                .get()
                .addOnSuccessListener(query -> {

                    float total = 0;

                    for (DocumentSnapshot doc : query) {
                        Double rating = doc.getDouble("rating");
                        if (rating != null) {
                            total += rating;
                        }
                    }

                    if (!query.isEmpty()) {
                        float avg = total / query.size();
                        holder.tvRating.setText("⭐ " + String.format("%.1f", avg) + " (" + query.size() + ")");
                    } else {
                        holder.tvRating.setText("⭐ 0.0");
                    }
                });

        // ❤️ FAVORITE BUTTON
        holder.btnFavorite.setOnClickListener(v -> {

            if (userId == null) {
                Toast.makeText(holder.itemView.getContext(),
                        "Login required",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            holder.btnFavorite.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(120)
                    .withEndAction(() ->
                            holder.btnFavorite.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                    );

            if (isSaved[0]) {

                db.collection("users")
                        .document(userId)
                        .collection("savedProperties")
                        .document(property.getId())
                        .delete();

                holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                isSaved[0] = false;

                Toast.makeText(holder.itemView.getContext(),
                        "Removed from Favorites",
                        Toast.LENGTH_SHORT).show();

            } else {

                db.collection("users")
                        .document(userId)
                        .collection("savedProperties")
                        .document(property.getId())
                        .set(property);

                holder.btnFavorite.setImageResource(R.drawable.ic_favorite);
                isSaved[0] = true;

                Toast.makeText(holder.itemView.getContext(),
                        "Saved to Favorites ❤️",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 OWNER CONTROLS
        if (isOwner) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(property));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(property));

        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // 🔥 VIEW CLICK + ANIMATION (UPDATED)
        holder.itemView.setOnClickListener(v -> {

            // 💥 Smooth click animation
            v.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100);

                        // 👇 Call listener AFTER animation
                        if (listener != null) {
                            listener.onView(property);
                        }
                    });
        });


    }

    @Override
    public int getItemCount() {
        return propertyList != null ? propertyList.size() : 0;
    }

    public static class PropertyViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvName, tvLocation, tvRent, tvAmenities, tvRating;
        ImageView imgProperty;
        ImageButton btnEdit, btnDelete, btnFavorite;
        TextView txtType;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProperty = itemView.findViewById(R.id.imgProperty);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRent = itemView.findViewById(R.id.tvRent);
            tvAmenities = itemView.findViewById(R.id.tvAmenities);
            txtType = itemView.findViewById(R.id.txtType);
            tvRating = itemView.findViewById(R.id.tvRating);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}