package com.example.studybuddy.notification;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.application.isradeleon.notify.Notify;
import com.example.studybuddy.MainActivity;
import com.example.studybuddy.PostActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.SessionManager;
import com.example.studybuddy.chat.ChannelChatActivity;
import com.example.studybuddy.chat.ChatActivity;
import com.example.studybuddy.chat.GroupChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Set;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) { super.onNewToken(token); }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        sendNotification(message);
    }
    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        String chatId = remoteMessage.getData().get("chatId");
        String chatType = remoteMessage.getData().get("chatType");

        if(chatType.equals("user")) {
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            Set<String> mutedChats = sessionManager.getMutedChats();

            boolean muted = false;

            if(mutedChats != null) {
                for(String m : mutedChats) {
                    if(m.equals(user)) {
                        muted = true;
                        break;
                    }
                }
            }

            if(muted == false) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("userId", user);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Notify.build(getApplicationContext())
                        .setTitle(title)
                        .setContent(body)
                        .setSmallIcon(Integer.parseInt(icon))
                        .setColor(R.color.primary_color)
                        .setAction(intent)
                        .setAutoCancel(true)
                        .show();
            }
        } else if(chatType.equals("group")) {
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            Set<String> mutedChats = sessionManager.getMutedChats();

            boolean muted = false;

            if(mutedChats != null) {
                for(String m : mutedChats) {
                    if(m.equals(chatId)) {
                        muted = true;
                        break;
                    }
                }
            }

            if(muted == false) {
                Intent intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                intent.putExtra("groupId", chatId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Notify.build(getApplicationContext())
                        .setTitle(title)
                        .setContent(body)
                        .setSmallIcon(Integer.parseInt(icon))
                        .setColor(R.color.primary_color)
                        .setAction(intent)
                        .setAutoCancel(true)
                        .show();
            }
        } else if(chatType.equals("channel")) {
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            Set<String> mutedChats = sessionManager.getMutedChats();

            boolean muted = false;

            if(mutedChats != null) {
                for(String m : mutedChats) {
                    if(m.equals(user)) {
                        muted = true;
                        break;
                    }
                }
            }

            if(muted == false) {
                Intent intent = new Intent(getApplicationContext(), ChannelChatActivity.class);
                intent.putExtra("channelId", chatId);
                intent.putExtra("groupId", user);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Notify.build(getApplicationContext())
                        .setTitle(title)
                        .setContent(body)
                        .setSmallIcon(Integer.parseInt(icon))
                        .setColor(R.color.primary_color)
                        .setAction(intent)
                        .setAutoCancel(true)
                        .show();
            }
        } else if(chatType.equals("post")){
            Intent intent = new Intent(getApplicationContext(), PostActivity.class);
            intent.putExtra("postId", chatId);
            intent.putExtra("type", user);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Notify.build(getApplicationContext())
                    .setTitle(title)
                    .setContent(body)
                    .setSmallIcon(Integer.parseInt(icon))
                    .setColor(R.color.primary_color)
                    .setAction(intent)
                    .setAutoCancel(true)
                    .show();
        }
    }
}
