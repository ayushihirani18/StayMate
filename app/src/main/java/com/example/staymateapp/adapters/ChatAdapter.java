package com.example.staymateapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.staymateapp.R;
import com.example.staymateapp.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;
    private Context context;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId()
                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatMessage message = messageList.get(position);

        if (holder instanceof SentViewHolder) {

            SentViewHolder h = (SentViewHolder) holder;

            handleDateHeader(h.tvDateHeader, position);

            // IMAGE MESSAGE
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                h.tvMessage.setVisibility(View.GONE);
                h.imgMessage.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(message.getImageUrl())
                        .into(h.imgMessage);
            } else {
                h.tvMessage.setVisibility(View.VISIBLE);
                h.imgMessage.setVisibility(View.GONE);
                h.tvMessage.setText(message.getMessage());
            }

            // TIME
            h.tvTime.setText(formatTime(message.getTimestamp()));

            // STATUS
            String status = message.getStatus();

            if (status != null) {
                if (status.equals("sent")) {
                    h.tvStatus.setText("✓");
                    h.tvStatus.setTextColor(0xFF888888);
                } else if (status.equals("delivered")) {
                    h.tvStatus.setText("✓✓");
                    h.tvStatus.setTextColor(0xFF888888);
                } else if (status.equals("seen")) {
                    h.tvStatus.setText("✓✓");
                    h.tvStatus.setTextColor(0xFF2196F3);
                }
            }
        }

        else if (holder instanceof ReceivedViewHolder) {

            ReceivedViewHolder h = (ReceivedViewHolder) holder;

            handleDateHeader(h.tvDateHeader, position);

            // IMAGE MESSAGE
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                h.tvMessage.setVisibility(View.GONE);
                h.imgMessage.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(message.getImageUrl())
                        .into(h.imgMessage);
            } else {
                h.tvMessage.setVisibility(View.VISIBLE);
                h.imgMessage.setVisibility(View.GONE);
                h.tvMessage.setText(message.getMessage());
            }

            // TIME
            h.tvTime.setText(formatTime(message.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // 🔥 DATE HEADER LOGIC (CLEAN)
    private void handleDateHeader(TextView tvDateHeader, int position) {

        long currentTimestamp = messageList.get(position).getTimestamp();

        if (currentTimestamp == 0) {
            tvDateHeader.setVisibility(View.GONE);
            return;
        }

        String currentDate = getDateLabel(currentTimestamp);

        String previousDate = null;

        if (position > 0) {
            long prevTimestamp = messageList.get(position - 1).getTimestamp();
            previousDate = getDateLabel(prevTimestamp);
        }

        // 🔥 ONLY show when date changes
        if (position == 0 || !currentDate.equals(previousDate)) {
            tvDateHeader.setVisibility(View.VISIBLE);
            tvDateHeader.setText(currentDate);
        } else {
            tvDateHeader.setVisibility(View.GONE);
        }
    }

    // 🔥 FORMAT TIME
    private String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    // 🔥 FORMAT DATE
    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    // 🔥 TODAY / YESTERDAY LOGIC
    private String getDateLabel(long timestamp) {

        java.util.Calendar msgCal = java.util.Calendar.getInstance();
        msgCal.setTimeInMillis(timestamp);

        java.util.Calendar today = java.util.Calendar.getInstance();

        // TODAY
        if (today.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        // YESTERDAY
        today.add(java.util.Calendar.DAY_OF_YEAR, -1);

        if (today.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        // NORMAL DATE
        return formatDate(timestamp);
    }

    // SENT VIEW HOLDER
    public static class SentViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage, tvStatus, tvTime, tvDateHeader;
        ImageView imgMessage;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgMessage = itemView.findViewById(R.id.imgMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }
    }

    // RECEIVED VIEW HOLDER
    public static class ReceivedViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage, tvTime, tvDateHeader;
        ImageView imgMessage;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMessage = itemView.findViewById(R.id.tvMessage);
            imgMessage = itemView.findViewById(R.id.imgMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }
    }
}