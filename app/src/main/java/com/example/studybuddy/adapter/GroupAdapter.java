package com.example.studybuddy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private Context context;
    private List<User> users;
    private List<User> checkedUsers;
    public GroupAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
        checkedUsers = new ArrayList<>();
    }

    @NonNull
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.create_group_user, parent, false);
        return new GroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupAdapter.ViewHolder holder, int position) {
        User user = users.get(position);
        holder.cg_name.setText(user.getName());

        if(user.getPhoto().equals("default")){
            holder.cg_photo.setImageResource(R.drawable.ic_create_profile_vectors_photo);
        }else {
            Glide.with(context).load(user.getPhoto()).into(holder.cg_photo);
        }

        Button buttonCreate = ((Activity) context).findViewById(R.id.buttonCreateGroup);

        holder.user_check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkedUsers.add(user);
                } else {
                    checkedUsers.remove(user);
                }
                if (checkedUsers.size() < 2) {
                    buttonCreate.setEnabled(false);
                }
                else buttonCreate.setEnabled(true);
        });

    }
    public List<User> getCheckedUsers() { return checkedUsers; }
    @Override
    public int getItemCount() { return users.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView cg_photo;
        public TextView cg_name;
        public CheckBox user_check;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cg_name = itemView.findViewById(R.id.cg_full_name);
            cg_photo = itemView.findViewById(R.id.cg_user_profile_photo);
            user_check = itemView.findViewById(R.id.check_user);
        }
    }
}
