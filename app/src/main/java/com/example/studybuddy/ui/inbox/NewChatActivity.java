package com.example.studybuddy.ui.inbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.studybuddy.R;
import com.example.studybuddy.adapter.UserAdapter;
import com.example.studybuddy.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewChatActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;
    private ImageButton imageButtonInvite;
    private EditText searchInbox;
    private List<User> users;
    private UserAdapter userAdapter;
    private RecyclerView recyclerView;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        users = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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
                    if(!firebaseUser.getUid().equals(u.getUserId())) { users.add(u); }
                }

                userAdapter = new UserAdapter(NewChatActivity.this, users);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}