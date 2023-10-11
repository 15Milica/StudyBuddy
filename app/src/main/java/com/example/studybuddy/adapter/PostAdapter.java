package com.example.studybuddy.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.algorithm.Algorithm;
import com.example.studybuddy.chat.ForwardMessageActivity;
import com.example.studybuddy.model.Comment;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
import com.example.studybuddy.notification.Notification;
import com.example.studybuddy.ui.profile.UserProfileActivity;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.common.api.Api;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context;
    private Activity activity;
    private List<Post> posts;
    private Map<String, ViewHolder> mHolders;
    private SimpleExoPlayer player;
    private FirebaseUser firebaseUser;
    private Map<String, String> tokens;
    private static final String LIKE = "likes";
    private static final String COMMENT = "comments";
    private static final String SHARE = "shares";
    public PostAdapter(Context context, Activity activity, List<Post> posts) {
        this.context = context;
        this.activity = activity;
        this.posts = posts;
        mHolders = new HashMap<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tokens = new HashMap<>();
    }
    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        mHolders.put(post.getId(), holder);
        if(post.getLocation() != null) holder.location.setText(post.getLocation());
        setUserDetails(post.getUser(), holder);

        Check.settingsPagePost(holder.textViewDescription, holder.linearLayoutHide, holder.coordinatorLayoutFullDescription, holder.coordinatorLayoutSettingPost, holder.constraintLayoutComments, holder.options);
        Check.enableButtonPagePost(holder.like, holder.comment, holder.share, holder.textViewDescription, true);

        if(post.getType().equals(Post.POST_TYPE_TEXT)){
            holder.linearLayoutTextPost.setVisibility(View.VISIBLE);
            holder.textPost.setText(post.getPost());
            holder.imagePost.setVisibility(View.GONE);
            holder.linearLayoutVideoPost.setVisibility(View.GONE);
        }else if(post.getType().equals(Post.POST_TYPE_IMAGE)){
            holder.linearLayoutVideoPost.setVisibility(View.GONE);
            holder.linearLayoutTextPost.setVisibility(View.GONE);
            holder.imagePost.setVisibility(View.VISIBLE);
            if(post.getPost() != null)
                Glide.with(context)
                        .load(post.getPost())
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(holder.imagePost);
        }else if(post.getType().equals(Post.POST_TYPE_VIDEO)){
            holder.imagePost.setVisibility(View.GONE);
            holder.linearLayoutTextPost.setVisibility(View.GONE);
            holder.linearLayoutVideoPost.setVisibility(View.VISIBLE);
            if(post.getPost() != null){
                holder.simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
                holder.videoPost.setPlayer(holder.simpleExoPlayer);

                MediaItem mediaItem = MediaItem.fromUri(post.getPost());
                holder.simpleExoPlayer.setMediaItem(mediaItem);
                holder.simpleExoPlayer.prepare();
            }
        }
        if(post.getDescription() != null) holder.textViewDescription.setText(post.getDescription());
        holder.textViewDescription.setOnClickListener(view -> {
            if(!post.getDescription().isEmpty()) {
                holder.coordinatorLayoutFullDescription.setVisibility(View.VISIBLE);
                holder.textViewFullDescription.setText(post.getDescription());
                holder.linearLayoutHide.setVisibility(View.VISIBLE);
                holder.textViewDescription.setVisibility(View.GONE);
                holder.options.setVisibility(View.GONE);
                holder.constraintLayoutComments.setVisibility(View.GONE);
            }
        });

        holder.buttonHide.setOnClickListener(view -> {
            holder.linearLayoutHide.setVisibility(View.GONE);
            holder.coordinatorLayoutFullDescription.setVisibility(View.GONE);
            holder.constraintLayoutComments.setVisibility(View.GONE);
            holder.textViewDescription.setVisibility(View.VISIBLE);
            holder.options.setVisibility(View.VISIBLE);
        });
        if(post.isOptionShare()) holder.linearLayoutShare.setVisibility(View.VISIBLE);
        else holder.linearLayoutShare.setVisibility(View.GONE);

        if(post.isOptionComments()) setComment(holder, post.getId(), post.getUser(), post.getHashtags());
        else holder.linearLayoutComment.setVisibility(View.GONE);

        setLike(holder, post.getId(), post.getUser(), post.getHashtags());

        holder.userPhoto.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", post.getUser());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        });

        holder.textViewFullDescription.setOnClickListener(view -> {
            if(post.getDescription().startsWith("https://") || post.getDescription().startsWith("http://")) {
                Uri uri = Uri.parse(post.getDescription());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });
        holder.share.setOnClickListener(view -> {
            Intent intent = new Intent(context, ForwardMessageActivity.class);
            intent.putExtra("chatId", "");
            intent.putExtra("message", post.getId());
            intent.putExtra("messageType", "post_home");
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            Algorithm.setAlgorithm(post.getId(), SHARE, "shared", post.getHashtags());
        });
        holder.options.setOnClickListener(view -> {
            if(holder.options.isActivated()){
                holder.coordinatorLayoutSettingPost.setVisibility(View.GONE);
                holder.options.setActivated(false);
                Check.enableButtonPagePost(holder.like, holder.comment, holder.share, holder.textViewDescription, true);
            }else {
                holder.coordinatorLayoutFullDescription.setVisibility(View.GONE);
                holder.constraintLayoutComments.setVisibility(View.GONE);
                holder.coordinatorLayoutSettingPost.setVisibility(View.VISIBLE);
                holder.options.setActivated(true);
                Check.enableButtonPagePost(holder.like, holder.comment, holder.share, holder.textViewDescription, false);
            }
        });
        holder.linearLayoutFollow.setOnClickListener(view -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("followers");
            ref.child(firebaseUser.getUid()).child(post.getUser()).setValue("following");
        });
        holder.linearLayoutNoFollow.setOnClickListener(view -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("followers");
            ref.child(firebaseUser.getUid()).child(post.getUser()).removeValue();
        });
    }
    private void setLike(ViewHolder holder, String postId, String userId, List<String> hashtags){
        DatabaseReference refLikes = FirebaseDatabase.getInstance().getReference("likes");
        refLikes.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> likes = new ArrayList<>();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    likes.add(dataSnapshot.getValue(String.class));
                }
                holder.textViewLike.setText(String.valueOf(likes.size()));

                if(likes.contains(firebaseUser.getUid())){
                    holder.textViewLike.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
                    holder.like.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like_primary));
                    holder.like.setActivated(true);
                }else {
                    holder.textViewLike.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                    holder.like.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like));
                    holder.like.setActivated(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.like.setOnClickListener(view -> {
            if(holder.like.isActivated()){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(postId).child(firebaseUser.getUid()).removeValue();
            }else {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(postId).child(firebaseUser.getUid()).setValue(firebaseUser.getUid());

                if(!firebaseUser.getUid().equals(userId)){
                    if(tokens.containsKey(userId))
                        Notification.sendNotificationPost(postId, "Like post", tokens.get(userId), "post_home");
                }
                Algorithm.setAlgorithm(postId, LIKE, "liked", hashtags);
            }
        });
    }
    private void setComment(ViewHolder holder, String postId, String userId, List<String> hashtags){
        holder.linearLayoutComment.setVisibility(View.VISIBLE);
        holder.recyclerViewComments.setLayoutManager(new LinearLayoutManager(context));

        DatabaseReference refComment = FirebaseDatabase.getInstance().getReference("comments");
        refComment.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();
                boolean b = false;
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    comments.add(comment);
                    if(comment.getUser().equals(firebaseUser.getUid())) b=true;
                }
                holder.textViewComment.setText(String.valueOf(comments.size()));
                holder.commentsNumber.setText(String.valueOf(comments.size()));
                if(b){
                    holder.comment.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_comment_active));
                    holder.comment.setActivated(true);
                    holder.textViewComment.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
                }else {
                    holder.comment.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_comment));
                    holder.comment.setActivated(false);
                    holder.textViewComment.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                }
                CommentAdapter commentAdapter = new CommentAdapter(context, comments, postId, null);
                holder.recyclerViewComments.setAdapter(commentAdapter);
                holder.recyclerViewComments.addOnItemTouchListener(mScrollTouchListener);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.comment.setOnClickListener(view -> {
            holder.textViewDescription.setVisibility(View.GONE);
            holder.coordinatorLayoutFullDescription.setVisibility(View.GONE);
            holder.options.setVisibility(View.GONE);
            holder.linearLayoutHide.setVisibility(View.VISIBLE);
            holder.constraintLayoutComments.setVisibility(View.VISIBLE);
        });
        holder.editTextComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!holder.editTextComment.getText().toString().trim().isEmpty()) holder.addComment.setVisibility(View.VISIBLE);
                else holder.addComment.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        holder.addComment.setOnClickListener(view -> {
            postComment(postId, holder.editTextComment.getText().toString().trim(), userId, hashtags);
            holder.editTextComment.setText("");
        });
    }
    private void postComment(String postId, String text, String userId, List<String> hashtags){
        if(!Check.networkConnect(context)){
            Toast.makeText(context, "Greška: nema mreže!", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comments");
        final String commentId = ref.child(postId).push().getKey();
        Comment comment = new Comment(commentId, firebaseUser.getUid(), text);
        ref.child(postId).child(commentId).setValue(comment).addOnCompleteListener(view->{
            if(!firebaseUser.getUid().equals(userId)){
                if(tokens.containsKey(userId))
                    Notification.sendNotificationPost(postId, "Novi komentar!", tokens.get(userId), "post_home");
            }
        });
        Algorithm.setAlgorithm(postId, COMMENT, "commented", hashtags);
    }
    private void setUserDetails(String userId, ViewHolder holder){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(userId).get().addOnSuccessListener(dataSnapshot -> {
           User user = dataSnapshot.getValue(User.class);
           holder.userName.setText(user.getName());
           if(user.getPhoto().equals("default")) holder.userPhoto.setImageResource(R.drawable.ic_create_profile_vectors_photo);
           else Glide.with(context).load(user.getPhoto()).into(holder.userPhoto);
           tokens.put(user.getUserId(), user.getToken());
           setFollowUnfollow(holder, user.getUserId());
        });
    }
    private void setFollowUnfollow(ViewHolder holder, String userId){
        DatabaseReference refFollow = FirebaseDatabase.getInstance().getReference("followers");
        refFollow.child(firebaseUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s = snapshot.getValue(String.class);
                if(s != null) {
                    holder.linearLayoutNoFollow.setVisibility(View.VISIBLE);
                    holder.linearLayoutFollow.setVisibility(View.GONE);
                } else {
                    holder.linearLayoutFollow.setVisibility(View.VISIBLE);
                    holder.linearLayoutNoFollow.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof LinearLayoutManager && getItemCount() > 0) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int itemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                    if (itemPosition > -1) checkItem(itemPosition);
                }
            });
        }
    }
    private void checkItem(int position) {
        Post post = posts.get(position);

        if (post.getType().equals(Post.POST_TYPE_VIDEO)) {
            ViewHolder viewHolder = mHolders.get(post.getId());

            if (viewHolder.simpleExoPlayer == player) {
                if (player != null && !player.isPlaying()) player.play();
            } else {
                stopPlayer();
                viewHolder.simpleExoPlayer.play();
                player = viewHolder.simpleExoPlayer;
            }
        } else {
            stopPlayer();
        }
    }
    public void stopPlayer() { if(player != null) player.pause(); }
    @Override
    public int getItemCount() { return posts.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userPhoto;
        public TextView userName;
        public TextView location;
        public ImageButton options;
        public CoordinatorLayout coordinatorLayoutSettingPost;
        public LinearLayout linearLayoutPostCurrentUser;
        public LinearLayout linearLayoutFollow;
        public LinearLayout linearLayoutNoFollow;
        public LinearLayout linearLayoutHideLike;
        public LinearLayout linearLayoutLike;
        public LinearLayout linearLayoutNoComments;
        public LinearLayout linearLayoutVisibilityComment;
        public LinearLayout linearLayoutDeletePost;
        public ImageView imagePost;
        public LinearLayout linearLayoutTextPost;
        public TextView textPost;
        public LinearLayout linearLayoutVideoPost;
        public PlayerView videoPost;
        public Button like;
        public TextView textViewLike;
        public LinearLayout linearLayoutComment;
        public ConstraintLayout constraintLayoutComments;
        public TextView commentsNumber;
        public RecyclerView recyclerViewComments;
        public EditText editTextComment;
        public TextView addComment;
        public Button comment;
        public TextView textViewComment;
        public TextView textViewDescription;
        public CoordinatorLayout coordinatorLayoutFullDescription;
        public TextView textViewFullDescription;
        public LinearLayout linearLayoutHide;
        public Button buttonHide;
        public LinearLayout linearLayoutShare;
        public Button share;
        public SimpleExoPlayer simpleExoPlayer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userPhoto = itemView.findViewById(R.id.post_user_photo);
            userName = itemView.findViewById(R.id.post_user_name);
            location = itemView.findViewById(R.id.post_user_location);
            options = itemView.findViewById(R.id.post_options);

            coordinatorLayoutSettingPost = itemView.findViewById(R.id.coordinatorLayout_post_settings);
            linearLayoutPostCurrentUser = itemView.findViewById(R.id.linearLayoutCurrentUserPost);
            linearLayoutHideLike = itemView.findViewById(R.id.linearLayoutHideNumberLikePost);
            linearLayoutLike = itemView.findViewById(R.id.linearLayoutNumberLikePost);
            linearLayoutNoComments = itemView.findViewById(R.id.linearLayoutHideCommentPost);
            linearLayoutVisibilityComment = itemView.findViewById(R.id.linearLayoutVisibilityCommentPost);
            linearLayoutFollow = itemView.findViewById(R.id.linearLayoutFollowPostUser);
            linearLayoutNoFollow = itemView.findViewById(R.id.linearLayoutNoFollowPostUser);
            linearLayoutDeletePost = itemView.findViewById(R.id.linearLayoutPostDelete);

            imagePost = itemView.findViewById(R.id.post_image);
            linearLayoutTextPost = itemView.findViewById(R.id.linearLayoutPostText);
            textPost = itemView.findViewById(R.id.post_text);
            linearLayoutVideoPost = itemView.findViewById(R.id.linearLayoutPostVideo);
            videoPost = itemView.findViewById(R.id.post_video);

            like = itemView.findViewById(R.id.buttonLikePost);
            textViewLike = itemView.findViewById(R.id.textViewLikePost);
            linearLayoutComment = itemView.findViewById(R.id.post_comment);

            constraintLayoutComments = itemView.findViewById(R.id.consPostComments);
            commentsNumber = itemView.findViewById(R.id.commentsNumberPost);
            recyclerViewComments = itemView.findViewById(R.id.recCommentsPost);
            editTextComment = itemView.findViewById(R.id.editTextCommentsPost);
            addComment = itemView.findViewById(R.id.textViewCommentsPost);
            comment = itemView.findViewById(R.id.buttonCommentPost);
            textViewComment = itemView.findViewById(R.id.textViewCommentPost);

            textViewDescription = itemView.findViewById(R.id.post_description);
            coordinatorLayoutFullDescription = itemView.findViewById(R.id.coordinatorLayout_post_description);
            textViewFullDescription = itemView.findViewById(R.id.post_full_description);

            buttonHide = itemView.findViewById(R.id.buttonHidePost);
            linearLayoutHide = itemView.findViewById(R.id.linearLayoutPostHide);

            linearLayoutShare = itemView.findViewById(R.id.lin_share_post);
            share = itemView.findViewById(R.id.buttonSharePost);
        }
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
