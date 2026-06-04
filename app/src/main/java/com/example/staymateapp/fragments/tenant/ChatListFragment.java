package com.example.staymateapp.fragments.tenant;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.staymateapp.R;
import com.example.staymateapp.adapters.ChatListAdapter;
import com.example.staymateapp.models.ChatUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatListFragment extends Fragment {

    RecyclerView recyclerView;
    List<ChatUser> userList;
    ChatListAdapter adapter;


    public ChatListFragment() {}

    private void loadChats() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    userList.clear();

                    for (var doc : value.getDocuments()) {

                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants == null) continue;

                        String lastMessage = doc.getString("lastMessage");
                        Long timestamp = doc.getLong("timestamp");
                        if (lastMessage == null) lastMessage = "";

                        String otherUserId = null;

                        for (String id : participants) {
                            if (id != null && !id.equals(currentUserId)) {
                                otherUserId = id;
                                break;
                            }
                        }
                        if (otherUserId == null) continue;


                        final String finalUserId = otherUserId;
                        final String finalLastMessage = lastMessage;

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(finalUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {

                                    String name = userDoc.getString("name");
                                    String profile = userDoc.getString("profileImage");
                                    Boolean online = userDoc.getBoolean("online");

                                    ChatUser chatUser = new ChatUser(finalUserId, name, profile, finalLastMessage);
                                    chatUser.setTimestamp(timestamp);
                                    chatUser.setOnline(online);


                                    chatUser.setUserId(finalUserId);
                                    chatUser.setName(name);
                                    chatUser.setProfileImage(profile);
                                    chatUser.setLastMessage(finalLastMessage);

                                    userList.add(chatUser);
                                    Collections.sort(userList, (a, b) -> {

                                        if (a.getTimestamp() == null) return 1;
                                        if (b.getTimestamp() == null) return -1;

                                        return b.getTimestamp().compareTo(a.getTimestamp());
                                    });
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerChatList);

        if (recyclerView == null) {
            throw new RuntimeException("RecyclerView NOT FOUND — check XML ID");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        userList = new ArrayList<>();
        adapter = new ChatListAdapter(userList, requireContext());

        recyclerView.setAdapter(adapter);

        loadChats();

        return view;
    }



}