package com.example.studybuddy.ui.inbox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.InboxAdapter;
import com.example.studybuddy.model.ChatItem;
import com.example.studybuddy.model.ChatList;
import com.example.studybuddy.model.Group;
import com.example.studybuddy.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {

    private ImageButton imageInboxGroup;
    private ImageButton imageInboxNewChat;
    private EditText search;
    private SwipeableRecyclerView recyclerView;
    private FirebaseUser firebaseUser;
    private DatabaseReference refDatabase;
    private List<ChatList> chatsList;
    private List<ChatItem> chatItems;
    private List<User> users;
    private List<Group> groups;
    private InboxAdapter inboxAdapter;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);

        chatsList = new ArrayList<>();
        chatItems = new ArrayList<>();
        users = new ArrayList<>();
        groups = new ArrayList<>();

        progressBar = (ProgressBar) root.findViewById(R.id.progressBarInbox);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        imageInboxGroup = (ImageButton) root.findViewById(R.id.imageInboxGroup);
        imageInboxNewChat =(ImageButton) root.findViewById(R.id.imageInboxNewChat);

        imageInboxGroup.setOnClickListener(view -> onClickInboxGroup());
        imageInboxNewChat.setOnClickListener((view -> onClickInboxNewChat()));

        search = (EditText) root.findViewById(R.id.editTextInboxSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchChat(search.getText().toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        recyclerView = (SwipeableRecyclerView) root.findViewById(R.id.recyclerViewInbox);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setListener(new SwipeLeftRightCallback.Listener() {
            @Override
            public void onSwipedLeft(int position) {
                ChatItem item = chatItems.get(position);
                onDeleteChat(item, position);
            }
            @Override
            public void onSwipedRight(int position) {}
        });

        refDatabase = FirebaseDatabase.getInstance().getReference("chats_list").child(firebaseUser.getUid());
        refDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    chatsList.add(chatList);
                }
                showChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return root;
    }
    private void onClickInboxGroup() {
        imageInboxGroup.setEnabled(false);
        Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
        startActivity(intent);
    }
    private void onClickInboxNewChat() {
        imageInboxNewChat.setEnabled(false);
        Intent intent = new Intent(getActivity(), NewChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        imageInboxNewChat.setEnabled(true);
        imageInboxGroup.setEnabled(true);
    }
    private void showChats(){
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");

        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(search.getText().toString().isEmpty()){
                    chatItems.clear();
                    users.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User u = dataSnapshot.getValue(User.class);
                        for(ChatList chatList :chatsList) {
                            if(u.getUserId().equals(chatList.getId())) users.add(u);
                        }
                    }
                    for(User u:users)
                    {
                        final String chatId = u.getUserId();
                        final String chatType = "user";
                        final String chatName = u.getName();
                        final String chatPhoto = u.getPhoto();
                        ChatItem item = new ChatItem(chatId, chatName, chatType, chatPhoto);
                        chatItems.add(item);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference refGroups = FirebaseDatabase.getInstance().getReference("groups");
        refGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (search.getText().toString().isEmpty()) {
                    groups.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Group g = dataSnapshot.getValue(Group.class);
                        for (ChatList chatList : chatsList)
                            if (g.getGroupId().equals(chatList.getId())) groups.add(g);
                    }
                    for (Group g : groups) {
                        final String chatId = g.getGroupId();
                        final String chatType = "group";
                        final String chatName = g.getGroupName();
                        final String chatPhoto = g.getGroupPhoto();
                        ChatItem item = new ChatItem(chatId, chatName, chatType, chatPhoto);
                        chatItems.add(item);
                    }
                    inboxAdapter = new InboxAdapter(getContext(), chatItems);
                    recyclerView.setAdapter(inboxAdapter);
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {progressBar.setVisibility(View.GONE);}
        });
    }
    private void searchChat(String text){
        progressBar.setVisibility(View.VISIBLE);
        Query gueryUser = FirebaseDatabase.getInstance().getReference("users").orderByChild("search")
                .startAt(text)
                .endAt(text+"\uf8ff");

        gueryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatItems.clear();
                users.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    User u = dataSnapshot.getValue(User.class);
                    for(ChatList chatList:chatsList) if(u.getUserId().equals(chatList.getId())) users.add(u);
                }
                for(User u : users)
                {
                    final String chatId = u.getUserId();
                    final String chatName = u.getName();
                    final String chatPhoto = u.getPhoto();
                    final String chatType = "user";
                    ChatItem item = new ChatItem(chatId,chatName, chatType, chatPhoto);
                    chatItems.add(item);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Query queryGroup = FirebaseDatabase.getInstance().getReference("groups").orderByChild("search")
                .startAt(text)
                .endAt(text+"\uf8ff");
        queryGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groups.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Group g = dataSnapshot.getValue(Group.class);
                    for(ChatList chatList:chatsList) if(g.getGroupId().equals(chatList.getId())) groups.add(g);
                }
                for(Group g : groups)
                {
                    final String chatId = g.getGroupId();
                    final String chatName = g.getGroupName();
                    final String chatPhoto = g.getGroupPhoto();
                    final String chatType = "group";
                    ChatItem item = new ChatItem(chatId,chatName, chatType, chatPhoto);
                    chatItems.add(item);
                }
                inboxAdapter = new InboxAdapter(getContext(), chatItems);
                recyclerView.setAdapter(inboxAdapter);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {progressBar.setVisibility(View.GONE);}
        });
    }
    private void onDeleteChat(ChatItem item, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_delete, null);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        Button buttonConfirm = dialogView.findViewById(R.id.delete_button);
        Button buttonCancel = dialogView.findViewById(R.id.cancel_button);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = firebaseUser.getUid();
                String chatId = item.getChatId();

                if(item.getChatType().equals("user")){
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats").child(userId);
                    ref.child((chatId)).removeValue();
                }

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats_list").child(userId);
                reference.child(chatId).removeValue().addOnCompleteListener(task-> { if(task.isSuccessful()) Toast.makeText(getContext(), "Uspešno obrisan čet", Toast.LENGTH_SHORT).show(); });

                Toast.makeText(getContext(), "Uspešno obrisana konverzacija!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.getAdapter().notifyItemChanged(position);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}