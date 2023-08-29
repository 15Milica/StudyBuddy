package com.example.studybuddy.ui.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.StorylineAdapter;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StorylineActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private TextView textViewUserName;
    private CircleImageView userPhoto;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private StorylineAdapter storylineAdapter;
    private String userId;
    private String postId;
    private List<Post> posts;
    private Post selectedPost;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storyline);
        posts = new ArrayList<>();
        activity = this;
        userId = getIntent().getStringExtra("userId");
        postId = getIntent().getStringExtra("postId");

        progressBar = (ProgressBar) findViewById(R.id.progressBarStoryline);
        buttonBack = (ImageButton) findViewById(R.id.backStoryline);
        buttonBack.setOnClickListener(view -> onBackPressed());
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewStoryline);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        textViewUserName = (TextView) findViewById(R.id.userNameStoryline);
        userPhoto = (CircleImageView) findViewById(R.id.photoStoryline);

        setUserDetails();
        getPosts();
    }
    private void setUserDetails(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(userId).get().addOnSuccessListener(dataSnapshot -> {
            User user = dataSnapshot.getValue(User.class);
            textViewUserName.setText(user.getName());
            if(user.getPhoto().equals("default")) userPhoto.setImageResource(R.drawable.ic_create_profile_vectors_photo);
            else Glide.with(getApplicationContext()).load(user.getPhoto()).into(userPhoto);
        });
    }
    private void getPosts(){
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts");
        postRef.get().addOnSuccessListener(dataSnapshot -> {
            posts.clear();
            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Post post = snapshot.getValue(Post.class);
                if(post.getUser().equals(userId)) {
                    if(post.getId().equals(postId)) selectedPost = post;
                    posts.add(post);
                }
            }
            progressBar.setVisibility(View.GONE);

            Collections.reverse(posts);

            int index = posts.indexOf(selectedPost);

            storylineAdapter = new StorylineAdapter(getApplicationContext(), activity, posts, index);
            recyclerView.setAdapter(storylineAdapter);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(storylineAdapter != null) storylineAdapter.stopPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(storylineAdapter != null) storylineAdapter.stopPlayer();
    }
}