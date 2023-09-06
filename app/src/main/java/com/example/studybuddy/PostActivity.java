package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Placeholder;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.adapter.CommentAdapter;
import com.example.studybuddy.algorithm.Algorithm;
import com.example.studybuddy.chat.ForwardMessageActivity;
import com.example.studybuddy.model.Comment;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
import com.example.studybuddy.notification.Notification;
import com.example.studybuddy.ui.profile.UserProfileActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private SimpleExoPlayer simpleExoPlayer;
    private static final String LIKE = "likes";
    private static final String COMMENT = "comments";
    private static final String SHARE = "shares";
    private String token;

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
            ref.child(type).child(postId).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        post = dataSnapshot.getValue(Post.class);
                        setPost();
                    });
        }
    }
    private void setPost(){
        String userId = post.getUser();
        setUserInfo(userId);
        if(!firebaseUser.getUid().equals(userId)) {
            Check.settingsPagePost(textDescription, linearLayoutHide, coordinatorLayoutDescription, coordinatorLayoutSettings, constraintLayoutComment, buttonOptions);
            Check.enableButtonPagePost(buttonLike, buttonComment, buttonShare, buttonSend, textDescription, true);
        }
        if(post.getLocation() != null) postLocation.setText(post.getLocation());

        if(post.getDescription() != null) {
            textDescription.setText(post.getDescription());
            textDescription.setOnClickListener(view -> onClickFullDescription());
        }
        if(post.getType().equals(Post.POST_TYPE_TEXT)){
            linearLayoutPostText.setVisibility(View.VISIBLE);
            postText.setText(post.getPost());
            linearLayoutPostVideo.setVisibility(View.GONE);
            postImage.setVisibility(View.GONE);
        }else if(post.getType().equals(Post.POST_TYPE_IMAGE)){
            linearLayoutPostText.setVisibility(View.GONE);
            linearLayoutPostVideo.setVisibility(View.GONE);
            postImage.setVisibility(View.VISIBLE);
            if(post.getPost() != null) {
                Glide.with(getApplicationContext())
                        .load(post.getPost())
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(postImage);
            }
        }else if (post.getType().equals(Post.POST_TYPE_VIDEO)){
            linearLayoutPostText.setVisibility(View.GONE);
            postImage.setVisibility(View.GONE);
            linearLayoutPostVideo.setVisibility(View.VISIBLE);
            simpleExoPlayer = new SimpleExoPlayer.Builder(getApplicationContext()).build();
            postVideo.setPlayer(simpleExoPlayer);

            MediaItem mediaItem = MediaItem.fromUri(post.getPost());
            simpleExoPlayer.setMediaItem(mediaItem);
            simpleExoPlayer.prepare();
        }
        setLike();

        if(post.isOptionComments()) setComment();
        else linearLayoutComment.setVisibility(View.GONE);

        if(post.isOptionShare()) linearLayoutShare.setVisibility(View.VISIBLE);
        else linearLayoutShare.setVisibility(View.GONE);

        postUserPhoto.setOnClickListener(view -> {
            Intent intent = new Intent(PostActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", post.getUser());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        buttonHide.setOnClickListener(view -> {
            coordinatorLayoutDescription.setVisibility(View.GONE);
            constraintLayoutComment.setVisibility(View.GONE);
            linearLayoutHide.setVisibility(View.GONE);
            textDescription.setVisibility(View.VISIBLE);
        });
        buttonSend.setOnClickListener(view -> {
            Intent intent = new Intent(this, ForwardMessageActivity.class);
            intent.putExtra("chatId", "");
            intent.putExtra("message", post.getId());
            intent.putExtra("messageType", type);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            this.startActivity(intent);
        });

        buttonOptions.setOnClickListener(view -> {
            if(buttonOptions.isActivated()){
                buttonOptions.setActivated(false);
                coordinatorLayoutSettings.setVisibility(View.GONE);
                Check.enableButtonPagePost(buttonLike, buttonComment, buttonShare, buttonSend, textDescription, true);
            }else {
                coordinatorLayoutDescription.setVisibility(View.GONE);
                constraintLayoutComment.setVisibility(View.GONE);
                linearLayoutHide.setVisibility(View.GONE);
                textDescription.setVisibility(View.VISIBLE);
                coordinatorLayoutSettings.setVisibility(View.VISIBLE);
                buttonOptions.setActivated(true);
                Check.enableButtonPagePost(buttonLike, buttonComment, buttonShare, buttonSend, textDescription, false);
            }
        });
        coordinatorLayoutSettings.setOnClickListener(view -> {
            coordinatorLayoutSettings.setVisibility(View.GONE);
            buttonOptions.setActivated(false);
            Check.enableButtonPagePost(buttonLike, buttonComment, buttonShare, buttonSend, textDescription, true);
        });
    }
    private void setLike(){
        DatabaseReference refLikes = FirebaseDatabase.getInstance().getReference("likes");
        refLikes.child(post.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> likes = new ArrayList<>();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    likes.add(dataSnapshot.getValue(String.class));
                }
                textLike.setText(String.valueOf(likes.size()));
                if(likes.contains(firebaseUser.getUid())){
                    buttonLike.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_vector_like_primary));
                    buttonLike.setActivated(true);
                    textLike.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_color));
                }else {
                    buttonLike.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_vector_like));
                    buttonLike.setActivated(false);
                    textLike.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_color));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        buttonLike.setOnClickListener(view -> {
            if(buttonLike.isActivated()){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(postId).child(firebaseUser.getUid()).removeValue();
            }else {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(postId).child(firebaseUser.getUid()).setValue(firebaseUser.getUid());

                if(!firebaseUser.getUid().equals(post.getUser()))
                    Notification.sendNotificationPost(postId, "Like post", token, type);

                Algorithm.setAlgorithm(postId, LIKE, "liked", post.getHashtags());
            }
        });
    }
    private void setComment(){
        linearLayoutComment.setVisibility(View.VISIBLE);
        recyclerViewComment.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        DatabaseReference refComment = FirebaseDatabase.getInstance().getReference("comments");
        refComment.child(post.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();
                boolean b = false;
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    comments.add(comment);
                    if(comment.getUser().equals(firebaseUser.getUid())) b = true;
                }
                textComment.setText(String.valueOf(comments.size()));
                textViewNumberComments.setText(String.valueOf(comments.size()));

                if(b){
                    textComment.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_color));
                    buttonComment.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_vector_comment_active));
                    buttonComment.setActivated(true);
                }else {
                    textComment.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_color));
                    buttonComment.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_vector_comment));
                    buttonComment.setActivated(false);
                }
                CommentAdapter commentAdapter;
                if(type.equals("post_home")) commentAdapter = new CommentAdapter(getApplicationContext(), comments, post.getId(), null);
                else commentAdapter = new CommentAdapter(getApplicationContext(), comments, post.getId(), type);

                recyclerViewComment.setAdapter(commentAdapter);
                recyclerViewComment.addOnItemTouchListener(mScrollTouchListener);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        buttonComment.setOnClickListener(view -> {
            textDescription.setVisibility(View.GONE);
            coordinatorLayoutDescription.setVisibility(View.GONE);
            linearLayoutHide.setVisibility(View.VISIBLE);
            constraintLayoutComment.setVisibility(View.VISIBLE);
        });
        editTextComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!editTextComment.getText().toString().trim().isEmpty()) addComment.setVisibility(View.VISIBLE);
                else addComment.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        addComment.setOnClickListener(view -> {
            postComment(editTextComment.getText().toString().trim());
            editTextComment.setText("");
        });
    }
    private void postComment(String textComment){
        if(!Check.networkConnect(getApplicationContext())){
            Toast.makeText(this, "Greška: nema mreže!", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference refComments = FirebaseDatabase.getInstance().getReference("comments");
        final String commentId = refComments.child(postId).push().getKey();
        Comment comment = new Comment(commentId, firebaseUser.getUid(), textComment);
        refComments.child(post.getId()).child(commentId).setValue(comment).addOnCompleteListener(task->{
            if(!firebaseUser.getUid().equals(post.getUser())){
                Notification.sendNotificationPost(postId, "Novi komentar!", token, type);
            }
        });
        Algorithm.setAlgorithm(postId, COMMENT, "commented", post.getHashtags());
    }
    private void onClickFullDescription(){
        if(!post.getDescription().isEmpty()){
            linearLayoutHide.setVisibility(View.VISIBLE);
            coordinatorLayoutDescription.setVisibility(View.VISIBLE);
            fullDescription.setText(post.getDescription());
            textDescription.setVisibility(View.GONE);
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
           }else buttonOptions.setVisibility(View.GONE);
           token = u.getToken();
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
    public void stopPlayer() {
        if(simpleExoPlayer != null) simpleExoPlayer.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

    RecyclerView.OnItemTouchListener mScrollTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            int action = e.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    rv.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) { }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
    };
}