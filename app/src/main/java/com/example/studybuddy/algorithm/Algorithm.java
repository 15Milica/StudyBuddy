package com.example.studybuddy.algorithm;

import androidx.annotation.NonNull;

import com.example.studybuddy.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Algorithm {
    private static FirebaseUser firebaseUser;
    private Map<String, Hashtag> hashtags;

    public Algorithm() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        hashtags = new HashMap<>();
    }

    public void setPosts(OnCompleteListener onCompleteListener) {
        getHashtags(onCompleteListener);
    }

    private void getHashtags(OnCompleteListener onCompleteListener) {
        DatabaseReference algoRef = FirebaseDatabase.getInstance().getReference("algorithm");

        algoRef.child(firebaseUser.getUid()).get().addOnSuccessListener(dataSnapshot -> {
            hashtags.clear();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Hashtag hashtag = snapshot.getValue(Hashtag.class);
                if (hashtag != null) hashtags.put(snapshot.getKey(), hashtag);
            }

            getPosts(onCompleteListener);
        });
    }

    private void getPosts(OnCompleteListener onCompleteListener) {
        List<Post> posts = new ArrayList<>();

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts");

        postRef.get().addOnSuccessListener(dataSnapshot -> {
            posts.clear();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Post post = snapshot.getValue(Post.class);
                if (!Objects.requireNonNull(post).getUser().equals(firebaseUser.getUid())) posts.add(post);
            }

            sortPosts(onCompleteListener, posts);
        });
    }

    private void sortPosts(OnCompleteListener onCompleteListener, List<Post> posts) {
        Collections.reverse(posts);
        posts.sort(new SortPosts(hashtags));
        onCompleteListener.onComplete(posts);
    }
    public static void setAlgorithm(String postId, String type, String value, List<String> hashtags) {
        DatabaseReference algorithmRef = FirebaseDatabase.getInstance().getReference("algorithm").child(firebaseUser.getUid());

        for(String hashtag : hashtags) {
            algorithmRef.child(hashtag).child(type).child(postId).setValue(value);
        }
    }

}
