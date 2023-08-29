package com.example.studybuddy.ui.pages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.R;
import com.example.studybuddy.adapter.PagePostAdapter;
import com.example.studybuddy.model.Page;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.ui.home.NewPostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PageActivity extends AppCompatActivity {

    private static final int REQUEST_DETAILS = 100;
    private TextView textViewBack;
    private TextView textViewDetails;
    private TextView textPageName;
    private TextView textPageMembers;
    private Page page;
    private RecyclerView recyclerView;
    private PagePostAdapter pagePostAdapter;
    private List<Post> posts;
    private Button buttonNewPost;
    private Map<String, String> members;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        posts = new ArrayList<>();
        activity = this;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        page = (Page) getIntent().getSerializableExtra("page");

        textViewBack = (TextView) findViewById(R.id.textViewBackPage);
        textViewBack.setOnClickListener(view -> onBackPressed());

        textViewDetails = (TextView) findViewById(R.id.textViewDetailsPage);
        textViewDetails.setOnClickListener(view -> onDetails());

        textPageName = (TextView) findViewById(R.id.textViewPageName);
        textPageName.setText(page.getPageName());

        members = page.getMembers();
        textPageMembers = (TextView) findViewById(R.id.textViewPagePeople);
        textPageMembers.setText(String.valueOf(members.size()));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewPage);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        setPosts();

        buttonNewPost = (Button) findViewById(R.id.buttonPageNewPost);
        buttonNewPost.setOnClickListener(view -> onClickNewPost());
    }
    private void onDetails() {
        Intent intent = new Intent(this, PageDetailsActivity.class);
        intent.putExtra("pageId", page.getPageId());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_DETAILS);
    }
    private void onClickNewPost(){
        Intent intent = new Intent(this, NewPostActivity.class);
        intent.putExtra("pageId", page.getPageId());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_DETAILS && resultCode == RESULT_OK) finish();
    }
    private void setPosts(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pages_posts");
        ref.child(page.getPageId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                    if(members.containsKey(post.getUser())) posts.add(post);
                }
                Collections.reverse(posts);
                pagePostAdapter = new PagePostAdapter(getApplicationContext(), activity, page, posts);
                recyclerView.setAdapter(pagePostAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(pagePostAdapter != null) pagePostAdapter.stopPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(pagePostAdapter != null) pagePostAdapter.stopPlayer();
    }

}