package com.example.studybuddy.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.Check;
import com.example.studybuddy.ImageViewFullscreenActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.chat.ForwardMessageActivity;
import com.example.studybuddy.model.Message;
import com.example.studybuddy.model.MessageTime;
import com.example.studybuddy.model.PinnedMessage;
import com.example.studybuddy.model.User;
import com.google.android.gms.common.api.Api;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<Message> messages;
    private String chatId;
    private FirebaseUser firebaseUser;
    private MediaPlayer mediaPlayer;
    private LinearLayout linearLayoutReply;
    private TextView replyName;
    private TextView textViewReplyMessage;
    private EditText editTextSendMessage;

    public MessageAdapter(Context context, List<Message> messages, String chatId, String chatType) {
        this.context = context;
        this.messages = messages;
        this.chatId = chatId;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatType.equals("user")) {
            linearLayoutReply = ((Activity) context).findViewById(R.id.chat_reply);
            replyName = ((Activity) context).findViewById(R.id.chat_reply_message_name);
            textViewReplyMessage = ((Activity) context).findViewById(R.id.chat_reply_message);
            editTextSendMessage = ((Activity) context).findViewById(R.id.editTextSendMessageChat);
        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if(message.getType().equals("text")){
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_message.setText(message.getMessage());

            holder.buttonPlay.setVisibility(View.GONE);
            holder.buttonPause.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.GONE);
        }else if(message.getType().equals("audio")){
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_message.setText("Glasovna poruka");

            holder.buttonPlay.setVisibility(View.VISIBLE);
            holder.buttonPause.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.GONE);
        }else if(message.getType().equals("image")){
            holder.show_message.setVisibility(View.GONE);
            holder.buttonPlay.setVisibility(View.GONE);
            holder.buttonPause.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.VISIBLE);
            if(message.getMessage() != null){
                Glide.with(context).load(message.getMessage()).into(holder.show_image);
            }
        }else {
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_message.setText("Post");
            holder.buttonPlay.setVisibility(View.GONE);
            holder.buttonPause.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.GONE);
        }
        MessageTime messageTime = message.getSendingTime();
        String time = messageTime.getTime();
        String date = messageTime.getDate();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
        String currentDate = simpleDateFormat.format(calendar.getTime());
        String currentTime = simpleDateFormat1.format(calendar.getTime());

        String TIME;
        if(!currentDate.equals(date)) {
            TIME = date + " " + time;
            holder.msg_time.setVisibility(View.VISIBLE);
        } else if (!currentTime.equals(time)) {
            TIME = time;
            holder.msg_time.setVisibility(View.VISIBLE);
        } else {
            TIME = "";
            holder.msg_time.setVisibility(View.GONE);
        }

        holder.msg_time.setText(TIME);

        if(holder.getItemViewType() == MSG_TYPE_LEFT){
            if(position>0){
                Message message1 = messages.get(position-1);
                if(message1.getSender().equals(message.getSender())){
                    holder.sender_photo.setVisibility(View.GONE);
                }else setSender(message.getSender(), holder);
            }else {
                holder.sender_photo.setVisibility(View.VISIBLE);
                setSender(message.getSender(), holder);
            }
        }

        holder.buttonPlay.setOnClickListener(view -> {
            if(Check.networkConnect(context)) {
                try {
                    onPlayAudio(holder, message.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else Toast.makeText(context, "Nema mreže!", Toast.LENGTH_SHORT).show();
        });
        holder.buttonPause.setOnClickListener(view -> onStopAudio(holder));
        holder.show_image.setOnClickListener(view -> onClickImage(message.getMessage()));

        holder.show_message.setOnLongClickListener(view -> {
            longClick(message, holder);
            return true;
        });
        holder.show_image.setOnLongClickListener(view -> {
            longClick(message, holder);
            return true;
        });
        holder.show_message.setOnClickListener(v -> {
            if(message.getType().equals("text") && (message.getMessage().startsWith("https://") || message.getMessage().startsWith("http://"))) {
                Uri uri = Uri.parse(message.getMessage());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            } else if(!message.getType().equals("audio") && !message.getType().equals("text")) {
               /* Intent intent = new Intent(mContext, PostActivity.class);
                intent.putExtra("postId", message.getMessage());
                intent.putExtra("type", message.getType());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);*/
            }
        });
    }
    private void longClick(Message message, ViewHolder holder){
        if(message.getSender().equals(firebaseUser.getUid())){
            CharSequence options[] = new CharSequence[] {
                    addIconToText("  Odgovori", R.drawable.ic_vector_replay_arrow, holder),
                    addIconToText("  Prosledi", R.drawable.ic_vector_forward, holder),
                    addIconToText("  Sačuvaj poruku", R.drawable.ic_settings_pinned_message, holder),
                    addIconToText("  Obriši", R.drawable.ic_vector_bin, holder)
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setCancelable(true);

            builder.setItems(options, (dialog, which) -> {
                if(which == 0) {
                    onReply(message);
                } else if(which == 1) {
                    onForward(message);
                } else if(which == 2) {
                    onPin(message);
                } else if(which == 3) {
                    onDelete(message);
                }
            });

            builder.show();
        }else {
            CharSequence options[] = new CharSequence[]{
                    addIconToText("  Odgovori", R.drawable.ic_vector_replay_arrow, holder),
                    addIconToText("  Prosledi", R.drawable.ic_vector_forward, holder),
                    addIconToText("  Sačuvaj poruku", R.drawable.ic_settings_pinned_message, holder)
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setCancelable(true);

            builder.setItems(options, (dialog, which) -> {
                if(which == 0) {
                    onReply(message);
                } else if(which == 1) {
                    onForward(message);
                } else if(which == 2) {
                    onPin(message);
                }
            });
            builder.show();
        }
    }
    private void onDelete(Message message){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats");
        ref.child(firebaseUser.getUid()).child(chatId).child(message.getId()).removeValue();

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("chats");
        ref1.child(chatId).child(firebaseUser.getUid()).child(message.getId()).removeValue();
    }
    private void  onPin(Message message){
        if(message.getType().equals("text")){
            final String sender = message.getSender();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pinned_messages")
                    .child(firebaseUser.getUid());
            final String id = ref.push().getKey();
            final String text = message.getMessage();
            final String date = message.getSendingTime().getDate();
            final String time = message.getSendingTime().getTime();

            PinnedMessage pinnedMessage = new PinnedMessage(id,sender, chatId, text, time, date);
            ref.child(id).setValue(pinnedMessage).addOnCompleteListener(task->{
                if(task.isSuccessful()){
                    Toast.makeText(context, "Uspešno sačuvana poruka", Toast.LENGTH_LONG).show();
                }else Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            });

        }else Toast.makeText(context, "Možete sačuvati samo tekstualne poruke", Toast.LENGTH_LONG).show();
    }
    private void onForward(Message message){
        Intent intent = new Intent(context, ForwardMessageActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("message", message.getMessage());
        intent.putExtra("messageType", message.getType());
        context.startActivity(intent);
    }
    private void onReply(Message message){

    }

    private CharSequence addIconToText(String text, int iconResId, ViewHolder holder) {
        Drawable icon = ContextCompat.getDrawable(holder.itemView.getContext(), iconResId);
        if (icon != null) {
            icon.setBounds(0, 0, 60, 70);
            ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);
            SpannableString spannableString = new SpannableString(" " + text);
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }
        return text;
    }

    private void onClickImage(String imageUrl){
        Intent intent = new Intent(context, ImageViewFullscreenActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ((Activity) context).startActivity(intent);
    }
    private void onPlayAudio(ViewHolder holder, String audioURL) throws IOException {
        holder.buttonPlay.setVisibility(View.GONE);
        holder.buttonPause.setVisibility(View.VISIBLE);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(audioURL);

        mediaPlayer.setOnCompletionListener(mp -> onStopAudio(holder));

        mediaPlayer.prepare();
        mediaPlayer.start();
    }
    private void onStopAudio(ViewHolder holder){
        holder.buttonPlay.setVisibility(View.VISIBLE);
        holder.buttonPause.setVisibility(View.GONE);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
    private void setSender(String userId, ViewHolder holder){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
        ref.get().addOnCompleteListener(task->{
            if(task.isSuccessful()) {
                User user = task.getResult().getValue(User.class);
                if(user.getPhoto().equals("default")){
                    holder.sender_photo.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                }else Glide.with(context).load(user.getPhoto()).into(holder.sender_photo);
            }
        });
    }

    @Override
    public int getItemCount() { return messages.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public TextView msg_time;
        public ImageButton buttonPlay;
        public ImageButton buttonPause;
        public ImageView show_image;
        public CircleImageView sender_photo;
        public LinearLayout layout_reply;
        public TextView reply_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            msg_time = itemView.findViewById(R.id.message_time);
            buttonPlay = itemView.findViewById(R.id.audio_message_play);
            buttonPause = itemView.findViewById(R.id.audio_message_stop);
            show_image = itemView.findViewById(R.id.image_message);
            sender_photo = (CircleImageView) itemView.findViewById(R.id.sender_photo);

            layout_reply = itemView.findViewById(R.id.layout_message_replay);
            reply_message = itemView.findViewById(R.id.show_reply_message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSender().equals(firebaseUser.getUid())) return MSG_TYPE_RIGHT;
        else return MSG_TYPE_LEFT;
    }
}
