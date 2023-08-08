package com.example.studybuddy.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.chat.ChatActivity;
import com.example.studybuddy.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        User user = users.get(position);

        String userId = user.getUserId();

        holder.user_name.setText(user.getName());

        if (!user.getPhoto().equals("default")) { Glide.with(context).load(user.getPhoto()).into(holder.profile_image); }
        else { holder.profile_image.setImageResource(R.drawable.ic_create_profile_vectors_photo); }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", userId);
            context.startActivity(intent);
            ((Activity) context).finish();
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView user_name;
        public ImageView profile_image;
        public ImageView user_border;
        public TextView status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name = itemView.findViewById(R.id.user_name);
            profile_image = itemView.findViewById(R.id.user_profile_photo);
            user_border = itemView.findViewById(R.id.user_border);
            status = itemView.findViewById(R.id.user_status);
        }
    }
}
