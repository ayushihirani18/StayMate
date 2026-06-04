package com.example.staymateapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.staymateapp.ChatActivity;
import com.example.staymateapp.R;
import com.example.staymateapp.models.ChatUser;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private List<ChatUser> userList;
    private Context context;

    public ChatListAdapter(List<ChatUser> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_chat_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ChatUser user = userList.get(position);

        holder.tvName.setText(user.getName()); // we'll improve later
        holder.tvLastMessage.setText(user.getLastMessage());
        holder.tvTime.setText(formatTime(user.getTimestamp()));


        holder.itemView.setOnClickListener(v -> {

            String userId = user.getUserId();

            if (userId == null || userId.isEmpty()) {
                Toast.makeText(context, "User ID missing!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", userId);
            context.startActivity(intent);
        });

        String image = user.getProfileImage();
        String name = user.getName();

        if (image != null && !image.isEmpty()) {

            holder.imgUser.setVisibility(View.VISIBLE);
            holder.tvInitial.setVisibility(View.GONE);

            Glide.with(context)
                    .load(image)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgUser);

        } else {

            holder.imgUser.setVisibility(View.GONE);
            holder.tvInitial.setVisibility(View.VISIBLE);

            if (name != null && name.length() > 0) {
                holder.tvInitial.setText(name.substring(0, 1).toUpperCase());
            } else {
                holder.tvInitial.setText("?");
            }

        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvLastMessage;
        TextView tvInitial;
        ImageView imgUser;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            imgUser = itemView.findViewById(R.id.imgUser);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private String formatTime(Long time) {

        if (time == null) return "";

        long now = System.currentTimeMillis();

        java.util.Calendar nowCal = java.util.Calendar.getInstance();
        java.util.Calendar msgCal = java.util.Calendar.getInstance();

        msgCal.setTimeInMillis(time);

        // Same day → show time
        if (nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                nowCal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)) {

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("hh:mm a");

            return sdf.format(new java.util.Date(time));
        }

        // Yesterday
        nowCal.add(java.util.Calendar.DAY_OF_YEAR, -1);

        if (nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                nowCal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)) {

            return "Yesterday";
        }

        // Older → show date
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("dd MMM");

        return sdf.format(new java.util.Date(time));
    }
}