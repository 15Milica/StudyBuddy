package com.example.studybuddy.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.Check;
import com.example.studybuddy.PinnedMessagesActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.ChannelAdapter;
import com.example.studybuddy.model.Channel;
import com.example.studybuddy.model.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatSettingsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private LinearLayout linearLayoutPinnedMessages;
    private LinearLayout linearLayoutChannels;
    private ImageButton buttonUp;
    private ImageButton buttonDown;
    private LinearLayout linearLayoutNewChannel;
    private EditText textNameChannel;
    private ImageButton buttonCreate;
    private SwipeableRecyclerView swipeableRecyclerView;
    private ProgressBar progressBar;
    private String userId;
    private String groupId;
    private List<Channel> channels;
    private ChannelAdapter channelAdapter;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        channels = new ArrayList<>();
        userId = getIntent().getStringExtra("user");
        groupId = getIntent().getStringExtra("group");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar = (ProgressBar) findViewById(R.id.progressBarChatSettings);
        linearLayoutChannels = (LinearLayout) findViewById(R.id.linearLayoutChannelsSettings);
        buttonUp = (ImageButton) findViewById(R.id.imageButtonUp);
        buttonDown = (ImageButton) findViewById(R.id.imageButtonDown);
        linearLayoutNewChannel = (LinearLayout) findViewById(R.id.linearLayoutNewChannel);
        textNameChannel = (EditText) findViewById(R.id.editTextChannelName);
        buttonCreate = (ImageButton) findViewById(R.id.imageButtonChatSettingsAddChannel);
        swipeableRecyclerView = (SwipeableRecyclerView) findViewById(R.id.recyclerViewChannels);
        swipeableRecyclerView.setLayoutManager(new LinearLayoutManager(ChatSettingsActivity.this));

        if(groupId != null) {
            ChannelSettings();
            showChannels();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups");
            ref.child(groupId).get().addOnCompleteListener(task->{
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot = task.getResult();
                    Group group = dataSnapshot.getValue(Group.class);
                    String groupAdmin = group.getGroupAdmin();

                    if(groupAdmin != null && groupAdmin.equals(firebaseUser.getUid())) {
                        visibilityAddChannel();
                        swipeableRecyclerView.setListener(new SwipeLeftRightCallback.Listener() {
                            @Override
                            public void onSwipedLeft(int position) {
                                Channel channel = channels.get(position);
                                deleteChannel(channel, position);
                            }
                            @Override
                            public void onSwipedRight(int position) {}
                        });
                    }
                    else linearLayoutNewChannel.setVisibility(View.GONE);
                }
            });
        }

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackChatSettings);
        buttonBack.setOnClickListener(view -> onBackPressed());

        linearLayoutPinnedMessages = (LinearLayout) findViewById(R.id.linearLayoutPinnedMessagesSettings);
        linearLayoutPinnedMessages.setOnClickListener(view -> onClickPinnedMessages());
    }

    private void onClickPinnedMessages(){
        if(userId != null) {
            Intent intent = new Intent(ChatSettingsActivity.this, PinnedMessagesActivity.class);
            intent.putExtra("chatId", userId);
            startActivity(intent);
        } else if(groupId != null) {
            Intent intent = new Intent(ChatSettingsActivity.this, PinnedMessagesActivity.class);
            intent.putExtra("chatId", groupId);
            startActivity(intent);
        }
    }
    private void ChannelSettings(){
        linearLayoutChannels.setVisibility(View.VISIBLE);
        swipeableRecyclerView.setVisibility(View.VISIBLE);
        buttonDown.setOnClickListener(view -> {
            buttonDown.setVisibility(View.GONE);
            buttonUp.setVisibility(View.VISIBLE);
            swipeableRecyclerView.setVisibility(View.GONE);
        });
        buttonUp.setOnClickListener(view -> {
            buttonDown.setVisibility(View.VISIBLE);
            buttonUp.setVisibility(View.GONE);
            swipeableRecyclerView.setVisibility(View.VISIBLE);
        });
    }
    private void visibilityAddChannel(){
        linearLayoutNewChannel.setVisibility(View.VISIBLE);
        textNameChannel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(Check.isEmpty(textNameChannel)){
                    buttonCreate.setActivated(false);
                }else buttonCreate.setActivated(true);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        buttonCreate.setOnClickListener(view -> {
            if(buttonCreate.isActivated()){
                onClickCreate();
                textNameChannel.setText("");
            }else Toast.makeText(this, "Unesite ime podgrupe!", Toast.LENGTH_SHORT).show();
        });
    }
    private void onClickCreate(){
        if(Check.networkConnect(getApplicationContext())) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("channels");
            final String name = textNameChannel.getText().toString().trim();
            final String id = ref.push().getKey();
            Channel channel = new Channel(id, name, groupId);
            ref.child(id).setValue(channel).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Uspešno dodata podgrupa.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else Toast.makeText(this, "Nema mreže!", Toast.LENGTH_SHORT).show();
    }
    private void showChannels(){
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("channels");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                channels.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Channel channel = dataSnapshot.getValue(Channel.class);
                    if(channel.getGroupId().equals(groupId)) channels.add(channel);
                }
                channelAdapter = new ChannelAdapter(ChatSettingsActivity.this, channels);
                swipeableRecyclerView.setAdapter(channelAdapter);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {progressBar.setVisibility(View.GONE);}
        });
    }
    private void deleteChannel(Channel channel, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatSettingsActivity.this);
        View dialogView = LayoutInflater.from(ChatSettingsActivity.this).inflate(R.layout.alert_dialog, null);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        Button buttonConfirm = dialogView.findViewById(R.id.confirm_button);
        Button buttonCancel = dialogView.findViewById(R.id.cancel_button);
        TextView text = dialogView.findViewById(R.id.textViewAlertDialog);
        text.setText("Želite li da obrišete podgrupu?");
        buttonConfirm.setText("Obriši");

        buttonConfirm.setOnClickListener(view -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("channels");
            reference.child(channel.getChannelId()).removeValue();
            dialog.dismiss();
        });
        buttonCancel.setOnClickListener(view -> {
            swipeableRecyclerView.getAdapter().notifyItemChanged(position);
            dialog.dismiss();
        });
        dialog.show();
    }
}