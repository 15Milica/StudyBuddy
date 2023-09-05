package com.example.studybuddy.notification;

import com.example.studybuddy.R;
import com.example.studybuddy.SessionManager;
import com.example.studybuddy.model.Channel;
import com.example.studybuddy.model.Group;
import com.example.studybuddy.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notification {
    private static APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    public static void SendToUser(String message, User receiver) {
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .get().addOnCompleteListener(task -> {
                    User user = task.getResult().getValue(User.class);

                    assert user != null;

                    final String name = user.getName();
                    String body = name + ": " + message;

                    Data data = new Data(user.getUserId(), R.mipmap.ic_launcher, body, "Nova poruka", receiver.getUserId(), "user");
                    Sender sender = new Sender(data, receiver.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) { }
                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) { }
                    });
                });
    }
    public static void SendToGroup(String message, Group group){
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .get().addOnCompleteListener(task -> {
                    User user = task.getResult().getValue(User.class);

                    String name = group.getGroupName();
                    String body = name + ": " + message;
                    Data data = new Data(user.getUserId(), R.mipmap.ic_launcher, body, "Nova poruka", group.getGroupId(), "group");

                    List<String> tokens = group.getTokens();
                    tokens.remove(user.getToken());

                    for (String token : tokens) {
                        Sender sender = new Sender(data, token);
                        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                            }
                        });
                    }
                });
    }
    public static void SendToChannel(String message, Group group, Channel channel){
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .get().addOnCompleteListener(task -> {
                    User user = task.getResult().getValue(User.class);
                    String name = group.getGroupName() + " -> " + channel.getChannelName();
                    String body = name + ": " + message;
                    Data data = new Data(group.getGroupId(), R.mipmap.ic_launcher, body, "Nova poruka", channel.getChannelId(), "channel");

                    List<String> tokens = group.getTokens();
                    tokens.remove(user.getToken());

                    for(String token : tokens) {
                        Sender sender = new Sender(data, token);
                        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) { }
                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) { }
                        });
                    }
                });
    }
    public static void sendNotificationPost(String postId, String message, String token, String pageId) {
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .get().addOnCompleteListener(task -> {
                    User user = task.getResult().getValue(User.class);
                    final String name =user.getName();

                    String body = name + ": " + message;
                    Data data = new Data(pageId, R.mipmap.ic_launcher, body, "Obaveštenje", postId, "post");
                    Sender sender = new Sender(data, token);
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        }
                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                        }
                    });

                });
    }
}
