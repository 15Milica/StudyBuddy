package com.example.studybuddy.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
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

import com.bumptech.glide.Glide;
import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.SessionManager;
import com.example.studybuddy.adapter.MessageAdapter;
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
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
    private static int PERMISSION_CODE_AUDIO = 21;
    private static String recordPermission = Manifest.permission.RECORD_AUDIO;
    private CircleImageView groupPhoto;
    private TextView groupName;
    private ImageButton buttonOptions;
    private ImageButton buttonCamera;
    private ImageButton buttonMic;
    private ImageButton buttonSend;
    private EditText editTextMessage;
    private RecyclerView recyclerView;
    private LinearLayout linearLayoutReplyMessage;
    private ImageButton buttonCancel;
    private String groupId;
    private FirebaseUser firebaseUser;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private String soundFile;
    private Group group;
    private boolean status;
    private DatabaseReference user_status;
    private SessionManager sessionManager;
    private boolean MUTE_UN_MUTE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        sessionManager = new SessionManager(getApplicationContext());
        messages = new ArrayList<>();
        isRecording = false;
        soundFile = null;

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user_status = FirebaseDatabase.getInstance().getReference("user_status").child(firebaseUser.getUid());
        groupId = getIntent().getStringExtra("groupId");

        groupPhoto = (CircleImageView) findViewById(R.id.groupChatPhoto);
        groupName = (TextView) findViewById(R.id.groupChatName);
        setGroupDetails();

        buttonCamera = (ImageButton) findViewById(R.id.imageButtonSendPhotoChat);
        buttonCamera.setOnClickListener(view -> onClickCamera());

        buttonMic = (ImageButton) findViewById(R.id.imageButtonSendSoundChat);
        buttonMic.setOnClickListener(view -> onClickMic());

        buttonOptions = (ImageButton) findViewById(R.id.imageButtonOptions);
        buttonOptions.setOnClickListener(view -> onClickOptions());

        buttonSend = (ImageButton) findViewById(R.id.imageButtonSendMessage);
        buttonSend.setOnClickListener(view -> {
            String msg = editTextMessage.getText().toString().trim();
            Notification.SendToGroup(msg, group);
            onClickSend( msg, "text");
            editTextMessage.setText("");
        });

        editTextMessage = (EditText) findViewById(R.id.groupChatSendMessage);
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!Check.isEmpty(editTextMessage))
                {
                    buttonCamera.setVisibility(View.GONE);
                    buttonMic.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                }else{
                    buttonCamera.setVisibility(View.VISIBLE);
                    buttonMic.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewChat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GroupChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        linearLayoutReplyMessage = (LinearLayout) findViewById(R.id.group_chat_reply);
        buttonCancel = (ImageButton) findViewById(R.id.group_imageButtonCancelReply);
        buttonCancel.setOnClickListener(view -> linearLayoutReplyMessage.setVisibility(View.GONE));
        readMessages();
    }
    private void readMessages(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("group_messages").child(groupId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    messages.add(dataSnapshot.getValue(Message.class));
                }
                messageAdapter = new MessageAdapter(GroupChatActivity.this, messages, groupId, "group");
                recyclerView.setAdapter(messageAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void setGroupDetails(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups");
        ref.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group = snapshot.getValue(Group.class);
                groupName.setText(group.getGroupName());
                if(group.getGroupPhoto().equals("default")) groupPhoto.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                else Glide.with(getApplicationContext()).load(group.getGroupPhoto()).into(groupPhoto);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void onClickCamera(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080,1080)
                .start();
    }
    @SuppressLint("ResourceAsColor")
    private void onClickMic(){
        if(isRecording){
            stopRecording();
            buttonMic.setImageResource(R.drawable.ic_chat_vectors_microphone);
            editTextMessage.setEnabled(true);
            editTextMessage.setHint("");
            isRecording = false;
        }else {
            if(checkPermission()) {
                startRecording();
                buttonMic.setImageResource(R.drawable.ic_chat_vectors_microphone_stop);
                editTextMessage.setEnabled(false);
                editTextMessage.setHint("Snimanje...");
                editTextMessage.setTextColor(R.color.text_color);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
            View dialogView = LayoutInflater.from(GroupChatActivity.this).inflate(R.layout.alert_dialog, null);

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
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("audio/" + uri.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(uri);
        uploadTask.addOnFailureListener(e->{
            Toast.makeText(this, "Greška: "+e, Toast.LENGTH_SHORT).show();
            soundFile = null;
        }).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            task.addOnCompleteListener(task1->{
                if(task1.isSuccessful()){
                    Uri uri1 = task1.getResult();
                    String audioUrl = uri1.toString();
                    Notification.SendToGroup("Glasovna poruka", group);
                    onClickSend(audioUrl, "audio");
                }
            });
            soundFile = null;
        });
    }
    private void onClickSend(String message, String type){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("group_messages").child(groupId);
        String id = ref.push().getKey();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatD = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dateFormatT = new SimpleDateFormat("HH:mm");

        String date = dateFormatD.format(calendar.getTime());
        String time = dateFormatT.format(calendar.getTime());
        MessageTime messageTime = new MessageTime(date, time);

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("type", type);
        map.put("sender", firebaseUser.getUid());
        map.put("receiver", groupId);
        map.put("message", message);
        map.put("sendingTime", messageTime);

        if(linearLayoutReplyMessage.getVisibility() == View.VISIBLE) {
            Message replyMessage = messageAdapter.getReplyMessage();

            final String r_sender = replyMessage.getSender();
            final String r_message = replyMessage.getMessage();
            final String r_type = replyMessage.getType();

            ReplyMessage rMessage = new ReplyMessage(r_sender,r_type, r_message);
            map.put("replyMessage", rMessage);

            linearLayoutReplyMessage.setVisibility(View.GONE);
            editTextMessage.setMaxLines(3);
        }
        ref.child(id).setValue(map);
    }
    private void onClickOptions(){
        buttonOptions.setEnabled(false);
        Intent intent = new Intent(this, ChatSettingsActivity.class);
        intent.putExtra("group", groupId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            if(Check.networkConnect(getApplicationContext())){
                StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + uri.getLastPathSegment());
                UploadTask uploadTask = ref.putFile(uri);
                uploadTask.addOnFailureListener(e->{
                    Toast.makeText(this, "Greška " + e, Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnCompleteListener(task1 -> {
                       if(task1.isSuccessful()){
                           Uri uri1 = task1.getResult();
                           String imageURL = uri1.toString();
                           Notification.SendToGroup("Slikovna poruka", group);
                           onClickSend(imageURL, "image");
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
            ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{recordPermission}, PERMISSION_CODE_AUDIO);
            return false;
        }
    }
    private void muteThisChat() {
        Set<String> mutedChats = new ArraySet<>();
        Set<String> mS = sessionManager.getMutedChats();

        if(mS != null) mutedChats.addAll(mS);

        mutedChats.add(groupId);

        sessionManager.setMutedChats(mutedChats);
    }

    private void unMuteThisChat() {
        Set<String> mutedChats = new ArraySet<>();
        Set<String> mS = sessionManager.getMutedChats();

        if(mS != null) mutedChats.addAll(mS);

        mutedChats.remove(groupId);

        sessionManager.setMutedChats(mutedChats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        buttonOptions.setEnabled(true);
        if(status) { user_status.setValue("Online"); }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(MUTE_UN_MUTE) unMuteThisChat();
        if(status) { user_status.setValue("Offline"); }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(MUTE_UN_MUTE) unMuteThisChat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(MUTE_UN_MUTE) unMuteThisChat();
    }
    @Override
    protected void onStart() {
        super.onStart();

        MUTE_UN_MUTE = true;

        Set<String> mutedChats = new ArraySet<>();
        Set<String> mS = sessionManager.getMutedChats();

        if(mS != null) {
            mutedChats.addAll(mS);

            for(String m : mutedChats) {
                if(m.equals(groupId)) {
                    MUTE_UN_MUTE = false;
                    break;
                }
            }
        }

        if(MUTE_UN_MUTE) muteThisChat();
        status = sessionManager.getActivityStatus();

        if(status) { user_status.onDisconnect().setValue("Offline"); }
        else {
            user_status.setValue("");
            user_status.onDisconnect().setValue("");
        }
    }

}