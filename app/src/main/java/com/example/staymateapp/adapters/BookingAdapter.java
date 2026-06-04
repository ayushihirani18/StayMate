package com.example.staymateapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.staymateapp.R;
import com.example.staymateapp.models.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    Button btnAccept, btnReject;
    private boolean isOwner;

    public BookingAdapter(List<Booking> bookingList, boolean isOwner) {
        this.bookingList = bookingList;
        this.isOwner = isOwner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Booking booking = bookingList.get(position);

        // 📝 Texts (SAFE)
        if (booking.getPropertyName() != null)
            holder.tvName.setText(booking.getPropertyName());

        if (booking.getRent() != null)
            holder.tvRent.setText("₹ " + booking.getRent());

        if (booking.getStatus() != null)
            holder.tvStatus.setText("Status: " + booking.getStatus());

        String status = booking.getStatus();

        if ("Approved".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_approved);
        } else if ("Rejected".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_rejected);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_pending);
        }

        if ("Approved".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }

        // 📅 Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        holder.tvDate.setText("Booked on: " + sdf.format(new Date(booking.getTimestamp())));

        // 🖼️ IMAGE
        if (booking.getImageUrls() != null &&
                !booking.getImageUrls().isEmpty() &&
                booking.getImageUrls().get(0) != null &&
                !booking.getImageUrls().get(0).isEmpty()) {

            Glide.with(holder.itemView.getContext())
                    .load(booking.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(holder.imgProperty);

        } else {
            holder.imgProperty.setImageResource(R.drawable.ic_home);
        }

        // 🔥 SHOW/HIDE BUTTONS BASED ON ROLE
        if (!isOwner) {
            // 🔥 TENANT
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.VISIBLE);

        } else {
            // 🔥 OWNER
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
        }
        // 🔥 ACCEPT BOOKING
        holder.btnApprove.setOnClickListener(v -> {

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("bookings")
                    .document(booking.getId())
                    .update("status", "Approved")
                    .addOnSuccessListener(unused -> {

                        android.widget.Toast.makeText(
                                v.getContext(),
                                "Booking Approved ✅",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();

                        booking.setStatus("Approved");
                        notifyItemChanged(holder.getAdapterPosition());
                    });
        });


// 🔥 REJECT BOOKING
        holder.btnReject.setOnClickListener(v -> {

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("bookings")
                    .document(booking.getId())
                    .update("status", "Rejected")
                    .addOnSuccessListener(unused -> {

                        android.widget.Toast.makeText(
                                v.getContext(),
                                "Booking Rejected ❌",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();

                        booking.setStatus("Rejected");
                        notifyItemChanged(holder.getAdapterPosition());
                    });
        });
        holder.btnCancel.setOnClickListener(v -> {

            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel?")
                    .setPositiveButton("Yes", (d, w) -> {

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("bookings")
                                .document(booking.getId())
                                .delete()
                                .addOnSuccessListener(unused -> {

                                    android.widget.Toast.makeText(
                                            v.getContext(),
                                            "Booking Cancelled",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();

                                    int pos = holder.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        bookingList.remove(pos);
                                        notifyItemRemoved(pos);
                                    }
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        holder.itemView.setOnClickListener(v -> {

            android.content.Intent intent =
                    new android.content.Intent(v.getContext(),
                            com.example.staymateapp.PropertyDetailsActivity.class);

            intent.putExtra("propertyName", booking.getPropertyName());
            intent.putExtra("location", booking.getLocation());
            intent.putExtra("rent", booking.getRent());
            intent.putExtra("ownerId", booking.getOwnerId());
            intent.putExtra("contact", booking.getContact());

            // 🔥 IMPORTANT (images)
            if (booking.getImageUrls() != null && !booking.getImageUrls().isEmpty()) {
                intent.putStringArrayListExtra(
                        "imageUrls",
                        new java.util.ArrayList<>(booking.getImageUrls())
                );
            }

            // 🔥 VERY IMPORTANT (for booking logic)
            intent.putExtra("propertyId", booking.getPropertyId());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProperty;
        TextView tvName, tvRent, tvStatus, tvDate;
        Button btnApprove, btnReject;
        com.google.android.material.button.MaterialButton btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProperty = itemView.findViewById(R.id.imgProperty);
            tvName = itemView.findViewById(R.id.txtPropertyName);
            tvRent = itemView.findViewById(R.id.txtRent);
            tvStatus = itemView.findViewById(R.id.txtStatus);
            tvDate = itemView.findViewById(R.id.txtDate);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}