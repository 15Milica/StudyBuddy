package com.example.studybuddy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.model.ChatItem;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ForwardAdapter extends RecyclerView.Adapter<ForwardAdapter.ViewHolder> {
    private Context context;
    private List<ChatItem> chatItemList;

    private List<ChatItem> checkedItems;

    public ForwardAdapter(Context context, List<ChatItem> chatItemList) {
        this.context = context;
        this.chatItemList = chatItemList;
        this.checkedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ForwardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.forward_chat_item, parent, false);
        return new ForwardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForwardAdapter.ViewHolder holder, int position) {
        ChatItem chatItem = chatItemList.get(position);
        holder.chat_name.setText(chatItem.getChatName());

        if(chatItem.getChatPhoto().equals("default")) holder.chat_photo.setImageResource(R.drawable.ic_create_profile_vectors_photo);
        else Glide.with(context).load(chatItem.getChatPhoto()).into(holder.chat_photo);

        Button buttonCreate = ((Activity) context).findViewById(R.id.buttonForwardMessage);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) { checkedItems.add(chatItem); }
            else { checkedItems.remove(chatItem); }

            if(checkedItems.size() < 1) buttonCreate.setEnabled(false);
            else buttonCreate.setEnabled(true);
        });
    }
    public List<ChatItem> getCheckedItems(){ return checkedItems; }
    @Override
    public int getItemCount() {
        return chatItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView chat_photo;
        public TextView chat_name;
        public CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chat_photo = (CircleImageView) itemView.findViewById(R.id.forward_chat_photo);
            chat_name = (TextView) itemView.findViewById(R.id.forward_name);
            checkBox = (CheckBox) itemView.findViewById(R.id.forward_check_user);
        }
    }
}
