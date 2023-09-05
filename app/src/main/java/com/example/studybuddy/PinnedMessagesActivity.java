package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.example.studybuddy.adapter.PinnedMessageAdapter;
import com.example.studybuddy.model.PinnedMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PinnedMessagesActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;
    private RecyclerView recyclerView;
    private PinnedMessageAdapter pinnedMessageAdapter;
    private List<PinnedMessage> pinnedMessageList;
    private FirebaseUser firebaseUser;
    private Activity activity;
    private boolean status;
    private SessionManager sessionManager;
    private DatabaseReference user_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_messages);
        activity = this;
        sessionManager = new SessionManager(getApplicationContext());

        final String chatId = getIntent().getStringExtra("chatId");
        pinnedMessageList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user_status = FirebaseDatabase.getInstance().getReference("user_status").child(firebaseUser.getUid());

        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBackPinnedMessage);
        imageButtonBack.setOnClickListener(view -> onBackPressed());

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewPinnedMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(PinnedMessagesActivity.this));

        showPinnedMessage(chatId);
    }
    private void showPinnedMessage(String chatId){
        DatabaseReference ref  = FirebaseDatabase.getInstance().getReference("pinned_messages")
                .child(firebaseUser.getUid());
        if(chatId == null){
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    pinnedMessageList.clear();
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                        pinnedMessageList.add(dataSnapshot.getValue(PinnedMessage.class));
                    }
                    pinnedMessageAdapter = new PinnedMessageAdapter(getApplicationContext(), pinnedMessageList, activity);
                    recyclerView.setAdapter(pinnedMessageAdapter);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        }else {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    pinnedMessageList.clear();
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                        PinnedMessage pinnedMessage = dataSnapshot.getValue(PinnedMessage.class);
                        if(pinnedMessage.getChatId().equals(chatId)){
                            pinnedMessageList.add(pinnedMessage);
                        }
                    }
                    pinnedMessageAdapter = new PinnedMessageAdapter(getApplicationContext(), pinnedMessageList, activity);
                    recyclerView.setAdapter(pinnedMessageAdapter);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        status = sessionManager.getActivityStatus();

        if(status) { user_status.onDisconnect().setValue("Offline"); }
        else {
            user_status.setValue("");
            user_status.onDisconnect().setValue("");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(status) { user_status.setValue("Online"); }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(status) { user_status.setValue("Offline"); }
    }
}