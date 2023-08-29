package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Placeholder;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {
    private CircleImageView postUserPhoto;
    private TextView postUserName;
    private TextView postLocation;
    private ImageButton buttonOptions;
    private ImageView postImage;
    private LinearLayout linearLayoutPostText;
    private TextView postText;
    private LinearLayout linearLayoutPostVideo;
    private PlayerView postVideo;
    private Button buttonLike;
    private TextView textLike;
    private LinearLayout linearLayoutComment;
    private Button buttonComment;
    private TextView textComment;
    private TextView textDescription;
    private LinearLayout linearLayoutHide;
    private Button buttonHide;
    private LinearLayout linearLayoutShare;
    private Button buttonShare;
    private Button buttonSend;
    private CoordinatorLayout coordinatorLayoutDescription;
    private TextView fullDescription;
    private CoordinatorLayout coordinatorLayoutSettings;
    private LinearLayout linearLayoutCurrentUser;
    private LinearLayout linearLayoutHideLike;
    private LinearLayout linearLayoutLike;
    private LinearLayout linearLayoutHideComment;
    private LinearLayout linearLayoutVisibleComment;
    private LinearLayout deletePost;
    private LinearLayout linearLayoutFollow;
    private LinearLayout linearLayoutUnfollow;
    private ConstraintLayout constraintLayoutComment;
    private TextView textViewNumberComments;
    private RecyclerView recyclerViewComment;
    private EditText editTextComment;
    private TextView addComment;
    private FirebaseUser firebaseUser;
    private String postId;
    private String type;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postId = getIntent().getStringExtra("postId");
        type = getIntent().getStringExtra("type");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        postUserPhoto = (CircleImageView) findViewById(R.id.pa_user_photo);
        postUserName = (TextView) findViewById(R.id.pa_user_name);
        postLocation = (TextView) findViewById(R.id.pa_user_location);
        buttonOptions = (ImageButton) findViewById(R.id.pa_options);

        postImage = (ImageView) findViewById(R.id.pa_image);
        linearLayoutPostText = (LinearLayout) findViewById(R.id.linearLayoutPAText);
        postText = (TextView) findViewById(R.id.pa_text);
        linearLayoutPostVideo = (LinearLayout) findViewById(R.id.linearLayoutPAVideo);
        postVideo = (PlayerView) findViewById(R.id.pa_video);

        buttonLike = (Button) findViewById(R.id.buttonLikePA);
        textLike = (TextView) findViewById(R.id.textViewLikePA);
        linearLayoutComment = (LinearLayout) findViewById(R.id.pa_comment);
        buttonComment = (Button) findViewById(R.id.buttonCommentPA);
        textComment = (TextView) findViewById(R.id.textViewCommentPA);
        textDescription = (TextView) findViewById(R.id.pa_description);
        linearLayoutHide = (LinearLayout) findViewById(R.id.linearLayoutPAHide);
        buttonHide = (Button) findViewById(R.id.buttonHidePA);
        linearLayoutShare = (LinearLayout) findViewById(R.id.lin_share_pa);
        buttonShare = (Button) findViewById(R.id.buttonSharePA);
        buttonSend = (Button) findViewById(R.id.buttonSendPA);

        coordinatorLayoutDescription = (CoordinatorLayout) findViewById(R.id.coordinatorLayout_pa_description);
        fullDescription = (TextView) findViewById(R.id.pa_full_description);
        coordinatorLayoutSettings = (CoordinatorLayout) findViewById(R.id.coordinatorLayout_pa_settings);
        linearLayoutCurrentUser = (LinearLayout) findViewById(R.id.linearLayout_pa_currentUser);
        linearLayoutHideLike = (LinearLayout) findViewById(R.id.linearLayout_pa_hideNumberLike);
        linearLayoutLike = (LinearLayout) findViewById(R.id.linearLayout_pa_numberLike);
        linearLayoutHideComment = (LinearLayout) findViewById(R.id.linearLayout_pa_hideComment);
        linearLayoutVisibleComment = (LinearLayout) findViewById(R.id.linearLayout_pa_visibilityComment);
        deletePost = (LinearLayout) findViewById(R.id.linearLayout_pa_deletePost);
        linearLayoutFollow = (LinearLayout) findViewById(R.id.linearLayout_pa_follow);
        linearLayoutUnfollow = (LinearLayout) findViewById(R.id.linearLayout_pa_noFollow);

        constraintLayoutComment = (ConstraintLayout) findViewById(R.id.consPAComments);
        textViewNumberComments = (TextView) findViewById(R.id.commentsNumberPA);
        recyclerViewComment = (RecyclerView) findViewById(R.id.recCommentsPA);
        editTextComment = (EditText) findViewById(R.id.editTextCommentsPA);
        addComment = (TextView) findViewById(R.id.textViewCommentsPA);

        getPost();
    }
    private void getPost(){
        if(type.equals("post_home")){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
            ref.child(postId).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        post = dataSnapshot.getValue(Post.class);
                        setPost();
                    });
        }else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pages_posts");
            ref.child(postId).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        post = dataSnapshot.getValue(Post.class);
                        setPost();
                    });
        }
    }
    private void setPost(){
        String userId = post.getUser();
        setUserInfo(userId);
        if(userId.equals(firebaseUser.getUid())){
            linearLayoutCurrentUser.setVisibility(View.VISIBLE);
            deletePost.setVisibility(View.VISIBLE);
        }else {
            linearLayoutCurrentUser.setVisibility(View.GONE);
            deletePost.setVisibility(View.GONE);
        }
        if(post.getLocation() != null) postLocation.setText(post.getLocation());
        if(post.getDescription() != null) {
            textDescription.setText(post.getDescription());
            textDescription.setOnClickListener(view -> onClickFullDescription());
        }
        setVisibilityLike();

        buttonHide.setOnClickListener(view -> {
            coordinatorLayoutDescription.setVisibility(View.GONE);
            constraintLayoutComment.setVisibility(View.GONE);
            linearLayoutHide.setVisibility(View.GONE);
            buttonOptions.setVisibility(View.VISIBLE);
            textDescription.setVisibility(View.VISIBLE);
        });
    }
    private void setVisibilityLike(){
        if(post.isOptionLike()){
            linearLayoutHideLike.setVisibility(View.VISIBLE);
            linearLayoutLike.setVisibility(View.GONE);
            textLike.setVisibility(View.VISIBLE);
        }else {
            linearLayoutHideLike.setVisibility(View.GONE);
            linearLayoutLike.setVisibility(View.VISIBLE);
            textLike.setVisibility(View.GONE);
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
        linearLayoutLike.setOnClickListener(view -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("optionLike", true);
            ref.child(postId).updateChildren(map);
        });
    }
    private void onClickFullDescription(){
        if(!post.getDescription().isEmpty()){
            linearLayoutHide.setVisibility(View.VISIBLE);
            coordinatorLayoutDescription.setVisibility(View.VISIBLE);
            fullDescription.setText(post.getDescription());
            textDescription.setVisibility(View.GONE);
            buttonOptions.setVisibility(View.GONE);
        }
    }
    private void setUserInfo(String userId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(userId).get().addOnSuccessListener(dataSnapshot -> {
           User u = dataSnapshot.getValue(User.class);
           postUserName.setText(u.getName());
           if(u.getPhoto().equals("default")) postUserPhoto.setImageResource(R.drawable.ic_create_profile_vectors_photo);
           else Glide.with(this).load(u.getPhoto()).into(postUserPhoto);
           if(!firebaseUser.getUid().equals(u.getUserId())){
               setFollowUnfollow(u.getUserId());
           }
        });
    }
    private void setFollowUnfollow(String userId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("followers");
        ref.child(firebaseUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s  = snapshot.getValue(String.class);
                if(s != null){
                    linearLayoutUnfollow.setVisibility(View.VISIBLE);
                    linearLayoutFollow.setVisibility(View.GONE);
                }else {
                    linearLayoutUnfollow.setVisibility(View.GONE);
                    linearLayoutFollow.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}