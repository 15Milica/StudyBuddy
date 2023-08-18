package com.example.studybuddy.ui.inbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.UserAdapter;
import com.example.studybuddy.model.InviteItem;
import com.example.studybuddy.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NewChatActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ImageButton imageButtonBack;
    private ImageButton imageButtonInvite;
    private EditText searchInbox;
    private List<User> users;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference refStatus;

    private List<String> mFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        users = new ArrayList<>();
        mFollowers = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        refStatus = FirebaseDatabase.getInstance().getReference("user_status").child(firebaseUser.getUid());

        progressBar = (ProgressBar) findViewById(R.id.progressBarNewChat);

        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBackNewChat);
        imageButtonBack.setOnClickListener(view -> onBackPressed());

        imageButtonInvite = (ImageButton) findViewById(R.id.imageButtonInvitePerson);
        imageButtonInvite.setOnClickListener(view -> onClickInvite());


        searchInbox = (EditText) findViewById(R.id.editTextNewChatSearch);
        searchInbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String user = searchInbox.getText().toString().toLowerCase();
                searchUser(user);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getFollowers();
    }
    private void onClickInvite(){
        imageButtonInvite.setEnabled(false);
        Intent intent = new Intent(this, InviteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageButtonInvite.setEnabled(true);
        refStatus.setValue("Online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        refStatus.setValue("Offline");
    }

    private void searchUser(String u) {
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("search")
                .startAt(u)
                .endAt(u + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    User u = dataSnapshot.getValue(User.class);

                    assert u != null;
                    assert firebaseUser != null;
                    if(!firebaseUser.getUid().equals(u.getUserId()) && Check.isFollower(u.getUserId(), mFollowers)) { users.add(u); }
                }

                userAdapter = new UserAdapter(NewChatActivity.this, users);
                recyclerView.setAdapter(userAdapter);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers(){
        DatabaseReference refFollowers = FirebaseDatabase.getInstance().getReference("followers");
        refFollowers.child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mFollowers.clear();
                        for(DataSnapshot dataSnapshot:snapshot.getChildren())
                            mFollowers.add(dataSnapshot.getKey());
                        readUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void readUsers(){
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(searchInbox.getText().toString().isEmpty()) {
                    users.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User u = dataSnapshot.getValue(User.class);
                        assert u != null;
                        assert firebaseUser != null;
                        if (!firebaseUser.getUid().equals(u.getUserId()) && Check.isFollower(u.getUserId(), mFollowers)) { users.add(u); }
                    }
                    userAdapter = new UserAdapter(NewChatActivity.this, users);
                    recyclerView.setAdapter(userAdapter);
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { progressBar.setVisibility(View.GONE); }
        });
    }

}