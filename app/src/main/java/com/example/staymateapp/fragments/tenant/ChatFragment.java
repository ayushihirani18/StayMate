package com.example.staymateapp.fragments.tenant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staymateapp.R;
import com.example.staymateapp.adapters.ChatAdapter;
import com.example.staymateapp.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    RecyclerView recyclerChat;
    EditText etMessage;
    ImageView btnSend;

    List<ChatMessage> messageList;
    ChatAdapter chatAdapter;

    public ChatFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerChat = view.findViewById(R.id.recyclerChat);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        // Initialize list
        messageList = new ArrayList<>();

        // Adapter setup
        chatAdapter = new ChatAdapter(messageList, getContext());
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(chatAdapter);

        // Send button click
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();

            if (!TextUtils.isEmpty(message)) {

                // Temporary dummy message (sender = tenant)
                messageList.add(new ChatMessage(message, "tenant", "sent"));

                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerChat.scrollToPosition(messageList.size() - 1);

                etMessage.setText("");
            }
        });

        return view;
    }
}