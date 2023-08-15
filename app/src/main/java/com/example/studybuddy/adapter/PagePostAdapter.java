package com.example.studybuddy.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.R;
import com.example.studybuddy.model.Page;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
import com.example.studybuddy.ui.profile.UserProfileActivity;
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

        setUserInfo(holder, post.getUser());
        if(post.getLocation() != null) holder.location.setText(post.getLocation());
        if(post.getDescription() != null) holder.textViewDescription.setText(post.getDescription());

        String isAdmin = page.getMembers().get(post.getUser());

        if(Objects.equals(isAdmin, "admin")) { holder.imageViewAdmin.setVisibility(View.VISIBLE); }
        else { holder.imageViewAdmin.setVisibility(View.GONE); }

        if(post.isOptionComments()) holder.linearLayoutComment.setVisibility(View.VISIBLE);
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
                    holder.options.setVisibility(View.VISIBLE);

                } else {
                    holder.options.setVisibility(View.GONE);
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
        public ImageView imagePost;
        public LinearLayout linearLayoutTextPost;
        public TextView textPost;
        public LinearLayout linearLayoutVideoPost;
        public PlayerView videoPost;
        public Button like;
        public TextView textViewLike;
        public LinearLayout linearLayoutComment;
        public Button comment;
        public TextView textViewComment;

        public TextView textViewDescription;
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

            imagePost = itemView.findViewById(R.id.page_post_image);
            linearLayoutTextPost = itemView.findViewById(R.id.linearLayoutPagePostText);
            textPost = itemView.findViewById(R.id.page_post_text);
            linearLayoutVideoPost = itemView.findViewById(R.id.linearLayoutPagePostVideo);
            videoPost = itemView.findViewById(R.id.page_post_video);

            like = itemView.findViewById(R.id.buttonLikePagePost);
            textViewLike = itemView.findViewById(R.id.textViewLikePagePost);
            linearLayoutComment = itemView.findViewById(R.id.page_post_comment);
            comment = itemView.findViewById(R.id.buttonCommentPagePost);
            textViewComment = itemView.findViewById(R.id.textViewCommentPagePost);

            textViewDescription = itemView.findViewById(R.id.page_post_description);
            linearLayoutShare = itemView.findViewById(R.id.lin_share_page_post);
            share = itemView.findViewById(R.id.buttonSharePagePost);
            save = itemView.findViewById(R.id.buttonSendPagePost);
        }
    }
}
