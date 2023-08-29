package com.example.studybuddy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.model.Post;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context;
    private Activity activity;
    private List<Post> posts;

    public PostAdapter(Context context, Activity activity, List<Post> posts) {
        this.context = context;
        this.activity = activity;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {

    }

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
        public Button save;
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
            save = itemView.findViewById(R.id.buttonSendPost);
        }
    }
}
