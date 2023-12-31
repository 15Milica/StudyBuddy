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
import com.example.studybuddy.model.Message;
import com.example.studybuddy.model.MessageTime;
import com.example.studybuddy.model.ReplyMessage;
import com.example.studybuddy.model.User;
import com.example.studybuddy.notification.Notification;
import com.example.studybuddy.ui.profile.UserProfileActivity;
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
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static String recordPermission = Manifest.permission.RECORD_AUDIO;
    private static int PERMISSION_CODE_AUDIO = 21;
    private CircleImageView user_photo;
    private TextView user_name;
    private TextView text_status;
    private EditText editTextMessage;
    private ImageButton buttonOptions;
    private ImageButton buttonCamera;
    private ImageButton buttonMic;
    private ImageButton buttonSend;
    private RecyclerView recyclerView;
    private LinearLayout linearLayoutReply;
    private ImageButton buttonCancel;
    private Intent intent;
    private String userId;
    private FirebaseUser firebaseUser;
    private User user;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private String soundFile;
    private DatabaseReference user_status;
    private boolean status;
    private SessionManager sessionManager;
    private boolean MUTE_UN_MUTE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        isRecording = false;
        messages = new ArrayList<>();
        soundFile = null;
        sessionManager = new SessionManager(getApplicationContext());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user_status = FirebaseDatabase.getInstance().getReference("user_status").child(firebaseUser.getUid());
        intent = getIntent();
        userId = intent.getStringExtra("userId");

        user_photo = (CircleImageView) findViewById(R.id.profilePhotoChat);
        user_name = (TextView) findViewById(R.id.textViewChatName);
        text_status = (TextView) findViewById(R.id.textViewChatUserStatus);

        setProfileDetails();

        buttonOptions = (ImageButton) findViewById(R.id.imageButtonOptions);
        buttonOptions.setOnClickListener(view -> onClickOptions());

        buttonCamera = (ImageButton) findViewById(R.id.imageButtonSendPhotoChat);
        buttonCamera.setOnClickListener(view -> onClickCamera());

        buttonMic = (ImageButton) findViewById(R.id.imageButtonSendSoundChat);
        buttonMic.setOnClickListener(view -> onClickMic());

        buttonSend =(ImageButton) findViewById(R.id.imageButtonSendMessage);
        buttonSend.setOnClickListener(view -> {
            String text = editTextMessage.getText().toString().trim();
            onClickSend(text, "text");
            Notification.SendToUser(text, user);
            editTextMessage.setText("");
        });

        editTextMessage = (EditText) findViewById(R.id.editTextSendMessageChat);
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        linearLayoutReply = (LinearLayout) findViewById(R.id.chat_reply);
        buttonCancel = (ImageButton) findViewById(R.id.imageButtonCancelReply);
        buttonCancel.setOnClickListener(view -> {
            linearLayoutReply.setVisibility(View.GONE);
            editTextMessage.setMaxLines(3);
        });
        user_photo.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, UserProfileActivity.class);
            intent1.putExtra("userId", userId);
            intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            this.startActivity(intent1);
        });
    }
    private void setProfileDetails(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                user_name.setText(user.getName());
                if(user.getPhoto().equals("default")){
                    user_photo.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                }else Glide.with(getApplicationContext()).load(user.getPhoto()).into(user_photo);
                readMessages(firebaseUser.getUid(), userId);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        DatabaseReference refStatus = FirebaseDatabase.getInstance().getReference("user_status").child(userId);
        refStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                text_status.setText(status);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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
            if(checkPermission()){
                startRecording();
                buttonMic.setImageResource(R.drawable.ic_chat_vectors_microphone_stop);
                editTextMessage.setEnabled(false);
                editTextMessage.setHint("Snimanje...");
                editTextMessage.setTextColor(R.color.text_color);
                isRecording = true;
            }
        }
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
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            View dialogView = LayoutInflater.from(ChatActivity.this).inflate(R.layout.alert_dialog, null);

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
                    Notification.SendToUser("Glasovna poruka", user);
                    onClickSend(audioUrl, "audio");
                }
            });
            soundFile = null;
        });
    }
    private void onClickCamera(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080,1080)
                .start();
    }
    private void onClickSend(String message, String type){
        DatabaseReference refSender = FirebaseDatabase.getInstance().getReference("chats")
                .child(firebaseUser.getUid())
                .child(userId);

        String msg_id = refSender.push().getKey();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatD = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dateFormatT = new SimpleDateFormat("HH:mm");

        String date = dateFormatD.format(calendar.getTime());
        String time = dateFormatT.format(calendar.getTime());
        MessageTime messageTime = new MessageTime(date, time);

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", msg_id);
        map.put("type", type);
        map.put("sender", firebaseUser.getUid());
        map.put("receiver", userId);
        map.put("message", message);
        map.put("sendingTime", messageTime);

        if(linearLayoutReply.getVisibility() == View.VISIBLE) {
            Message replyMessage = messageAdapter.getReplyMessage();

            final String r_sender = replyMessage.getSender();
            final String r_message = replyMessage.getMessage();
            final String r_type = replyMessage.getType();

            ReplyMessage rMessage = new ReplyMessage(r_sender,r_type, r_message);
            map.put("replyMessage", rMessage);

            linearLayoutReply.setVisibility(View.GONE);
            editTextMessage.setMaxLines(3);
        }
        refSender.child(msg_id).setValue(map);
        DatabaseReference refReceiver = FirebaseDatabase.getInstance().getReference("chats")
                .child(userId)
                .child(firebaseUser.getUid());
        refReceiver.child(msg_id).setValue(map);

        DatabaseReference refChatListSender = FirebaseDatabase.getInstance().getReference("chats_list")
                .child(firebaseUser.getUid())
                .child(userId);

        refChatListSender.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { if(!snapshot.exists()) { refChatListSender.child("id").setValue(userId); } }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        DatabaseReference refChatListReceiver = FirebaseDatabase.getInstance().getReference("chats_list")
                .child(userId)
                .child(firebaseUser.getUid());

        refChatListReceiver.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { if(!snapshot.exists()) { refChatListReceiver.child("id").setValue(firebaseUser.getUid()); } }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    private void readMessages(String myId, String userId) {
        DatabaseReference referenceMyMessages = FirebaseDatabase.getInstance().getReference("chats")
                .child(myId)
                .child(userId);

        referenceMyMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messages.add(message);
                }
                messageAdapter = new MessageAdapter(ChatActivity.this, messages, userId, "user");
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void onClickOptions() {
        buttonOptions.setEnabled(false);
        Intent intent = new Intent(this, ChatSettingsActivity.class);
        intent.putExtra("user", userId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            if(Check.networkConnect(getApplicationContext())){
                StorageReference refImage = FirebaseStorage.getInstance().getReference("images/" + uri.getLastPathSegment());
                UploadTask uploadTask = refImage.putFile(uri);

                uploadTask.addOnFailureListener(e -> {
                    Toast.makeText(this, "Greška " + e, Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            Uri uri1 = task1.getResult();
                            String imageUrl  = uri1.toString();
                            Notification.SendToUser("Slikovna poruka", user);
                            onClickSend(imageUrl, "image");
                        }
                    });
                });
            }else Toast.makeText(this, "Nema mreže!", Toast.LENGTH_SHORT).show();
        }
    }
    private void muteThisChat() {
        Set<String> mutedChats = new ArraySet<>();
        Set<String> mS = sessionManager.getMutedChats();

        if(mS != null) mutedChats.addAll(mS);
        mutedChats.add(userId);
        sessionManager.setMutedChats(mutedChats);
    }

    private void unMuteThisChat() {
        Set<String> mutedChats = new ArraySet<>();
        Set<String> mS = sessionManager.getMutedChats();

        if(mS != null) mutedChats.addAll(mS);
        mutedChats.remove(userId);
        sessionManager.setMutedChats(mutedChats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        buttonOptions.setEnabled(true);

        if(status) { user_status.setValue("Online"); }
    }
    @Override
    protected void onStart() {
        super.onStart();
        status = sessionManager.getActivityStatus();
        MUTE_UN_MUTE = true;

        Set<String> mutedChats = new ArraySet<>();
        Set<String> mS = sessionManager.getMutedChats();

        if(mS != null) {
            mutedChats.addAll(mS);

            for(String m : mutedChats) {
                if(m.equals(userId)) {
                    MUTE_UN_MUTE = false;
                    break;
                }
            }
        }

        if(MUTE_UN_MUTE) muteThisChat();

        if(status) { user_status.onDisconnect().setValue("Offline"); }
        else {
            user_status.setValue("");
            user_status.onDisconnect().setValue("");
        }
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

    private boolean checkPermission() {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{recordPermission}, PERMISSION_CODE_AUDIO);
            return false;
        }
    }
}