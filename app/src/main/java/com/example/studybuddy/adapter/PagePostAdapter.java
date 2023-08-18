package com.example.studybuddy.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.compose.ui.layout.HorizontalAlignmentLine;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.model.Like;
import com.example.studybuddy.model.Page;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PagePostAdapter extends RecyclerView.Adapter<PagePostAdapter.ViewHolder> {

    private Context context;
    private Activity activity;
    private Page page;
    private List<Post> posts;
    private Map<String, ViewHolder> mHolders;
    private FirebaseUser firebaseUser;
    private SimpleExoPlayer player;

    public PagePostAdapter(Context context, Activity activity, Page page, List<Post> posts) {
        this.context = context;
        this.activity = activity;
        this.page = page;
        this.posts = posts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mHolders = new HashMap<>();
    }

    @NonNull
    @Override
    public PagePostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.page_post_item, parent, false);
        return new PagePostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagePostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        mHolders.put(post.getId(), holder);

        String admin = page.getMembers().get(firebaseUser.getUid());

        if(post.getUser().equals(firebaseUser.getUid())) {
            holder.linearLayoutPostCurrentUser.setVisibility(View.VISIBLE);
            holder.linearLayoutDeletePost.setVisibility(View.VISIBLE);
        } else if (Objects.equals(admin,"admin")) {
            holder.linearLayoutPostCurrentUser.setVisibility(View.GONE);
            holder.linearLayoutDeletePost.setVisibility(View.VISIBLE);
        } else {
            holder.linearLayoutPostCurrentUser.setVisibility(View.GONE);
            holder.linearLayoutDeletePost.setVisibility(View.GONE);
        }
        holder.linearLayoutDeletePost.setOnClickListener(view -> onClickDeletePost(post.getId()));
        holder.linearLayoutNoComments.setOnClickListener(view -> {holder.linearLayoutComment.setVisibility(View.GONE);});

        setLike(holder, post.getId(), post.getUser(), post.getHashtags());
        holder.linearLayoutHideLike.setOnClickListener(view->{
            HashMap<String, Object> map = new HashMap<>();
            map.put("visibility", false);
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("likes");
            ref.child(post.getId()).updateChildren(map);
        });
        holder.linearLayoutLike.setOnClickListener(view -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("visibility", true);
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("likes");
            ref.child(post.getId()).updateChildren(map);
        });

        setUserInfo(holder, post.getUser());

        holder.linearLayoutFollow.setOnClickListener(view -> {
            DatabaseReference fRef = FirebaseDatabase.getInstance().getReference("followers");
            fRef.child(firebaseUser.getUid()).child(post.getUser()).setValue("following");
        });
        holder.linearLayoutNoFollow.setOnClickListener(view -> {
            DatabaseReference fRef = FirebaseDatabase.getInstance().getReference("followers");
            fRef.child(firebaseUser.getUid()).child(post.getUser()).removeValue();
        });

        if(post.getLocation() != null) holder.location.setText(post.getLocation());
        if(post.getDescription() != null) {
            holder.textViewDescription.setText(post.getDescription());
            holder.textViewDescription.setOnClickListener(view-> onClickDescription(holder, post));
        }

        holder.textViewDescription.setVisibility(View.VISIBLE);
        holder.linearLayoutHide.setVisibility(View.GONE);
        holder.coordinatorLayoutComments.setVisibility(View.GONE);
        holder.coordinatorLayoutFullDescription.setVisibility(View.GONE);
        holder.coordinatorLayoutSettingPost.setVisibility(View.GONE);

        holder.coordinatorLayoutSettingPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.coordinatorLayoutSettingPost.setVisibility(View.GONE);
            }
        });

        String isAdmin = page.getMembers().get(post.getUser());

        if(Objects.equals(isAdmin, "admin")) {holder.imageViewAdmin.setVisibility(View.VISIBLE);
        } else { holder.imageViewAdmin.setVisibility(View.GONE); }

        if(post.isOptionComments()) setComment(holder, post.getId(), post.getUser(), post.getHashtags());
        else holder.linearLayoutComment.setVisibility(View.GONE);

        if(post.isOptionShare()) holder.linearLayoutShare.setVisibility(View.VISIBLE);
        else holder.linearLayoutShare.setVisibility(View.GONE);

        if(post.getType().equals(Post.POST_TYPE_TEXT)){
            holder.imagePost.setVisibility(View.GONE);
            holder.linearLayoutVideoPost.setVisibility(View.GONE);
            holder.linearLayoutTextPost.setVisibility(View.VISIBLE);
            holder.textPost.setText(post.getPost());
        }else if(post.getType().equals(Post.POST_TYPE_IMAGE)){
            holder.linearLayoutTextPost.setVisibility(View.GONE);
            holder.linearLayoutVideoPost.setVisibility(View.GONE);
            holder.imagePost.setVisibility(View.VISIBLE);

            if(post.getPost() != null) {
                Glide.with(context)
                        .load(post.getPost())
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(holder.imagePost);
            }
        }else if(post.getType().equals(Post.POST_TYPE_VIDEO)){
            holder.linearLayoutTextPost.setVisibility(View.GONE);
            holder.linearLayoutVideoPost.setVisibility(View.VISIBLE);
            holder.imagePost.setVisibility(View.GONE);

            if(post.getPost() != null) {
                holder.simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
                holder.videoPost.setPlayer(holder.simpleExoPlayer);

                MediaItem mediaItem = MediaItem.fromUri(post.getPost());
                holder.simpleExoPlayer.setMediaItem(mediaItem);
                holder.simpleExoPlayer.prepare();

            }
        }
        holder.userPhoto.setOnClickListener(view->onClickPhoto(post));
        holder.options.setOnClickListener(view -> {
            holder.coordinatorLayoutSettingPost.setVisibility(View.VISIBLE);
        });
    }
    private void setComment(ViewHolder holder, String postId, String postUser, List<String> hashtags){
        holder.linearLayoutComment.setVisibility(View.VISIBLE);
        holder.recyclerViewComments.setLayoutManager(new LinearLayoutManager(context));

        DatabaseReference refComments = FirebaseDatabase.getInstance().getReference("comments");


        holder.comment.setOnClickListener(view -> {
            holder.coordinatorLayoutComments.setVisibility(View.VISIBLE);
            holder.buttonHide.setVisibility(View.VISIBLE);
            holder.coordinatorLayoutFullDescription.setVisibility(View.GONE);
            holder.textViewDescription.setVisibility(View.GONE);
        });
    }
    private void setLike(ViewHolder holder, String postId, String userId, List<String> hashtags){
        DatabaseReference refLike = FirebaseDatabase.getInstance().getReference("likes");
        refLike.child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Like like = snapshot.getValue(Like.class);
                        if(like == null){
                            Map<String, String> users = new HashMap<>();
                            Like like1 = new Like(postId, users, true);
                            refLike.child(postId).setValue(like1).addOnSuccessListener(aVoid->{
                                holder.textViewLike.setText(String.valueOf(0));
                                holder.textViewLike.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                                holder.like.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like));
                                holder.like.setActivated(false);
                            }).addOnFailureListener(e->{
                                Toast.makeText(context, "Greška: "+ e.getMessage(),Toast.LENGTH_LONG).show();
                            });
                        }else {
                            holder.textViewLike.setText(String.valueOf(like.getUsers().size()));
                            if(like.getUsers().containsValue(firebaseUser.getUid())){
                                holder.textViewLike.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
                                holder.like.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like_primary));
                                holder.like.setActivated(true);
                            }else {
                                holder.textViewLike.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                                holder.like.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like));
                                holder.like.setActivated(false);
                            }
                            if(like.isVisibility()){
                                holder.linearLayoutHideLike.setVisibility(View.VISIBLE);
                                holder.textViewLike.setVisibility(View.VISIBLE);
                                holder.linearLayoutLike.setVisibility(View.GONE);
                            }else {
                                holder.linearLayoutHideLike.setVisibility(View.GONE);
                                holder.textViewLike.setVisibility(View.GONE);
                                holder.linearLayoutLike.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        holder.like.setOnClickListener(view ->{
            if(holder.like.isActivated()){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(postId).child("users").child(firebaseUser.getUid()).removeValue();
            } else {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(postId).child("users").child(firebaseUser.getUid()).setValue(firebaseUser.getUid());
            }
        });
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if(layoutManager instanceof LinearLayoutManager && getItemCount() > 0) {
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

                    if(itemPosition > -1) checkItem(itemPosition);
                }
            });
        }
    }
    private void checkItem(int position) {
        Post post = posts.get(position);

        if(post.getType().equals(Post.POST_TYPE_VIDEO)) {
            ViewHolder viewHolder = mHolders.get(post.getId());

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
    public void stopPlayer() {
        if(player != null) player.pause();
    }
    private void onClickPhoto(Post post){
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("userId", post.getUser());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }
    private void onClickDescription(ViewHolder holder, Post post){
        if(!holder.textViewDescription.getText().toString().isEmpty()) {
            holder.coordinatorLayoutFullDescription.setVisibility(View.VISIBLE);
            holder.textViewDescription.setVisibility(View.GONE);
            holder.linearLayoutHide.setVisibility(View.VISIBLE);
            holder.textViewFullDescription.setText(post.getDescription());

            holder.buttonHide.setOnClickListener(view -> {
                holder.coordinatorLayoutFullDescription.setVisibility(View.GONE);
                holder.linearLayoutHide.setVisibility(View.GONE);
                holder.textViewDescription.setVisibility(View.VISIBLE);
            });
        }
    }
    private void onClickDeletePost(String postId){
        if(!Check.networkConnect(context)){
            Toast.makeText(context, "Greška: nema mreže!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pages_posts");
        ref.child(page.getPageId()).child(postId).removeValue()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        Toast.makeText(context, "Uspešno obrisana objava.",Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setUserInfo(ViewHolder holder, String userId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                holder.userName.setText(user.getName());

                if(user.getPhoto().equals("deafult")) holder.userPhoto.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                else Glide.with(context).load(user.getPhoto()).into(holder.userPhoto);
                if(!firebaseUser.getUid().equals(user.getUserId())) {
                    setFollowUnfollow(holder, user.getUserId());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
    public int getItemCount() { return posts.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userPhoto;
        public TextView userName;
        public ImageView imageViewAdmin;
        public TextView location;
        public ImageButton options;
        public CoordinatorLayout coordinatorLayoutSettingPost;
        public LinearLayout linearLayoutPostCurrentUser;
        public LinearLayout linearLayoutFollow;
        public LinearLayout linearLayoutNoFollow;
        public LinearLayout linearLayoutHideLike;
        public LinearLayout linearLayoutLike;
        public LinearLayout linearLayoutNoComments;
        public LinearLayout linearLayoutDeletePost;
        public ImageView imagePost;
        public LinearLayout linearLayoutTextPost;
        public TextView textPost;
        public LinearLayout linearLayoutVideoPost;
        public PlayerView videoPost;
        public Button like;
        public TextView textViewLike;
        public LinearLayout linearLayoutComment;
        public ConstraintLayout coordinatorLayoutComments;
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
        public Button save;
        public SimpleExoPlayer simpleExoPlayer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userPhoto = itemView.findViewById(R.id.page_post_user_photo);
            userName = itemView.findViewById(R.id.page_post_user_name);
            location = itemView.findViewById(R.id.page_post_user_location);
            imageViewAdmin = (ImageView) itemView.findViewById(R.id.page_post_admin);
            options = itemView.findViewById(R.id.page_post_options);

            coordinatorLayoutSettingPost = itemView.findViewById(R.id.coordinatorLayout_page_post_settings);
            linearLayoutPostCurrentUser = itemView.findViewById(R.id.linearLayoutPostCurrentUser);
            linearLayoutHideLike = itemView.findViewById(R.id.linearLayoutHideNumberLike);
            linearLayoutLike = itemView.findViewById(R.id.linearLayoutNumberLike);
            linearLayoutNoComments = itemView.findViewById(R.id.linearLayoutHideComment);
            linearLayoutFollow = itemView.findViewById(R.id.linearLayoutFollow);
            linearLayoutNoFollow = itemView.findViewById(R.id.linearLayoutNoFollow);
            linearLayoutDeletePost = itemView.findViewById(R.id.linearLayoutDeletePost);

            imagePost = itemView.findViewById(R.id.page_post_image);
            linearLayoutTextPost = itemView.findViewById(R.id.linearLayoutPagePostText);
            textPost = itemView.findViewById(R.id.page_post_text);
            linearLayoutVideoPost = itemView.findViewById(R.id.linearLayoutPagePostVideo);
            videoPost = itemView.findViewById(R.id.page_post_video);

            like = itemView.findViewById(R.id.buttonLikePagePost);
            textViewLike = itemView.findViewById(R.id.textViewLikePagePost);
            linearLayoutComment = itemView.findViewById(R.id.page_post_comment);

            coordinatorLayoutComments = itemView.findViewById(R.id.consPagePostComments);
            commentsNumber = itemView.findViewById(R.id.commentsNumberPagePost);
            recyclerViewComments = itemView.findViewById(R.id.recCommentsPagePost);
            editTextComment = itemView.findViewById(R.id.editTextCommentsPagePost);
            addComment = itemView.findViewById(R.id.textViewCommentsPagePost);
            comment = itemView.findViewById(R.id.buttonCommentPagePost);
            textViewComment = itemView.findViewById(R.id.textViewCommentPagePost);

            textViewDescription = itemView.findViewById(R.id.page_post_description);
            coordinatorLayoutFullDescription = itemView.findViewById(R.id.coordinatorLayout_page_post_description);
            textViewFullDescription = itemView.findViewById(R.id.page_post_full_description);

            buttonHide = itemView.findViewById(R.id.buttonHidePagePost);
            linearLayoutHide = itemView.findViewById(R.id.linearLayoutPagePostHide);

            linearLayoutShare = itemView.findViewById(R.id.lin_share_page_post);
            share = itemView.findViewById(R.id.buttonSharePagePost);
            save = itemView.findViewById(R.id.buttonSendPagePost);
        }
    }
}
