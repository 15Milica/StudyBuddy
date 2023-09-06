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

import com.example.studybuddy.R;
import com.example.studybuddy.chat.ChannelChatActivity;
import com.example.studybuddy.model.Channel;

import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

    private Context context;
    private List<Channel> channels;

    public ChannelAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
    }

    @NonNull
    @Override
    public ChannelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.channel_item, parent, false);
        return new ChannelAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelAdapter.ViewHolder holder, int position) {
        Channel channel = channels.get(position);
        holder.channelName.setText(channel.getChannelName());
        holder.buttonView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChannelChatActivity.class);
            intent.putExtra("channelId", channel.getChannelId());
            intent.putExtra("groupId", channel.getGroupId());
            ((Activity) context).startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return channels.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView channelName;
        public ImageView buttonView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            channelName = itemView.findViewById(R.id.channel_name);
            buttonView = itemView.findViewById(R.id.channel_view);
        }
    }
}
