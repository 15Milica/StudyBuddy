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
import java.util.Map;

public class StorylineAdapter extends RecyclerView.Adapter<StorylineAdapter.ViewHolder> {
    private Context context;
    private Activity activity;
    private List<Post> posts;
    private int index;
    private SimpleExoPlayer player;
    private Map<String, ViewHolder> mHolders;
    private FirebaseUser firebaseUser;
    private Map<String, String> tokens;
    private static final String LIKE = "likes";
    private static final String COMMENT = "comments";
    private static final String SHARE = "shares";

    public StorylineAdapter(Context context, Activity activity, List<Post> posts, int index) {
        this.context = context;
        this.activity = activity;
        this.posts = posts;
        this.index = index;
        mHolders = new HashMap<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tokens = new HashMap<>();
    }

    @NonNull
    @Override
    public StorylineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.storyline_item, parent, false);
        return new StorylineAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StorylineAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        mHolders.put(post.getId(), holder);
        setUserToken(post.getUser());

        holder.constraintLayoutComments.setVisibility(View.GONE);
        holder.coordinatorLayoutDescription.setVisibility(View.GONE);
        holder.textViewDescription.setVisibility(View.VISIBLE);
        holder.linearLayoutHide.setVisibility(View.GONE);

        if(post.getType().equals(Post.POST_TYPE_IMAGE)){
            holder.linearLayoutPostVideo.setVisibility(View.GONE);
            holder.linearLayoutPostText.setVisibility(View.GONE);
            holder.postImage.setVisibility(View.VISIBLE);
            if(post.getPost() != null) {
                Glide.with(context)
                        .load(post.getPost())
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(holder.postImage);
            }
        }else if(post.getType().equals(Post.POST_TYPE_VIDEO)){
            holder.linearLayoutPostVideo.setVisibility(View.VISIBLE);
            holder.postImage.setVisibility(View.GONE);
            holder.linearLayoutPostText.setVisibility(View.GONE);
            if(post.getPost() != null) {
                holder.simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
                holder.postVideo.setPlayer(holder.simpleExoPlayer);

                MediaItem mediaItem = MediaItem.fromUri(post.getPost());
                holder.simpleExoPlayer.setMediaItem(mediaItem);
                holder.simpleExoPlayer.prepare();
            }
        }else if(post.getType().equals(Post.POST_TYPE_TEXT)) {
            holder.linearLayoutPostText.setVisibility(View.VISIBLE);
            holder.postText.setText(post.getPost());
            holder.postImage.setVisibility(View.GONE);
            holder.linearLayoutPostVideo.setVisibility(View.GONE);
        }
        if(post.getDescription() != null) holder.textViewDescription.setText(post.getDescription());
        holder.textViewDescription.setOnClickListener(view -> {
            if(!post.getDescription().isEmpty()){
                holder.textViewDescription.setVisibility(View.GONE);
                holder.coordinatorLayoutDescription.setVisibility(View.VISIBLE);
                holder.textViewFullDescription.setText(post.getDescription());
                holder.linearLayoutHide.setVisibility(View.VISIBLE);
            }
        });
        holder.textViewFullDescription.setOnClickListener(view -> {
            if(post.getDescription().startsWith("https://") || post.getDescription().startsWith("http://")) {
                Uri uri = Uri.parse(post.getDescription());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });

        holder.buttonHide.setOnClickListener(view -> {
            holder.coordinatorLayoutDescription.setVisibility(View.GONE);
            holder.linearLayoutHide.setVisibility(View.GONE);
            holder.textViewDescription.setVisibility(View.VISIBLE);
            holder.constraintLayoutComments.setVisibility(View.GONE);
        });
        setLike(holder, post.getId(), post.getUser(), post.getHashtags());

        if(post.isOptionShare()) holder.linearLayoutShare.setVisibility(View.VISIBLE);
        else holder.linearLayoutShare.setVisibility(View.INVISIBLE);

        if(post.isOptionComments()) setComment(holder, post.getId(), post.getUser(), post.getHashtags());
        else holder.linearLayoutComment.setVisibility(View.GONE);

        holder.buttonShare.setOnClickListener(view -> {
            Intent intent = new Intent(context, ForwardMessageActivity.class);
            intent.putExtra("chatId", "");
            intent.putExtra("message", post.getId());
            intent.putExtra("messageType", "post_home");
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });
    }
    private void setUserToken(String userId){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        userRef.child(userId).get().addOnSuccessListener(dataSnapshot -> {
            User user = dataSnapshot.getValue(User.class);
            if(user != null) tokens.put(user.getUserId(), user.getToken());
        });
    }

    private void setLike(ViewHolder holder, String postId, String userId, List<String> hashtags){
        DatabaseReference refLike = FirebaseDatabase.getInstance().getReference("likes");
        refLike.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> likes = new ArrayList<>();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String like = dataSnapshot.getValue(String.class);
                    likes.add(like);
                }
                holder.textViewLike.setText(String.valueOf(likes.size()));

                if(likes.contains(firebaseUser.getUid())){
                    holder.buttonLike.setActivated(true);
                    holder.buttonLike.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like_primary));
                    holder.textViewLike.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
                }else {
                    holder.buttonLike.setActivated(false);
                    holder.buttonLike.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like));
                    holder.textViewLike.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.buttonLike.setOnClickListener(view -> {
            if(holder.buttonLike.isActivated()){
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

        holder.recyclerViewComment.setLayoutManager(new LinearLayoutManager(context));

        DatabaseReference refComment = FirebaseDatabase.getInstance().getReference("comments");
        refComment.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();

                boolean b = false;
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    comments.add(comment);
                    if(comment.getUser().equals(firebaseUser.getUid())) b = true;
                }
                holder.textViewNumberComments.setText(String.valueOf(comments.size()));
                holder.textViewComment.setText(String.valueOf(comments.size()));

                if(b){
                    holder.textViewComment.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
                    holder.buttonComment.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_comment_active));
                    holder.buttonComment.setActivated(true);
                }else {
                    holder.textViewComment.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                    holder.buttonComment.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_comment));
                    holder.buttonComment.setActivated(false);
                }
                CommentAdapter commentAdapter = new CommentAdapter(context, comments, postId, null);
                holder.recyclerViewComment.setAdapter(commentAdapter);
                holder.recyclerViewComment.addOnItemTouchListener(mScrollTouchListener);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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

        holder.buttonComment.setOnClickListener(view -> {
            holder.constraintLayoutComments.setVisibility(View.VISIBLE);
            holder.linearLayoutHide.setVisibility(View.VISIBLE);
            holder.textViewDescription.setVisibility(View.GONE);
            holder.coordinatorLayoutDescription.setVisibility(View.GONE);
        });
    }
    private void postComment(String postId, String text, String userId, List<String> hashtags){
        if(!Check.networkConnect(context)){
            Toast.makeText(context, "Greška: nema mreže!", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference refComments = FirebaseDatabase.getInstance().getReference("comments");
        final String commentId = refComments.child(postId).push().getKey();
        Comment comment = new Comment(commentId, firebaseUser.getUid(), text);
        refComments.child(postId).child(commentId).setValue(comment).addOnCompleteListener(task->{
            if(!firebaseUser.getUid().equals(userId)){
                if(tokens.containsKey(userId))
                    Notification.sendNotificationPost(postId, "Novi komentar!", tokens.get(userId), "post_home");
            }
        });
        Algorithm.setAlgorithm(postId, COMMENT, "commented", hashtags);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if(layoutManager instanceof LinearLayoutManager && getItemCount() > 0) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            linearLayoutManager.scrollToPositionWithOffset(index, 150);

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int itemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                    if(itemPosition > -1) checkItem(itemPosition);
                }
            });
        }
    }
    private void checkItem(int position) {
        Post post = posts.get(position);

        if(post.getType().equals(Post.POST_TYPE_VIDEO)) {
            StorylineAdapter.ViewHolder viewHolder = mHolders.get(post.getId());

            if(viewHolder.simpleExoPlayer == player) {
                if(player != null && !player.isPlaying()) player.play();
            } else {
                stopPlayer();
                viewHolder.simpleExoPlayer.play();
                player = viewHolder.simpleExoPlayer;
            }
        } else {
            stopPlayer();
        }
    }
    public void stopPlayer(){if(player != null) player.pause();}

    @Override
    public int getItemCount() { return posts.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView postImage;
        public LinearLayout linearLayoutPostText;
        public TextView postText;
        public LinearLayout linearLayoutPostVideo;
        public PlayerView postVideo;
        public Button buttonLike;
        public TextView textViewLike;
        public LinearLayout linearLayoutComment;
        public Button buttonComment;
        public TextView textViewComment;
        public TextView textViewDescription;
        public LinearLayout linearLayoutHide;
        public Button buttonHide;
        public LinearLayout linearLayoutShare;
        public Button buttonShare;
        public CoordinatorLayout coordinatorLayoutDescription;
        public TextView textViewFullDescription;
        public ConstraintLayout constraintLayoutComments;
        public TextView textViewNumberComments;
        public RecyclerView recyclerViewComment;
        public EditText editTextComment;
        public TextView addComment;
        public SimpleExoPlayer simpleExoPlayer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postImage = itemView.findViewById(R.id.storyline_image);
            linearLayoutPostText = itemView.findViewById(R.id.linearLayoutStorylineText);
            postText = itemView.findViewById(R.id.storyline_text);
            linearLayoutPostVideo = itemView.findViewById(R.id.linearLayoutStorylineVideo);
            postVideo = itemView.findViewById(R.id.storyline_video);

            buttonLike = itemView.findViewById(R.id.buttonLikeStoryline);
            textViewLike = itemView.findViewById(R.id.textViewLikeStoryline);
            linearLayoutComment = itemView.findViewById(R.id.storyline_comment);
            buttonComment = itemView.findViewById(R.id.buttonCommentStoryline);
            textViewComment = itemView.findViewById(R.id.textViewCommentStoryline);
            textViewDescription = itemView.findViewById(R.id.storyline_description);
            linearLayoutHide = itemView.findViewById(R.id.linearLayoutStorylineHide);
            buttonHide = itemView.findViewById(R.id.buttonHideStoryline);
            linearLayoutShare = itemView.findViewById(R.id.lin_share_storyline);
            buttonShare = itemView.findViewById(R.id.buttonShareStoryline);

            coordinatorLayoutDescription = itemView.findViewById(R.id.coordinatorLayout_storyline_description);
            textViewFullDescription = itemView.findViewById(R.id.storyline_full_description);
            constraintLayoutComments = itemView.findViewById(R.id.consStorylineComments);
            textViewNumberComments = itemView.findViewById(R.id.commentsNumberStoryline);
            recyclerViewComment = itemView.findViewById(R.id.recCommentsStoryline);
            editTextComment = itemView.findViewById(R.id.editTextCommentsStoryline);
            addComment = itemView.findViewById(R.id.textViewCommentsStoryline);
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
