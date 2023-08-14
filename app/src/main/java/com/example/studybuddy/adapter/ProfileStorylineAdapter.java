package com.example.studybuddy.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.LabeledIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.R;
import com.example.studybuddy.model.Post;

import org.w3c.dom.Text;

import java.util.List;

public class ProfileStorylineAdapter extends RecyclerView.Adapter<ProfileStorylineAdapter.ViewHolder> {
    private Context context;
    private Activity activity;
    private List<Post> posts;

    public ProfileStorylineAdapter(Context context, Activity activity, List<Post> posts) {
        this.context = context;
        this.activity = activity;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ProfileStorylineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_storyline_item, parent, false);
        return new ProfileStorylineAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileStorylineAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);

        if(post.getType().equals(Post.POST_TYPE_IMAGE)) {
            holder.linearLayoutPostText.setVisibility(View.GONE);
            holder.imageViewPostPhoto.setVisibility(View.VISIBLE);
            holder.imageViewPostVideo.setVisibility(View.GONE);

            Glide.with(context)
                    .load(post.getPost())
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(holder.imageViewPostPhoto);
        } else if(post.getType().equals(Post.POST_TYPE_TEXT)) {
            holder.imageViewPostPhoto.setVisibility(View.GONE);
            holder.linearLayoutPostText.setVisibility(View.VISIBLE);
            holder.imageViewPostVideo.setVisibility(View.GONE);

            if(post.getPost() != null) holder.editTextPostText.setText(post.getPost());
        } else if(post.getType().equals(Post.POST_TYPE_VIDEO)) {
            holder.linearLayoutPostText.setVisibility(View.GONE);
            holder.imageViewPostPhoto.setVisibility(View.VISIBLE);
            holder.imageViewPostVideo.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(post.getPost())
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(holder.imageViewPostPhoto);
        }
    }

    @Override
    public int getItemCount() { return posts.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewPostPhoto;
        public ImageView imageViewPostVideo;
        public TextView editTextPostText;
        public LinearLayout linearLayoutPostText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewPostPhoto = itemView.findViewById(R.id.profile_storyline_image);
            imageViewPostVideo = itemView.findViewById(R.id.profile_storyline_camera);
            editTextPostText = itemView.findViewById(R.id.profile_storyline_text);
            linearLayoutPostText = itemView.findViewById(R.id.linearLayoutProfileStorylineText);
        }
    }
}
