package com.example.staymateapp.fragments.owner;

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

import java.util.*;

public class OwnerChatListFragment extends Fragment {

    RecyclerView recyclerView;
    List<ChatUser> userList;
    ChatListAdapter adapter;

    public OwnerChatListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerChatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        adapter = new ChatListAdapter(userList, getContext());
        recyclerView.setAdapter(adapter);

        loadChats();

        return view;
    }

    private void loadChats() {

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    userList.clear();

                    for (var doc : value.getDocuments()) {

                        List<String> participants = (List<String>) doc.get("participants");
                        String lastMessage = doc.getString("lastMessage");

                        String otherUserId = "";

                        for (String id : participants) {
                            if (!id.equals(currentUserId)) {
                                otherUserId = id;
                            }
                        }

                        if (otherUserId == null || otherUserId.isEmpty()) continue;

                        final String finalUserId = otherUserId;
                        final String finalLastMessage = lastMessage;

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(finalUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {

                                    String name = userDoc.getString("name");
                                    String profile = userDoc.getString("profileImage");

                                    userList.add(new ChatUser(finalUserId, name, profile, finalLastMessage));
                                    adapter.notifyDataSetChanged();
                                });
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}