package com.example.studybuddy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.chat.ChatActivity;
import com.example.studybuddy.chat.GroupChatActivity;
import com.example.studybuddy.model.ChatItem;
import com.example.studybuddy.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private Context context;
    private List<ChatItem> chats;
    private String textMessage;
    private String messageTime;

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

        LastMessage(item, holder.last_message, holder.time);

        holder.itemView.setOnClickListener(v -> {
            final String type = item.getChatType();

            if(type.equals("user")) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userId", item.getChatId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", item.getChatId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });
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
    private void LastMessage(ChatItem item, final TextView last_message, final TextView time){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String chatId = item.getChatId();
        final String type = item.getChatType();

        textMessage = "default";
        messageTime = "";

        if(type.equals("user")) {
            DatabaseReference refDatabase = FirebaseDatabase.getInstance().getReference("chats").child(firebaseUser.getUid()).child(chatId);

            refDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                        Message message = dataSnapshot.getValue(Message.class);
                        if (message.getType().equals("text"))
                            textMessage = message.getMessage();
                        else if (message.getType().equals("audio"))
                            textMessage = "Zvučna poruka";
                        else if (message.getType().equals("image"))
                            textMessage = "Slikovna poruke";
                        else if (message.getType().equals("file"))
                            textMessage = "Datoteka";
                        else
                            textMessage = "Objava";
                        messageTime = message.getSendingTime().getTime();
                    }
                    if(textMessage.equals("default")){
                        last_message.setText("Nema poruke");
                        time.setText("");
                    }else {
                        last_message.setText(textMessage);
                        time.setText(messageTime);
                    }

                    textMessage = "default";
                    messageTime = "";
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        }else {
            DatabaseReference refDatabase = FirebaseDatabase.getInstance().getReference("group_messages").child(chatId);
            refDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                        Message message = dataSnapshot.getValue(Message.class);
                        if (message.getType().equals("text"))
                            textMessage = message.getMessage();
                        else if (message.getType().equals("audio"))
                            textMessage = "Zvučna poruka";
                        else if (message.getType().equals("image"))
                            textMessage = "Slikovna poruke";
                        else if (message.getType().equals("file"))
                            textMessage = "Datoteka";
                        else
                            textMessage = "Objava";
                        messageTime = message.getSendingTime().getTime();
                    }
                    if(textMessage.equals("default")){
                        last_message.setText("Nema poruke");
                        time.setText("");
                    }else {
                        last_message.setText(textMessage);
                        time.setText(messageTime);
                    }

                    textMessage = "default";
                    messageTime = "";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}
