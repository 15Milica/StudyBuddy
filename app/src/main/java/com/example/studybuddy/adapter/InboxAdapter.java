package com.example.studybuddy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.model.ChatItem;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private Context context;
    private List<ChatItem> chats;

    public InboxAdapter(Context context, List<ChatItem>chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new InboxAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxAdapter.ViewHolder holder, int position) {
        ChatItem item = chats.get(position);

        holder.chat_name.setText(item.getChatName());

        if(item.getChatPhoto().equals("default")) holder.chat_photo.setImageResource(R.drawable.ic_create_profile_vectors_photo);
        else Glide.with(context).load(item.getChatPhoto()).into(holder.chat_photo);
    }

    @Override
    public int getItemCount() { return chats.size(); }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView chat_photo;
        public TextView chat_name;
        public TextView last_message;
        public TextView time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chat_photo = itemView.findViewById(R.id.chat_photo);
            chat_name = itemView.findViewById(R.id.chat_name);
            last_message = itemView.findViewById(R.id.chat_last_message);
            time = itemView.findViewById(R.id.msg_time);
        }
    }
}
