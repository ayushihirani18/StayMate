package com.example.staymateapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.staymateapp.adapters.ChatAdapter;
import com.example.staymateapp.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerChat;
    EditText etMessage;
    ImageView btnSend, imgProfile, btnBack;
    TextView tvUserName, tvUserStatus;

    List<ChatMessage> messageList;
    ChatAdapter chatAdapter;

    String receiverId, senderId, chatId;

    FirebaseFirestore db;

    ListenerRegistration userListener, chatListener, messageListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverId = getIntent().getStringExtra("userId");

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "Chat user missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 INIT VIEWS
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);

        db = FirebaseFirestore.getInstance();

        chatId = getChatId(senderId, receiverId);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, this);

        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());

        setupTypingListener();
        loadUserData();
        listenTypingStatus();
        loadMessages();
    }

    // 🔥 TYPING LISTENER
    private void setupTypingListener() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (chatId == null) return;

                db.collection("chats")
                        .document(chatId)
                        .update(
                                "typing", s.length() > 0,
                                "typingBy", senderId
                        );
            }
        });
    }

    // 🔥 LOAD USER DATA
    private void loadUserData() {

        userListener = db.collection("users")
                .document(receiverId)
                .addSnapshotListener((doc, e) -> {

                    if (doc != null && doc.exists()) {

                        String name = doc.getString("name");
                        String image = doc.getString("profileImage");
                        Boolean online = doc.getBoolean("online");
                        Long lastSeen = doc.getLong("lastSeen");

                        if (name != null) tvUserName.setText(name);

                        if (image != null && !image.isEmpty()) {
                            Glide.with(this)
                                    .load(image)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(imgProfile);
                        }

                        if (online != null && online) {
                            tvUserStatus.setText("Online 🟢");
                        } else if (lastSeen != null) {
                            tvUserStatus.setText("Last seen: " + getTimeAgo(lastSeen));
                        } else {
                            tvUserStatus.setText("Offline");
                        }
                    }
                });
    }

    // 🔥 LISTEN TYPING
    private void listenTypingStatus() {
        chatListener = db.collection("chats")
                .document(chatId)
                .addSnapshotListener((doc, e) -> {

                    if (doc != null && doc.exists()) {

                        Boolean typing = doc.getBoolean("typing");
                        String typingBy = doc.getString("typingBy");

                        if (typing != null && typing &&
                                typingBy != null &&
                                !typingBy.equals(senderId)) {

                            tvUserStatus.setText("Typing...");
                        }
                    }
                });
    }

    // 🔥 SEND MESSAGE
    private void sendMessage() {

        String msg = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(msg)) return;

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        message.put("receiverId", receiverId);
        message.put("message", msg);
        message.put("status", "sent");
        message.put("timestamp", System.currentTimeMillis());

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> etMessage.setText(""));

        db.collection("chats")
                .document(chatId)
                .set(new HashMap<String, Object>() {{
                    put("participants", Arrays.asList(senderId, receiverId));
                    put("lastMessage", msg);
                    put("timestamp", System.currentTimeMillis());
                }}, SetOptions.merge());

        db.collection("chats")
                .document(chatId)
                .update("typing", false);
    }

    // 🔥 LOAD MESSAGES
    private void loadMessages() {

        messageListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    messageList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        String msg = doc.getString("message");
                        String sender = doc.getString("senderId");
                        String status = doc.getString("status");
                        String imageUrl = doc.getString("imageUrl");
                        Long timestamp = doc.getLong("timestamp");

                        ChatMessage msgObj = new ChatMessage(msg, sender, status);
                        msgObj.setImageUrl(imageUrl);
                        msgObj.setTimestamp(timestamp != null ? timestamp : 0);

                        messageList.add(msgObj);

                        // STATUS UPDATE
                        if (!sender.equals(senderId)) {

                            if ("sent".equals(status)) {
                                doc.getReference().update("status", "delivered");
                            } else if ("delivered".equals(status)) {
                                doc.getReference().update("status", "seen");
                            }
                        }
                    }

                    chatAdapter.notifyDataSetChanged();
                    recyclerChat.smoothScrollToPosition(messageList.size() - 1);
                });
    }

    private String getChatId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ?
                user1 + "_" + user2 :
                user2 + "_" + user1;
    }

    private String getTimeAgo(long time) {
        long diff = System.currentTimeMillis() - time;

        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hr ago";

        return (hours / 24) + " days ago";
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        // 🔥 RESET TYPING
        db.collection("chats")
                .document(chatId)
                .update("typing", false);

        // 🔥 REMOVE LISTENERS
        if (userListener != null) userListener.remove();
        if (chatListener != null) chatListener.remove();
        if (messageListener != null) messageListener.remove();

        db.collection("users")
                .document(senderId)
                .update("online", false);
    }
}