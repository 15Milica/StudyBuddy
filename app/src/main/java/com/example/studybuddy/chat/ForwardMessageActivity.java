package com.example.studybuddy.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.ForwardAdapter;
import com.example.studybuddy.model.ChatItem;
import com.example.studybuddy.model.ChatList;
import com.example.studybuddy.model.Group;
import com.example.studybuddy.model.MessageTime;
import com.example.studybuddy.model.User;
import com.example.studybuddy.notification.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ForwardMessageActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private Button buttonForward;
    private ProgressBar progressBar;
    private ImageView imageMessage;
    private TextView textMessage;
    private TextView title;
    private TextView sub_title;
    private RecyclerView recyclerView;
    private ForwardAdapter forwardAdapter;
    private List<ChatItem> chatItems;
    private List<ChatList> chatsList;
    private List<User> users;
    private List<Group> groups;
    private String chatId;
    private String message;
    private String message_type;
    private FirebaseUser firebaseUser;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_message);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatId = getIntent().getStringExtra("chatId");
        message = getIntent().getStringExtra("message");
        message_type = getIntent().getStringExtra("messageType");

        chatItems = new ArrayList<>();
        chatsList = new ArrayList<>();
        users = new ArrayList<>();
        groups = new ArrayList<>();

        progressBar = (ProgressBar) findViewById(R.id.progressBarForwardMessage);
        title  =(TextView) findViewById(R.id.titleForwardMessage);
        sub_title = (TextView) findViewById(R.id.textViewForwardMessage_Text1);
        imageMessage = (ImageView) findViewById(R.id.forward_image);
        textMessage = (TextView) findViewById(R.id.forward_message);

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackForward);
        buttonBack.setOnClickListener(view -> onBackPressed());

        buttonForward = (Button) findViewById(R.id.buttonForwardMessage);
        buttonForward.setOnClickListener(view -> onClickForward());

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewForwardMessage);
        recyclerView.setLayoutManager(new LinearLayoutManager(ForwardMessageActivity.this));

        setDetails();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats_list").child(firebaseUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    if(!chatList.getId().equals(chatId)) chatsList.add(chatList);
                }
                setChats();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void setDetails(){
        if(message_type.equals("text")){
            textMessage.setText(message);
            textMessage.setVisibility(View.VISIBLE);
        }else if(message_type.equals("image")){
            imageMessage.setVisibility(View.VISIBLE);
            if(message != null) Glide.with(getApplicationContext()).load(message).into(imageMessage);
        }else if(message_type.equals("audio")){
            textMessage.setVisibility(View.VISIBLE);
            textMessage.setText("Glasovna poruka");
        }else {
            title.setText("Prosledi objavu");
            sub_title.setText("Prosledi objavu ostalim četovima");
            textMessage.setText("Post");
            textMessage.setVisibility(View.VISIBLE);
        }
    }
    private void setChats(){
        progressBar.setVisibility(View.VISIBLE);
        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                chatItems.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    for (ChatList chatList:chatsList){
                        if(chatList.getId().equals(user.getUserId())) users.add(user);
                    }
                }
                for(User u : users) {
                    final String id = u.getUserId();
                    final String name = u.getName();
                    final String type = "user";
                    final String photo = u.getPhoto();

                    ChatItem chatItem = new ChatItem(id, name, type, photo);
                    chatItems.add(chatItem);
                }
                for(Group g : groups){
                    final String id = g.getGroupId();
                    final String name = g.getGroupName();
                    final String type = "group";
                    final String photo = g.getGroupPhoto();

                    ChatItem chatItem = new ChatItem(id, name, type, photo);
                    chatItems.add(chatItem);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        ref = FirebaseDatabase.getInstance().getReference("groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                groups.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Group group = dataSnapshot.getValue(Group.class);
                    for(ChatList chatList:chatsList){
                        if(chatList.getId().equals(group.getGroupId())) groups.add(group);
                    }
                }
                for(User u : users) {
                    final String id = u.getUserId();
                    final String name = u.getName();
                    final String type = "user";
                    final String photo = u.getPhoto();

                    ChatItem chatItem = new ChatItem(id, name, type, photo);
                    chatItems.add(chatItem);
                }
                for(Group g : groups){
                    final String id = g.getGroupId();
                    final String name = g.getGroupName();
                    final String type = "group";
                    final String photo = g.getGroupPhoto();

                    ChatItem chatItem = new ChatItem(id, name, type, photo);
                    chatItems.add(chatItem);
                }

                forwardAdapter = new ForwardAdapter(ForwardMessageActivity.this, chatItems);
                recyclerView.setAdapter(forwardAdapter);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { progressBar.setVisibility(View.GONE);}
        });
    }
    private void onClickForward(){
        List<ChatItem> checkedItems = forwardAdapter.getCheckedItems();
        for(ChatItem chatItem:checkedItems){
            String id = chatItem.getChatId();
            String chatType = chatItem.getChatType();

            if(chatType.equals("user")){
                sendMessageToUser(firebaseUser.getUid(), id);
                FirebaseDatabase.getInstance().getReference("users").child(id).get().addOnCompleteListener(task -> {
                    User user = task.getResult().getValue(User.class);
                    if(message_type.equals("text")) { Notification.SendToUser(message, user); }
                    else if(message_type.equals("image")) { Notification.SendToUser("Slikovna poruka", user); }
                    else if(message_type.equals("audio")) { Notification.SendToUser("Glasovna poruka", user); }
                    else { Notification.SendToUser("Objava", user); }
                });
            }else {
                sendMessageToGroup(firebaseUser.getUid(), id);
                FirebaseDatabase.getInstance().getReference("groups").child(id).get().addOnCompleteListener(task->{
                   Group group = task.getResult().getValue(Group.class);
                    if(message_type.equals("text")) { Notification.SendToGroup(message, group); }
                    else if(message_type.equals("image")) { Notification.SendToGroup("Slikovna poruka", group); }
                    else if(message_type.equals("audio")) { Notification.SendToGroup("Glasovna poruka", group); }
                    else { Notification.SendToGroup("Objava", group); }
                });
            }
        }
        if(!message_type.equals("post_home") || !message_type.equals("post_page")) {
            Toast.makeText(ForwardMessageActivity.this, "Uspešno prosleđena objava", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ForwardMessageActivity.this, "Uspešno prosleđena poruka", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    private void sendMessageToUser(String sender, String receiver){
        DatabaseReference refChats = FirebaseDatabase.getInstance().getReference("chats")
                .child(sender)
                .child(receiver);

        String msg_id = refChats.push().getKey();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatD = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dateFormatT = new SimpleDateFormat("HH:mm");

        String date = dateFormatD.format(calendar.getTime());
        String time = dateFormatT.format(calendar.getTime());

        MessageTime messageTime = new MessageTime(date, time);
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", msg_id);
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);
        map.put("type", message_type);
        map.put("sendingTime", messageTime);

        refChats.child(msg_id).setValue(map);

        DatabaseReference refChats1 = FirebaseDatabase.getInstance().getReference("chats")
                .child(receiver)
                .child(sender);
        refChats1.child(msg_id).setValue(map);

        DatabaseReference chat1Ref = FirebaseDatabase.getInstance().getReference("chats_list")
                .child(sender)
                .child(receiver);

        chat1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { if(!snapshot.exists()) { chat1Ref.child("id").setValue(receiver); } }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        DatabaseReference chat2Ref = FirebaseDatabase.getInstance().getReference("chats_list")
                .child(receiver)
                .child(sender);

        chat2Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { if(!snapshot.exists()) { chat2Ref.child("id").setValue(sender); } }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    private void sendMessageToGroup(String sender, String receiver){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("group_messages").child(receiver);
        String key = reference.push().getKey();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormatD = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dateFormatT = new SimpleDateFormat("HH:mm");

        String date = dateFormatD.format(calendar.getTime());
        String time = dateFormatT.format(calendar.getTime());

        MessageTime messageTime = new MessageTime(date, time);

        HashMap<String, Object> map = new HashMap<>();

        map.put("id", key);
        map.put("sender", sender);
        map.put("type", message_type);
        map.put("message", message);
        map.put("sendingTime", messageTime);

        reference.child(key).setValue(map);
    }
}