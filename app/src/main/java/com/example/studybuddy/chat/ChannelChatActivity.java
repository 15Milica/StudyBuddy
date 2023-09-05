package com.example.studybuddy.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.MessageAdapter;
import com.example.studybuddy.model.Channel;
import com.example.studybuddy.model.Group;
import com.example.studybuddy.model.Message;
import com.example.studybuddy.model.MessageTime;
import com.example.studybuddy.model.ReplyMessage;
import com.example.studybuddy.notification.Notification;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChannelChatActivity extends AppCompatActivity {
    private static String recordPermission = Manifest.permission.RECORD_AUDIO;
    private static int PERMISSION_CODE_AUDIO = 21;
    private TextView nameGroup;
    private TextView nameChannel;
    private EditText editTextSendMessage;
    private ImageButton buttonMic;
    private ImageButton buttonCamera;
    private ImageButton buttonSend;
    private ImageButton buttonCancel;
    private RecyclerView recyclerView;
    private LinearLayout linearLayoutChatReplyMessage;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private FirebaseUser firebaseUser;
    private String channelId;
    private MediaRecorder mediaRecorder;
    private boolean isRecording;
    private String soundFile;
    private Channel channel;
    private Group group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_chat);

        messages = new ArrayList<>();
        isRecording = false;
        soundFile = null;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        channelId = getIntent().getStringExtra("channelId");

        nameGroup = (TextView) findViewById(R.id.channelChatGroupName);
        nameChannel = (TextView) findViewById(R.id.channelChatName);
        setDetailsChannel();

        editTextSendMessage = (EditText) findViewById(R.id.channelChatSendMessage);
        editTextSendMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!Check.isEmpty(editTextSendMessage)){
                    buttonSend.setVisibility(View.VISIBLE);
                    buttonMic.setVisibility(View.GONE);
                    buttonCamera.setVisibility(View.GONE);
                }else {
                    buttonSend.setVisibility(View.GONE);
                    buttonMic.setVisibility(View.VISIBLE);
                    buttonCamera.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        buttonCamera = (ImageButton) findViewById(R.id.channelChatSendPhoto);
        buttonCamera.setOnClickListener(view-> onClickCamera());

        buttonMic = (ImageButton) findViewById(R.id.channelChatSendSound);
        buttonMic.setOnClickListener(view -> onClickMic());

        buttonSend = (ImageButton) findViewById(R.id.channelChatSend);
        buttonSend.setOnClickListener(view -> {
            String text = editTextSendMessage.getText().toString().trim();
            onClickSend(text, "text");
            Notification.SendToChannel(text, group, channel);
            editTextSendMessage.setText("");
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewChannelChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChannelChatActivity.this));

        linearLayoutChatReplyMessage = (LinearLayout) findViewById(R.id.channel_chat_reply);
        buttonCancel = (ImageButton) findViewById(R.id.channel_imageButtonCancelReply);
        buttonCancel.setOnClickListener(view -> linearLayoutChatReplyMessage.setVisibility(View.GONE));
        readMessages();
    }
    private void onClickCamera(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }
    @SuppressLint("ResourceAsColor")
    private void onClickMic(){
        if(isRecording){
            stopRecording();
            buttonMic.setImageResource(R.drawable.ic_chat_vectors_microphone);
            editTextSendMessage.setEnabled(true);
            editTextSendMessage.setHint("");
            isRecording = false;
        }else {
            if(checkPermission()) {
                startRecording();
                buttonMic.setImageResource(R.drawable.ic_chat_vectors_microphone_stop);
                editTextSendMessage.setEnabled(false);
                editTextSendMessage.setHint("Snimanje...");
                editTextSendMessage.setTextColor(R.color.text_color);
                isRecording = true;
            }
        }
    }
    private void startRecording(){
        String recordPath = getApplication().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        Date now = new Date();
        String recordFile = "Recording_" + simpleDateFormat.format(now) + ".3gp";

        soundFile = recordPath + "/" + recordFile;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(soundFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }
    private void stopRecording(){
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        if(!Check.networkConnect(getApplicationContext())){
            Toast.makeText(this, "Nema mreže!", Toast.LENGTH_SHORT).show();
        }else if(soundFile == null){
            Toast.makeText(this, "Greška: pokušaj ponovo!", Toast.LENGTH_SHORT).show();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChannelChatActivity.this);
            View dialogView = LayoutInflater.from(ChannelChatActivity.this).inflate(R.layout.alert_dialog, null);

            builder.setView(dialogView);
            final AlertDialog dialog = builder.create();
            Button buttonConfirm = dialogView.findViewById(R.id.confirm_button);
            Button buttonCancel = dialogView.findViewById(R.id.cancel_button);
            TextView text = dialogView.findViewById(R.id.textViewAlertDialog);
            text.setText("Želite li da pošaljete poruku?");
            buttonConfirm.setText("DA");
            buttonCancel.setText("NE");

            buttonConfirm.setOnClickListener(view -> {
                uploadVideo();
                dialog.dismiss();
            });
            buttonCancel.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }
    private void uploadVideo(){
        Uri uri = Uri.fromFile(new File(soundFile));
        StorageReference ref = FirebaseStorage.getInstance().getReference("audio/"+uri.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(uri);

        uploadTask.addOnFailureListener(e->{
            Toast.makeText(this, "Greška: "+e, Toast.LENGTH_SHORT).show();
            soundFile = null;
        }).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            task.addOnCompleteListener(task1 -> {
                if(task1.isSuccessful()){
                    Uri uri1 = task1.getResult();
                    String audioUrl = uri1.toString();
                    onClickSend(audioUrl, "audio");
                    Notification.SendToChannel("Glasovna poruka", group, channel);
                }
            });
            soundFile = null;
        });
    }
    private void onClickSend(String message, String type){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("channel_messages").child(channelId);
        String key = reference.push().getKey();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormatD = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dateFormatT = new SimpleDateFormat("HH:mm");

        String date = dateFormatD.format(calendar.getTime());
        String time = dateFormatT.format(calendar.getTime());

        MessageTime messageTime = new MessageTime(date, time);

        HashMap<String, Object> map = new HashMap<>();

        map.put("id", key);
        map.put("sender", firebaseUser.getUid());
        map.put("receiver", channelId);
        map.put("type", type);
        map.put("message", message);
        map.put("sendingTime", messageTime);

        if(linearLayoutChatReplyMessage.getVisibility() == View.VISIBLE) {
            Message replyMessage = messageAdapter.getReplyMessage();

            final String r_sender = replyMessage.getSender();
            final String r_message = replyMessage.getMessage();
            final String r_type = replyMessage.getType();

            ReplyMessage rMessage = new ReplyMessage(r_sender, r_message, r_type);

            map.put("replyMessage", rMessage);

            linearLayoutChatReplyMessage.setVisibility(View.GONE);
            editTextSendMessage.setMaxLines(3);
        }
        reference.child(key).setValue(map);
    }
    private void readMessages(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("channel_messages").child(channelId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    messages.add(dataSnapshot.getValue(Message.class));
                }
                messageAdapter = new MessageAdapter(ChannelChatActivity.this, messages, channelId, "channel");
                recyclerView.setAdapter(messageAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void setDetailsChannel(){
        DatabaseReference refGroups = FirebaseDatabase.getInstance().getReference("groups");

        DatabaseReference refChannel = FirebaseDatabase.getInstance().getReference("channels");
        refChannel.child(channelId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot dataSnapshot = task.getResult();
                channel = dataSnapshot.getValue(Channel.class);
                nameChannel.setText(channel.getChannelName());

                String groupId = channel.getGroupId();
                refGroups.child(groupId).get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        DataSnapshot dataSnapshot1 = task1.getResult();
                        group = dataSnapshot1.getValue(Group.class);
                        nameGroup.setText(group.getGroupName());
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            if(Check.networkConnect(getApplicationContext())){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("images/" + uri.getLastPathSegment());
                UploadTask uploadTask = storageReference.putFile(uri);
                uploadTask.addOnFailureListener(e->{
                    Toast.makeText(this, "Greška " + e, Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            Uri uri1 = task1.getResult();
                            String imageUrl = uri1.toString();
                            onClickSend(imageUrl, "image");
                            Notification.SendToChannel("Slikovna poruka", group, channel);
                        }
                    });
                });
            }else Toast.makeText(this, "Nema mreže!", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkPermission() {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ChannelChatActivity.this, new String[]{recordPermission}, PERMISSION_CODE_AUDIO);
            return false;
        }
    }
}